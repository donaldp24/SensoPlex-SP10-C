package com.sensorplex.sdk;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is a simple blocking queue of bytes implemented as a blocking queue of byte buffers using standard Java Concurrency classes
 */
public class ByteQueue {
    private BlockingQueue<byte[]> pendingData = new LinkedBlockingQueue<byte[]>();
    private byte[] currentData = null;
    private int currentIndex = 0;

    public ByteQueue() {}

    public byte next() throws InterruptedException {
        fetchCurrentData();

        return currentData[currentIndex++];
    }

    private void fetchCurrentData() throws InterruptedException {
        if (null == currentData || currentIndex >= currentData.length) {
            currentIndex = 0;
            currentData = pendingData.take();
            fetchCurrentData(); // recursive call to make sure we got a non-emply buffer or something bad did not happen
        }
    }

    public void put(byte[] buffer) throws InterruptedException {
        pendingData.put(buffer);
    }
}
