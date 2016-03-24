package com.zouag.contacts.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zouag.contacts.R;
import com.zouag.contacts.adapters.ContactsRecyclerAdapter;
import com.zouag.contacts.adapters.DatabaseAdapter;
import com.zouag.contacts.models.Contact;
import com.zouag.contacts.utils.ResultCodes;
import com.zouag.contacts.utils.SpacesItemDecoration;
import com.zouag.contacts.utils.VCFContactConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final int REQUEST_ADD_NEW = 100;
    private static final int REQUEST_VIEW_CONTACT = 101;

    private DatabaseAdapter databaseAdapter;
    private List<Contact> mContacts;
    /**
     * The RecyclerView's adapter.
     */
    private ContactsRecyclerAdapter mAdapter;

    /**
     * The main contacts' RecyclerView.
     */
    @Bind(R.id.contactsList)
    RecyclerView contactsRecyclerView;

    /**
     * The TextView to be displayed in case there were no stored contacts.
     */
    @Bind(R.id.emptyLayout)
    RelativeLayout emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        databaseAdapter = DatabaseAdapter.getInstance(this);
        contactsRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                startAddContactActivity();
                return true;
            case R.id.action_import_contacts:
                importContacts();
                return true;
            case R.id.action_export_contacts:
                exportContacts();
                return true;
            case R.id.action_quit:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Imports the list of contacts that are present in the .vcf file.
     */
    private void importContacts() {

        // Get the .vcf file
        File file = new File(VCFContactConverter.getVCFSavePath(this));
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fis);

            // Get the list of VCards stored inside the .vcf file
            List<VCard> vCards = Ezvcard.parse(reader).all();

            // Show alert dialog
            CharSequence options[] = new CharSequence[]
                    {"Append to existing contacts", "Overwrite existing contacts"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Import options")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                // Append
                                break;
                            case 1:
                                // Overwrite
                                overwriteContacts(vCards);
                                break;
                        }

                        // Show contacts
                        toggleRecyclerviewState();
                        setupRecyclerView();

                        Snackbar.make(getWindow().getDecorView(),
                                "Contacts successfully loaded.", Snackbar.LENGTH_LONG).show();
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();

        } catch (IOException e) {
            Snackbar.make(getWindow().getDecorView(),
                    "The save file cannot be found.", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Converts the passed-in list of VCards to Contact objects, deletes all
     * existing contacts & adds the new ones.
     *
     * @param vCards to be converted & saved
     */
    private void overwriteContacts(List<VCard> vCards) {
        // Convert those cards to Contact objects
        mContacts = VCFContactConverter.parseVCards(vCards);

        // Drop all existing contacts
        databaseAdapter.deleteAllContacts();

        // Save new contacts to database
        databaseAdapter.insertContacts(mContacts);
    }

    /**
     * Exports the list of contacts to a .vcf file.
     */
    private void exportContacts() {
        if (mContacts.size() == 0)
            Snackbar.make(getWindow().getDecorView(),
                    "There are no contacts to export.", Snackbar.LENGTH_LONG).show();
        else {
            List<VCard> cards = VCFContactConverter.parseContacts(mContacts);
            writeContactsToFile(cards);
        }
    }

    /**
     * Writes a list of VCards to external storage.
     *
     * @param cards to be saved
     */
    private void writeContactsToFile(List<VCard> cards) {
        String appName = getString(R.string.app_name);
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                appName);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("CONTACTS", "Failed to create directory.");
            }
        }

        String path = VCFContactConverter.getVCFSavePath(this);
        File vcfFile = new File(path);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(vcfFile);
            OutputStreamWriter writer = new OutputStreamWriter(out);

            String vcardString = Ezvcard.write(cards).version(VCardVersion.V4_0).go();

            writer.write(vcardString);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Snackbar.make(getWindow().getDecorView(),
                "Contacts successfully exported.", Snackbar.LENGTH_LONG).show();
    }

    private void startAddContactActivity() {
        startActivityForResult(
                new Intent(this, AlterContactActivity.class), REQUEST_ADD_NEW);
    }

    /**
     * @param view the empty view of the activity.
     */
    public void onEmptyViewClicked(View view) {
        startAddContactActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get the list of contacts
        mContacts = getContacts();
        toggleRecyclerviewState();

        // Setup the adapter & the RecyclerView
        setupRecyclerView();
    }

    /**
     * Toggles the visibility of the RecyclerView & the empty view associated with it.
     */
    private void toggleRecyclerviewState() {
        /* Set the visibility of the empty view & the contactsListView
        according to the contacts' state */
        emptyView.setVisibility(mContacts.size() == 0 ? View.VISIBLE : View.INVISIBLE);
        contactsRecyclerView.setVisibility(mContacts.size() == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private void setupRecyclerView() {
        if (mAdapter == null) {
            mAdapter = new ContactsRecyclerAdapter(this, mContacts);
            mAdapter.setContactClickListener(contact -> {
                Intent intent = new Intent(this, ViewContactActivity.class);
                intent.putExtra("contact", contact);
                startActivityForResult(intent, REQUEST_VIEW_CONTACT);
            });
            contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            contactsRecyclerView.setHasFixedSize(true);
            contactsRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.refill(mContacts);
            mAdapter.notifyDataSetChanged();
            contactsRecyclerView.invalidate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String message = ""; // Message to be displayed in the snack bar

        switch (requestCode) {
            case REQUEST_ADD_NEW:
                switch (resultCode) {
                    case ResultCodes.CONTACT_CREATED:
                        message = getString(R.string.contact_added);
                        break;
                    case RESULT_CANCELED:
                        message = getString(R.string.adding_contact_discarded);
                        break;
                }

                Snackbar.make(getWindow().getDecorView(),
                        message, Snackbar.LENGTH_LONG).show();
                break;
            case REQUEST_VIEW_CONTACT:
                switch (resultCode) {
                    case ResultCodes.CONTACT_DELETED:
                        // A contact has been deleted
                        message = getString(R.string.contact_removed);
                        break;
                }

                if (!"".equals(message))
                    Snackbar.make(getWindow().getDecorView(),
                            message, Snackbar.LENGTH_LONG).show();
                Log.i("WATCH", getContacts().size() + "");
                break;
        }
    }

    /**
     * @return the full list of contacts.
     */
    private List<Contact> getContacts() {
        return databaseAdapter.getAllContacts();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Contact> filteredContacts = filter(mContacts, newText);
        mAdapter.animateTo(filteredContacts);
        contactsRecyclerView.scrollToPosition(0);
        return true;
    }

    /**
     * @param contacts to be filtered
     * @param query    based upon the contacts will be filtered
     * @return the filtered list of contacts
     */
    private List<Contact> filter(List<Contact> contacts, String query) {
        query = query.toLowerCase();

        final List<Contact> filteredContacts = new ArrayList<>();
        for (Contact contact : contacts) {
            final String text = contact.getName().toLowerCase();
            if (text.contains(query)) {
                filteredContacts.add(contact);
            }
        }
        return filteredContacts;
    }
}
