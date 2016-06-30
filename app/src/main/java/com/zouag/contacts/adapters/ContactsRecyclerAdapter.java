package com.zouag.contacts.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.annimon.stream.function.FunctionalInterface;
import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;
import com.zouag.contacts.utils.ContactPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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
    private String actionMode;

    public ContactsRecyclerAdapter(Context context, List<Contact> contacts) {
        mContext = context;
        mContacts = new ArrayList<>(contacts);
        actionMode = "NORMAL";
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layout = viewType == 0 ? R.layout.contacts_row :
                R.layout.contacts_delete_mode_row;
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(layout, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(v);
        v.setTag(viewHolder);
        return viewHolder;
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

    @Override
    public int getItemViewType(int position) {
        return actionMode.equals("NORMAL") ? 0 : 1;
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

    public void setActionMode(String actionMode) {
        this.actionMode = actionMode;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView nameText;
        public final CircleImageView contactImage;
        public final CheckBox checkBox;

        ViewHolder(View v) {
            super(v);
            nameText = (TextView) v.findViewById(R.id.nameText);
            contactImage = (CircleImageView) v.findViewById(R.id.mainContactImage);
            checkBox = (CheckBox) v.findViewById(R.id.selectCheckbox);

            if (checkBox != null) {
                if (ContactPreferences.getIsDeleting(mContext)) {
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                            listener.onContactChanged(getAdapterPosition(), isChecked));
                }
            }

            // Notify the activity to display the contact
            v.setOnClickListener(view -> listener.showContact(
                    contactImage, mContacts.get(getLayoutPosition())));

            v.setOnLongClickListener(v1 -> {
                listener.showCheckboxes();
                return true;
            });
        }

        public void bind(Contact contact) {
            Log.i("CONTACTV", "bind() got called.");
            if (checkBox != null) {
                checkBox.setChecked(false);
            }
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
        void showContact(View view, Contact contact);
        void showCheckboxes();
        void onContactChanged(int position, boolean state);
    }

    public void setContactClickListener(ContactClickListener listener) {
        this.listener = listener;
    }
}
