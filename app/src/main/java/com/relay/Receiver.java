package com.relay;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by erickalantyrski on 2017-05-28.
 */

/*
This receives sms sent by others
 */

public class Receiver extends BroadcastReceiver {

    Queue<SmsMessage> smsMessages;
    Network network;


    public Receiver(Network network)
    {
        smsMessages = new LinkedList<>();
        this.network = network;
    }


    @Override
    //Listens for the actual SMS
    //Param are the context of the current app, and intent of sms delivery
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            Object[] pdus = (Object[])bundle.get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
//            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
//            SmsMessage smsMessage = msgs[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String format = bundle.getString("format");
                messages[0] = SmsMessage.createFromPdu((byte[]) pdus[0], format);
            }
            else {
                messages[0] = SmsMessage.createFromPdu((byte[]) pdus[0]);
            }
            SmsMessage smsMessage = messages[0];
            String contactName = getContactName(context, smsMessage.getOriginatingAddress());
            String[] name;
            // check if there is a name associated with contact
            if (contactName != null) {
                name = contactName.split(" ");
            } else {
                //if not, set first name to the phonenumber
                name = new String[2];
                name[0] = smsMessage.getOriginatingAddress();
                name[1] = "";
            }

            if (network != null && network.isConnected()) // if a text is received before a connection is established
            {
                Contact contact = new Contact(name[0], name[name.length-1], smsMessage.getOriginatingAddress(), new Message(smsMessage.getMessageBody(), false));
                com.relay.SmsMessage message = new com.relay.SmsMessage(contact);
                network.sendLocalMessage(message);
            }
            //TODO Handle texts that are received while searching for a connection
        }


    }
    //This method gets the contact name of someone based on their telephone
    //Param is the context of APP, and the phonenumber to look up with
    //Returns the name of the person with that phone number
    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

}
