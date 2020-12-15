package com.tibco.be.custom.channel.aws.sqs;


import com.tibco.be.custom.channel.BaseChannel;
import com.tibco.be.custom.channel.BaseDestination;
import com.tibco.be.custom.channel.BaseDriver;

public class SqsDriver extends BaseDriver {
    public BaseChannel getChannel() {
        return new SqsChannel();
    }

    public BaseDestination getDestination() {
        return new SqsDestination();
    }
}
