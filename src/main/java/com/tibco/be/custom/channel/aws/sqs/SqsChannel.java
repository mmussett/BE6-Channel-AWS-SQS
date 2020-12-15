package com.tibco.be.custom.channel.aws.sqs;

import com.tibco.be.custom.channel.BaseChannel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SqsChannel extends BaseChannel {

    private ExecutorService executor;


    /**
     * We have chosen to override init() It will initialize a common threadpool for all SQS Destinations to use, to start their consumers
     */
    @Override
    public void init() throws Exception {
        executor = Executors.newCachedThreadPool();

        ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
        pool.setCorePoolSize(2);

        //Call super.init here, it in turn invokes each of the destinations init methods. This gives a chance to each of the destinations to initialize itself.
        super.init();
    }

    /**
     * We have chosen to override close() It will call the super close
     */
    @Override
    public void close() throws Exception {

        //this invokes destination.close on each destination.
        super.close();

        //close the executor service

        try {
            executor.shutdownNow();
        } catch (Exception e) {
            //ignore this exception.
        }

    }

    /**
     * Gets the executor Service
     *
     * @return the executorService
     */

    public ExecutorService getJobPool() {
        return executor;
    }



}
