package com.example;

import java.io.File;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class FileUtils {

    public static boolean isFilePresent(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean isFileOlderThan24Hours(String filePath) {
        File file = new File(filePath);

        // Check if file exists
        if (!file.exists()) {
            System.out.println("File does not exist: " + filePath);
            return false;
        }

        // Get file's last modified time
        long lastModified = file.lastModified();
        Instant fileInstant = Instant.ofEpochMilli(lastModified);
        LocalDateTime fileDateTime = LocalDateTime.ofInstant(fileInstant, ZoneId.systemDefault());

        // Get current time
        LocalDateTime now = LocalDateTime.now();

        // Calculate hours difference
        long hoursDifference = ChronoUnit.HOURS.between(fileDateTime, now);

        System.out.println("File: " + filePath);
        System.out.println("Last modified: " + fileDateTime);
        System.out.println("Current time: " + now);
        System.out.println("Hours difference: " + hoursDifference);

        return hoursDifference > 24;
    }

    public  static void saveString(String filename, String content) throws IOException {
        Files.write(Paths.get(filename), content.getBytes(StandardCharsets.UTF_8));
    }

    public static String readFileAsString(String filePath) {
        try {
            Path path = Paths.get(filePath);
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
    }
}
