package com.zouag.contacts.models;

/**
 * Created by Mohammed Aouf ZOUAG on 21/03/2016.
 *
 * Represents the contact's data to be displayed.
 */
public class ContactData {
    private String phonenumber;
    private String email;
    private String address;

    public ContactData(String phonenumber, String email, String address) {
        this.phonenumber = phonenumber;
        this.email = email;
        this.address = address;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "ContactData{" +
                "phonenumber='" + phonenumber + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
