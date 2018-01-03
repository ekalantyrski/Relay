package com.relay;

import android.telephony.SmsManager;

/**
 * Created by erickalantyrski on 2017-06-16.
 */

public class SmsSender {
    private SmsManager smsManager;

    public SmsSender(SmsManager smsManager)
    {
        this.smsManager = smsManager;
    }

    //This method sends an SMS
    //Param is the contact to sendInfo to, and the message aswell
    public void sendSms(SmsMessage message)
    {
        String phoneNumber = message.getPhoneNumber();
        String messageToSend = message.getMessage();
        smsManager.sendTextMessage(phoneNumber, null, messageToSend, null, null);
        System.out.println("Sent message");
    }



}
