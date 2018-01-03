package com.relay;

import java.io.Serializable;

/**
 * Created by erickalantyrski on 2017-10-12.
 */

public class SmsMessage extends LocalMessage implements Serializable{

    private static final long serialVersionUID = 3L;
    private Contact contact;

    public SmsMessage(Contact contact)
    {
        super(MessageType.SMS);
        this.contact = contact;
    }

    /**
     * Get the message
     */
    public String getMessage()
    {
        return contact.getFirstMessage().getMessage();
    }

    public String getPhoneNumber()
    {
        return contact.getPhoneNumber();
    }

    public Contact getContact()
    {
        return contact;
    }
}

