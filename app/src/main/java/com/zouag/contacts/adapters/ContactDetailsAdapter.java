package com.zouag.contacts.adapters;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.models.ContactData;

import java.util.List;

/**
 * Created by Mohammed Aouf ZOUAG on 21/03/2016.
 * <p>
 * The adapter of the contacts' details list view.
 */
public class ContactDetailsAdapter extends ArrayAdapter<ContactData> {

    private Context context;
    private List<ContactData> details;

    public ContactDetailsAdapter(Context context, List<ContactData> details) {
        super(context, -1, details);

        this.context = context;
        this.details = details;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = ((AppCompatActivity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.contact_detail_row, parent, false);
            // Setup the ViewHolder
            viewHolder = new ViewHolder();
            viewHolder.description = (TextView) convertView.findViewById(R.id.description);
            viewHolder.data = (TextView) convertView.findViewById(R.id.data);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.detailIcon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ContactData contactData = details.get(position);
        viewHolder.description.setText(contactData.getDescription());
        viewHolder.data.setText(contactData.getData());
        viewHolder.icon.setImageResource(contactData.getIcon());

        return convertView;
    }

    private static class ViewHolder {
        TextView description;
        TextView data;
        ImageView icon;
    }
}
