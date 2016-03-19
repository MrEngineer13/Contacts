package com.zouag.contacts.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.adapters.ContactsAdapter;
import com.zouag.contacts.models.Contact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

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
                startActivity(new Intent(this, AddContactActivity.class));
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

    /**
     * @return the full list of contacts.
     */
    private List<Contact> getContacts() {
        Contact contact1 = new Contact("name1", "email1", "phone1", "address1");
        Contact contact2 = new Contact("name2", "email2", "phone2", "address2");
        return Arrays.asList(contact1, contact2);
    }
}
