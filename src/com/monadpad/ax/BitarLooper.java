package com.monadpad.ax;

/**
 * User: m
 * Date: 8/27/13
 * Time: 2:19 AM
 */
public class BitarLooper {

    private long duration = 0l;
    private long started = 0l;

    private boolean mIsSetup = false;

    private long durationOverride = 0l;
    private long startedOverride;

    public BitarLooper(long defaultDuration, long defaultStarted) {
        durationOverride = defaultDuration;
        startedOverride = defaultStarted;
    }

    public BitarLooper() {
    }

    boolean isSetup() {
        return mIsSetup;
    }

    void start(long duration) {
        long now = System.currentTimeMillis();
        if (durationOverride > 0 && startedOverride > 0) {
            this.duration = durationOverride;

            int loopsBehind = (int)Math.ceil((now - startedOverride) / (float)durationOverride);
            started = startedOverride + loopsBehind * durationOverride;

        }
        else {
            this.duration = duration;
            started = now;
        }
        mIsSetup = true;
    }


    long getStarted() {
        return started;
    }

    long getDuration() {
        return duration;
    }

    void clear() {
        duration = 0l;
        started = 0l;
        mIsSetup = false;
    }

    void start(long duration, long started) {
        durationOverride = duration;
        startedOverride = started;
        start(4000); //4000
    }
}
