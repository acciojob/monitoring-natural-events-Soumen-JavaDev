
package com.driver;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventMonitoringServer {

    private static final int THREAD_POOL_SIZE = 5;
    private static final ExecutorService executorService =
            Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private static final CountDownLatch shutdownLatch =
            new CountDownLatch(1);

    private static final AtomicBoolean highMagnitudeEventDetected =
            new AtomicBoolean(false);

    public static void main(String[] args) {
        try {
            startServer();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    private static void startServer() throws InterruptedException {

        System.out.println("Event Monitoring Server Started");

        // Submit event processing tasks
        for (int i = 1; i <= 10; i++) {
            final int eventId = i;

            executorService.submit(() -> processEvent(eventId));
        }

        // Wait until a shutdown signal is received
        waitForShutdownSignal();
    }

    private static void processEvent(int eventId) {

        System.out.println("Processing Event: " + eventId);

        // Simulate event processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // Simulate detecting a high-magnitude event
        if (eventId >= 8) {
            highMagnitudeEventDetected.set(true);

            System.out.println(
                "High Magnitude Event Detected: " + eventId
            );

            // Signal server shutdown
            shutdownLatch.countDown();
        } else {
            System.out.println("Event Processed: " + eventId);
        }
    }

    private static void waitForShutdownSignal()
            throws InterruptedException {

        System.out.println("Waiting for shutdown signal...");

        // Block until the latch reaches zero
        shutdownLatch.await();

        System.out.println("Shutdown signal received!");
    }

    private static String getUserInput() {

        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private static void stopServer() {

        System.out.println("Stopping Event Monitoring Server...");

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Server Stopped.");

        if (highMagnitudeEventDetected.get()) {
            System.out.println("High magnitude event was detected.");
        }
    }
}
