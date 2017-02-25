package com.monadpad.ax;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SynthPreferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.synth_prefs);
	}

    /*
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference){

        final View viewmpad = SynthPreferences.this.getActivity().findViewById(R.id.mpad);
        if (viewmpad != null){

            if (preference.getClass().equals(CheckBoxPreference.class)){
                ((MonadView)viewmpad).setPreferences();
                viewmpad.invalidate();
            }
            else {

                ((MonadView)viewmpad).maybeUpdateTutorial();

                preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object o) {

                        ((MonadaphoneFragment)
                        SynthPreferences.this.getActivity().getFragmentManager().findFragmentById(R.id.mpf))
                            .refreshPreference(preference, o);

                        return true;
                    }
                });
            }

        }
        return true;
    }
    */
}

