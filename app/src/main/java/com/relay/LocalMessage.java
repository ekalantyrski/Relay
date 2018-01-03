package com.relay;

import java.io.Serializable;

/**
 * Created by erickalantyrski on 2017-06-14.
 */

public abstract class LocalMessage implements Serializable {

    private static final long serialVersionUID = 2L;

    private MessageType messageType;

    protected LocalMessage(MessageType type)
    {
        this.messageType = type;

    }

    public MessageType getMessageType()
    {
        return messageType;
    }


}
