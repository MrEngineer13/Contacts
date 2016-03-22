package com.zouag.contacts.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.function.FunctionalInterface;
import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;

import java.io.File;
import java.util.List;

/**
 * Created by Mohammed Aouf ZOUAG on 22/03/2016.
 */
public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<Contact> mContacts;
    /**
     * A listener on the items handled by this adapter.
     */
    private ContactClickListener listener;

    public ContactsRecyclerAdapter(Context context, List<Contact> contacts) {
        mContext = context;
        mContacts = contacts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.contacts_row, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        TextView contactName = viewHolder.nameText;
        ImageView contactImage = viewHolder.contactImage;

        Contact contact = mContacts.get(position);

        contactName.setText(contact.getName());

        String imgPath = contact.getImgPath();
        if ("".equals(imgPath)) {
            // Set default contact image
            contactImage.setImageResource(R.drawable.ic_action_user);
        } else {
            // Set the contact's image
            contactImage.setImageURI(Uri.fromFile(new File(imgPath)));
        }
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameText;
        private final ImageView contactImage;

        ViewHolder(View v) {
            super(v);
            nameText = (TextView) v.findViewById(R.id.nameText);
            contactImage = (ImageView) v.findViewById(R.id.rowContactImage);

            // Notify the activity to display the contact
            v.setOnClickListener(view -> listener.showContact(mContacts.get(getLayoutPosition())));
        }
    }

    /**
     * An outer activity listener on the click events over the items of the adapter.
     */
    @FunctionalInterface
    public interface ContactClickListener {
        void showContact(Contact contact);
    }

    public void setContactClickListener(ContactClickListener listener) {
        this.listener = listener;
    }
}
