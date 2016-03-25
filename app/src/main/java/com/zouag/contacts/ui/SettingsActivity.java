package com.zouag.contacts.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.zouag.contacts.R;

/**
 * Created by Mohammed Aouf ZOUAG on 25/03/2016.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment())
                .commit();

        LinearLayout root = (LinearLayout) findViewById(android.R.id.content).getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this)
                .inflate(R.layout.settings_toolbar, root, false);
        bar.setTitleTextColor(Color.parseColor("#ffffffff"));
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(v -> finish());
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }
    }
}
