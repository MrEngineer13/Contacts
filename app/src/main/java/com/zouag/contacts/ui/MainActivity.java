package com.zouag.contacts.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.zouag.contacts.R;
import com.zouag.contacts.adapters.ContactsRecyclerAdapter;
import com.zouag.contacts.adapters.DatabaseAdapter;
import com.zouag.contacts.models.Contact;
import com.zouag.contacts.utils.ResultCodes;
import com.zouag.contacts.utils.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

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
            case R.id.action_quit:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

        /* Set the visibility of the empty view & the contactsListView
        according to the contacts' state */
        emptyView.setVisibility(mContacts.size() == 0 ? View.VISIBLE : View.INVISIBLE);
        contactsRecyclerView.setVisibility(mContacts.size() == 0 ? View.INVISIBLE : View.VISIBLE);

        // Setup the adapter & the RecyclerView
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        mAdapter = new ContactsRecyclerAdapter(this, mContacts);
        mAdapter.setContactClickListener(contact -> {
            Intent intent = new Intent(this, ViewContactActivity.class);
            intent.putExtra("contact", contact);
            startActivityForResult(intent, REQUEST_VIEW_CONTACT);
        });
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setHasFixedSize(true);
        contactsRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String message = ""; // Message to be displayed in the snack bar

        switch (requestCode) {
            case REQUEST_ADD_NEW:
                switch (resultCode) {
                    case ResultCodes.CONTACT_CREATED:
                        message = "Contact successfully added.";
                        break;
                    case RESULT_CANCELED:
                        message = "Adding contact discarded.";
                        break;
                }

                Snackbar.make(getWindow().getDecorView(),
                        message, Snackbar.LENGTH_LONG).show();
                break;
            case REQUEST_VIEW_CONTACT:
                switch (resultCode) {
                    case ResultCodes.CONTACT_DELETED:
                        // A contact has been deleted
                        message = "The contact has been removed from your directory.";
                        break;
                }

                if (!"".equals(message))
                    Snackbar.make(getWindow().getDecorView(),
                            message, Snackbar.LENGTH_LONG).show();
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
