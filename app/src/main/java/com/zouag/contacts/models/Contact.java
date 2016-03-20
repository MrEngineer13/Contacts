package com.zouag.contacts.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Mohammed Aouf ZOUAG on 18/03/2016.
 */
public class Contact implements Parcelable {
    private int id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String imgPath;

    public static class Builder {
        private int id;
        private String name;
        private String email;
        private String phoneNumber;
        private String address;
        private String imgPath;

        public Contact createContact() {
            return new Contact(this);
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder imgPath(String imgPath) {
            this.imgPath = imgPath;
            return this;
        }
    }

    public Contact(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.address = builder.address;
        this.imgPath = builder.imgPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(phoneNumber);
        dest.writeString(address);
        dest.writeString(imgPath);
    }

    private Contact(Parcel in) {
        id = in.readInt();
        name = in.readString();
        email = in.readString();
        phoneNumber = in.readString();
        address = in.readString();
        imgPath = in.readString();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {

        @Override
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
