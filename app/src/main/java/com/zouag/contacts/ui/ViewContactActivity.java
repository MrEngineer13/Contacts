package com.zouag.contacts.ui;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewContactActivity extends AppCompatActivity {

    @Bind(R.id.profilName)
    TextView profilName;
    @Bind(R.id.profilImage)
    ImageView profilImage;
    @Bind(R.id.detailsListview)
    ListView detailsListview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        ButterKnife.bind(this);

        // Set the contact's name
        Contact contact = getIntent().getExtras().getParcelable("contact");
        profilName.setText(contact.getName());

        // Set the contact's image
        profilImage.setImageURI(Uri.fromFile(new File(contact.getImgPath())));


    }
}
