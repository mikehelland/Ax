package com.monadpad.ax.dsp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class Dac extends UGen {
	private final float[] localBuffer;
	private boolean isClean;
	private final AudioTrack track;
	private final short [] target = new short[UGen.CHUNK_SIZE];
    private final float [] target2 = new float[UGen.CHUNK_SIZE];
	private final short [] silentTarget = new short[UGen.CHUNK_SIZE];

    public boolean recording = false;

	public Dac() {
		localBuffer = new float[CHUNK_SIZE];
		
		int minSize = AudioTrack.getMinBufferSize(
				UGen.SAMPLE_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
        		AudioFormat.ENCODING_PCM_16BIT);
		
		track = new AudioTrack(
        		AudioManager.STREAM_MUSIC,
        		UGen.SAMPLE_RATE,
        		AudioFormat.CHANNEL_CONFIGURATION_MONO,
        		AudioFormat.ENCODING_PCM_16BIT,
        		Math.max(UGen.CHUNK_SIZE*4, minSize),
        		AudioTrack.MODE_STREAM);
	}
	
	public boolean render(final float[] _buffer) {
		if(!isClean) {
			zeroBuffer(localBuffer);

			isClean = true;
		}
		// localBuffer is always clean right here, does it stay that way?
		isClean = !renderKids(localBuffer);
		return !isClean; // we did some work if the buffer isn't clean
	}
	
	public void open() {
		track.play();
	}
	
	public void tick() {
		
		render(localBuffer);

		
		if(isClean) {
			// sleeping is messy, so lets just queue this silent buffer
			track.write(silentTarget, 0, silentTarget.length);
		} else {
			for(int i = 0; i < CHUNK_SIZE; i++) {
                target2[i] = 32768.0f*localBuffer[i];
				target[i] = (short)target2[i];
			}
			
			track.write(target, 0, target.length);
        }
	}
	
	public void close() {
		track.stop();
        track.release();
	}
}
