package com.monadpad.ax.dsp;

interface ISynthService {

	int newChannel();
	void stopChannel(int chan);
	void setChannel(int chan, int x, long cur);


	// synth config
	float[] getScale();
	boolean isDuet();
	int getOctaves();

	void doneRecording();

}