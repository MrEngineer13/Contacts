package com.zouag.contacts.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.adapters.ContactDetailsAdapter;
import com.zouag.contacts.adapters.DatabaseAdapter;
import com.zouag.contacts.models.Contact;
import com.zouag.contacts.models.ContactData;
import com.zouag.contacts.utils.ResultCodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewContactActivity extends AppCompatActivity {

    @Bind(R.id.profilName)
    TextView profilName;
    @Bind(R.id.profilImage)
    ImageView profilImage;
    @Bind(R.id.detailsListview)
    ListView detailsListview;

    /**
     * The currently viewed contact.
     */
    private Contact currentContact;
    /**
     * A list of this contact's data. (Phone + Email + Address)
     */
    private List<ContactData> contactDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        ButterKnife.bind(this);

        setupContactData();
        setupActionbar();
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
                // TODO: Edit contact
                return true;
            case R.id.action_delete_contact:
                deleteContact();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Basic setup for the details of the currently displayed contact.
     * These details will be passed to a custom adapter to be displayed.
     */
    private void setupContactData() {
        // Set the contact's name
        currentContact = getIntent().getExtras().getParcelable("contact");
        profilName.setText(currentContact.getName());

        // Set the contact's image
        String imgPath = currentContact.getImgPath();
        if ("".equals(imgPath))
            profilImage.setImageResource(R.drawable.ic_action_user);
        else
            profilImage.setImageURI(Uri.fromFile(new File(imgPath)));

        contactDataList = new ArrayList<>();

        if (!"".equals(currentContact.getPhoneNumber())) {
            contactDataList.add(
                    new ContactData(
                            "Mobile", currentContact.getPhoneNumber(), R.drawable.ic_action_phone_start));
        }

        if (!"".equals(currentContact.getEmail())) {
            contactDataList.add(
                    new ContactData(
                            "Email", currentContact.getEmail(), R.drawable.ic_action_attachment));
        }

        if (!"".equals(currentContact.getAddress())) {
            contactDataList.add(
                    new ContactData(
                            "Address", currentContact.getAddress(), R.drawable.ic_action_location));
        }
    }

    private void setupActionbar() {
        // Show the back arrow button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the action bar's title
        getSupportActionBar().setTitle(currentContact.getName());
    }

    private void setupDetailsListView() {
        ContactDetailsAdapter adapter = new ContactDetailsAdapter(this, contactDataList);
        detailsListview.setAdapter(adapter);
    }

    /**
     * Deletes a contact from the local database.
     */
    private void deleteContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Delete contact");
        builder.setMessage("Are you sure that you want to delete this contact ?\n" +
                "This operation cannot be undone.");
        builder.setPositiveButton("I'm positive", (dialog, which) -> {
            DatabaseAdapter databaseAdapter =
                    DatabaseAdapter.getInstance(ViewContactActivity.this);
            databaseAdapter.deleteContact(currentContact.getId());

            setResult(ResultCodes.CONTACT_DELETED);
            finish();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
