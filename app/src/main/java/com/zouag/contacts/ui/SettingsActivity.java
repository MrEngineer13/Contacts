package com.zouag.contacts.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mohammed Aouf ZOUAG on 25/03/2016.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
