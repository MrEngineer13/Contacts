package com.zouag.contacts.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;

import java.util.Comparator;

/**
 * Created by Mohammed Aouf ZOUAG on 25/03/2016.
 * <p>
 * A utility class that handles interactions with the shared preferences of the app.
 */
public class ContactPreferences {

    private static final String CONTACT_NAME = "contactName";
    private static final String CONTACT_IMAGE = "contactImage";
    private static final String CONTACT_NUMBER = "contactNumber";
    private static final String CONTACT_EMAIL = "contactEmail";
    private static final String CONTACT_ADDRESS = "contactAddress";

    public static final String ORDERING = "natural";

    /**
     * @param context
     * @return true if the user has chosen to save discarded contacts to draft,
     * or return false otherwise.
     */
    public static boolean isSaveContactToDraftON(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPreferences.getBoolean(
                context.getString(R.string.save_draft_contact), true);
    }

    /**
     * Saves the passed-in contact to shared preferences.
     *
     * @param context
     * @param contact to be saved
     */
    public static void saveContact(Context context, Contact contact) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(CONTACT_NAME, contact.getName())
                .putString(CONTACT_IMAGE, contact.getImgPath())
                .putString(CONTACT_NUMBER, contact.getPhoneNumber())
                .putString(CONTACT_EMAIL, contact.getEmail())
                .putString(CONTACT_ADDRESS, contact.getAddress())
                .apply();
    }

    /**
     * @param context
     * @return a Contact object that represents the previously saved contact to draft.
     */
    public static Contact loadContact(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return new Contact.Builder()
                .name(sharedPreferences.getString(CONTACT_NAME, ""))
                .imgPath(sharedPreferences.getString(CONTACT_IMAGE, ""))
                .phoneNumber(sharedPreferences.getString(CONTACT_NUMBER, ""))
                .email(sharedPreferences.getString(CONTACT_EMAIL, ""))
                .address(sharedPreferences.getString(CONTACT_ADDRESS, ""))
                .createContact();
    }

    /**
     * Clears the contact saved to draft.
     *
     * @param context
     */
    public static void clearDraft(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(CONTACT_NAME)
                .remove(CONTACT_IMAGE)
                .remove(CONTACT_NUMBER)
                .remove(CONTACT_EMAIL)
                .remove(CONTACT_ADDRESS)
                .apply();
    }

    /**
     * @return the ordering of the contacts. (ASC, DESC, NATURAL)
     */
    public static String getOrdering(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("key_sort_contacts", ORDERING);
    }

    /**
     * @param context
     * @return a Comparator object if sorting was enabled in preferences,
     * or null otherwise.
     */
    public static Comparator<Contact> getComparator(Context context) {
        String ordering = getOrdering(context);
        return ordering.equals("ASC") ?
                (c1, c2) -> c1.getName().compareTo(c2.getName()) :
                ordering.equals("DESC") ?
                        (c1, c2) -> c2.getName().compareTo(c1.getName()) :
                        null;
    }
}
