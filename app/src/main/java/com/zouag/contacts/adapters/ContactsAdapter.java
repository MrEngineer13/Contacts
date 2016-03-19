package com.zouag.contacts.adapters;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;

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

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.nameText.setText(contacts.get(position).getName());

        return convertView;
    }

    private static class ViewHolder {
        TextView nameText;
    }
}
