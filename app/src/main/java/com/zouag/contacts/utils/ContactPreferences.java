package com.zouag.contacts.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;

/**
 * Created by Mohammed Aouf ZOUAG on 25/03/2016.
 */
public class ContactPreferences {
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

    public static void saveContact(Context context, Contact contact) {
        Log.i("SAVING", "# " + contact);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit()
                .putString("contactName", contact.getName())
                .putString("contactImage", contact.getImgPath())
                .putString("contactNumber", contact.getPhoneNumber())
                .putString("contactEmail", contact.getEmail())
                .putString("contactAddress", contact.getAddress())
                .apply();
    }
}
