package com.zouag.contacts.utils;

/**
 * Created by Mohammed Aouf ZOUAG on 27/03/2016.
 */
public enum Actions {
    IMPORT_APPEND(0), IMPORT_OVERWRITE(1);

    private int id;

    Actions(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
