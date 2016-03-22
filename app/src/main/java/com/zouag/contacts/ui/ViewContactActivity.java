package com.zouag.contacts.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.adapters.ContactDetailsAdapter;
import com.zouag.contacts.models.Contact;
import com.zouag.contacts.models.ContactData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewContactActivity extends AppCompatActivity {

    @Bind(R.id.profilName)
    TextView profilName;
    @Bind(R.id.profilImage)
    ImageView profilImage;
    @Bind(R.id.detailsListview)
    ListView detailsListview;

    private List<ContactData> contactDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the contact's name
        Contact contact = getIntent().getExtras().getParcelable("contact");
        profilName.setText(contact.getName());

        // Set the contact's image
        profilImage.setImageURI(Uri.fromFile(new File(contact.getImgPath())));

        setupContactData(contact);
        setupDetailsListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_edit_contact:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Basic setup for the details of the currently displayed contact.
     * These details will be passed to a custom adapter to be displayed.
     *
     * @param contact to be setup
     */
    private void setupContactData(Contact contact) {
        contactDataList = new ArrayList<>();

        if (!"".equals(contact.getPhoneNumber())) {
            contactDataList.add(
                    new ContactData(
                            "Mobile", contact.getPhoneNumber(), R.drawable.ic_action_phone_start));
        }

        if (!"".equals(contact.getEmail())) {
            contactDataList.add(
                    new ContactData(
                            "Email", contact.getEmail(), R.drawable.ic_action_attachment));
        }

        if (!"".equals(contact.getAddress())) {
            contactDataList.add(
                    new ContactData(
                            "Address", contact.getAddress(), R.drawable.ic_action_location));
        }
    }

    private void setupDetailsListView() {
        ContactDetailsAdapter adapter = new ContactDetailsAdapter(this, contactDataList);
        detailsListview.setAdapter(adapter);
    }
}
