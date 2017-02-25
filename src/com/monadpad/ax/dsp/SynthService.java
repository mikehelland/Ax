package com.monadpad.ax.dsp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

public class SynthService extends Service {
	private static final String TAG = "RecordService";
	
	Thread thread;
    Thread wThread;

    int channelCounter = 0;

    @Override
	public IBinder onBind(Intent intent) {
		final SharedPreferences synthPrefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());

		final float[] scale = buildScale(synthPrefs.getString("quantizer", "1,4,6,9,11"));
		final int octaves = Integer.parseInt(synthPrefs.getString("octaves", "3"));
		
        final SynthChannel channel1 = new SynthChannel();
        final SynthChannel channel2 = new SynthChannel();
		//final Dac ugDac = new Dac();

		if(thread != null) {
			Log.e(TAG,
					"Someone tried to bind when there was already an audio thread!",
					new RuntimeException());
		}
		
		thread = new Thread() {
			@Override
			public void run() {
				Log.d(TAG, "started audio rendering");
				
				System.gc();
				Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                channel1.dac.open();
                channel2.dac.open();

                while(!isInterrupted()) {
                    channel1.dac.tick();
                    channel2.dac.tick();
                }
                channel1.dac.close();
                channel2.dac.close();

                Log.d(TAG, "finished audio rendering");
			}
		};

        thread.start();

		return new ISynthService.Stub() {

			@Override
			public float[] getScale() {
				return scale;
			}
			
			@Override
			public boolean isDuet() {
				return false;
			}
			
			@Override
			public int getOctaves() {
				return octaves;
			}
			

            @Override public void stopChannel(int chanId) {
                if (chanId == 0){
                    channel1.env.setActive(false);
                }
                else {
                    channel2.env.setActive(false);
                }
                channelCounter = chanId;
            }

            @Override public int newChannel() {
                if (channelCounter == 0) {
                    channelCounter++;
                    channel1.env.setActive(true);
                    return channel1.id;
                }
                else {
                    channel2.env.setActive(true);
                    channelCounter = 0;
                    return channel2.id;
                }
            }

            @Override public void doneRecording() {}

            @Override public void setChannel(int chan, int x, long cur) {
                if (cur > 0)
                Log.d("MGH speed test", Long.toString(System.currentTimeMillis() - cur));
                if (chan == 0)
                    channel1.osc.setFreq(SynthService.buildFrequencyFromMapped(x));
                else
                    channel2.osc.setFreq(SynthService.buildFrequencyFromMapped(x));
            }

		};
	}
	
	
	@Override
	public void onDestroy() {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
	
	
	static float[] buildScale(String quantizerString) {
		if(quantizerString != null && quantizerString.length() > 0) {
			String[] parts = quantizerString.split(",");
			float[] scale = new float[parts.length];
			for(int i = 0; i < parts.length; i++) {
				scale[i] = Float.parseFloat(parts[i]);
			}
			return scale;
		} else {
			return null;
		}
	}
	
    public static float buildFrequencyFromMapped(float mapped) {
        return (float)Math.pow(2, (mapped-69.0f)/12.0f) * 440.0f;
    }

    class SynthChannel {
        WtOsc osc;
        ExpEnv env;
        int id;
        long doneAt;
        Dac dac;

        public SynthChannel() {
            osc = new WtOsc();
            env = new ExpEnv();
            dac = new Dac();

//            osc.fillWithSqrDuty(0.6f);
            osc.fillWithHardSin(7.0f);
            env.chuck(dac);
            osc.chuck(env);

//            dac.open();

            //env.setFactor(ExpEnv.hardFactor);

            id = channelCounter++;
        }
    }
}
