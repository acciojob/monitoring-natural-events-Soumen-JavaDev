
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
        } finally {
            stopServer();
        }
    }

    private static void startServer() throws InterruptedException {

        System.out.println("Event monitoring server started. Enter 'shutdown' to stop the server manually.");

        // Submit 10 event-processing tasks
        for (int i = 1; i <= 10; i++) {
            final int eventId = i;

            executorService.submit(() -> processEvent(eventId));
        }

        // Listen for manual shutdown in a separate thread
        Thread inputThread = new Thread(() -> {
            String input = getUserInput();

            if ("shutdown".equalsIgnoreCase(input.trim())) {
                shutdownLatch.countDown();
            }
        });

        inputThread.setDaemon(true);
        inputThread.start();

        // Wait for automatic or manual shutdown
        waitForShutdownSignal();
    }

    private static void processEvent(int eventId) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // Example high-magnitude event condition
        if (eventId == 9) {
            if (highMagnitudeEventDetected.compareAndSet(false, true)) {
                System.out.println("High magnitude event detected!");
                shutdownLatch.countDown();
            }
        }

        System.out.println("Event " + eventId + " processed.");
    }

    private static void waitForShutdownSignal() throws InterruptedException {
        shutdownLatch.await();
    }

    private static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private static void stopServer() {

        System.out.println("Shutting down the server gracefully...");

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
