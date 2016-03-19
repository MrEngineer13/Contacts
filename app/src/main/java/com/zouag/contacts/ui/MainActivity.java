package com.zouag.contacts.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.adapters.ContactsAdapter;
import com.zouag.contacts.adapters.DatabaseAdapter;
import com.zouag.contacts.models.Contact;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_NEW = 100;
    private DatabaseAdapter databaseAdapter;

    /**
     * The main contacts' ListView.
     */
    @Bind(R.id.contactsList)
    ListView contactsListView;

    /**
     * The TextView to be displayed in case there were no stored contacts.
     */
    @Bind(R.id.noContactsEmptyView)
    TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        databaseAdapter = new DatabaseAdapter(this).open();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                startActivityForResult(
                        new Intent(this, AddContactActivity.class), REQUEST_ADD_NEW);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get the list of contacts
        List<Contact> contacts = getContacts();

        /* Set the visibility of the empty view & the contactsListView
        according to the contacts' state */
        emptyView.setVisibility(contacts.size() == 0 ? View.VISIBLE : View.INVISIBLE);
        contactsListView.setVisibility(contacts.size() == 0 ?
                View.INVISIBLE : View.VISIBLE);

        // Setup the adapter & the ListView
        ContactsAdapter adapter = new ContactsAdapter(this, contacts);
        contactsListView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_NEW:
                String message = ""; // Message to be displayed in the snack bar

                switch (resultCode) {
                    case RESULT_OK:
                        message = "Contact successfully added.";
                        break;
                    case RESULT_CANCELED:
                        message = "Adding contact discarded.";
                        break;
                }

                Snackbar.make(getWindow().getDecorView(),
                        message, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * @return the full list of contacts.
     */
    private List<Contact> getContacts() {
        return databaseAdapter.getAllContacts();
    }
}
