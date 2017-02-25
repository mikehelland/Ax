package com.monadpad.ax;

import android.content.Context;
import android.media.SoundPool;

/**
 * User: m
 * Date: 8/26/13
 * Time: 11:47 PM
 */
public class SamplerPool extends SoundPool {

    private boolean hasLoadedDrums = false;
    private boolean hasLoadedBass = false;
    private boolean hasLoadedEGuitarChords = false;
    private boolean hasLoadedAGuitar = false;
    private boolean hasLoadedEGuitar = false;

    private int electricIds[];
    private int drumIds[];
    private int powerChordsIds[];
    private int bassIds[];
    private int acousticIds[];
    private Context mContext;

    public SamplerPool(Context context, int maxStreams, int streamType, int srcQuality) {
        super(maxStreams, streamType, srcQuality);
        mContext = context;
    }

    public int[] getPowerChordIds() {
        if (hasLoadedEGuitarChords) {
            return powerChordsIds;
        }

        int[] ids =  new int[18];

        Context context = mContext;

        ids[0] = load(context, R.raw.epower, 1);
        ids[1] = load(context, R.raw.fpower, 1);
        ids[2] = load(context, R.raw.fspower, 1);
        ids[3] = load(context, R.raw.gpower, 1);
        ids[4] = load(context, R.raw.gspower, 1);
        ids[5] = load(context, R.raw.apower, 1);
        ids[6] = load(context, R.raw.bfpower, 1);
        ids[7] = load(context, R.raw.bpower, 1);
        ids[8] = load(context, R.raw.cpower, 1);
        ids[9] = load(context, R.raw.cspower, 1);
        ids[10] = load(context, R.raw.dpower, 1);
        ids[11] = load(context, R.raw.dspower, 1);
        ids[12] = load(context, R.raw.e2power, 1);
        ids[13] = load(context, R.raw.f2power, 1);
        ids[14] = load(context, R.raw.fs2power, 1);
        ids[15] = load(context, R.raw.g2power, 1);
        ids[16] = load(context, R.raw.gs2power, 1);
        ids[17] = load(context, R.raw.a2power, 1);

        powerChordsIds = ids;

        hasLoadedEGuitarChords = true;
        return ids;
    }

    public int[] getElectricIds() {
        if (hasLoadedEGuitar) {
            return electricIds;
        }

        int[] ids =  new int[46];

        Context context = mContext;

        ids[0] = load(context, R.raw.electric_e, 1);
        ids[1] = load(context, R.raw.electric_f, 1);
        ids[2] = load(context, R.raw.electric_fs, 1);
        ids[3] = load(context, R.raw.electric_g, 1);
        ids[4] = load(context, R.raw.electric_gs, 1);
        ids[5] = load(context, R.raw.electric_a, 1);
        ids[6] = load(context, R.raw.electric_bf, 1);
        ids[7] = load(context, R.raw.electric_b, 1);
        ids[8] = load(context, R.raw.electric_c, 1);
        ids[9] = load(context, R.raw.electric_cs, 1);
        ids[10] = load(context, R.raw.electric_d, 1);
        ids[11] = load(context, R.raw.electric_ds, 1);
        ids[12] = load(context, R.raw.electric_e2, 1);
        ids[13] = load(context, R.raw.electric_f2, 1);
        ids[14] = load(context, R.raw.electric_fs2, 1);
        ids[15] = load(context, R.raw.electric_g2, 1);
        ids[16] = load(context, R.raw.electric_gs2, 1);
        ids[17] = load(context, R.raw.electric_a2, 1);
        ids[18] = load(context, R.raw.electric_bf2, 1);
        ids[19] = load(context, R.raw.electric_b2, 1);
        ids[20] = load(context, R.raw.electric_c2, 1);
        ids[21] = load(context, R.raw.electric_cs2, 1);
        ids[22] = load(context, R.raw.electric_d2, 1);
        ids[23] = load(context, R.raw.electric_ds2, 1);
        ids[24] = load(context, R.raw.electric_e3, 1);
        ids[25] = load(context, R.raw.electric_f3, 1);
        ids[26] = load(context, R.raw.electric_fs3, 1);
        ids[27] = load(context, R.raw.electric_g3, 1);
        ids[28] = load(context, R.raw.electric_gs3, 1);
        ids[29] = load(context, R.raw.electric_a3, 1);
        ids[30] = load(context, R.raw.electric_bf3, 1);
        ids[31] = load(context, R.raw.electric_b3, 1);
        ids[32] = load(context, R.raw.electric_c3, 1);
        ids[33] = load(context, R.raw.electric_cs3, 1);
        ids[34] = load(context, R.raw.electric_d3, 1);
        ids[35] = load(context, R.raw.electric_ds3, 1);
        ids[36] = load(context, R.raw.electric_e4, 1);
        ids[37] = load(context, R.raw.electric_f4, 1);
        ids[38] = load(context, R.raw.electric_fs4, 1);
        ids[39] = load(context, R.raw.electric_g4, 1);
        ids[40] = load(context, R.raw.electric_gs4, 1);
        ids[41] = load(context, R.raw.electric_a4, 1);
        ids[42] = load(context, R.raw.electric_bf4, 1);
        ids[43] = load(context, R.raw.electric_b4, 1);
        ids[44] = load(context, R.raw.electric_c4, 1);
        ids[45] = load(context, R.raw.electric_cs4, 1);

        electricIds = ids;

        hasLoadedEGuitar = true;
        return ids;
    }


    public int[] getDrumIds() {
        if (hasLoadedDrums) {
            return drumIds;
        }

        int[] ids = new int[4];

        ids[0] = load(mContext, R.raw.hh_kick, 1);
        ids[1] = load(mContext, R.raw.hh_clap, 1);
        ids[2] = load(mContext, R.raw.hh_tamb, 1);
        ids[3] = load(mContext, R.raw.hh_scratch, 1);

        hasLoadedDrums = true;
        drumIds = ids;

        return ids;
    }


    public int[] getAcousticIds() {
        if (hasLoadedAGuitar)
            return acousticIds;

        int[] ids = new int[6];

        Context context = mContext;
        ids[0] = load(context, R.raw.cacoustic, 1);
        ids[1] = load(context, R.raw.facoustic, 1);
        ids[2] = load(context, R.raw.gacoustic, 1);
        ids[3] = load(context, R.raw.dacoustic, 1);
        ids[4] = load(context, R.raw.amacoustic, 1);
        ids[5] = load(context, R.raw.carppegacoustic, 1);

        hasLoadedAGuitar = true;
        acousticIds = ids;
        return ids;
    }
    
    
    public int[] getBassIds() {
        if (hasLoadedBass)
            return bassIds;
        
        int[] ids = new int[18];
        Context context = mContext;
        ids[0] = load(context, R.raw.ebass, 1);
        //ids[1] = load(context, R.raw.fpower, 1);
        //ids[2] = load(context, R.raw.fspower, 1);
        ids[3] = load(context, R.raw.gbass, 1);
        //ids[4] = load(context, R.raw.gsbass, 1);
        ids[5] = load(context, R.raw.abass, 1);
        ids[6] = load(context, R.raw.bfbass, 1);
        ids[7] = load(context, R.raw.bbass, 1);
        ids[8] = load(context, R.raw.cbass, 1);
        ids[9] = load(context, R.raw.csbass, 1);
        ids[10] = load(context, R.raw.dbass, 1);
        ids[11] = load(context, R.raw.dsbass, 1);
        ids[12] = load(context, R.raw.e2bass, 1);
        //ids[13] = load(context, R.raw.f2bass, 1);
        //ids[14] = load(context, R.raw.fs2bass, 1);
        //ids[15] = load(context, R.raw.g2bass, 1);
        //ids[16] = load(context, R.raw.gs2bass, 1);
        //ids[17] = load(context, R.raw.a2bass, 1);

        hasLoadedBass = true;
        bassIds = ids;
        return ids;
    }
}
