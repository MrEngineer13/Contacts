package com.zouag.contacts.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.zouag.contacts.R;

/**
 * Created by Mohammed Aouf ZOUAG on 25/03/2016.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
