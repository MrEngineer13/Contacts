package com.zouag.contacts.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.models.ContactData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohammed Aouf ZOUAG on 22/03/2016.
 */
public class ContactDetailsAdapter extends RecyclerView.Adapter<ContactDetailsAdapter.ViewHolder> {

    private Context mContext;
    private List<ContactData> contactData;

    public ContactDetailsAdapter(Context context, List<ContactData> contactData) {
        mContext = context;
        this.contactData = new ArrayList<>(contactData);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.contact_detail_row, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ContactData data = contactData.get(position);
        viewHolder.bind(data);
    }

    @Override
    public int getItemCount() {
        return contactData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView description;
        TextView itemData;
        ImageView icon;

        ViewHolder(View v) {
            super(v);
            description = (TextView) v.findViewById(R.id.description);
            itemData = (TextView) v.findViewById(R.id.data);
            icon = (ImageView) v.findViewById(R.id.detailIcon);
        }

        public void bind(ContactData dat) {
            String desc = dat.getDescription();
            if (desc.equals(mContext.getString(R.string.mobile))) {

                // Show the telephone number as a series of 2 numbers separated by a space.

                String data = dat.getData();
                for (int i = 2; i < data.length(); i += 3)
                    data = data.substring(0, i) + " " + data.substring(i);

                dat.setData(data);
            }

            description.setText(dat.getDescription());
            itemData.setText(dat.getData());
            icon.setImageResource(dat.getIcon());
        }
    }
}
