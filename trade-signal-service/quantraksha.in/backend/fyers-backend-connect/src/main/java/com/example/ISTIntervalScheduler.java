package com.example;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class ISTIntervalScheduler  {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ZoneId istZone = ZoneId.of("Asia/Kolkata");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Define the execution times
    private final LocalTime[] executionTimes = {
            LocalTime.of(9, 15),   // 9:15 AM
            LocalTime.of(10, 35),  // 10:35 AM
            LocalTime.of(11, 50),  // 11:50 AM
            LocalTime.of(13, 5),   // 1:05 PM (13:05)
            LocalTime.of(14, 20),  // 2:20 PM (14:20)
            LocalTime.of(15, 30)   // 3:30 PM (15:30)
    };

    public void startScheduler() {
        System.out.println("Starting IST Interval Scheduler...");
        System.out.println("Current IST Time: " + getCurrentISTTime());
        System.out.println("Scheduled execution times:");
        for (LocalTime time : executionTimes) {
            System.out.println("  " + time);
        }
        System.out.println("-----------------------------------");

        // Schedule tasks for today and tomorrow
        scheduleTasksForDate(LocalDate.now());
        scheduleTasksForDate(LocalDate.now().plusDays(1));

        // Schedule a daily task to set up next day's schedule
        scheduler.scheduleAtFixedRate(this::scheduleNextDay, 24, 24, TimeUnit.HOURS);
    }

    private void scheduleTasksForDate(LocalDate date) {
        ZonedDateTime now = ZonedDateTime.now(istZone);

        for (LocalTime executionTime : executionTimes) {
            ZonedDateTime scheduledTime = date.atTime(executionTime).atZone(istZone);

            // Only schedule if the time is in the future
            if (scheduledTime.isAfter(now)) {
                long delay = Duration.between(now, scheduledTime).toMillis();

                scheduler.schedule(() -> {
                    executeTask(scheduledTime);
                }, delay, TimeUnit.MILLISECONDS);

                System.out.println("Scheduled task for: " + scheduledTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        }
    }

    private void scheduleNextDay() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        scheduleTasksForDate(tomorrow);
        System.out.println("Scheduled tasks for tomorrow: " + tomorrow);
    }

    private void executeTask(ZonedDateTime scheduledTime) {
        String currentTime = getCurrentISTTime();
        System.out.println("=== TASK EXECUTION ===");
        System.out.println("Scheduled Time: " + scheduledTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Actual Execution Time: " + currentTime);

        // Your actual task logic goes here
        performYourTask();

        System.out.println("Task completed at: " + getCurrentISTTime());
        System.out.println("=====================");
    }

    private void performYourTask() {
        // Replace this method with your actual business logic
        System.out.println("Executing your scheduled task...");

        // Example: Simulate some work
        try {
            Thread.sleep(1000); // Simulate 1 second of work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Add your actual task implementation here
        // For example:
        // - Database operations
        // - API calls
        // - File processing
        // - Data analysis
        // - Email notifications
        // etc.
    }

    private String getCurrentISTTime() {
        return ZonedDateTime.now(istZone).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public void shutdown() {
        System.out.println("Shutting down scheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        ISTIntervalScheduler scheduler = new ISTIntervalScheduler();
        scheduler.startScheduler();

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));

        // Keep the main thread alive
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted. Shutting down...");
            scheduler.shutdown();
        }
    }
}