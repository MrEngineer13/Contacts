package com.zouag.contacts.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewContactActivity extends AppCompatActivity {

    @Bind(R.id.profilName)
    TextView profilName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        ButterKnife.bind(this);

        Contact contact = getIntent().getExtras().getParcelable("contact");
        profilName.setText(contact.getName());
    }
}
