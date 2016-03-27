package com.zouag.contacts.threads;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.zouag.contacts.R;
import com.zouag.contacts.utils.Messages;

/**
 * Created by Mohammed Aouf ZOUAG on 27/03/2016.
 *
 * The handler of the MainActivity.
 */
public class MainHandler implements Handler.Callback {

    private AppCompatActivity context;

    public MainHandler(Context context) {
        this.context = (AppCompatActivity) context;
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case Messages.MSG_EXPORT_ENDED:
                Snackbar.make(context.getWindow().getDecorView(),
                        R.string.contacts_export_success, Snackbar.LENGTH_LONG).show();
                break;
        }

        return true;
    }
}
