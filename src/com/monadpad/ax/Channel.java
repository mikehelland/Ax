package com.monadpad.ax;

import android.util.Log;

import java.util.ArrayList;

/**
 * User: m
 * Date: 8/26/13
 * Time: 1:44 PM
 */
public class Channel {

    String instrument;
    private AudioDevice audio;

    long recordingStarted = 0;

    PlaybackLoop playbackLoop;

    private ArrayList<RecordedState> recordedStates = new ArrayList<RecordedState>();

    private ArrayList<SynthCommand> savedCommands = new ArrayList<SynthCommand>();

    final static int STATUS_LIVE = 0;
    final static int STATUS_RECORDING_ARMED = 1;
    final static int STATUS_RECORDING = 2;
    final static int STATUS_PLAYBACK = 3;

    private int status = STATUS_LIVE;

    private boolean[] channelsTurnedOn = {false, false};

    private BitarLooper looper;

    private boolean muted = false;

    public Channel(BitarLooper looper, String instrument, AudioDevice device) {

        this.instrument = instrument;
        this.looper = looper;
        audio = device;


    }

    void cancelPlayback() {

        if (playbackLoop != null && !playbackLoop.isInterrupted()) {

            playbackLoop.finish();
            playbackLoop = null;
            status = STATUS_LIVE;

        }
    }

    void recordState(String state) {
        recordedStates.add(new RecordedState(state,
                System.currentTimeMillis() - recordingStarted));

    }

    public void startRecording() {

        if (looper.isSetup()) {
            recordingStarted = looper.getStarted();
        }
        else {
            recordingStarted = System.currentTimeMillis();
        }

        status = STATUS_RECORDING;
    }


    class RecordedState {

        long time;
        String state;

        public RecordedState(String state, long time) {
            this.state = state;
            this.time = time;

//            Log.d("MGH record state", state);
//            Log.d("MGH record time", Integer.toString(time));
        }
    }

    PlaybackLoop stopRecording() {
        long duration;
        if (!looper.isSetup()) {
            duration = System.currentTimeMillis() - recordingStarted;
            looper.start(duration);
        }

        recordingStarted = 0;

        //playbackThread = new PlaybackThread(view, recordedStates);
        //playbackThread.start();

        playbackLoop = new PlaybackLoop(looper,  savedCommands, this);
        playbackLoop.start();

        status = STATUS_PLAYBACK;

        return playbackLoop;
    }

    int startChannel(int x) {
        int ret = audio.startChannel(x);

        if (status == STATUS_RECORDING_ARMED) {
            startRecording();
        }

        if (recordingStarted > 0) {
            savedCommands.add(new SynthCommand(AudioDevice.ACTION_STARTCHANNEL, ret, x,
                    System.currentTimeMillis() - recordingStarted));
        }

        return ret;
    }

    void startChannel(int channel, int x) {

        if (status == STATUS_RECORDING_ARMED) {
            startRecording();
        }

        if (recordingStarted > 0) {
            savedCommands.add(new SynthCommand(AudioDevice.ACTION_STARTCHANNEL, channel, x,
                    System.currentTimeMillis() - recordingStarted));
        }

        audio.startChannel(channel, x);
    }

    void setChannel(int channel, int x) {

        if (recordingStarted > 0) {
            savedCommands.add(new SynthCommand(AudioDevice.ACTION_SETCHANNEL, channel, x,
                    System.currentTimeMillis() - recordingStarted));
        }


        audio.setChannel(channel, x);
    }

    void stopChannel(int channel) {

        if (recordingStarted > 0) {
            savedCommands.add(new SynthCommand(AudioDevice.ACTION_STOPCHANNEL, channel, -1,
                    System.currentTimeMillis() - recordingStarted));
        }

        audio.stopChannel(channel);
    }

    void setAudio(AudioDevice device) {
        audio = device;
    }


    class SynthCommand {
        String action;
        int x;
        int id;
        long when;

        SynthCommand(String action, int id, int x, long when) {
            this.action = action;
            this.id = id;
            this.x = x;
            this.when = when;


        }

        public void doIt() {

            if (muted)
                return;

            if (AudioDevice.ACTION_STARTCHANNEL.equals(action)) {
                audio.startChannel(id, x);
                channelsTurnedOn[id] = true;
            }
            if (AudioDevice.ACTION_SETCHANNEL.equals(action)) {
                audio.setChannel(id, x);
            }
            if (AudioDevice.ACTION_STOPCHANNEL.equals(action)) {
                audio.stopChannel(id);
                channelsTurnedOn[id] = false;
            }
        }


    }

    int getStatus() {
        return status;
    }

    void armRecording() {
        status = STATUS_RECORDING_ARMED;
    }

    void disarmRecording() {
        status = STATUS_LIVE;
    }

    void doneRecording() {
        status = STATUS_LIVE;
    }

    public void toggleMute() {
        muted = !muted;

        if (muted) {
            muteAllFingers();
        }
    }

    public void setMute(boolean newMuted) {
        muted = newMuted;

        if (muted) {
            muteAllFingers();
        }
    }

    public void finish() {
        if (playbackLoop != null) {
            playbackLoop.finish();
            muteAllFingers();
        }
    }

    private void muteAllFingers() {
        for (int i = 0; i < channelsTurnedOn.length; i++) {
            if (channelsTurnedOn[i]) {
                stopChannel(i);
            }
        }

    }

}

