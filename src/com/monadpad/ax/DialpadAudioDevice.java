package com.monadpad.ax;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.monadpad.ax.dsp.*;

/**
 * User: m
 * Date: 8/11/13
 * Time: 9:47 PM
 */
public class DialpadAudioDevice extends AudioDevice {

    final SynthChannel channel1;
    final SynthChannel channel2;
    Thread thread;

    int base = 40;

    boolean delay;
    boolean flange;
    boolean softe;
    String wave;

    private boolean stop = false;

    public DialpadAudioDevice(Context context, String instrument) {
        super(instrument);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        delay = prefs.getBoolean("soft_e", true);
        flange = prefs.getBoolean("flange", false);
        softe = prefs.getBoolean("soft_e", false);
        wave = prefs.getString("waveform", "Sine");
        base = Integer.parseInt(prefs.getString("base", "40"));

        channel1 = new SynthChannel(0);
        channel2 = new SynthChannel(1);

        setup();
    }
    public DialpadAudioDevice(String instrument, String[] data) {
        super(instrument);

        wave = "Sine";
        if (data.length > 2)
            wave = data[2];

        for (int i = 2; i < data.length; i++) {
            if (data[i].equals("delay")) delay = true;
            if (data[i].equals("softe")) softe = true;
            if (data[i].equals("flange")) flange = true;
        }

        channel1 = new SynthChannel(0);
        channel2 = new SynthChannel(1);

        setup();
    }

    private void setup() {

        thread = new Thread() {
            @Override
            public void run() {
                Log.d("LocalSynthService", "started audio rendering");

                System.gc();
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                channel1.dac.open();
                channel2.dac.open();

                while(!stop) {
                    channel1.dac.tick();
                    channel2.dac.tick();
                }
                channel1.dac.close();
                channel2.dac.close();
                Log.d("MGH", "finished audio rendering");
            }
        };

        thread.start();

    }

    public void stopChannel(int chanId) {
        if (chanId == 0){
            channel1.env.setActive(false);
        }
        else {
            channel2.env.setActive(false);
        }
        super.stopChannel(chanId);
    }

    public int startChannel(int x) {
        int id = super.startChannel(x);
        if (id == 0) {
            channel1.env.setActive(true);
        }
        else {
            channel2.env.setActive(true);
        }
        setChannel(id, x);
        return id;

    }

    public void startChannel(int id, int x) {
        if (id == 0) {
            channel1.env.setActive(true);
        }
        else {
            channel2.env.setActive(true);
        }
        setChannel(id, x);
    }

    public void setChannel(int id, int x) {
        Log.d("MGH setting channel", Integer.toString(id));
        if (id == 0)
            channel1.osc.setFreq(SynthService.buildFrequencyFromMapped(base + x));
        else
            channel2.osc.setFreq(SynthService.buildFrequencyFromMapped(base + x));

    }

    public void finish() {

        channel1.finish();
        channel2.finish();


        stop = true;
    }

    class SynthChannel {
        WtOsc osc;
        ExpEnv env;
        int id;
        long doneAt;
        Dac dac;
        Delay ugDelay;
        final Flange ugFlange;

        public SynthChannel(int id) {


            this.id = id;
            osc = new WtOsc();
            env = new ExpEnv();
            dac = new Dac();
            ugDelay = new Delay(UGen.SAMPLE_RATE/2);
            ugFlange = new Flange(UGen.SAMPLE_RATE/64,0.25f);

            if ("Sawtooth".equals(wave))
                osc.fillWithSaw();
            else if ("Square".equals(wave)) {
                osc.fillWithSqrDuty(0.6f);
            } else {
                osc.fillWithHardSin(7.0f);
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

    @Override
    public String getCreateChannelParameters() {
        StringBuilder ret = new StringBuilder(instrument);

        ret.append(";");
        ret.append(wave);

        if (delay) {
            ret.append(";delay");
        }
        if (flange) {
            ret.append(";flange");
        }
        if (softe) {
            ret.append(";flange");
        }

        return ret.toString();

    }
}
