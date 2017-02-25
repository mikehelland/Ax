package com.monadpad.ax;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import com.monadpad.ax.dsp.*;

/**
 * User: m
 * Date: 8/11/13
 * Time: 9:47 PM
 */
public class PickSynth {

    final SynthChannel[] channels;
    int usedChannels = 0;

    Thread thread;

    int base;

    public PickSynth(Context context) {

        base = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString("base", "40"));


        channels = new SynthChannel[16];
//        for (int i = 0; i<channels.length; i++) {
//            channels[i] = new SynthChannel(i*2, i);
//        }

        thread = new Thread() {
            @Override
            public void run() {
                Log.d("MGH PickSynth", "started audio rendering");

                System.gc();

                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
//                for (int i = 0; i < channels.length; i++)
//                    channels[i].dac.open();



                while(!isInterrupted()) {
                    for (int i = 0; i < usedChannels; i++)
                        channels[i].dac.tick();
                }
                for (int i = 0; i < usedChannels; i++)
                    channels[i].dac.close();

                Log.d("MGH", "finished audio rendering");
            }
        };

        thread.start();

    }

    public void stopChannel(int chanId) {
        channels[chanId].env.setActive(false);
    }

    public void startChannel(int chanId, int x) {
        channels[chanId].env.setActive(true);
        setChannel(chanId, x);
    }

    public void setChannel(int id, int x) {
        channels[id].osc.setFreq(SynthService.buildFrequencyFromMapped(base + x));
    }

    public void finish() {
        for (int i = 0; i<channels.length; i++)
            channels[i].finish();

        thread.interrupt();
    }

    public void createChannel(String[] data) {

        channels[usedChannels] = new SynthChannel(data, 0);
        usedChannels++;

    }

    class SynthChannel {
        WtOsc osc;
        ExpEnv env;
        int id;
        long doneAt;
        Dac dac;
        Delay ugDelay;
        Flange ugFlange;

        public SynthChannel(String[] data, int id) {
            this.id = id;
            osc = new WtOsc();
            env = new ExpEnv();
            dac = new Dac();
            ugDelay = new Delay(UGen.SAMPLE_RATE/2);

            boolean delay = false;
            boolean flange = false;
            boolean softe = false;

            String wave = "Sine";
            if (data.length > 2)
                wave = data[2];


            for (int i = 2; i < data.length; i++) {
                if (data[i].equals("delay")) delay = true;
                if (data[i].equals("softe")) softe = true;
                if (data[i].equals("flange")) flange = true;
            }


            if (wave.equals("Sawtooth"))
                osc.fillWithSaw();
            else if (wave.equals("Sine")) {
                osc.fillWithHardSin(7.0f);
            } else {
                osc.fillWithSqrDuty(0.6f);
            }

            if (delay) {
                env.chuck(ugDelay);

                if(flange) {
                    ugDelay.chuck(ugFlange).chuck(dac);
                } else {
                    ugDelay.chuck(dac);
                }

            } else {
                if(flange) {
                    env.chuck(ugFlange).chuck(dac);
                } else {
                    env.chuck(dac);
                }
            }

            osc.chuck(env);
            if (!softe) {
                env.setFactor(ExpEnv.hardFactor);
            }

        }

        public void finish() {
            env.setActive(false);
            doneAt = System.currentTimeMillis() + 2000;
        }

    }

}
