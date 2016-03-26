package com.zouag.contacts.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zouag.contacts.R;
import com.zouag.contacts.models.ContactData;

import java.util.List;

/**
 * Created by Mohammed Aouf ZOUAG on 22/03/2016.
 */
public class ContactDetailsAdapter extends RecyclerView.Adapter<ContactDetailsAdapter.ViewHolder> {

    private Context mContext;
    private List<ContactData> contactData;

    public ContactDetailsAdapter(Context context, List<ContactData> contactData) {
        mContext = context;
        this.contactData = contactData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.contact_detail_row, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ContactData data = contactData.get(position);
        viewHolder.bind(data);
        viewHolder.itemView.setOnClickListener(view -> {
            String mobile = mContext.getString(R.string.mobile);
            String email = mContext.getString(R.string.email);

            Intent intent = null;

            if (data.getDescription().equals(mobile)) {
                // Call contact
                intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + data.getData()));

            } else if (data.getDescription().equals(email)) {
                // Send email
                ShareCompat.IntentBuilder
                        .from(((AppCompatActivity) mContext))
                        .setType("message/rfc822")
                        .addEmailTo(data.getData())
                        .setChooserTitle(R.string.choose_email_client)
                        .startChooser();
            } else {
                // Show address in map
                String map = "http://maps.google.co.in/maps?q=" + data.getData();
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
            }

            if (intent != null)
                mContext.startActivity(intent);
        });
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
