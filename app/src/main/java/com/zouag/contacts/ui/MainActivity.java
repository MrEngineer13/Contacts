package com.zouag.contacts.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
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

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
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

    private static final String TAG = MainActivity.class.getSimpleName();
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

        // Show the toolbar's icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        // Hide the toolbar's title
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        databaseAdapter = DatabaseAdapter.getInstance(this);
        contactsRecyclerView.addItemDecoration(new SpacesItemDecoration(20));

        refreshContacts();
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
            case R.id.action_clear_contacts:
                clearContacts();
                return true;
            case R.id.action_settings:
                showSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshContacts();
    }

    /**
     * Removes all contacts from the database.
     */
    private void clearContacts() {

        if (mContacts.size() == 0) {
            Snackbar.make(getWindow().getDecorView(),
                    R.string.empty_contacts_list,
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.clear_contacts);
        builder.setMessage(R.string.are_you_sure_clear_contacts);
        builder.setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.proceed, (dialog, which) -> {
                    // Save the deleted contacts in case the user decided to undo the operation
                    List<Contact> allDeletedContacts = new ArrayList<>(mContacts);

                    databaseAdapter.deleteAllContacts();
                    refreshContacts();

                    Snackbar.make(getWindow().getDecorView(),
                            R.string.all_contacts_cleared,
                            Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, view -> {
                                undoDeleteAll(allDeletedContacts);
                            })
                            .show();
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    private void showSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Imports the list of contacts that are present in the .vcf file.
     */
    private void importContacts() {

        if (mContacts.size() == 0) {
            // There are no stored contacts, so there is no need for the popup dialog

            // Get the list of contacts stored within the .vcf file
            try {
                List<Contact> newContacts = getContactsFromFile();
                overwriteContacts(newContacts);

                // Show contacts
                refreshContacts();

                Snackbar.make(getWindow().getDecorView(),
                        String.format(
                                getString(R.string.contacts_loaded_success), mContacts.size()),
                        Snackbar.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // There are contacts, so show the popup to append/overwrite

            // Show alert dialog
            CharSequence options[] = new CharSequence[]
                    {getString(R.string.action_append), getString(R.string.action_overwrite)};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.import_options)
                    .setItems(options, (dialog, which) -> {

                        try {
                            // Get the list of contacts stored within the .vcf file
                            List<Contact> newContacts = getContactsFromFile();
                            // The number of new contacts
                            int nbr_contacts = 0;

                            switch (which) {
                                case 0:
                                    // Append
                                    appendContacts(newContacts);
                                    nbr_contacts = Math.abs(mContacts.size() - newContacts.size());
                                    break;
                                case 1:
                                    // Overwrite
                                    overwriteContacts(newContacts);
                                    nbr_contacts = newContacts.size();
                                    break;
                            }

                            // Show contacts
                            mContacts = getContacts();
                            toggleRecyclerviewState();
                            mAdapter.animateTo(mContacts);
                            contactsRecyclerView.scrollToPosition(0);

                            Snackbar.make(getWindow().getDecorView(),
                                    String.format(
                                            nbr_contacts == 1 ? getString(R.string.contact_loaded_success) :
                                                    nbr_contacts == 0 ? getString(R.string.zero_contacts_loaded) :
                                                            getString(R.string.contacts_loaded_success),
                                            nbr_contacts),
                                    Snackbar.LENGTH_LONG)
                                    .show();

                        } catch (IOException e) {
                            Snackbar.make(getWindow().getDecorView(),
                                    R.string.save_file_not_found,
                                    Snackbar.LENGTH_LONG)
                                    .setAction(R.string.open_settings, view -> showSettings())
                                    .show();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();
        }
    }

    public List<Contact> getContactsFromFile() throws IOException {
        // Get the .vcf file
        File file = new File(VCFContactConverter.getVCFSavePath(this));

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(fis);

        // Get the list of VCards stored inside the .vcf file
        List<VCard> vCards = Ezvcard.parse(reader).all();

        // Convert those cards to Contact objects & return them
        return VCFContactConverter.parseVCards(vCards);
    }

    /**
     * Appends these contacts to the list of existing contacts.
     *
     * @param contacts to be appended
     */
    private void appendContacts(List<Contact> contacts) {
        List<Contact> filteredContacts = filterExistingContacts(contacts);

        // Save new contacts to database
        databaseAdapter.insertContacts(filteredContacts);
    }

    private List<Contact> filterExistingContacts(List<Contact> contacts) {
        return Stream.of(contacts)
                .filter(contact -> !Stream.of(mContacts)
                        .map(Contact::getName)
                        .anyMatch(name -> name.equals(contact.getName())))
                .collect(Collectors.toList());
    }

    /**
     * Deletes all existing contacts & adds the new ones.
     *
     * @param contacts to be saved
     */
    private void overwriteContacts(List<Contact> contacts) {
        // Drop all existing contacts
        databaseAdapter.deleteAllContacts();

        // Save new contacts to database
        databaseAdapter.insertContacts(contacts);
    }

    /**
     * Exports the list of contacts to a .vcf file.
     */
    private void exportContacts() {
        if (mContacts.size() == 0)
            Snackbar.make(getWindow().getDecorView(),
                    R.string.no_contacts_to_export, Snackbar.LENGTH_LONG).show();
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
                R.string.contacts_export_success, Snackbar.LENGTH_LONG).show();
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

    private void refreshContacts() {
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
            mAdapter.setContactClickListener((view, contact) -> {
                Intent intent = new Intent(this, ViewContactActivity.class);
                intent.putExtra("contact", contact);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                                    new Pair<>(view, getString(R.string.transition_contact_img)));
                    ActivityCompat.startActivityForResult(
                            this, intent, REQUEST_VIEW_CONTACT, options.toBundle());
                } else
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
                    case ResultCodes.CONTACT_SAVED_TO_DRAFT:
                        message = getString(R.string.contact_saved_to_draft);
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
                        Contact deletedContact =
                                data.getParcelableExtra("deletedContact");
                        message = getString(R.string.contact_removed);

                        mContacts.remove(deletedContact);
                        mAdapter.animateTo(mContacts);

                        Snackbar.make(
                                getWindow().getDecorView(),
                                message,
                                Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, v -> {
                                    // Re-add the deleted contact
                                    undoDelete(deletedContact);
                                })
                                .show();
                        mContacts = getContacts();
                        break;
                }
                break;
        }
    }

    /**
     * Undoes the deletion of the passed-in contact.
     *
     * @param contact to be re-added.
     */
    private void undoDelete(Contact contact) {
        databaseAdapter.insertContactByID(contact);
        mContacts = getContacts();
        mAdapter.animateTo(mContacts);
    }

    /**
     * Undoes the deletion of all contacts.
     *
     * @param contacts to be re-added
     */
    private void undoDeleteAll(List<Contact> contacts) {
        databaseAdapter.insertContacts(contacts);
        mContacts = getContacts();
        toggleRecyclerviewState();
        mAdapter.animateTo(mContacts);
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
