package com.zouag.contacts.threads;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zouag.contacts.R;
import com.zouag.contacts.models.Contact;
import com.zouag.contacts.utils.Actions;
import com.zouag.contacts.utils.Contacts;
import com.zouag.contacts.utils.Messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;

/**
 * Created by Mohammed Aouf ZOUAG on 27/03/2016.
 * <p>
 * Performs IO actions. (contacts' import/export)
 */
public class IOThread extends Thread implements Handler.Callback {

    private Context mContext;
    private Handler mMainHandler;

    private Looper mWorkerLooper;
    private Handler mWorkerHandler;

    public IOThread(Context context, Handler mainHandler) {
        mContext = context;
        mMainHandler = mainHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        mWorkerLooper = Looper.myLooper();
        mWorkerHandler = new Handler(mWorkerLooper, this);
        Looper.loop();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Messages.MSG_START_EXPORTING:
                writeContactsToFile((List<VCard>) msg.obj);
                break;
            case Messages.MSG_START_IMPORTING:
                Actions action = (Actions) msg.obj;
                getContactsFromFile(action);
                break;
            case Messages.MSG_SHUTDOWN:
                mWorkerLooper.quit();
                break;
        }

        return true;
    }

    public Handler getWorkerHandler() {
        return mWorkerHandler;
    }

    /**
     * Writes a list of VCards to external storage.
     *
     * @param cards to be saved
     */
    private void writeContactsToFile(List<VCard> cards) {
        String appName = mContext.getString(R.string.app_name);
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                appName);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("CONTACTS", "Failed to create directory.");
            }
        }

        String path = Contacts.getVCFSavePath(mContext);
        File vcfFile = new File(path);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(vcfFile);
            OutputStreamWriter writer = new OutputStreamWriter(out);

            String vcardString = Ezvcard.write(cards).version(VCardVersion.V4_0).go();

            writer.write(vcardString);
            writer.close();
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

        mMainHandler.obtainMessage(Messages.MSG_EXPORT_ENDED)
                .sendToTarget();
    }

    /**
     * Fetches the contacts from the save' files.
     *
     * @param action to be taken. (Append/Overwrite)
     */
    public void getContactsFromFile(Actions action) {
        try {
            // Get the .vcf file
            File file = new File(Contacts.getVCFSavePath(mContext));
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fis);

            // Get the list of VCards stored inside the .vcf file
            List<VCard> vCards = Ezvcard.parse(reader).all();

            // Convert those cards to Contact objects & return them
            List<Contact> importedContacts = Contacts.parseVCards(vCards);

            mMainHandler.obtainMessage(Messages.MSG_IMPORT_COMPLETED,
                    action.getId(), 0, importedContacts)
                    .sendToTarget();
        } catch (IOException e) {
            mMainHandler.obtainMessage(Messages.MSG_IMPORT_FAILED)
                    .sendToTarget();
        }
    }
}
