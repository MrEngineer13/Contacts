package com.zouag.contacts.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.zouag.contacts.R;
import com.zouag.contacts.adapters.DatabaseAdapter;
import com.zouag.contacts.models.Contact;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddContactActivity extends AppCompatActivity {

    @Bind(R.id.contactName)
    EditText contactName;
    @Bind(R.id.contactNumber)
    EditText contactNumber;
    @Bind(R.id.contactEmail)
    EditText contactEmail;
    @Bind(R.id.contactAddress)
    EditText contactAddress;

    private DatabaseAdapter databaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        ButterKnife.bind(this);

        databaseAdapter = new DatabaseAdapter(this).open();
    }

    /**
     * Invoked when the 'CANCEL' button is clicked.
     *
     * @param view
     */
    public void onCancel(View view) {
        // Terminate the activity
        setResult(RESULT_CANCELED);
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

        Contact newContact = validateFields(name, phoneNumber, email, address);
        if (newContact != null) {
            databaseAdapter.insertContact(newContact);

            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * Invoked when the contact's image is clicked.
     *
     * @param view the button.
     */
    public void onImageClicked(View view) {
        CharSequence options[] = new CharSequence[] {"Gallery", "Camera"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from ...");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    Toast.makeText(this, "Gallery", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(this, "Camera", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
        builder.show();
    }

    /**
     * @param name        of the contact
     * @param phoneNumber of the contact
     * @param email       of the contact
     * @param address     of the contact
     * @return a new Contact object if it passed validation, or null otherwise.
     */
    private Contact validateFields(String name, String phoneNumber,
                                   String email, String address) {
        String dialogTitle;
        // The error message to be displayed in case the validation failed.
        String dialogMessage;

        boolean nameStatus = name.length() > 0;
        boolean phoneStatus = phoneNumber.length() == 10;
        boolean emailStatus = email.length() != 0;
        boolean addressStatus = address.length() != 0;

        if (nameStatus && phoneStatus) {
            return new Contact.Builder()
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .email(emailStatus ? email : "")
                    .address(addressStatus ? address : "")
                    .createContact();
        } else if (!nameStatus) {
            // Invalid contact name.
            dialogTitle = "Invalid contact name.";
            dialogMessage = "Please enter the name of the new contact.";
        } else {
            // Invalid phone number.
            dialogTitle = "Invalid phone number.";
            dialogMessage = "Please enter the phone number of the new contact.";
        }

        // Something went wrong: show the error dialog.
        showDialog(dialogTitle, dialogMessage, "GOT IT", null);

        return null;
    }

    /**
     * Shows a custom dialog.
     *
     * @param title      of the dialog
     * @param message    of the dialog
     * @param actionText text of the dialog's action button
     * @param listener   on the action button
     */
    public void showDialog(final String title, final String message, final String actionText,
                           final DialogInterface.OnClickListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(actionText, listener);

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
