package com.zouag.contacts.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.zouag.contacts.R;
import com.zouag.contacts.adapters.DatabaseAdapter;
import com.zouag.contacts.models.Contact;
import com.zouag.contacts.utils.ContactPreferences;
import com.zouag.contacts.utils.ResultCodes;
import com.zouag.contacts.utils.Validation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AlterContactActivity extends AppCompatActivity {

    private static final String TAG = AlterContactActivity.class.getSimpleName();

    // Request codes
    private static final int REQUEST_OPEN_GALLERY = 10;
    private static final int REQUEST_OPEN_CAMERA = 11;

    /**
     * The maximum size of the contacts' profile images.
     */
    private static final int IMAGE_SIZE_LIMIT = 1024 * 1024; // 1 MB

    /**
     * The path of the currently selected image.
     */
    private String current_img_path = "";

    /**
     * The path of the image stored in shared preferences.
     */
    private String preference_img_path = "";

    /**
     * The path of the image stored from the last orientation change.
     */
    private String orientation_img_path = "";
    /**
     * The ID of the currently-being-modified contact.
     */
    private int currentContactID;
    private DatabaseAdapter databaseAdapter;
    private boolean isUpdating;

    @Bind(R.id.contactName)
    EditText contactName;
    @Bind(R.id.contactNumber)
    EditText contactNumber;
    @Bind(R.id.contactEmail)
    EditText contactEmail;
    @Bind(R.id.contactAddress)
    EditText contactAddress;
    @Bind(R.id.contactImage)
    ImageView contactImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        isUpdating = intent.getBooleanExtra("isUpdating", false);

        if (savedInstanceState != null) {
            orientation_img_path = savedInstanceState.getString("imgPath");
        } else {
            if (!isUpdating) {
                // If we're not updating an old contact, check if there is one
                // stored in shared preferences

                if (ContactPreferences.isSaveContactToDraftON(this)) {
                    Contact contact = ContactPreferences.loadContact(this);

                    setupFieldValues(contact);
                    preference_img_path = contact.getImgPath();
                }
            }
        }

        databaseAdapter = DatabaseAdapter.getInstance(this);
        initializeUI();

        setListenerOnSharedPreferences();
    }

    private void setListenerOnSharedPreferences() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(
                (sharedPref, key) -> {
                    if (key.equals(getString(R.string.save_draft_contact)))
                        ContactPreferences.clearDraft(this);
                }
        );
    }

    private void setupFieldValues(Contact contact) {
        contactName.setText(contact.getName());
        contactNumber.setText(contact.getPhoneNumber());
        contactEmail.setText(contact.getEmail());
        contactAddress.setText(contact.getAddress());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the path of the selected image on orientation change
        outState.putString("imgPath", current_img_path);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OPEN_GALLERY:
                switch (resultCode) {
                    case RESULT_OK:
                        Uri imageData = data.getData();

                        // Check the selected image's size
                        if (isSizeAppropriate(imageData)) {
                            // Save the path of the retrieved image
                            current_img_path = getRealPathFromURI(imageData);
                            contactImage.setImageURI(imageData);
                            break;
                        } else {
                            Toast.makeText(this,
                                    String.format(
                                            getString(R.string.maximum_img_size_exceeded),
                                            IMAGE_SIZE_LIMIT / (1024 * 1024)),
                                    Toast.LENGTH_LONG).show();
                        }
                }
                break;
            case REQUEST_OPEN_CAMERA:
                switch (resultCode) {
                    case RESULT_OK:
                        Bundle extras = data.getExtras();
                        Bitmap image = (Bitmap) extras.get("data");
                        saveImageToDisk(image);
                        contactImage.setImageBitmap(image);
                        break;
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (ContactPreferences.isSaveContactToDraftON(this)) {
            // Only save contact to draft if we're creating a new contact, not updating one
            if (!isUpdating) {
                // Save contact to draft

                String name = contactName.getText().toString();
                String imgPath = current_img_path;
                String phone = contactNumber.getText().toString();
                String email = contactEmail.getText().toString();
                String address = contactAddress.getText().toString();

                if (name.length() != 0 || imgPath.length() != 0 || phone.length() != 0
                        || email.length() != 0 || address.length() != 0)
                    setResult(ResultCodes.CONTACT_SAVED_TO_DRAFT);

                ContactPreferences.saveContact(this,
                        new Contact.Builder()
                                .name(name)
                                .imgPath(imgPath)
                                .phoneNumber(phone)
                                .email(email)
                                .address(address)
                                .createContact());
            }
        }

        super.onBackPressed();
    }

    /**
     * @param image to be checked
     * @return true if the image's size is below the maximum size, and false otherwise.
     */
    private boolean isSizeAppropriate(Uri image) {
        int filesize = 0;

        try (InputStream inputStream =
                     getContentResolver().openInputStream(image)) {
            filesize = inputStream.available();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    R.string.error_opening_img,
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    R.string.unknown_error,
                    Toast.LENGTH_LONG).show();
        }

        return filesize < IMAGE_SIZE_LIMIT;
    }

    /**
     * This method checks whether we will be creating or updating a contact.
     * If updating, retrieve the contact associated with the passed-in intent
     * & fill the contact fields accordingly.
     */
    private void initializeUI() {
        setupActionbar();

        if (isUpdating) {
            // We're updating, retrieve the passed-in contact
            Contact contact = getIntent().getParcelableExtra("contact");

            // Set the action bar's title
            getSupportActionBar().setTitle(R.string.update_contact);

            // Save the ID of the currently-being-modified contact
            currentContactID = contact.getId();

            if ("".equals(orientation_img_path)) {
                // There is no image stored from the last orientation change

                // Get the path of the image associated with the current contact
                String contact_img_path = contact.getImgPath();
                if ("".equals(contact_img_path)) {
                    // The contact has no image associated with it
                    contactImage.setImageResource(R.drawable.ic_action_user_grey);
                } else {
                    // The contact has an image associated with it
                    contactImage.setImageURI(Uri.fromFile(new File(contact_img_path)));
                    current_img_path = contact_img_path;
                }
            } else {
                // There is an image stored from the last orientation change
                contactImage.setImageURI(Uri.fromFile(new File(orientation_img_path)));
                current_img_path = orientation_img_path;
            }

            setupFieldValues(contact);
        } else {
            // We're creating a new contact

            if ("".equals(orientation_img_path)) {
                // There is no image stored from the last orientation change

                if ("".equals(preference_img_path)) {
                    // There is no image stored in shared preferences
                    contactImage.setImageResource(R.drawable.ic_action_user_grey);
                } else {
                    // There is an image stored in shared preferences
                    contactImage.setImageURI(Uri.fromFile(new File(preference_img_path)));
                    current_img_path = preference_img_path;
                }
            } else {
                // There is an image stored from the last orientation change
                contactImage.setImageURI(Uri.fromFile(new File(orientation_img_path)));
                current_img_path = orientation_img_path;
            }
        }
    }

    private void setupActionbar() {
        // Show the back arrow button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void saveImageToDisk(Bitmap image) {
        // 1. Get the external storage directory
        String appName = getString(R.string.app_name);
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                appName);

        // 2. Create our subdirectory
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("CONTACTS", "Failed to create directory.");
            }
        }

        // 3. Create a file name
        // 4. Create the file
        Date now = new Date();
        // The format of the names of the images to be saved
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(now);
        String path = mediaStorageDir.getPath() + File.separator;

        // Save the path of the retrieved image
        current_img_path = path + "IMG_" + timestamp + ".png";
        File mediaFile = new File(current_img_path);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mediaFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.i("CONTACTS", "Image successfully saved.");
    }

    /**
     * @param contentURI of the image
     * @return the path of this image in disk.
     */
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
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
            // This boolean flag indicates whether we're creating a new contact,
            // or updating an old one.
            boolean isUpdating = getIntent().getBooleanExtra("isUpdating", false);
            if (isUpdating) {
                // Update contact
                databaseAdapter.updateContact(currentContactID, newContact);

                // Contact successfully updated
                setResult(ResultCodes.CONTACT_UPDATED);
                finish();
            } else {
                // Check if there is already a contact with the same name as the new one
                Contact existing = databaseAdapter.getContact(newContact.getName());
                if (existing != null) {
                    if (existing.getName().equals(newContact.getName()))
                        showDialog(getString(R.string.add_contact),
                                getString(R.string.contact_already_exists),
                                getString(R.string.create_anyway),
                                (dialog, which) -> confirmAddContact(newContact));
                }
                else
                    confirmAddContact(newContact);
            }
        }
    }

    private void confirmAddContact(Contact contact) {
        // Insert contact
        databaseAdapter.insertContact(contact);

        // Delete draft
        ContactPreferences.clearDraft(this);

        // Contact successfully created
        setResult(ResultCodes.CONTACT_CREATED);
        finish();
    }

    /**
     * Invoked when the contact's image is clicked.
     *
     * @param view the button.
     */
    public void onImageClicked(View view) {
        CharSequence options[] = new CharSequence[]{"Gallery", "Camera"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_img_from));
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                    gallery.setType("image/*");
                    startActivityForResult(gallery, REQUEST_OPEN_GALLERY);
                    break;
                case 1:
                    Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camera, REQUEST_OPEN_CAMERA);
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
        String dialogTitle = "";
        // The error message to be displayed in case the validation failed.
        String dialogMessage = "";

        boolean nameLengthStatus = name.length() > 0;
        boolean nameValidStatus = Validation.isAlpha(name);
        boolean phoneStatus = (phoneNumber.length() == 10);
        boolean emailLengthStatus = email.length() != 0;

        if (nameLengthStatus && nameValidStatus && phoneStatus) {

            boolean emailValidStatus = Validation.isValidEmail(email);
            boolean addressStatus = address.length() != 0;

            Contact.Builder contactBuilder = new Contact.Builder()
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .imgPath(current_img_path);

            if (emailLengthStatus) {
                // An email has been specified, verify it.
                if (!emailValidStatus) {
                    // Invalid Email address.
                    dialogTitle = getString(R.string.invalid_email);
                    dialogMessage = getString(R.string.enter_valid_email);

                    showDialog(dialogTitle, dialogMessage, getString(R.string.ok), null);
                    return null;
                } else
                    contactBuilder.email(email);
            } else {
                contactBuilder.email("");
            }

            contactBuilder.address(addressStatus ? address : "");
            return contactBuilder.createContact();

        } else if (!nameLengthStatus) {
            // Invalid contact name.
            dialogTitle = getString(R.string.invalid_contact);
            dialogMessage = getString(R.string.enter_contact_name);
        } else if (!nameValidStatus) {
            // Invalid contact name.
            dialogTitle = getString(R.string.invalid_contact);
            dialogMessage = getString(R.string.enter_valid_contact_name);
        } else if (!phoneStatus) {
            // Invalid phone number.
            dialogTitle = getString(R.string.invalid_phone);
            dialogMessage = getString(R.string.enter_valid_phone);
            contactNumber.setError(getString(R.string.ten_digits));
        }

        // Something went wrong: show the error dialog.
        showDialog(dialogTitle, dialogMessage, getString(R.string.ok), null);

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
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }
}
