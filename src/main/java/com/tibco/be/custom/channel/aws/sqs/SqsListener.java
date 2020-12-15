package com.tibco.be.custom.channel.aws.sqs;

import com.tibco.be.custom.channel.BaseEventSerializer;
import com.tibco.be.custom.channel.Event;
import com.tibco.be.custom.channel.EventProcessor;
import com.tibco.cep.kernel.service.logging.Level;
import com.tibco.cep.kernel.service.logging.Logger;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

public class SqsListener implements Runnable {

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final int maxNumberOfMessages;
    private final int pollingInterval;
    private final EventProcessor eventProcessor;
    private final BaseEventSerializer serializer;
    private final Logger logger;

    public SqsListener(final SqsClient sqsClient, final String queueUrl, final int maxNumberOfMessages, final int pollingInterval, final int threadNumber,
                       final EventProcessor eventProcessor, BaseEventSerializer serializer, Logger logger) {

        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.maxNumberOfMessages = maxNumberOfMessages;
        this.pollingInterval = pollingInterval;
        this.eventProcessor = eventProcessor;
        this.serializer = serializer;
        this.logger = logger;
    }


    @Override
    public void run() {

        logger.log(Level.DEBUG,"Listener thread running");


        while(true) {

            logger.log(Level.DEBUG,"Listener waiting for SQS message");

            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(1)
                    .waitTimeSeconds(pollingInterval)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

            Event event = null;
            for (Message message : messages) {

                try {
                    event = serializer.deserializeUserEvent(message,null);
                } catch(Exception e) {
                    logger.log(Level.ERROR,"SqsListener : Exception occurred while deserializing message : " +e );
                }

                if (event != null) {
                    try {
                        logger.log(Level.DEBUG,"Dispatching message to Event Processor");
                        eventProcessor.processEvent(event);
                        logger.log(Level.DEBUG,"Dispatch completed");
                    } catch(final Exception e) {
                        logger.log(Level.ERROR, e, "SqsListener : Exception occurred while processing event : "+ e);
                    }
                }

                logger.log(Level.DEBUG,"Deleting SQS message");

                DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build();

                try {
                    sqsClient.deleteMessage(deleteMessageRequest);
                } catch (Exception e) {
                    logger.log(Level.ERROR, e, "Unable to delete message from SQS");
                    e.printStackTrace();
                }

                logger.log(Level.DEBUG,"SQS message deleted");
            }
        }
    }
}
