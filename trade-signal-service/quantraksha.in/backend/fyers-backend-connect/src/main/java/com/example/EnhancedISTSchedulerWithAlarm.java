package com.example;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class EnhancedISTSchedulerWithAlarm {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ZoneId istZone = ZoneId.of("Asia/Kolkata");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Clip currentAlarmClip;
    private boolean alarmEnabled = true;

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
        System.out.println("🚨 Starting Enhanced IST Interval Scheduler with Alarm Voice...");
        System.out.println("Current IST Time: " + getCurrentISTTime());
        System.out.println("Alarm Status: " + (alarmEnabled ? "ENABLED" : "DISABLED"));
        System.out.println("Scheduled execution times:");
        for (LocalTime time : executionTimes) {
            System.out.println("  📅 " + time);
        }
        System.out.println("-----------------------------------");

        // Test alarm system
        testAlarmSystem();

        // Schedule tasks for today and tomorrow
        scheduleTasksForDate(LocalDate.now());
        scheduleTasksForDate(LocalDate.now().plusDays(1));

        // Schedule a daily task to set up next day's schedule
        scheduler.scheduleAtFixedRate(this::scheduleNextDay, 24, 24, TimeUnit.HOURS);
    }

    private void testAlarmSystem() {
        System.out.println("🔊 Testing alarm system in 3 seconds...");
        scheduler.schedule(() -> {
            playAlarmNotification("System test successful!");
        }, 3, TimeUnit.SECONDS);
    }

    private void scheduleTasksForDate(LocalDate date) {
        ZonedDateTime now = ZonedDateTime.now(istZone);

        for (LocalTime executionTime : executionTimes) {
            ZonedDateTime scheduledTime = date.atTime(executionTime).atZone(istZone);

            // Only schedule if the time is in the future
            if (scheduledTime.isAfter(now)) {
                long delay = Duration.between(now, scheduledTime).toMillis();

                scheduler.schedule(() -> {
                    executeTaskWithAlarm(scheduledTime);
                }, delay, TimeUnit.MILLISECONDS);

                System.out.println("⏰ Scheduled task for: " + scheduledTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        }
    }

    private void scheduleNextDay() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        scheduleTasksForDate(tomorrow);
        System.out.println("📅 Scheduled tasks for tomorrow: " + tomorrow);
    }

    private void executeTaskWithAlarm(ZonedDateTime scheduledTime) {
        String currentTime = getCurrentISTTime();
        System.out.println("🚨 === ALARM TASK EXECUTION ===");
        System.out.println("⏰ Scheduled Time: " + scheduledTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("🕐 Actual Execution Time: " + currentTime);

        // Play alarm notification
        if (alarmEnabled) {
            String message = "Task scheduled for " + scheduledTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " is starting now!";
            playAlarmNotification(message);
        }

        // Your actual task logic goes here
        performYourTask();

        System.out.println("✅ Task completed at: " + getCurrentISTTime());
        System.out.println("===============================");
    }

    private void playAlarmNotification(String message) {
        if (!alarmEnabled) return;

        System.out.println("🔊 Playing alarm notification: " + message);

        // Multiple notification methods for reliability
        CompletableFuture.runAsync(() -> {
            // 1. System beep pattern
            playAlarmBeepPattern();

            // 2. Generated alarm tone
            generateAlarmTone();

            // 3. Text-to-speech (if available)
            speakAlarmMessage(message);
        });
    }

    private void playAlarmBeepPattern() {
        try {
            // Create an attention-grabbing beep pattern
            for (int i = 0; i < 3; i++) {
                // Triple beep with increasing intervals
                for (int j = 0; j < 3; j++) {
                    Toolkit.getDefaultToolkit().beep();
                    Thread.sleep(200);
                }
                Thread.sleep(500); // Pause between sets
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void generateAlarmTone() {
        try {
            // Audio format: 44.1kHz, 16-bit, mono
            AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();

            // Generate urgent alarm pattern
            for (int i = 0; i < 5; i++) {
                // High urgency tone (1000 Hz)
                generateTone(line, 1000, 300);
                // Medium tone (600 Hz)
                generateTone(line, 600, 200);
                // High tone again
                generateTone(line, 1200, 300);
                // Brief pause
                Thread.sleep(100);
            }

            line.drain();
            line.close();

        } catch (LineUnavailableException | InterruptedException e) {
            System.err.println("Error generating alarm tone: " + e.getMessage());
        }
    }

    private void generateTone(SourceDataLine line, double frequency, int duration) {
        int sampleRate = 44100;
        int samples = (int) ((duration / 1000.0) * sampleRate);
        byte[] buffer = new byte[samples * 2];

        for (int i = 0; i < samples; i++) {
            double angle = 2.0 * Math.PI * i * frequency / sampleRate;
            // Add some amplitude modulation for more attention-grabbing sound
            double amplitude = 0.7 * (1 + 0.3 * Math.sin(2.0 * Math.PI * i * 5 / sampleRate));
            short sample = (short) (Short.MAX_VALUE * Math.sin(angle) * amplitude);

            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        line.write(buffer, 0, buffer.length);
    }

    private void speakAlarmMessage(String message) {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("windows")) {
                // Windows TTS
                String command = "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                        "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$synth.Rate = 2; $synth.Volume = 100; " +
                        "$synth.Speak('" + message.replace("'", "''") + "')\"";
                Runtime.getRuntime().exec(command);

            } else if (os.contains("mac")) {
                // macOS TTS
                String[] command = {"say", "-r", "200", message};
                Runtime.getRuntime().exec(command);

            } else if (os.contains("linux")) {
                // Linux TTS (requires espeak)
                String[] command = {"espeak", "-s", "150", "-a", "200", message};
                Runtime.getRuntime().exec(command);
            }

        } catch (IOException e) {
            System.err.println("TTS not available: " + e.getMessage());
        }
    }

    // Play custom WAV alarm file if available
    public void playCustomAlarmFile(String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.out.println("Custom alarm file not found: " + filePath);
                return;
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            currentAlarmClip = AudioSystem.getClip();
            currentAlarmClip.open(audioInputStream);

            // Play 3 times for urgency
            for (int i = 0; i < 3; i++) {
                currentAlarmClip.setFramePosition(0);
                currentAlarmClip.start();

                // Wait for clip to finish
                while (currentAlarmClip.isRunning()) {
                    Thread.sleep(100);
                }

                Thread.sleep(500); // Brief pause between repeats
            }

            currentAlarmClip.close();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
            System.err.println("Error playing custom alarm: " + e.getMessage());
        }
    }

    private void performYourTask() {
        System.out.println("🔄 Executing your scheduled task...");

        // Simulate some work
        try {
            Thread.sleep(2000); // Simulate 2 seconds of work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Add your actual task implementation here
        System.out.println("✅ Task logic completed successfully");

        // Optional: Play completion sound
        if (alarmEnabled) {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000);
                    Toolkit.getDefaultToolkit().beep();
                    Thread.sleep(100);
                    Toolkit.getDefaultToolkit().beep();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public void setAlarmEnabled(boolean enabled) {
        this.alarmEnabled = enabled;
        System.out.println("🔊 Alarm " + (enabled ? "ENABLED" : "DISABLED"));
    }

    public void stopCurrentAlarm() {
        if (currentAlarmClip != null && currentAlarmClip.isRunning()) {
            currentAlarmClip.stop();
            currentAlarmClip.close();
            System.out.println("🔇 Current alarm stopped");
        }
    }

    private String getCurrentISTTime() {
        return ZonedDateTime.now(istZone).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public void shutdown() {
        System.out.println("⏹️ Shutting down enhanced scheduler...");
        stopCurrentAlarm();
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
        EnhancedISTSchedulerWithAlarm scheduler = new EnhancedISTSchedulerWithAlarm();
        scheduler.startScheduler();

        // Example: Set custom alarm file (uncomment if you have a WAV file)
        // scheduler.playCustomAlarmFile("path/to/your/alarm.wav");

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));

        // Console commands for runtime control
        System.out.println("\n📝 Runtime Commands:");
        System.out.println("  Type 'disable' to disable alarms");
        System.out.println("  Type 'enable' to enable alarms");
        System.out.println("  Type 'test' to test alarm");
        System.out.println("  Type 'stop' to stop current alarm");
        System.out.println("  Type 'quit' to exit");

        // Simple command line interface
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            while (true) {
                String command = scanner.nextLine().trim().toLowerCase();

                switch (command) {
                    case "disable":
                        scheduler.setAlarmEnabled(false);
                        break;
                    case "enable":
                        scheduler.setAlarmEnabled(true);
                        break;
                    case "test":
                        scheduler.playAlarmNotification("This is a test alarm!");
                        break;
                    case "stop":
                        scheduler.stopCurrentAlarm();
                        break;
                    case "quit":
                        scheduler.shutdown();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Unknown command: " + command);
                }
            }
        }
    }
}