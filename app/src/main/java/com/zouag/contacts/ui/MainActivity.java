package com.zouag.contacts.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.zouag.contacts.threads.IOThread;
import com.zouag.contacts.utils.Actions;
import com.zouag.contacts.utils.ContactPreferences;
import com.zouag.contacts.utils.Contacts;
import com.zouag.contacts.utils.Messages;
import com.zouag.contacts.utils.ResultCodes;
import com.zouag.contacts.utils.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import ezvcard.VCard;

public class MainActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener, Handler.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ADD_NEW = 100;
    private static final int REQUEST_VIEW_CONTACT = 101;

    private DatabaseAdapter databaseAdapter;
    private List<Contact> mContacts;
    private Set<String> deleteIndices;
    /**
     * The RecyclerView's adapter.
     */
    private ContactsRecyclerAdapter mAdapter;
    private LinearLayoutManager layoutManager;

    /**
     * The main contacts' RecyclerView.
     */
    @Bind(R.id.contactsList)
    RecyclerView contactsRecyclerView;

    /**
     * The view to be displayed in case there were no stored contacts.
     */
    @Bind(R.id.emptyLayout)
    RelativeLayout emptyView;

    private Handler mHandler;
    private IOThread mIOThread;

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
        deleteIndices = new HashSet<>();
        contactsRecyclerView.addItemDecoration(new SpacesItemDecoration(20));

        setupIOThread();
        setupRecyclerView();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean isDeleting = ContactPreferences.getIsDeleting(this);
        if (isDeleting) {
            Stream.of(
                    menu.findItem(R.id.action_import_contacts),
                    menu.findItem(R.id.action_export_contacts),
                    menu.findItem(R.id.action_clear_contacts),
                    menu.findItem(R.id.action_settings),
                    menu.findItem(R.id.action_add),
                    menu.findItem(R.id.action_search))
                    .forEach(item -> item.setVisible(false));
            menu.findItem(R.id.action_delete)
                    .setVisible(true);
        } else {
            menu.findItem(R.id.action_delete)
                    .setVisible(false);
            Stream.of(
                    menu.findItem(R.id.action_import_contacts),
                    menu.findItem(R.id.action_export_contacts),
                    menu.findItem(R.id.action_clear_contacts),
                    menu.findItem(R.id.action_settings),
                    menu.findItem(R.id.action_add),
                    menu.findItem(R.id.action_search))
                    .forEach(item -> item.setVisible(true));
        }

        return true;
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
            case R.id.action_delete:
                if (deleteIndices.size() != 0) {
                    Stream.of(deleteIndices)
                            .map(Integer::valueOf)
                            .forEach(index -> {
                                Contact contactToDelete = mContacts.get(index);
                                databaseAdapter.deleteContact(contactToDelete.getId());
                            });

                    hideDeleteCheckboxes();
                    deleteIndices = new HashSet<>();
                    refreshContactsAdapter();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshContactsAdapter();
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
                        break;
                }
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case Messages.MSG_EXPORT_ENDED:
                Snackbar.make(getWindow().getDecorView(),
                        R.string.contacts_export_success, Snackbar.LENGTH_LONG).show();
                break;
            case Messages.MSG_IMPORT_COMPLETED:
                handleImportCompleted((List<Contact>) msg.obj, msg.arg1);
                break;
            case Messages.MSG_IMPORT_FAILED:
                showFileNotFoundSnack();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        boolean isDeleting = ContactPreferences.getIsDeleting(this);
        if (isDeleting) {
            hideDeleteCheckboxes();
            deleteIndices = new HashSet<>();
        } else super.onBackPressed();
    }

    private void hideDeleteCheckboxes() {
        ContactPreferences.setIsDeleting(this, false);
        invalidateOptionsMenu();
        mAdapter.setActionMode("NORMAL");
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Contact> filteredContacts = Contacts.filter(mContacts, newText);
        mAdapter.animateTo(filteredContacts);
        contactsRecyclerView.scrollToPosition(0);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        boolean isDeleting = ContactPreferences.getIsDeleting(this);
        if (isDeleting) {
            hideDeleteCheckboxes();
            deleteIndices = new HashSet<>();
        }
    }

    @Override
    protected void onDestroy() {
        mIOThread.getWorkerHandler()
                .obtainMessage(Messages.MSG_SHUTDOWN).sendToTarget();
        try {
            mIOThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    /**
     * @param view the empty view of the activity.
     */
    public void onEmptyViewClicked(View view) {
        startAddContactActivity();
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
                    refreshContactsAdapter();

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
            executeImport(Actions.IMPORT_OVERWRITE);
        } else {
            // There are contacts so show the popup to append/overwrite
            // Next, check if the 'default action setting' is set
            String default_action = ContactPreferences.getDefaultImportAction(this);
            if (default_action.equals(getString(R.string.ask_for_action)))
                showImportDialog();
            else if (default_action.equals(getString(R.string.action_append)))
                executeImport(Actions.IMPORT_APPEND);
            else
                executeImport(Actions.IMPORT_OVERWRITE);
        }
    }

    private void showImportDialog() {
        // Show alert dialog
        CharSequence options[] = new CharSequence[]
                {getString(R.string.action_append), getString(R.string.action_overwrite)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.import_options)
                .setItems(options, (dialog, which) -> {
                    // which is 0 : append / 1 : overwrite
                    executeImport(which == 0 ?
                            Actions.IMPORT_APPEND : Actions.IMPORT_OVERWRITE);
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    /**
     * Executes the contacts' import based on the passed-in action.
     *
     * @param action to be executed (append/overwrite)
     */
    private void executeImport(Actions action) {
        // Start importing contacts
        mIOThread.getWorkerHandler()
                .obtainMessage(Messages.MSG_START_IMPORTING, action)
                .sendToTarget();
    }

    /**
     * @param nbr_contacts that were added.
     */
    private void showImportSuccessSnack(int nbr_contacts) {
        Snackbar.make(getWindow().getDecorView(),
                String.format(
                        nbr_contacts == 1 ? getString(R.string.contact_loaded_success) :
                                nbr_contacts == 0 ? getString(R.string.zero_contacts_loaded) :
                                        getString(R.string.contacts_loaded_success),
                        nbr_contacts),
                Snackbar.LENGTH_LONG)
                .show();
    }

    private void showFileNotFoundSnack() {
        Snackbar.make(getWindow().getDecorView(),
                R.string.save_file_not_found,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.open_settings, view -> showSettings())
                .show();
    }

    /**
     * Appends these contacts to the list of existing contacts.
     *
     * @param contacts to be appended
     * @return the number of newly added contacts.
     */
    private int appendContacts(List<Contact> contacts) {
        List<Contact> filteredContacts = filterExistingContacts(contacts);

        // Save new contacts to database
        databaseAdapter.insertContacts(filteredContacts);

        return filteredContacts.size();
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
            List<VCard> cards = Contacts.parseContacts(mContacts);

            // Start exporting contacts
            mIOThread.getWorkerHandler()
                    .obtainMessage(Messages.MSG_START_EXPORTING, cards)
                    .sendToTarget();
        }
    }

    private void startAddContactActivity() {
        startActivityForResult(
                new Intent(this, AlterContactActivity.class), REQUEST_ADD_NEW);
    }

    private void setupIOThread() {
        mHandler = new Handler(this);
        mIOThread = new IOThread(this, mHandler);
        mIOThread.start();
    }

    /**
     * Initial setup of the contacts' RecyclerView.
     */
    private void setupRecyclerView() {
        mContacts = getContacts();
        layoutManager = new LinearLayoutManager(this);

        mAdapter = new ContactsRecyclerAdapter(this, mContacts);
        mAdapter.setContactClickListener(new ContactsRecyclerAdapter.ContactClickListener() {
            @Override
            public void showContact(View view, Contact contact) {
                Intent intent = new Intent(MainActivity.this, ViewContactActivity.class);
                intent.putExtra("contact", contact);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this,
                                    new Pair<>(view, getString(R.string.transition_contact_img)));
                    ActivityCompat.startActivityForResult(
                            MainActivity.this, intent, REQUEST_VIEW_CONTACT, options.toBundle());
                } else
                    startActivityForResult(intent, REQUEST_VIEW_CONTACT);
            }

            @Override
            public void showCheckboxes() {
                mAdapter.notifyDataSetChanged();
                ContactPreferences.setIsDeleting(MainActivity.this, true);
                invalidateOptionsMenu();
                mAdapter.setActionMode("DELETE_MODE");
            }

            @Override
            public void onContactChanged(int position, boolean state) {
                if (state)
                    deleteIndices.add(position + "");
                else
                    deleteIndices.remove(position + "");
                Log.i("CONTACTV", "Selected indices: " + deleteIndices);
            }
        });
        contactsRecyclerView.setLayoutManager(layoutManager);
        contactsRecyclerView.setHasFixedSize(true);
        contactsRecyclerView.setAdapter(mAdapter);
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

    private void refreshContactsAdapter() {
        mContacts = getContacts();
        toggleRecyclerviewState();
        mAdapter.animateTo(mContacts);
    }

    /**
     * Undoes the deletion of the passed-in contact.
     *
     * @param contact to be re-added.
     */
    private void undoDelete(Contact contact) {
        databaseAdapter.insertContactByID(contact);
        refreshContactsAdapter();
    }

    /**
     * Undoes the deletion of all contacts.
     *
     * @param contacts to be re-added
     */
    private void undoDeleteAll(List<Contact> contacts) {
        databaseAdapter.insertContacts(contacts);
        refreshContactsAdapter();
    }

    /**
     * @return the full list of contacts.
     */
    private List<Contact> getContacts() {
        String ordering = ContactPreferences.getOrdering(this);
        return databaseAdapter.getAllContacts(ordering);
    }

    /**
     * Handles the import request based on the passed-in action.
     *
     * @param importedContacts
     * @param action           to be taken
     */
    private void handleImportCompleted(List<Contact> importedContacts, int action) {
        // The number of new contacts
        int nbr_contacts = 0;
        // The action to take (append/overwrite)
        Actions act = action == 0 ? Actions.IMPORT_APPEND : Actions.IMPORT_OVERWRITE;

        switch (act) {
            case IMPORT_APPEND:
                // Append
                nbr_contacts = appendContacts(importedContacts);
                break;
            case IMPORT_OVERWRITE:
                // Overwrite
                overwriteContacts(importedContacts);
                nbr_contacts = importedContacts.size();
                break;
        }

        // Show contacts
        refreshContactsAdapter();
        contactsRecyclerView.scrollToPosition(0);

        showImportSuccessSnack(nbr_contacts);
    }
}