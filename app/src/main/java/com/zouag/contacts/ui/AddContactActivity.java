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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.zouag.contacts.R;
import com.zouag.contacts.adapters.DatabaseAdapter;
import com.zouag.contacts.models.Contact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddContactActivity extends AppCompatActivity {

    private static final int REQUEST_OPEN_GALLERY = 10;
    private static final int REQUEST_OPEN_CAMERA = 11;

    private String current_img_path = "";

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

        databaseAdapter = new DatabaseAdapter(this).open();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OPEN_GALLERY:
                switch (resultCode) {
                    case RESULT_OK:
                        Uri imageData = data.getData();
                        Log.i("CONTACTS", getRealPathFromURI(imageData));
                        contactImage.setImageURI(imageData);
                        break;
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
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(now);
        String path = mediaStorageDir.getPath() + File.separator;

        // Save the path of the image
        current_img_path = path + "IMG_" + timestamp + ".jpg";
        Log.i("CONTACTS", "Camera path: " + current_img_path);
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
        CharSequence options[] = new CharSequence[]{"Gallery", "Camera"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from ...");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    Intent gallery = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
                    .imgPath(current_img_path)
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
