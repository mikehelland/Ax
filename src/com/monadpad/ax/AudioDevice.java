package com.monadpad.ax;

/**
 * User: m
 * Date: 5/26/13
 * Time: 9:13 PM
 */
public abstract class AudioDevice {

    public static final String ACTION_STOPCHANNEL = "STOPCHANNEL";
    public static final String ACTION_SETCHANNEL = "SETCHANNEL";
    public static final String ACTION_STARTCHANNEL = "STARTCHANNEL";
    public static final String ACTION_CREATECHANNEL = "CREATECHANNEL";


    int channelCounter = 0;

    protected String instrument;


    public AudioDevice(String instrument) {
        this.instrument = instrument;

    }

    public void stopChannel(int chanId) {
        channelCounter = chanId;
    }

    public int startChannel(int x) {
        int id;
        if (channelCounter == 0) {
            id = channelCounter++;
        }
        else {
            id = channelCounter--;
        }
        return id;
    }

    public abstract void startChannel(int chan, int x);
    public abstract void setChannel(int chan, int x);

    public abstract void finish();

    public String getCreateChannelParameters() {

        return instrument;
    }
}
