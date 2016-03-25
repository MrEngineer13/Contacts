package com.zouag.contacts.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

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

    private static final String TAG = ViewContactActivity.class.getSimpleName();
    private static final int REQUEST_UPDATE_CONTACT = 200;

    private SharedPreferences sharedPref;

    @Bind(R.id.detailsRecyclerview)
    RecyclerView detailsRecyclerView;

    @Bind(R.id.profileImage)
    ImageView profileImage;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    /**
     * The currently viewed contact.
     */
    private Contact currentContact;
    /**
     * A list of this contact's data. (Phone + Email + Address)
     */
    private List<ContactData> contactDataList;

    /**
     * The adapter of the currently displayed contact details.
     */
    ContactDetailsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            // Get saved contact from previous orientation change
            currentContact = savedInstanceState.getParcelable("savedContact");
        }
        else {
            // Get contact from launching intent
            currentContact = getIntent().getExtras().getParcelable("contact");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the back arrow button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupContactData();
        setActionbarTitle();
        setupDetailsRecyclerView();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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
                                R.string.contact_updated,
                                Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("savedContact", currentContact);
        super.onSaveInstanceState(outState);
    }

    /**
     * Refreshes the informations of the currently displayed contact after modification.
     */
    private void requestNewContactInfo() {
        DatabaseAdapter databaseAdapter = DatabaseAdapter.getInstance(this);
        currentContact = databaseAdapter.getContact(currentContact.getId());

        setupContactData();
        setActionbarTitle();
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

        if (contactDataList == null)
            contactDataList = new ArrayList<>();
        else
            contactDataList.clear();

        if (!"".equals(currentContact.getPhoneNumber())) {
            contactDataList.add(
                    new ContactData(
                            getString(R.string.mobile),
                            currentContact.getPhoneNumber(),
                            R.drawable.ic_action_phone_start));
        }

        if (!"".equals(currentContact.getEmail())) {
            contactDataList.add(
                    new ContactData(
                            getString(R.string.email),
                            currentContact.getEmail(),
                            R.drawable.ic_action_attachment));
        }

        if (!"".equals(currentContact.getAddress())) {
            contactDataList.add(
                    new ContactData(
                            getString(R.string.address),
                            currentContact.getAddress(),
                            R.drawable.ic_action_location));
        }
    }

    private void setActionbarTitle() {
        // Set the action bar's title
        collapsingToolbarLayout.setTitle(currentContact.getName());
    }

    private void setupDetailsRecyclerView() {
        if (mAdapter == null) {
            mAdapter = new ContactDetailsAdapter(this, contactDataList);
            detailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            detailsRecyclerView.setHasFixedSize(true);
            detailsRecyclerView.setAdapter(mAdapter);
        } else
            mAdapter.notifyDataSetChanged();
    }

    /**
     * Deletes a contact from the local database.
     */
    private void deleteContact() {
        // Get the setting
        boolean confirm_delete = sharedPref.getBoolean(
                getString(R.string.show_confirm_dialog), true);

        if (!confirm_delete) {
            // Delete contact without prompting a confirmation dialog
            performDeletion();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getString(R.string.delete_contact));
            builder.setMessage(getString(R.string.are_you_sure_delete_contact));
            builder.setNegativeButton(getString(R.string.cancel), null)
                    .setPositiveButton(getString(R.string.positive), (dialog, which) -> {
                        performDeletion();
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private void performDeletion() {
        DatabaseAdapter databaseAdapter =
                DatabaseAdapter.getInstance(ViewContactActivity.this);
        databaseAdapter.deleteContact(currentContact.getId());

        setResult(ResultCodes.CONTACT_DELETED);
        finish();
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
