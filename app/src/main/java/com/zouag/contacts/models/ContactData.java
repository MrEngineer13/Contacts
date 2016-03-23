package com.zouag.contacts.models;

/**
 * Created by Mohammed Aouf ZOUAG on 21/03/2016.
 * <p>
 * A model class used to hold the data of the contacts' informations.
 */
public class ContactData {
    private String description;
    private String data;
    private int icon;

    public ContactData(String description, String data, int icon) {
        this.description = description;
        this.data = data;
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public String getData() {
        return data;
    }

    public int getIcon() {
        return icon;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ContactData{" +
                "description='" + description + '\'' +
                ", data='" + data + '\'' +
                ", icon=" + icon +
                '}';
    }
}
