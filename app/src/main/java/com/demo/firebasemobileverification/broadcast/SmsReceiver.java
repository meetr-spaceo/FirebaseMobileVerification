package com.demo.firebasemobileverification.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.demo.firebasemobileverification.activity.SmsListener;

/**
 * Created by sotsys-112 on 25/9/17.
 */

public class SmsReceiver extends BroadcastReceiver {

    private static SmsListener smsListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();

        Object[] pdus = (Object[]) data.get("pdus");

        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

            String sender = smsMessage.getDisplayOriginatingAddress();
            //You must check here if the sender is your provider and not another one with same text.

            String messageBody = smsMessage.getMessageBody();

            //Pass on the text to our listener.
            smsListener.messageReceive(messageBody);
        }

    }

    public static void bindListener(SmsListener listener) {
        smsListener = listener;
    }
}

