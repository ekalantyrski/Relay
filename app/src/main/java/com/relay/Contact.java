package com.relay;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by erickalantyrski on 2017-06-14.
 */

public class Contact implements Serializable {
    private static final long serialVersionUID = 1L;
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private ArrayList<Message> messageList;

    public Contact(String firstName, String lastName, String phoneNumber, Message message)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        messageList = new ArrayList<Message>();
        messageList.add(message);
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public Message getFirstMessage()
    {
        return messageList.get(0);
    }




}
