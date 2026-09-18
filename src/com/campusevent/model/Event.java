package com.campusevent.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a campus event.
 */
public class Event {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private String id;
    private String name;
    private EventType type;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String venueId;
    private String organizer;
    private int expectedAttendees;
    private double budget;
    private EventStatus status;

    public Event() {
        this.status = EventStatus.PLANNED;
    }

    public Event(String id, String name, EventType type, LocalDate date, LocalTime startTime, LocalTime endTime,
                 String venueId, String organizer, int expectedAttendees, double budget, EventStatus status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venueId = venueId;
        this.organizer = organizer;
        this.expectedAttendees = expectedAttendees;
        this.budget = budget;
        this.status = status != null ? status : EventStatus.PLANNED;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getVenueId() { return venueId; }
    public void setVenueId(String venueId) { this.venueId = venueId; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public int getExpectedAttendees() { return expectedAttendees; }
    public void setExpectedAttendees(int expectedAttendees) { this.expectedAttendees = expectedAttendees; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    /**
     * Converts Event object to CSV string line.
     */
    public String toCsvLine() {
        return String.join(",",
                escapeCsv(id),
                escapeCsv(name),
                type.name(),
                date.format(DATE_FORMATTER),
                startTime.format(TIME_FORMATTER),
                endTime.format(TIME_FORMATTER),
                escapeCsv(venueId),
                escapeCsv(organizer),
                String.valueOf(expectedAttendees),
                String.valueOf(budget),
                status.name()
        );
    }

    /**
     * Parses an Event object from a CSV string line.
     */
    public static Event fromCsvLine(String csvLine) throws Exception {
        String[] tokens = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (tokens.length < 11) {
            throw new IllegalArgumentException("Insufficient columns in Event CSV record. Expected 11, got " + tokens.length);
        }
        String id = unescapeCsv(tokens[0]);
        String name = unescapeCsv(tokens[1]);
        EventType type = EventType.fromString(tokens[2]);
        LocalDate date = LocalDate.parse(tokens[3].trim(), DATE_FORMATTER);
        LocalTime startTime = LocalTime.parse(tokens[4].trim(), TIME_FORMATTER);
        LocalTime endTime = LocalTime.parse(tokens[5].trim(), TIME_FORMATTER);
        String venueId = unescapeCsv(tokens[6]);
        String organizer = unescapeCsv(tokens[7]);
        int expectedAttendees = Integer.parseInt(tokens[8].trim());
        double budget = Double.parseDouble(tokens[9].trim());
        EventStatus status = EventStatus.fromString(tokens[10]);

        return new Event(id, name, type, date, startTime, endTime, venueId, organizer, expectedAttendees, budget, status);
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
        return String.format("[%s] %s (%s) on %s %s-%s at Venue: %s [Organizer: %s, Attendees: %d, Status: %s]",
                id, name, type, date, startTime, endTime, venueId, organizer, expectedAttendees, status);
    }
}
