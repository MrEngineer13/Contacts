package com.zouag.contacts.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zouag.contacts.R;

public class AddContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
    }

    /**
     * Invoked when the 'CANCEL' button is clicked.
     *
     * @param view the button.
     */
    public void onCancel(View view) {
        // Terminate the activity
        finish();
    }
}
