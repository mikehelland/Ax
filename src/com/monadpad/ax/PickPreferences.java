package com.monadpad.ax;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PickPreferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pick_prefs);
	}

}

