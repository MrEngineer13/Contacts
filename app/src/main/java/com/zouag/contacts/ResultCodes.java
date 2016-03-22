package com.zouag.contacts;

/**
 * Created by Mohammed Aouf ZOUAG on 22/03/2016.
 *
 * This class holds a variety of result codes to be used when
 * communicating between the AlterContactActivity and the MainActivity.
 */
public class ResultCodes {
    /**
     * A signal that a new contact has been created.
     */
    public static final int CONTACT_CREATED = 1;
    /**
     * A signal that a certain contact has been updated.
     */
    public static final int CONTACT_UPDATED = 2;
    /**
     * A signal that a contact has been dropped.
     */
    public static final int CONTACT_DELETED = 3;
}
