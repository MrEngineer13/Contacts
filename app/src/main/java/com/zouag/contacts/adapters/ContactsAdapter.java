package com.zouag.contacts.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.function.FunctionalInterface;
import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;

import java.io.File;
import java.util.List;

/**
 * Created by Mohammed Aouf ZOUAG on 18/03/2016.
 *
 * The adapter of the main list of contacts.
 */
public class ContactsAdapter extends ArrayAdapter<Contact> {

    /**
     * A listener on the items handled by this adapter.
     */
    private ContactClickListener listener;

    private Context context;
    private List<Contact> contacts;

    public ContactsAdapter(Context context, List<Contact> contacts) {
        super(context, -1, contacts);
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = ((AppCompatActivity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.contacts_row, parent, false);
            // Setup the ViewHolder
            viewHolder = new ViewHolder();
            viewHolder.nameText = (TextView) convertView.findViewById(R.id.nameText);
            viewHolder.contactImage = (ImageView) convertView.findViewById(R.id.rowContactImage);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Contact contact = contacts.get(position);
        viewHolder.nameText.setText(contact.getName());

        String imgPath = contact.getImgPath();
        if ("".equals(imgPath)) {
            // Set default contact image
            viewHolder.contactImage.setImageResource(R.drawable.ic_action_user);
        }
        else {
            // Set the contact's image
            viewHolder.contactImage.setImageURI(Uri.fromFile(new File(imgPath)));
        }

        // Notify the activity to display the contact
        convertView.setOnClickListener(view -> listener.showContact(contact));

        return convertView;
    }

    private static class ViewHolder {
        TextView nameText;
        ImageView contactImage;
    }

    public void setContactClickListener(ContactClickListener listener) {
        this.listener = listener;
    }

    /**
     * An outer activity listener on the click events over the items of the adapter.
     */
    @FunctionalInterface
    public interface ContactClickListener {
        void showContact(Contact contact);
    }
}
