package com.zouag.contacts.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zouag.contacts.R;

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
        boolean status = sharedPreferences.getBoolean(
                context.getString(R.string.save_draft_contact), true);

        return status;
    }
}
