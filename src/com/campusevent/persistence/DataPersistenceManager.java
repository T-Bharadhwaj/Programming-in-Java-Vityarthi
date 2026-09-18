package com.campusevent.persistence;

import com.campusevent.model.Event;
import com.campusevent.model.EventStatus;
import com.campusevent.model.EventType;
import com.campusevent.model.Resource;
import com.campusevent.model.ResourceAllocation;
import com.campusevent.model.ResourceStatus;
import com.campusevent.model.ResourceType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages loading and saving data to CSV files with error resilience and sample data seeding.
 */
public class DataPersistenceManager {

    private final String dataDirPath;
    private final File eventsFile;
    private final File resourcesFile;
    private final File allocationsFile;

    public DataPersistenceManager() {
        this("data");
    }

    public DataPersistenceManager(String dataDirPath) {
        this.dataDirPath = dataDirPath;
        File dir = new File(dataDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.eventsFile = new File(dir, "events.csv");
        this.resourcesFile = new File(dir, "resources.csv");
        this.allocationsFile = new File(dir, "allocations.csv");
    }

    // --- EVENTS PERSISTENCE ---

    public List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        if (!eventsFile.exists() || eventsFile.length() == 0) {
            System.out.println("[DataPersistenceManager] events.csv not found or empty. Seeding sample events.");
            events = createSampleEvents();
            saveEvents(events);
            return events;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(eventsFile), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                if (isHeader) {
                    isHeader = false;
                    if (line.toLowerCase().contains("id")) continue; // Skip header line
                }
                try {
                    Event event = Event.fromCsvLine(line);
                    events.add(event);
                } catch (Exception e) {
                    System.err.printf("[DataPersistenceManager Warning] Skipping malformed line %d in events.csv: %s (Error: %s)\n",
                            lineNumber, line, e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[DataPersistenceManager Error] Failed reading events.csv: " + e.getMessage());
        }

        return events;
    }

    public void saveEvents(List<Event> events) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(eventsFile), StandardCharsets.UTF_8))) {
            writer.println("id,name,type,date,startTime,endTime,venueId,organizer,expectedAttendees,budget,status");
            for (Event e : events) {
                writer.println(e.toCsvLine());
            }
        } catch (IOException e) {
            System.err.println("[DataPersistenceManager Error] Failed saving events.csv: " + e.getMessage());
        }
    }

    // --- RESOURCES PERSISTENCE ---

    public List<Resource> loadResources() {
        List<Resource> resources = new ArrayList<>();
        if (!resourcesFile.exists() || resourcesFile.length() == 0) {
            System.out.println("[DataPersistenceManager] resources.csv not found or empty. Seeding sample resources.");
            resources = createSampleResources();
            saveResources(resources);
            return resources;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourcesFile), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                if (isHeader) {
                    isHeader = false;
                    if (line.toLowerCase().contains("id")) continue;
                }
                try {
                    Resource resource = Resource.fromCsvLine(line);
                    resources.add(resource);
                } catch (Exception e) {
                    System.err.printf("[DataPersistenceManager Warning] Skipping malformed line %d in resources.csv: %s (Error: %s)\n",
                            lineNumber, line, e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[DataPersistenceManager Error] Failed reading resources.csv: " + e.getMessage());
        }

        return resources;
    }

    public void saveResources(List<Resource> resources) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resourcesFile), StandardCharsets.UTF_8))) {
            writer.println("id,name,type,totalQuantity,status,costPerUnit,location");
            for (Resource r : resources) {
                writer.println(r.toCsvLine());
            }
        } catch (IOException e) {
            System.err.println("[DataPersistenceManager Error] Failed saving resources.csv: " + e.getMessage());
        }
    }

    // --- ALLOCATIONS PERSISTENCE ---

    public List<ResourceAllocation> loadAllocations() {
        List<ResourceAllocation> allocations = new ArrayList<>();
        if (!allocationsFile.exists() || allocationsFile.length() == 0) {
            System.out.println("[DataPersistenceManager] allocations.csv not found or empty. Seeding sample allocations.");
            allocations = createSampleAllocations();
            saveAllocations(allocations);
            return allocations;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(allocationsFile), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                if (isHeader) {
                    isHeader = false;
                    if (line.toLowerCase().contains("allocationid")) continue;
                }
                try {
                    ResourceAllocation alloc = ResourceAllocation.fromCsvLine(line);
                    allocations.add(alloc);
                } catch (Exception e) {
                    System.err.printf("[DataPersistenceManager Warning] Skipping malformed line %d in allocations.csv: %s (Error: %s)\n",
                            lineNumber, line, e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[DataPersistenceManager Error] Failed reading allocations.csv: " + e.getMessage());
        }

        return allocations;
    }

    public void saveAllocations(List<ResourceAllocation> allocations) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(allocationsFile), StandardCharsets.UTF_8))) {
            writer.println("allocationId,eventId,resourceId,allocatedQuantity,allocatedAt");
            for (ResourceAllocation a : allocations) {
                writer.println(a.toCsvLine());
            }
        } catch (IOException e) {
            System.err.println("[DataPersistenceManager Error] Failed saving allocations.csv: " + e.getMessage());
        }
    }

    // --- SAMPLE DATA INITIALIZATION ---

    public void restoreSampleData() {
        List<Event> sampleEvents = createSampleEvents();
        List<Resource> sampleResources = createSampleResources();
        List<ResourceAllocation> sampleAllocations = createSampleAllocations();

        saveEvents(sampleEvents);
        saveResources(sampleResources);
        saveAllocations(sampleAllocations);

        System.out.println("[DataPersistenceManager] Sample data restored successfully.");
    }

    private List<Event> createSampleEvents() {
        List<Event> list = new ArrayList<>();
        LocalDate baseDate = LocalDate.now().plusDays(2);
        list.add(new Event("E101", "AI & ML Workshop", EventType.WORKSHOP, baseDate, LocalTime.of(9, 0), LocalTime.of(12, 0), "R101", "CS Department", 60, 500.0, EventStatus.PLANNED));
        list.add(new Event("E102", "Annual Hackathon 2026", EventType.HACKATHON, baseDate, LocalTime.of(13, 0), LocalTime.of(18, 0), "R101", "Coding Club", 120, 1500.0, EventStatus.PLANNED));
        list.add(new Event("E103", "Cultural Night Fest", EventType.CULTURAL, baseDate.plusDays(1), LocalTime.of(18, 0), LocalTime.of(22, 0), "R102", "Student Council", 300, 3000.0, EventStatus.PLANNED));
        list.add(new Event("E104", "Robotics Seminar", EventType.SEMINAR, baseDate.plusDays(3), LocalTime.of(10, 0), LocalTime.of(12, 30), "R103", "IEEE Student Chapter", 45, 300.0, EventStatus.PLANNED));
        return list;
    }

    private List<Resource> createSampleResources() {
        List<Resource> list = new ArrayList<>();
        list.add(new Resource("R101", "Seminar Hall A", ResourceType.AUDITORIUM, 1, ResourceStatus.ACTIVE, 100.0, "Block 1, 2nd Floor"));
        list.add(new Resource("R102", "Main Auditorium", ResourceType.AUDITORIUM, 1, ResourceStatus.ACTIVE, 350.0, "Central Campus"));
        list.add(new Resource("R103", "Lab 302", ResourceType.CLASSROOM, 1, ResourceStatus.ACTIVE, 50.0, "Block 3, 3rd Floor"));
        list.add(new Resource("R104", "4K Laser Projector", ResourceType.PROJECTOR, 5, ResourceStatus.ACTIVE, 25.0, "AV Store Room"));
        list.add(new Resource("R105", "Wireless Microphones", ResourceType.MICROPHONE, 10, ResourceStatus.ACTIVE, 10.0, "AV Store Room"));
        list.add(new Resource("R106", "Cushioned Event Chairs", ResourceType.CHAIRS, 200, ResourceStatus.ACTIVE, 1.5, "Warehouse A"));
        list.add(new Resource("R107", "PA Sound System", ResourceType.SPEAKERS, 4, ResourceStatus.UNDER_MAINTENANCE, 50.0, "Warehouse B"));
        return list;
    }

    private List<ResourceAllocation> createSampleAllocations() {
        List<ResourceAllocation> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        list.add(new ResourceAllocation("A1001", "E101", "R104", 1, now.minusHours(5)));
        list.add(new ResourceAllocation("A1002", "E101", "R105", 2, now.minusHours(5)));
        list.add(new ResourceAllocation("A1003", "E102", "R104", 2, now.minusHours(4)));
        list.add(new ResourceAllocation("A1004", "E102", "R105", 4, now.minusHours(4)));
        list.add(new ResourceAllocation("A1005", "E102", "R106", 80, now.minusHours(4)));
        list.add(new ResourceAllocation("A1006", "E103", "R106", 150, now.minusHours(2)));
        return list;
    }
}
