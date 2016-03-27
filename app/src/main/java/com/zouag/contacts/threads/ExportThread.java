package com.zouag.contacts.threads;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zouag.contacts.R;
import com.zouag.contacts.utils.Contacts;
import com.zouag.contacts.utils.Messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;

/**
 * Created by Mohammed Aouf ZOUAG on 27/03/2016.
 */
public class ExportThread extends Thread implements Handler.Callback {

    private Context mContext;
    private Handler mMainHandler;

    private Looper mWorkerLooper;
    private Handler mWorkerHandler;

    public ExportThread(Context context, Handler mainHandler) {
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
}
