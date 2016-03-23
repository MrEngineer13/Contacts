package com.zouag.contacts.utils;

import android.text.TextUtils;

/**
 * Created by Mohammed Aouf ZOUAG on 22/03/2016.
 * <p>
 * This class holds several methods to validate the contents
 * of different inputs of the application.
 */
public class Validation {

    /**
     * Validates the name of the contact.
     *
     * @param name to be verified.
     * @return true if alphabetic, false otherwise.
     */
    public static boolean isAlpha(String name) {
        return name.matches("[a-zA-Z ]+");
    }

    /**
     * Validates the contact's email.
     *
     * @param target the email to be verified.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
