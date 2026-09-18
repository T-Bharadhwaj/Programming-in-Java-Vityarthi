package com.campusevent;

import com.campusevent.exception.CampusEventException;
import com.campusevent.model.*;
import com.campusevent.persistence.DataPersistenceManager;
import com.campusevent.service.AllocationManager;
import com.campusevent.service.EventManager;
import com.campusevent.service.ResourceManager;
import com.campusevent.util.InputValidator;
import com.campusevent.util.ReportGenerator;
import com.campusevent.util.TableFormatter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main Entry Point for Campus Event Resource Manager Console Application.
 */
public class Main {

    private static DataPersistenceManager persistenceManager;
    private static EventManager eventManager;
    private static ResourceManager resourceManager;
    private static AllocationManager allocationManager;
    private static Scanner scanner;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("       WELCOME TO CAMPUS EVENT RESOURCE MANAGER (CORE JAVA)       ");
        System.out.println("==================================================================");

        // Initialize persistence & services
        persistenceManager = new DataPersistenceManager("data");
        List<Event> loadedEvents = persistenceManager.loadEvents();
        List<Resource> loadedResources = persistenceManager.loadResources();
        List<ResourceAllocation> loadedAllocations = persistenceManager.loadAllocations();

        eventManager = new EventManager(loadedEvents);
        resourceManager = new ResourceManager(loadedResources);
        allocationManager = new AllocationManager(loadedAllocations, eventManager, resourceManager);

        scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = InputValidator.readInt(scanner, "Enter option (1-8)", 8, 1, 8);
            System.out.println();
            switch (choice) {
                case 1:
                    handleEventManagementMenu();
                    break;
                case 2:
                    handleResourceManagementMenu();
                    break;
                case 3:
                    handleAllocationMenu();
                    break;
                case 4:
                    handleCheckAvailability();
                    break;
                case 5:
                    handleViewSchedule();
                    break;
                case 6:
                    handleReportsMenu();
                    break;
                case 7:
                    handleRestoreSampleData();
                    break;
                case 8:
                    running = false;
                    saveAllData();
                    System.out.println("Thank you for using Campus Event Resource Manager. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please select from 1 to 8.");
            }
        }
        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("\n------------------------------------------------------------------");
        System.out.println("                        MAIN MENU                                 ");
        System.out.println("------------------------------------------------------------------");
        System.out.println(" 1. Manage Events (Add, View, Update, Delete, Search)");
        System.out.println(" 2. Manage Resources (Add, View, Update, Status, Delete)");
        System.out.println(" 3. Allocate Resources (Allocate, Release Allocation)");
        System.out.println(" 4. Check Resource & Venue Availability (By Date & Time)");
        System.out.println(" 5. View Event Schedule (Date Filter)");
        System.out.println(" 6. Generate Reports & Analytics");
        System.out.println(" 7. Restore Sample Data");
        System.out.println(" 8. Save & Exit");
        System.out.println("------------------------------------------------------------------");
    }

    // --- 1. EVENT MANAGEMENT ---

    private static void handleEventManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- EVENT MANAGEMENT SUB-MENU ---");
            System.out.println(" 1. View All Events");
            System.out.println(" 2. Add New Event");
            System.out.println(" 3. Update Existing Event");
            System.out.println(" 4. Delete Event");
            System.out.println(" 5. Search Events");
            System.out.println(" 6. Back to Main Menu");

            int choice = InputValidator.readInt(scanner, "Select Event option", 6, 1, 6);
            switch (choice) {
                case 1:
                    displayEvents(eventManager.getAllEvents());
                    break;
                case 2:
                    addNewEvent();
                    break;
                case 3:
                    updateEvent();
                    break;
                case 4:
                    deleteEvent();
                    break;
                case 5:
                    searchEvents();
                    break;
                case 6:
                    back = true;
                    break;
            }
        }
    }

    private static void displayEvents(List<Event> events) {
        List<String[]> rows = new ArrayList<>();
        for (Event e : events) {
            rows.add(new String[]{
                    e.getId(),
                    e.getName(),
                    e.getType().name(),
                    e.getDate().toString(),
                    e.getStartTime() + " - " + e.getEndTime(),
                    e.getVenueId(),
                    e.getOrganizer(),
                    String.valueOf(e.getExpectedAttendees()),
                    String.format("$%.2f", e.getBudget()),
                    e.getStatus().name()
            });
        }
        String[] headers = {"ID", "Name", "Type", "Date", "Time", "Venue ID", "Organizer", "Attendees", "Budget", "Status"};
        TableFormatter.printTable("Registered Campus Events", headers, rows);
    }

    private static void addNewEvent() {
        System.out.println("\n>>> Add New Event");
        String id = InputValidator.readString(scanner, "Enter Event ID (e.g., E105)", null);
        if (eventManager.getEventById(id) != null) {
            System.out.println("[Error] Event ID already exists!");
            return;
        }
        String name = InputValidator.readString(scanner, "Enter Event Name", null);
        EventType type = InputValidator.readEventType(scanner, "Choose Event Type:", EventType.WORKSHOP);
        LocalDate date = InputValidator.readLocalDate(scanner, "Enter Event Date", LocalDate.now().plusDays(1));
        LocalTime startTime = InputValidator.readLocalTime(scanner, "Enter Start Time", LocalTime.of(10, 0));
        LocalTime endTime = InputValidator.readLocalTime(scanner, "Enter End Time", LocalTime.of(12, 0));
        String venueId = InputValidator.readString(scanner, "Enter Venue Resource ID (e.g., R101 or Main Hall)", "R101");
        String organizer = InputValidator.readString(scanner, "Enter Organizer Name/Department", "Student Council");
        int attendees = InputValidator.readInt(scanner, "Enter Expected Attendees", 50, 1, 10000);
        double budget = InputValidator.readDouble(scanner, "Enter Event Budget", 500.0, 0.0);
        EventStatus status = EventStatus.PLANNED;

        Event newEvent = new Event(id, name, type, date, startTime, endTime, venueId, organizer, attendees, budget, status);
        try {
            eventManager.addEvent(newEvent);
            saveAllData();
            System.out.println("[Success] Event '" + name + "' [" + id + "] added successfully!");
        } catch (CampusEventException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void updateEvent() {
        System.out.println("\n>>> Update Event");
        String id = InputValidator.readString(scanner, "Enter Event ID to Update", null);
        Event existing = eventManager.getEventById(id);
        if (existing == null) {
            System.out.println("[Error] Event with ID '" + id + "' not found.");
            return;
        }

        System.out.println("Editing event: " + existing.getName());
        String name = InputValidator.readString(scanner, "Enter Name", existing.getName());
        EventType type = InputValidator.readEventType(scanner, "Choose Event Type:", existing.getType());
        LocalDate date = InputValidator.readLocalDate(scanner, "Enter Event Date", existing.getDate());
        LocalTime startTime = InputValidator.readLocalTime(scanner, "Enter Start Time", existing.getStartTime());
        LocalTime endTime = InputValidator.readLocalTime(scanner, "Enter End Time", existing.getEndTime());
        String venueId = InputValidator.readString(scanner, "Enter Venue ID", existing.getVenueId());
        String organizer = InputValidator.readString(scanner, "Enter Organizer", existing.getOrganizer());
        int attendees = InputValidator.readInt(scanner, "Enter Attendees", existing.getExpectedAttendees(), 1, 10000);
        double budget = InputValidator.readDouble(scanner, "Enter Budget", existing.getBudget(), 0.0);
        EventStatus status = InputValidator.readEventStatus(scanner, "Select Event Status:", existing.getStatus());

        Event updated = new Event(id, name, type, date, startTime, endTime, venueId, organizer, attendees, budget, status);
        try {
            eventManager.updateEvent(updated);
            saveAllData();
            System.out.println("[Success] Event updated successfully!");
        } catch (CampusEventException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void deleteEvent() {
        System.out.println("\n>>> Delete Event");
        String id = InputValidator.readString(scanner, "Enter Event ID to Delete", null);
        try {
            eventManager.deleteEvent(id);
            int releasedCount = allocationManager.releaseAllAllocationsForEvent(id);
            saveAllData();
            System.out.println("[Success] Event '" + id + "' and " + releasedCount + " associated allocation(s) removed.");
        } catch (CampusEventException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void searchEvents() {
        String query = InputValidator.readString(scanner, "Enter search keyword (Name / ID / Organizer)", null);
        List<Event> results = eventManager.searchEventsByName(query);
        displayEvents(results);
    }

    // --- 2. RESOURCE MANAGEMENT ---

    private static void handleResourceManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- RESOURCE INVENTORY SUB-MENU ---");
            System.out.println(" 1. View All Resources");
            System.out.println(" 2. Add New Resource");
            System.out.println(" 3. Update Resource");
            System.out.println(" 4. Change Resource Maintenance Status");
            System.out.println(" 5. Delete Resource");
            System.out.println(" 6. Search Resources");
            System.out.println(" 7. Back to Main Menu");

            int choice = InputValidator.readInt(scanner, "Select Resource option", 7, 1, 7);
            switch (choice) {
                case 1:
                    displayResources(resourceManager.getAllResources());
                    break;
                case 2:
                    addNewResource();
                    break;
                case 3:
                    updateResource();
                    break;
                case 4:
                    changeResourceStatus();
                    break;
                case 5:
                    deleteResource();
                    break;
                case 6:
                    searchResources();
                    break;
                case 7:
                    back = true;
                    break;
            }
        }
    }

    private static void displayResources(List<Resource> resources) {
        List<String[]> rows = new ArrayList<>();
        for (Resource r : resources) {
            int allocated = allocationManager.getTotalAllocatedQuantity(r.getId());
            int unallocated = Math.max(0, r.getTotalQuantity() - allocated);
            rows.add(new String[]{
                    r.getId(),
                    r.getName(),
                    r.getType().name(),
                    String.valueOf(r.getTotalQuantity()),
                    String.valueOf(allocated),
                    String.valueOf(unallocated),
                    r.getLocation(),
                    r.getStatus().name(),
                    String.format("$%.2f", r.getCostPerUnit())
            });
        }
        String[] headers = {"ID", "Name", "Type", "Total Qty", "Allocated", "Unallocated", "Location", "Status", "Rate/Unit"};
        TableFormatter.printTable("Campus Resource Inventory", headers, rows);
    }

    private static void addNewResource() {
        System.out.println("\n>>> Add New Resource");
        String id = InputValidator.readString(scanner, "Enter Resource ID (e.g., R108)", null);
        if (resourceManager.getResourceById(id) != null) {
            System.out.println("[Error] Resource ID already exists!");
            return;
        }
        String name = InputValidator.readString(scanner, "Enter Resource Name", null);
        ResourceType type = InputValidator.readResourceType(scanner, "Select Resource Type:", ResourceType.PROJECTOR);
        int quantity = InputValidator.readInt(scanner, "Enter Total Quantity", 1, 0, 10000);
        ResourceStatus status = InputValidator.readResourceStatus(scanner, "Select Operational Status:", ResourceStatus.ACTIVE);
        double cost = InputValidator.readDouble(scanner, "Enter Cost Per Unit Rate", 10.0, 0.0);
        String location = InputValidator.readString(scanner, "Enter Storage Location / Room", "Central Store");

        Resource res = new Resource(id, name, type, quantity, status, cost, location);
        try {
            resourceManager.addResource(res);
            saveAllData();
            System.out.println("[Success] Resource '" + name + "' [" + id + "] added to inventory.");
        } catch (CampusEventException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void updateResource() {
        System.out.println("\n>>> Update Resource");
        String id = InputValidator.readString(scanner, "Enter Resource ID to update", null);
        Resource existing = resourceManager.getResourceById(id);
        if (existing == null) {
            System.out.println("[Error] Resource with ID '" + id + "' not found.");
            return;
        }

        String name = InputValidator.readString(scanner, "Enter Resource Name", existing.getName());
        ResourceType type = InputValidator.readResourceType(scanner, "Select Resource Type:", existing.getType());
        int quantity = InputValidator.readInt(scanner, "Enter Total Quantity", existing.getTotalQuantity(), 0, 10000);
        ResourceStatus status = InputValidator.readResourceStatus(scanner, "Select Status:", existing.getStatus());
        double cost = InputValidator.readDouble(scanner, "Enter Rate/Unit", existing.getCostPerUnit(), 0.0);
        String location = InputValidator.readString(scanner, "Enter Location", existing.getLocation());

        Resource updated = new Resource(id, name, type, quantity, status, cost, location);
        try {
            resourceManager.updateResource(updated);
            saveAllData();
            System.out.println("[Success] Resource updated successfully.");
        } catch (CampusEventException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void changeResourceStatus() {
        System.out.println("\n>>> Change Resource Status");
        String id = InputValidator.readString(scanner, "Enter Resource ID", null);
        Resource existing = resourceManager.getResourceById(id);
        if (existing == null) {
            System.out.println("[Error] Resource not found.");
            return;
        }
        ResourceStatus status = InputValidator.readResourceStatus(scanner, "Select New Status:", existing.getStatus());
        try {
            resourceManager.setResourceStatus(id, status);
            saveAllData();
            System.out.println("[Success] Status for resource '" + id + "' updated to " + status);
        } catch (CampusEventException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void deleteResource() {
        System.out.println("\n>>> Delete Resource");
        String id = InputValidator.readString(scanner, "Enter Resource ID to remove", null);
        try {
            resourceManager.removeResource(id);
            saveAllData();
            System.out.println("[Success] Resource '" + id + "' removed from inventory.");
        } catch (CampusEventException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    private static void searchResources() {
        String query = InputValidator.readString(scanner, "Enter search keyword (Name / ID)", null);
        displayResources(resourceManager.searchResourcesByName(query));
    }

    // --- 3. ALLOCATION MODULE --

    private static void handleAllocationMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- RESOURCE ALLOCATION SUB-MENU ---");
            System.out.println(" 1. Allocate Resource / Venue to Event");
            System.out.println(" 2. View All Allocations");
            System.out.println(" 3. Release Specific Allocation");
            System.out.println(" 4. Back to Main Menu");

            int choice = InputValidator.readInt(scanner, "Select Allocation option", 4, 1, 4);
            switch (choice) {
                case 1:
                    allocateResourceToEvent();
                    break;
                case 2:
                    displayAllocations();
                    break;
                case 3:
                    releaseAllocation();
                    break;
                case 4:
                    back = true;
                    break;
            }
        }
    }

    private static void allocateResourceToEvent() {
        System.out.println("\n>>> Allocate Resource / Venue");
        String eventId = InputValidator.readString(scanner, "Enter Event ID", null);
        Event event = eventManager.getEventById(eventId);
        if (event == null) {
            System.out.println("[Error] Event '" + eventId + "' not found.");
            return;
        }
        System.out.printf("Selected Event: [%s] %s | Date: %s [%s - %s]\n",
                event.getId(), event.getName(), event.getDate(), event.getStartTime(), event.getEndTime());

        String resourceId = InputValidator.readString(scanner, "Enter Resource ID to Allocate", null);
        Resource resource = resourceManager.getResourceById(resourceId);
        if (resource == null) {
            System.out.println("[Error] Resource '" + resourceId + "' not found.");
            return;
        }

        int availableInSlot = allocationManager.getAvailableQuantityForTimeSlot(
                resource.getId(), event.getDate(), event.getStartTime(), event.getEndTime(), event.getId());
        System.out.printf("Resource '%s' [%s] - Max Available in this time slot: %d\n",
                resource.getName(), resource.getId(), availableInSlot);

        if (availableInSlot <= 0 && resource.getStatus() == ResourceStatus.ACTIVE) {
            System.out.println("[Warning] No units of this resource are available in the requested time slot!");
        }

        int qty = InputValidator.readInt(scanner, "Enter Quantity to Allocate", 1, 1, 10000);

        try {
            ResourceAllocation alloc = allocationManager.allocateResource(eventId, resourceId, qty);
            saveAllData();
            System.out.println("\n=======================================================");
            System.out.println("          [SUCCESS] RESOURCE ALLOCATED                 ");
            System.out.println("=======================================================");
            System.out.println(" Allocation ID : " + alloc.getAllocationId());
            System.out.println(" Event         : " + event.getName() + " [" + event.getId() + "]");
            System.out.println(" Resource      : " + resource.getName() + " [" + resource.getId() + "]");
            System.out.println(" Quantity      : " + qty);
            System.out.println(" Time Window   : " + event.getDate() + " (" + event.getStartTime() + " to " + event.getEndTime() + ")");
            System.out.println("=======================================================\n");
        } catch (CampusEventException e) {
            System.out.println("\n[ALLOCATION REJECTED] " + e.getMessage());
        }
    }

    private static void displayAllocations() {
        List<ResourceAllocation> allocations = allocationManager.getAllAllocations();
        List<String[]> rows = new ArrayList<>();
        for (ResourceAllocation a : allocations) {
            Event e = eventManager.getEventById(a.getEventId());
            Resource r = resourceManager.getResourceById(a.getResourceId());
            rows.add(new String[]{
                    a.getAllocationId(),
                    a.getEventId(),
                    e != null ? e.getName() : "Unknown Event",
                    a.getResourceId(),
                    r != null ? r.getName() : "Unknown Resource",
                    String.valueOf(a.getAllocatedQuantity()),
                    e != null ? e.getDate().toString() : "N/A",
                    e != null ? (e.getStartTime() + " - " + e.getEndTime()) : "N/A"
            });
        }
        String[] headers = {"Alloc ID", "Event ID", "Event Name", "Resource ID", "Resource Name", "Qty", "Date", "Time Slot"};
        TableFormatter.printTable("Current Active Resource Allocations", headers, rows);
    }

    private static void releaseAllocation() {
        System.out.println("\n>>> Release Allocation");
        String allocId = InputValidator.readString(scanner, "Enter Allocation ID to release", null);
        try {
            allocationManager.releaseAllocation(allocId);
            saveAllData();
            System.out.println("[Success] Allocation '" + allocId + "' released.");
        } catch (CampusEventException e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    // --- 4. CHECK AVAILABILITY ---

    private static void handleCheckAvailability() {
        System.out.println("\n>>> Check Resource & Venue Availability");
        LocalDate date = InputValidator.readLocalDate(scanner, "Enter Target Date", LocalDate.now().plusDays(1));
        LocalTime startTime = InputValidator.readLocalTime(scanner, "Enter Start Time", LocalTime.of(9, 0));
        LocalTime endTime = InputValidator.readLocalTime(scanner, "Enter End Time", LocalTime.of(12, 0));

        System.out.printf("\nResource Availability Window: %s [%s - %s]\n", date, startTime, endTime);

        List<Resource> allResources = resourceManager.getAllResources();
        List<String[]> rows = new ArrayList<>();
        for (Resource r : allResources) {
            int availableInSlot = allocationManager.getAvailableQuantityForTimeSlot(r.getId(), date, startTime, endTime, null);
            String availStatus = r.getStatus() == ResourceStatus.UNDER_MAINTENANCE ? "MAINTENANCE" :
                    (availableInSlot > 0 ? "AVAILABLE (" + availableInSlot + ")" : "BOOKED / FULL");

            rows.add(new String[]{
                    r.getId(),
                    r.getName(),
                    r.getType().name(),
                    String.valueOf(r.getTotalQuantity()),
                    String.valueOf(availableInSlot),
                    r.getLocation(),
                    availStatus
            });
        }

        String[] headers = {"ID", "Resource Name", "Type", "Total Qty", "Available Qty in Slot", "Location", "Slot Availability"};
        TableFormatter.printTable("Availability Status for Date & Time Window", headers, rows);
    }

    // -- 5. VIEW SCHEDULE --

    private static void handleViewSchedule() {
        System.out.println("\n>>> View Campus Event Schedule");
        LocalDate date = InputValidator.readLocalDate(scanner, "Enter Date to View Schedule", LocalDate.now().plusDays(1));

        List<Event> dayEvents = eventManager.getEventsByDate(date);
        List<String[]> rows = new ArrayList<>();
        for (Event e : dayEvents) {
            rows.add(new String[]{
                    e.getId(),
                    e.getName(),
                    e.getType().name(),
                    e.getStartTime() + " - " + e.getEndTime(),
                    e.getVenueId(),
                    e.getOrganizer(),
                    e.getStatus().name()
            });
        }
        String[] headers = {"Event ID", "Event Name", "Type", "Time Slot", "Venue ID", "Organizer", "Status"};
        TableFormatter.printTable("Campus Events Timeline for " + date, headers, rows);
    }

    // --- 6. REPORTS ---

    private static void handleReportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- REPORTS & ANALYTICS SUB-MENU ---");
            System.out.println(" 1. View Upcoming Events Report");
            System.out.println(" 2. View Event-wise Resource Allocations");
            System.out.println(" 3. View Resource Inventory Status");
            System.out.println(" 4. View Executive Summary Analytics");
            System.out.println(" 5. Generate Full Audit Report");
            System.out.println(" 6. Back to Main Menu");

            int choice = InputValidator.readInt(scanner, "Select Report option", 6, 1, 6);
            switch (choice) {
                case 1:
                    ReportGenerator.printUpcomingEventsReport(eventManager);
                    break;
                case 2:
                    ReportGenerator.printEventResourceAllocationsReport(eventManager, resourceManager, allocationManager);
                    break;
                case 3:
                    ReportGenerator.printResourceInventoryStatusReport(resourceManager, allocationManager);
                    break;
                case 4:
                    ReportGenerator.printSummaryAnalyticsReport(eventManager, resourceManager, allocationManager);
                    break;
                case 5:
                    ReportGenerator.printCombinedAuditReport(eventManager, resourceManager, allocationManager);
                    break;
                case 6:
                    back = true;
                    break;
            }
        }
    }

    // -- 7. RESTORE SAMPLE DATA --

    private static void handleRestoreSampleData() {
        String confirm = InputValidator.readString(scanner, "Are you sure you want to reset all data to sample records? (yes/no)", "no");
        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            persistenceManager.restoreSampleData();
            // Reload services
            eventManager = new EventManager(persistenceManager.loadEvents());
            resourceManager = new ResourceManager(persistenceManager.loadResources());
            allocationManager = new AllocationManager(persistenceManager.loadAllocations(), eventManager, resourceManager);
            System.out.println("[Success] Sample data reloaded into memory.");
        } else {
            System.out.println("[Cancelled] Data restore cancelled.");
        }
    }

    // --- HELPER DATA SAVE ---

    private static void saveAllData() {
        persistenceManager.saveEvents(eventManager.getAllEvents());
        persistenceManager.saveResources(resourceManager.getAllResources());
        persistenceManager.saveAllocations(allocationManager.getAllAllocations());
    }
}
