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
import java.util.ArrayList;
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
        mContacts = new ArrayList<>(contacts);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.contacts_row, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Contact contact = mContacts.get(position);
        viewHolder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public Contact removeItem(int position) {
        final Contact contact = mContacts.remove(position);
        notifyItemRemoved(position);
        return contact;
    }

    public void addItem(int position, Contact contact) {
        mContacts.add(position, contact);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Contact contact = mContacts.remove(fromPosition);
        mContacts.add(toPosition, contact);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<Contact> contacts) {
        applyAndAnimateRemovals(contacts);
        applyAndAnimateAdditions(contacts);
        applyAndAnimateMovedItems(contacts);
    }

    private void applyAndAnimateRemovals(List<Contact> newContacts) {
        for (int i = mContacts.size() - 1; i >= 0; i--) {
            final Contact contact = mContacts.get(i);
            if (!newContacts.contains(contact))
                removeItem(i);
        }
    }

    private void applyAndAnimateAdditions(List<Contact> newContacts) {
        for (int i = 0, count = newContacts.size(); i < count; i++) {
            final Contact contact = newContacts.get(i);
            if (!mContacts.contains(contact))
                addItem(i, contact);
        }
    }

    private void applyAndAnimateMovedItems(List<Contact> newContacts) {
        for (int toPosition = newContacts.size() - 1; toPosition >= 0; toPosition--) {
            final Contact contact = newContacts.get(toPosition);
            final int fromPosition = mContacts.indexOf(contact);

            if (fromPosition >= 0 && fromPosition != toPosition)
                moveItem(fromPosition, toPosition);
        }
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

        public void bind(Contact contact) {
            nameText.setText(contact.getName());

            String imgPath = contact.getImgPath();
            if ("".equals(imgPath)) {
                // Set default contact image
                contactImage.setImageResource(R.drawable.ic_action_user);
            } else {
                // Set the contact's image
                contactImage.setImageURI(Uri.fromFile(new File(imgPath)));
            }
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
