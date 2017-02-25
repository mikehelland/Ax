package com.monadpad.ax;

import android.util.Log;

import java.util.ArrayList;

/**
 * User: m
 * Date: 8/12/13
 * Time: 1:16 AM
 */
public class PlaybackLoop extends Thread {
    private long started;
    private long duration;

    private ArrayList<Channel.SynthCommand> savedCommands = new ArrayList<Channel.SynthCommand>();

    private boolean timeToStop = false;

    private Device mDevice;

    private Channel mChannel;

    public PlaybackLoop(BitarLooper looper, ArrayList<Channel.SynthCommand> savedCommands, Channel channel) {
        this.savedCommands = savedCommands;

        duration = looper.getDuration();
        started = looper.getStarted();

        //quantizeBeats();

        mChannel = channel;

    }

    public Channel getChannel() {
        return mChannel;
    }

    @Override
    public void run() {

        int i = 0;

        while (!timeToStop) {

            if (savedCommands.size() == 0)
                break;

            if (i < savedCommands.size()) {
                Channel.SynthCommand command = savedCommands.get(i);
                if (command.when < System.currentTimeMillis() - started) {

                    command.doIt();

                    i++;
                }
            }
            else {
                i = 0;
                started += duration;
            }
        }
    }

    void finish() {
        timeToStop = true;
    }


    public float getBPM() {
        float ret = 0f;

        if (duration > 0) {
            ret = 240000f / ((float)duration);
        }

        while (ret < 68.0f) {
            ret = ret * 2.0f;
        }
        while (ret > 208.0f) {
            ret = ret / 2.0f;
        }

        return ret;
    }


    public void quantizeBeats() {
        boolean quantize = true;
        if (quantize) {

            int divisions = 16 * Math.max(1, (int)Math.ceil(duration / 8000.0f));

            int beatDuration = (int)(duration / divisions);
            int lastBeat = -1;
            int thisBeat;

            long firstWhen;


            float time;
            for (Channel.SynthCommand cmd : savedCommands) {

                firstWhen = cmd.when;

                thisBeat = Math.round((float)cmd.when / beatDuration);
                time = beatDuration * thisBeat;

                /*if (thisBeat == lastBeat) {
                    time = -1 * time;
                }*/

                cmd.when = (long)time;
                //Log.d("MGH beat quantizer ", Long.toString(firstWhen) + " >>> " + Float.toString(time));

                lastBeat = thisBeat;
            }

        }

    }

    public Device getDevice() {
        return mDevice;
    }

    public void setDevice(Device device) {
        mDevice = device;
    }
}
