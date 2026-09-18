package com.campusevent.util;

import com.campusevent.model.EventStatus;
import com.campusevent.model.EventType;
import com.campusevent.model.ResourceStatus;
import com.campusevent.model.ResourceType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Robust console input reader utility to prevent application crashes on bad input.
 */
public class InputValidator {

    public static String readString(Scanner scanner, String prompt, String defaultValue) {
        if (defaultValue != null && !defaultValue.trim().isEmpty()) {
            System.out.printf("%s [%s]: ", prompt, defaultValue);
        } else {
            System.out.printf("%s: ", prompt);
        }
        String input = scanner.nextLine().trim();
        if (input.isEmpty() && defaultValue != null) {
            return defaultValue;
        }
        while (input.isEmpty()) {
            System.out.print("Input cannot be empty. Please enter again: ");
            input = scanner.nextLine().trim();
        }
        return input;
    }

    public static int readInt(Scanner scanner, String prompt, Integer defaultValue, int min, int max) {
        while (true) {
            if (defaultValue != null) {
                System.out.printf("%s [%d]: ", prompt, defaultValue);
            } else {
                System.out.printf("%s: ", prompt);
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty() && defaultValue != null) {
                return defaultValue;
            }
            try {
                int val = Integer.parseInt(line);
                if (val < min || val > max) {
                    System.out.printf("Value must be between %d and %d. Please retry.\n", min, max);
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please enter a valid integer.");
            }
        }
    }

    public static double readDouble(Scanner scanner, String prompt, Double defaultValue, double min) {
        while (true) {
            if (defaultValue != null) {
                System.out.printf("%s [%.2f]: ", prompt, defaultValue);
            } else {
                System.out.printf("%s: ", prompt);
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty() && defaultValue != null) {
                return defaultValue;
            }
            try {
                double val = Double.parseDouble(line);
                if (val < min) {
                    System.out.printf("Value cannot be less than %.2f. Please retry.\n", min);
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please enter a valid decimal number.");
            }
        }
    }

    public static LocalDate readLocalDate(Scanner scanner, String prompt, LocalDate defaultValue) {
        while (true) {
            if (defaultValue != null) {
                System.out.printf("%s (YYYY-MM-DD) [%s]: ", prompt, defaultValue.toString());
            } else {
                System.out.printf("%s (YYYY-MM-DD): ", prompt);
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty() && defaultValue != null) {
                return defaultValue;
            }
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use format YYYY-MM-DD (e.g., 2026-10-15).");
            }
        }
    }

    public static LocalTime readLocalTime(Scanner scanner, String prompt, LocalTime defaultValue) {
        while (true) {
            if (defaultValue != null) {
                System.out.printf("%s (HH:mm) [%s]: ", prompt, defaultValue.toString());
            } else {
                System.out.printf("%s (HH:mm): ", prompt);
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty() && defaultValue != null) {
                return defaultValue;
            }
            try {
                return LocalTime.parse(line);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time format. Use format HH:mm (e.g., 09:30 or 14:00).");
            }
        }
    }

    public static EventType readEventType(Scanner scanner, String prompt, EventType defaultValue) {
        System.out.println(prompt);
        EventType[] types = EventType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("  %d. %s\n", (i + 1), types[i]);
        }
        int choice = readInt(scanner, "Select Event Type number", defaultValue != null ? defaultValue.ordinal() + 1 : 1, 1, types.length);
        return types[choice - 1];
    }

    public static ResourceType readResourceType(Scanner scanner, String prompt, ResourceType defaultValue) {
        System.out.println(prompt);
        ResourceType[] types = ResourceType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("  %d. %s\n", (i + 1), types[i]);
        }
        int choice = readInt(scanner, "Select Resource Type number", defaultValue != null ? defaultValue.ordinal() + 1 : 1, 1, types.length);
        return types[choice - 1];
    }

    public static EventStatus readEventStatus(Scanner scanner, String prompt, EventStatus defaultValue) {
        System.out.println(prompt);
        EventStatus[] statuses = EventStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.printf("  %d. %s\n", (i + 1), statuses[i]);
        }
        int choice = readInt(scanner, "Select Status number", defaultValue != null ? defaultValue.ordinal() + 1 : 1, 1, statuses.length);
        return statuses[choice - 1];
    }

    public static ResourceStatus readResourceStatus(Scanner scanner, String prompt, ResourceStatus defaultValue) {
        System.out.println(prompt);
        ResourceStatus[] statuses = ResourceStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.printf("  %d. %s\n", (i + 1), statuses[i]);
        }
        int choice = readInt(scanner, "Select Status number", defaultValue != null ? defaultValue.ordinal() + 1 : 1, 1, statuses.length);
        return statuses[choice - 1];
    }
}
