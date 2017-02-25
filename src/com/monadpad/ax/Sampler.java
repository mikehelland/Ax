package com.monadpad.ax;

/**
 * User: m
 * Date: 8/16/13
 * Time: 3:46 AM
 */
public class Sampler extends AudioDevice {

    SamplerPool pool;
    int[] ids;
    int[] fingerToStream = {-1, -1};

    int loaded = 0;
    boolean isLoaded = false;


    public Sampler(String instrument, SamplerPool samplerPool) {
        super(instrument);

        pool = samplerPool;

    }

    @Override
    public int startChannel(int x) {
        int ret = super.startChannel(x);

        fingerToStream[ret] = play(x);

        return ret;
    }

    public void startChannel(int chan, int x) {
        fingerToStream[chan] = play(x);
    }


    @Override
    public void setChannel(int chan, int x) {
        int old = fingerToStream[chan];
        fingerToStream[chan] =  play(x);
        if (old > -1 && old != x) {
            stop(old);
        }

    }

    @Override
    public void stopChannel(int chan) {
        stop(fingerToStream[chan]);
    }

    @Override
    public void finish() {

        pool.release();

    }

    int play(int id) {

        return pool.play(ids[id], 0.75f, 0.75f, 10, 0, 1);

    }

    void stop(int id) {
        pool.stop(id);

    }

    public Sampler fillPoolWithPowerChords() {

        ids = pool.getPowerChordIds();

        return this;
    }

    public Sampler fillPoolWithElectric() {

        ids = pool.getElectricIds();

        return this;
    }



    public Sampler fillPoolWithBass() {
        ids =  pool.getBassIds();
        return this;
    }


    public Sampler fillPoolWithAcoustic() {



        ids =  pool.getAcousticIds();

        return this;
    }

    public Sampler fillPoolWithHipHopDrums() {

        ids =  pool.getDrumIds();

        return this;
    }


}
