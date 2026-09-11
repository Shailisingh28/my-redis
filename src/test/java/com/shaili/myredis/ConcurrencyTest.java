package com.shaili.myredis;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrencyTest {

    @Test
    void concurrentPutsCanLoseData() throws InterruptedException {
        MyHashMap map = new MyHashMap();
        int numberOfThreads = 100;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                map.put("key" + index, "value" + index);
                latch.countDown();
            });
        }

        latch.await(); // sabhi threads ke khatam hone ka wait karo
        executor.shutdown();

        System.out.println("Expected size: " + numberOfThreads);
        System.out.println("Actual size: " + map.size());

        // Yeh assertion FAIL ho sakta hai, jo hi hume prove karna hai!
        assertEquals(numberOfThreads, map.size());
    }
}