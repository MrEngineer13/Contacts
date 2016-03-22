package com.zouag.contacts.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
     * The ID of the currently-being-modified contact.
     */
    private int currentContactID;

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

    private DatabaseAdapter databaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        ButterKnife.bind(this);

        databaseAdapter = DatabaseAdapter.getInstance(this);
        initializeUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
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
                        }
                        else {
                            Toast.makeText(this,
                                    String.format(
                                            "Maximum image size exceeded. " +
                                                    "(%d MB)\nPlease try a different one.",
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
                    "There was an error opening the image.",
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "An unknown error occured. " +
                            "Please try again with a different image.",
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

        Intent intent = getIntent();
        if (intent.getBooleanExtra("isUpdating", false)) {
            // We're updating, retrieve the passed-in contact
            Contact contact = intent.getParcelableExtra("contact");

            // Set the action bar's title
            getSupportActionBar().setTitle(R.string.update_contact);

            // Save the ID of the currently-being-modified contact
            currentContactID = contact.getId();

            // Setup the inputs
            String imgPath = contact.getImgPath();
            if ("".equals(imgPath))
                contactImage.setImageResource(R.drawable.ic_action_user);
            else
                contactImage.setImageURI(Uri.fromFile(new File(imgPath)));
            contactName.setText(contact.getName());
            contactNumber.setText(contact.getPhoneNumber());
            contactEmail.setText(contact.getEmail());
            contactAddress.setText(contact.getAddress());

            // Setup the current contact's image path
            current_img_path = contact.getImgPath();
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
            } else {
                // Insert contact
                databaseAdapter.insertContact(newContact);

                // Contact successfully created
                setResult(ResultCodes.CONTACT_CREATED);
            }

            finish();
        }
    }

    /**
     * Invoked when the contact's image is clicked.
     *
     * @param view the button.
     */
    public void onImageClicked(View view) {
        CharSequence options[] = new CharSequence[]{"Gallery", "Camera"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from ...");
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
                    dialogTitle = "Invalid Email address.";
                    dialogMessage = "Please enter a valid email address.";

                    showDialog(dialogTitle, dialogMessage, "GOT IT", null);
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
            dialogTitle = "Invalid contact name.";
            dialogMessage = "Please enter the name of the new contact.";
        } else if (!nameValidStatus) {
            // Invalid contact name.
            dialogTitle = "Invalid contact name.";
            dialogMessage = "Please enter a valid contact name.";
        } else if (!phoneStatus) {
            // Invalid phone number.
            dialogTitle = "Invalid phone number.";
            dialogMessage = "Please enter the phone number of the new contact.";
            contactNumber.setError("10 digits");
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
