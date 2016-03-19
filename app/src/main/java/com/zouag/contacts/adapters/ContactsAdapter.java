package com.zouag.contacts.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;

import java.io.File;
import java.util.List;

/**
 * Created by Mohammed Aouf ZOUAG on 18/03/2016.
 */
public class ContactsAdapter extends ArrayAdapter<Contact> {

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
        viewHolder.contactImage.setImageURI(Uri.fromFile(new File(contact.getImgPath())));

        return convertView;
    }

    private static class ViewHolder {
        TextView nameText;
        ImageView contactImage;
    }
}
