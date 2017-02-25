package com.monadpad.ax;

import android.widget.ImageButton;

/**
 * User: m
 * Date: 9/4/13
 * Time: 2:18 PM
 */
public class Device {
    private ImageButton mButton;
    private Channel mChannel;
    private BluetoothFactory.ConnectedThread mConnection;

    private BitarLooper mLooper;
    private SamplerPool mPool;

    final static String ACTION_CHANGE_INSTRUMENT = "CHANGE_INSTRUMENT";
    final static String ACTION_ARM_RECORDING = "CHANGE_ARM_RECORDING";
    final static String ACTION_DISARM_RECORDING = "CHANGE_DISARM_RECORDING";
    final static String ACTION_DONE_RECORDING = "CHANGE_DONE_RECORDING";

    private String[] channelParams;

    Device(BluetoothFactory.ConnectedThread connection) {
        mConnection = connection;
    }
    public void setButton(ImageButton button) {
        mButton = button;
    }
    public ImageButton getButton() {
        return mButton;
    }
    public void setChannel(Channel channel) {
        mChannel = channel;
    }
    public Channel getChannel() {
        return mChannel;
    }


    void sendChangeInstrumentMessage(String instrument) {

        if (mConnection == null) {
            return;
        }

        mConnection.writeString(ACTION_CHANGE_INSTRUMENT + ";" + instrument + ":");

    }

    void setupChannel(BitarLooper looper, SamplerPool pool, String[] data) {

        mLooper = looper;
        mPool = pool;
        channelParams = data;

        if (mChannel != null) {
            mChannel.finish();
        }

        reloadChannel();
    }

    void reloadChannel() {

        String[] data = channelParams;
        SamplerPool pool = mPool;
        if (data.length < 1)
            return;

        AudioDevice device;

        String instrument = data[1];
        if (instrument.equals("SYNTH")) {
            device = new DialpadAudioDevice(instrument,  data);
        }
        else if (instrument.equals("EGUITAR CHORDS")) {
            device = new Sampler(instrument,  pool).fillPoolWithPowerChords();
        }
        else if (instrument.equals("AGUITAR CHORDS")) {
            device = new Sampler(instrument,  pool).fillPoolWithAcoustic();
        }
        else if (instrument.equals("EBASS")) {
            device = new Sampler(instrument,  pool).fillPoolWithBass();
        }
        else if (instrument.equals("EBASS")) {
            device = new Sampler(instrument,  pool).fillPoolWithBass();
        }
        else if (instrument.equals("HHDRUMS")) {
            device = new Sampler(instrument,  pool).fillPoolWithHipHopDrums();
        }
        else {
            device = new Sampler(instrument,  pool).fillPoolWithPowerChords();
        }

        mChannel = new Channel(mLooper, instrument, device);

    }

    void armRecording() {

        mChannel.armRecording();

        if (mConnection == null) {
            return;
        }

        mConnection.writeString(ACTION_ARM_RECORDING + ":");

    }


    void disarmRecording() {

        mChannel.disarmRecording();

        if (mConnection == null) {
            return;
        }

        mConnection.writeString(ACTION_DISARM_RECORDING + ":");

    }

    PlaybackLoop doneRecording() {
        PlaybackLoop ret = mChannel.stopRecording();

        ret.setDevice(this);

        if (mConnection != null) {
            mConnection.writeString(ACTION_DONE_RECORDING + ":");
        }

        return ret;
    }

}
