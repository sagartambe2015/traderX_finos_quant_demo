package com.example;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.time.format.DateTimeFormatter;

public class NextWednesdayCalculator {

    public static void main(String[] args) {
        // Method 1: Using TemporalAdjusters.next()
        NextWednesdayCalculator nextWednesdayCalculator = new NextWednesdayCalculator();
        System.out.println("Test:"+nextWednesdayCalculator.getNextWednesDay());
    }

    /**
     * Convert date string to epoch seconds (UTC)
     * @param dateString Date in format "YYYY-MM-DD HH:MM:SS"
     * @return Epoch seconds since 1970-01-01 00:00:00 UTC
     */
    public static long dateStringToEpochSeconds(String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
            return dateTime.toEpochSecond(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected: YYYY-MM-DD HH:MM:SS", e);
        }
    }

    public String getNextWednesDay(){
        LocalDate nextWednesday1 = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));

        // Method 2: Using TemporalAdjusters.nextOrSame()
        LocalDate nextOrCurrentWednesday = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));

        // Method 3: Manual calculation
        LocalDate nextWednesday2 = getNextWednesday();

        // Method 4: Custom method that handles edge cases
        LocalDate nextWednesday3 = findNextWednesday(LocalDate.now());

        // Display results in YYYY-MM-DD format
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
        DateTimeFormatter readableFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");

//        System.out.println("=== YYYY-MM-DD Format ===");
//        System.out.println("Today: " + LocalDate.now().toString()); // toString() gives YYYY-MM-DD
//        System.out.println("Next Wednesday (Method 1): " + nextWednesday1.format(isoFormatter));
//        System.out.println("Next/Current Wednesday: " + nextOrCurrentWednesday.format(isoFormatter));
//        System.out.println("Next Wednesday (Method 3): " + nextWednesday2.format(isoFormatter));
//        System.out.println("Next Wednesday (Method 4): " + nextWednesday3.format(isoFormatter));
//
//        System.out.println("\n=== Readable Format ===");
//        System.out.println("Today: " + LocalDate.now().format(readableFormatter));
//        System.out.println("Next Wednesday: " + nextWednesday1.format(readableFormatter));
        return nextWednesday3.format(isoFormatter);
    }

    // Manual calculation method
    public static LocalDate getNextWednesday() {
        LocalDate today = LocalDate.now();
        int daysUntilWednesday = (DayOfWeek.WEDNESDAY.getValue() - today.getDayOfWeek().getValue() + 7) % 7;

        // If today is Wednesday, get next Wednesday (7 days from now)
        if (daysUntilWednesday == 0) {
            daysUntilWednesday = 7;
        }

        return today.plusDays(daysUntilWednesday);
    }

    // Custom method with clear logic
    public static LocalDate findNextWednesday(LocalDate fromDate) {
        LocalDate date = fromDate.plusDays(1); // Start from tomorrow

        while (date.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
            date = date.plusDays(1);
        }

        return date;
    }

    // Utility method to get next occurrence of any day in YYYY-MM-DD format
    public static String getNextDayOfWeekFormatted(DayOfWeek targetDay) {
        LocalDate today = LocalDate.now();
        LocalDate nextDay = today.with(TemporalAdjusters.next(targetDay));
        return nextDay.toString(); // Returns YYYY-MM-DD format
    }

    // Get next Wednesday specifically in YYYY-MM-DD format
    public static String getNextWednesdayFormatted() {
        return getNextDayOfWeekFormatted(DayOfWeek.WEDNESDAY);
    }
}