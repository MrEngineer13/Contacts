package com.zouag.contacts.models;

/**
 * Created by Mohammed Aouf ZOUAG on 18/03/2016.
 */
public class Contact {
    private String name;
    private String email;
    private String phoneNumber;
    private String address;

    public static class Builder {
        private String name;
        private String email;
        private String phoneNumber;
        private String address;

        public Contact createContact() {
            return new Contact(this);
        }

        private Builder name(String name) {
            this.name = name;
            return this;
        }

        private Builder email(String email) {
            this.email = email;
            return this;
        }

        private Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        private Builder address(String address) {
            this.address = address;
            return this;
        }
    }

    public Contact(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.address = builder.address;
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
