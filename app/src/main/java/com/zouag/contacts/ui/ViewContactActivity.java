package com.zouag.contacts.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
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

    private static final int REQUEST_UPDATE_CONTACT = 200;

    @Bind(R.id.detailsRecyclerview)
    RecyclerView detailsRecyclerView;

    @Bind(R.id.profileImage)
    ImageView profileImage;

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

        currentContact = getIntent().getExtras().getParcelable("contact");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupContactData();
        setupActionbar();
        setupDetailsRecyclerView();
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
                updateContact();
                return true;
            case R.id.action_delete_contact:
                deleteContact();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_UPDATE_CONTACT:
                switch (resultCode) {
                    case ResultCodes.CONTACT_UPDATED:
                        // Refresh the Activity's details after returning from
                        // the AlterContactActivity (an update occured)
                        requestNewContactInfo();

                        Snackbar.make(getWindow().getDecorView(),
                                "Contact successfully updated.",
                                Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
        }
    }

    /**
     * Refreshes the informations of the currently displayed contact after modification.
     */
    private void requestNewContactInfo() {
        DatabaseAdapter databaseAdapter = DatabaseAdapter.getInstance(this);
        currentContact = databaseAdapter.getContact(currentContact.getId());

        setupContactData();
        setupActionbar();
        setupDetailsRecyclerView();
    }

    /**
     * Basic setup for the details of the currently displayed contact.
     * These details will be passed to a custom adapter to be displayed.
     */
    private void setupContactData() {
        // Set the contact's image
        String imgPath = currentContact.getImgPath();
        if ("".equals(imgPath))
            profileImage.setImageResource(R.drawable.ic_action_user);
        else
            profileImage.setImageURI(Uri.fromFile(new File(imgPath)));

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

    private void setupDetailsRecyclerView() {
        ContactDetailsAdapter adapter = new ContactDetailsAdapter(this, contactDataList);
        detailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        detailsRecyclerView.setHasFixedSize(true);
        detailsRecyclerView.setAdapter(adapter);
    }

    /**
     * Deletes a contact from the local database.
     */
    private void deleteContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Delete contact");
        builder.setMessage("Are you sure that you want to delete this contact ?\n" +
                "This operation cannot be undone.");
        builder.setNegativeButton("Cancel", null)
                .setPositiveButton("I'm positive", (dialog, which) -> {
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

    /**
     * Updates the selected contact.
     * This method will start the activity that will proceed with the update.
     */
    private void updateContact() {
        Intent intent = new Intent(this, AlterContactActivity.class);
        intent.putExtra("isUpdating", true);
        intent.putExtra("contact", currentContact);
        startActivityForResult(intent, REQUEST_UPDATE_CONTACT);
    }
}
