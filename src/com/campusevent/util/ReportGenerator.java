package com.campusevent.util;

import com.campusevent.model.Event;
import com.campusevent.model.EventStatus;
import com.campusevent.model.Resource;
import com.campusevent.model.ResourceAllocation;
import com.campusevent.model.ResourceStatus;
import com.campusevent.service.AllocationManager;
import com.campusevent.service.EventManager;
import com.campusevent.service.ResourceManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates formatted textual and tabular reports.
 */
public class ReportGenerator {

    public static void printUpcomingEventsReport(EventManager eventManager) {
        List<Event> events = eventManager.getAllEvents();
        LocalDate today = LocalDate.now();

        List<String[]> rows = new ArrayList<>();
        for (Event e : events) {
            if (!e.getDate().isBefore(today) && e.getStatus() != EventStatus.CANCELLED) {
                rows.add(new String[]{
                        e.getId(),
                        e.getName(),
                        e.getType().name(),
                        e.getDate().toString(),
                        e.getStartTime() + " - " + e.getEndTime(),
                        e.getVenueId(),
                        e.getOrganizer(),
                        String.valueOf(e.getExpectedAttendees()),
                        e.getStatus().name()
                });
            }
        }

        String[] headers = {"Event ID", "Event Name", "Type", "Date", "Time Window", "Venue ID", "Organizer", "Attendees", "Status"};
        TableFormatter.printTable("Upcoming Campus Events Report", headers, rows);
    }

    public static void printEventResourceAllocationsReport(EventManager eventManager, ResourceManager resourceManager, AllocationManager allocationManager) {
        List<Event> events = eventManager.getAllEvents();
        System.out.println("\n=======================================================");
        System.out.println("        EVENT-WISE RESOURCE ALLOCATION REPORT          ");
        System.out.println("=======================================================");

        if (events.isEmpty()) {
            System.out.println("No events registered in the system.");
            return;
        }

        for (Event event : events) {
            System.out.printf("\n>> Event: [%s] %s (%s) | Date: %s [%s - %s] | Venue: %s\n",
                    event.getId(), event.getName(), event.getType(), event.getDate(), event.getStartTime(), event.getEndTime(), event.getVenueId());

            List<ResourceAllocation> allocations = allocationManager.getAllocationsForEvent(event.getId());
            if (allocations.isEmpty()) {
                System.out.println("   (No additional resources allocated to this event)");
            } else {
                List<String[]> rows = new ArrayList<>();
                double totalEventResourceCost = 0;
                for (ResourceAllocation alloc : allocations) {
                    Resource res = resourceManager.getResourceById(alloc.getResourceId());
                    String resName = res != null ? res.getName() : "Unknown";
                    String resType = res != null ? res.getType().name() : "N/A";
                    double unitCost = res != null ? res.getCostPerUnit() : 0.0;
                    double totalCost = unitCost * alloc.getAllocatedQuantity();
                    totalEventResourceCost += totalCost;

                    rows.add(new String[]{
                            alloc.getAllocationId(),
                            alloc.getResourceId(),
                            resName,
                            resType,
                            String.valueOf(alloc.getAllocatedQuantity()),
                            String.format("$%.2f", unitCost),
                            String.format("$%.2f", totalCost),
                            alloc.getAllocatedAt().format(ResourceAllocation.DATETIME_FORMATTER)
                    });
                }
                String[] headers = {"Alloc ID", "Resource ID", "Resource Name", "Type", "Alloc Qty", "Unit Rate", "Total Cost", "Allocated At"};
                TableFormatter.printTable(null, headers, rows);
                System.out.printf("   Total Estimated Resource Cost for Event: $%.2f | Event Budget: $%.2f\n",
                        totalEventResourceCost, event.getBudget());
            }
        }
    }

    public static void printResourceInventoryStatusReport(ResourceManager resourceManager, AllocationManager allocationManager) {
        List<Resource> resources = resourceManager.getAllResources();
        List<String[]> rows = new ArrayList<>();

        for (Resource r : resources) {
            int currentlyAllocated = allocationManager.getTotalAllocatedQuantity(r.getId());
            int availableNow = Math.max(0, r.getTotalQuantity() - currentlyAllocated);
            rows.add(new String[]{
                    r.getId(),
                    r.getName(),
                    r.getType().name(),
                    r.getLocation(),
                    String.valueOf(r.getTotalQuantity()),
                    String.valueOf(currentlyAllocated),
                    String.valueOf(availableNow),
                    r.getStatus().name(),
                    String.format("$%.2f", r.getCostPerUnit())
            });
        }

        String[] headers = {"Resource ID", "Name", "Category", "Location", "Total Qty", "Allocated", "Unallocated", "Status", "Rate/Unit"};
        TableFormatter.printTable("Resource Inventory & Availability Status", headers, rows);
    }

    public static void printSummaryAnalyticsReport(EventManager eventManager, ResourceManager resourceManager, AllocationManager allocationManager) {
        List<Event> events = eventManager.getAllEvents();
        List<Resource> resources = resourceManager.getAllResources();
        List<ResourceAllocation> allocations = allocationManager.getAllAllocations();

        long plannedCount = events.stream().filter(e -> e.getStatus() == EventStatus.PLANNED).count();
        long ongoingCount = events.stream().filter(e -> e.getStatus() == EventStatus.ONGOING).count();
        long completedCount = events.stream().filter(e -> e.getStatus() == EventStatus.COMPLETED).count();
        long cancelledCount = events.stream().filter(e -> e.getStatus() == EventStatus.CANCELLED).count();

        int totalInventoryUnits = resources.stream().mapToInt(Resource::getTotalQuantity).sum();
        int activeResourcesCount = (int) resources.stream().filter(r -> r.getStatus() == ResourceStatus.ACTIVE).count();
        int maintenanceResourcesCount = (int) resources.stream().filter(r -> r.getStatus() == ResourceStatus.UNDER_MAINTENANCE).count();
        int totalAllocatedUnits = allocations.stream().mapToInt(ResourceAllocation::getAllocatedQuantity).sum();

        double totalBudget = events.stream().mapToDouble(Event::getBudget).sum();

        System.out.println("\n=======================================================");
        System.out.println("          CAMPUS EXECUTIVE SUMMARY ANALYTICS           ");
        System.out.println("=======================================================");
        System.out.printf(" Total Events Registered      : %d\n", events.size());
        System.out.printf("   - Planned Events           : %d\n", plannedCount);
        System.out.printf("   - Ongoing Events           : %d\n", ongoingCount);
        System.out.printf("   - Completed Events         : %d\n", completedCount);
        System.out.printf("   - Cancelled Events         : %d\n", cancelledCount);
        System.out.println("-------------------------------------------------------");
        System.out.printf(" Total Resources in Catalog   : %d\n", resources.size());
        System.out.printf("   - Active Resources         : %d\n", activeResourcesCount);
        System.out.printf("   - Under Maintenance        : %d\n", maintenanceResourcesCount);
        System.out.printf(" Total Physical Units         : %d\n", totalInventoryUnits);
        System.out.printf(" Total Units Currently Assigned: %d\n", totalAllocatedUnits);
        System.out.println("-------------------------------------------------------");
        System.out.printf(" Aggregate Event Budget       : $%.2f\n", totalBudget);
        System.out.println("=======================================================\n");
    }

    public static void printCombinedAuditReport(EventManager eventManager, ResourceManager resourceManager, AllocationManager allocationManager) {
        printSummaryAnalyticsReport(eventManager, resourceManager, allocationManager);
        printUpcomingEventsReport(eventManager);
        printResourceInventoryStatusReport(resourceManager, allocationManager);
        printEventResourceAllocationsReport(eventManager, resourceManager, allocationManager);
    }
}
