package com.pvlf.android.timer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Application settings activity.
 * Extends the base class for an activity to show a hierarchy of preferences.
 */
public class SettingsActivity extends PreferenceActivity {
	
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
