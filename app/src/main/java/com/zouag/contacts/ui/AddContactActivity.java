package com.zouag.contacts.ui;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;

import butterknife.Bind;

public class AddContactActivity extends AppCompatActivity {

    @Bind(R.id.contactName)
    EditText contactName;
    @Bind(R.id.contactNumber)
    EditText contactNumber;
    @Bind(R.id.contactEmail)
    EditText contactEmail;
    @Bind(R.id.contactAddress)
    EditText contactAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
    }

    /**
     * Invoked when the 'CANCEL' button is clicked.
     *
     * @param view
     */
    public void onCancel(View view) {
        // Terminate the activity
        finish();
    }

    /**
     * Invoked when pressing the 'SAVE' button to save a contact.
     *
     * @param view
     */
    public void onSave(View view) {
        String name = contactName.getText().toString();
        String phoneNumber = contactNumber.getText().toString();
        String email = contactEmail.getText().toString();
        String address = contactAddress.getText().toString();

        String dialogTitle;
        // The error message to be displayed in case the validation failed.
        String dialogMessage = "";

        boolean nameStatus = name.length() > 0;
        boolean phoneStatus = phoneNumber.length() == 10;
        boolean emailStatus = email.length() != 0;
        boolean addressStatus = address.length() != 0;

        if (nameStatus && phoneStatus) {
            Contact newContact = new Contact.Builder()
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .email(emailStatus ? email : "")
                    .address(addressStatus ? address : "")
                    .createContact();

            return;
        }
        else if(!nameStatus) {
            // Invalid contact name.
            dialogTitle = "Invalid contact name.";
            dialogMessage = "Please enter the name of the new contact.";
        }
        else {
            // Invalid phone number.
            dialogTitle = "Invalid phone number.";
            dialogMessage = "Please enter the phone number of the new contact.";
        }

        if (!dialogMessage.equals("")) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(dialogTitle);
            builder.setMessage(dialogMessage);
            builder.setPositiveButton("GOT IT", null);
            builder.show();
        }
    }
}
