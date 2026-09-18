package com.campusevent.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents the assignment of a quantity of a resource to an event.
 */
public class ResourceAllocation {
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String allocationId;
    private String eventId;
    private String resourceId;
    private int allocatedQuantity;
    private LocalDateTime allocatedAt;

    public ResourceAllocation() {
        this.allocatedAt = LocalDateTime.now();
    }

    public ResourceAllocation(String allocationId, String eventId, String resourceId, int allocatedQuantity, LocalDateTime allocatedAt) {
        this.allocationId = allocationId;
        this.eventId = eventId;
        this.resourceId = resourceId;
        this.allocatedQuantity = allocatedQuantity;
        this.allocatedAt = allocatedAt != null ? allocatedAt : LocalDateTime.now();
    }

    // Getters and Setters
    public String getAllocationId() { return allocationId; }
    public void setAllocationId(String allocationId) { this.allocationId = allocationId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public int getAllocatedQuantity() { return allocatedQuantity; }
    public void setAllocatedQuantity(int allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; }

    public LocalDateTime getAllocatedAt() { return allocatedAt; }
    public void setAllocatedAt(LocalDateTime allocatedAt) { this.allocatedAt = allocatedAt; }

    public String toCsvLine() {
        return String.join(",",
                escapeCsv(allocationId),
                escapeCsv(eventId),
                escapeCsv(resourceId),
                String.valueOf(allocatedQuantity),
                allocatedAt.format(DATETIME_FORMATTER)
        );
    }

    public static ResourceAllocation fromCsvLine(String csvLine) throws Exception {
        String[] tokens = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (tokens.length < 5) {
            throw new IllegalArgumentException("Insufficient columns in Allocation CSV record. Expected 5, got " + tokens.length);
        }
        String allocationId = unescapeCsv(tokens[0]);
        String eventId = unescapeCsv(tokens[1]);
        String resourceId = unescapeCsv(tokens[2]);
        int quantity = Integer.parseInt(tokens[3].trim());
        LocalDateTime allocatedAt = LocalDateTime.parse(tokens[4].trim(), DATETIME_FORMATTER);

        return new ResourceAllocation(allocationId, eventId, resourceId, quantity, allocatedAt);
    }

    private static String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    private static String unescapeCsv(String input) {
        if (input == null) return "";
        input = input.trim();
        if (input.startsWith("\"") && input.endsWith("\"") && input.length() >= 2) {
            input = input.substring(1, input.length() - 1).replace("\"\"", "\"");
        }
        return input;
    }

    @Override
    public String toString() {
        return String.format("AllocID: %s | EventID: %s | ResourceID: %s | Qty: %d | Time: %s",
                allocationId, eventId, resourceId, allocatedQuantity, allocatedAt.format(DATETIME_FORMATTER));
    }
}
