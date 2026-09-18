package com.campusevent.test;

import com.campusevent.exception.*;
import com.campusevent.model.*;
import com.campusevent.persistence.DataPersistenceManager;
import com.campusevent.service.AllocationManager;
import com.campusevent.service.EventManager;
import com.campusevent.service.ResourceManager;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Automated Core Java Test Suite verifying all core business rules and conflict detection.
 */
public class TestRunner {

    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  CAMPUS EVENT RESOURCE MANAGER - AUTOMATED TEST SUITE RUNNER     ");
        System.out.println("==================================================================");

        testCreateAndUpdateEvent();
        testAddAndUpdateResource();
        testSuccessfulAllocation();
        testPreventVenueDoubleBooking();
        testPreventOverAllocationQuantity();
        testAllowBackToBackEvents();
        testReleaseResourcesAfterCancellation();
        testHandlingInvalidInputAndMissingIds();
        testCsvPersistenceLoadSave();

        System.out.println("\n==================================================================");
        System.out.printf(" TEST SUMMARY: Total: %d | PASSED: %d | FAILED: %d\n",
                (passedTests + failedTests), passedTests, failedTests);
        System.out.println("==================================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String detail) {
        if (condition) {
            System.out.printf(" [PASS] %s\n", testName);
            passedTests++;
        } else {
            System.out.printf(" [FAIL] %s - %s\n", testName, detail);
            failedTests++;
        }
    }

    private static void testCreateAndUpdateEvent() {
        System.out.println("\n--- 1. Testing Event Creation and Update ---");
        EventManager em = new EventManager();
        try {
            Event e = new Event("T101", "Test Workshop", EventType.WORKSHOP, LocalDate.now().plusDays(5),
                    LocalTime.of(10, 0), LocalTime.of(12, 0), "R101", "CS Dept", 50, 400.0, EventStatus.PLANNED);
            em.addEvent(e);
            assertTrue("Create Event", em.getEventById("T101") != null, "Event was not saved.");

            e.setName("Updated Workshop Name");
            em.updateEvent(e);
            assertTrue("Update Event", "Updated Workshop Name".equals(em.getEventById("T101").getName()), "Name was not updated.");
        } catch (Exception ex) {
            assertTrue("Create and Update Event Exception Check", false, ex.getMessage());
        }
    }

    private static void testAddAndUpdateResource() {
        System.out.println("\n--- 2. Testing Resource Creation and Update ---");
        ResourceManager rm = new ResourceManager();
        try {
            Resource r = new Resource("TR101", "Test Projector", ResourceType.PROJECTOR, 5, ResourceStatus.ACTIVE, 20.0, "Store A");
            rm.addResource(r);
            assertTrue("Add Resource", rm.getResourceById("TR101") != null, "Resource was not saved.");

            r.setTotalQuantity(10);
            rm.updateResource(r);
            assertTrue("Update Resource", rm.getResourceById("TR101").getTotalQuantity() == 10, "Quantity not updated.");
        } catch (Exception ex) {
            assertTrue("Add/Update Resource Exception Check", false, ex.getMessage());
        }
    }

    private static void testSuccessfulAllocation() {
        System.out.println("\n--- 3. Testing Successful Resource Allocation ---");
        EventManager em = new EventManager();
        ResourceManager rm = new ResourceManager();
        AllocationManager am = new AllocationManager(em, rm);

        try {
            LocalDate date = LocalDate.now().plusDays(2);
            Event e = new Event("E1", "Seminar", EventType.SEMINAR, date, LocalTime.of(9, 0), LocalTime.of(11, 0), "R1", "Org", 30, 100.0, EventStatus.PLANNED);
            Resource r = new Resource("R1", "Chairs", ResourceType.CHAIRS, 50, ResourceStatus.ACTIVE, 1.0, "Hall");
            em.addEvent(e);
            rm.addResource(r);

            ResourceAllocation alloc = am.allocateResource("E1", "R1", 20);
            assertTrue("Successful Allocation", alloc != null && alloc.getAllocatedQuantity() == 20, "Allocation failed.");
        } catch (Exception ex) {
            assertTrue("Successful Allocation Exception Check", false, ex.getMessage());
        }
    }

    private static void testPreventVenueDoubleBooking() {
        System.out.println("\n--- 4. Testing Double Booking Prevention for Venues ---");
        EventManager em = new EventManager();
        ResourceManager rm = new ResourceManager();
        AllocationManager am = new AllocationManager(em, rm);

        try {
            LocalDate date = LocalDate.now().plusDays(3);
            Resource venue = new Resource("V1", "Auditorium 1", ResourceType.AUDITORIUM, 1, ResourceStatus.ACTIVE, 100.0, "Block A");
            rm.addResource(venue);

            Event e1 = new Event("EV1", "Event 1", EventType.SEMINAR, date, LocalTime.of(10, 0), LocalTime.of(13, 0), "V1", "Org 1", 50, 100.0, EventStatus.PLANNED);
            Event e2 = new Event("EV2", "Event 2 Overlapping", EventType.WORKSHOP, date, LocalTime.of(11, 0), LocalTime.of(14, 0), "V1", "Org 2", 50, 100.0, EventStatus.PLANNED);

            em.addEvent(e1);
            em.addEvent(e2);

            am.allocateResource("EV1", "V1", 1);

            boolean conflictThrown = false;
            try {
                am.allocateResource("EV2", "V1", 1);
            } catch (ScheduleConflictException ex) {
                conflictThrown = true;
            }

            assertTrue("Prevent Venue Double Booking", conflictThrown, "ScheduleConflictException was expected but not thrown.");
        } catch (Exception ex) {
            assertTrue("Venue Double Booking Exception Check", false, ex.getMessage());
        }
    }

    private static void testPreventOverAllocationQuantity() {
        System.out.println("\n--- 5. Testing Over-Allocation Quantity Prevention ---");
        EventManager em = new EventManager();
        ResourceManager rm = new ResourceManager();
        AllocationManager am = new AllocationManager(em, rm);

        try {
            LocalDate date = LocalDate.now().plusDays(4);
            Event e = new Event("EQ1", "Hackathon", EventType.HACKATHON, date, LocalTime.of(9, 0), LocalTime.of(17, 0), "V1", "Org", 100, 200.0, EventStatus.PLANNED);
            Resource mic = new Resource("M1", "Microphone", ResourceType.MICROPHONE, 4, ResourceStatus.ACTIVE, 5.0, "Lab");
            em.addEvent(e);
            rm.addResource(mic);

            boolean errorThrown = false;
            try {
                am.allocateResource("EQ1", "M1", 6); // Requesting 6 when only 4 exist
            } catch (InsufficientResourceException ex) {
                errorThrown = true;
            }

            assertTrue("Prevent Over-Allocation Quantity", errorThrown, "InsufficientResourceException was expected but not thrown.");
        } catch (Exception ex) {
            assertTrue("Over Allocation Exception Check", false, ex.getMessage());
        }
    }

    private static void testAllowBackToBackEvents() {
        System.out.println("\n--- 6. Testing Back-to-Back Events Allowance ---");
        EventManager em = new EventManager();
        ResourceManager rm = new ResourceManager();
        AllocationManager am = new AllocationManager(em, rm);

        try {
            LocalDate date = LocalDate.now().plusDays(5);
            Resource hall = new Resource("H1", "Lecture Hall 1", ResourceType.CLASSROOM, 1, ResourceStatus.ACTIVE, 50.0, "Block B");
            rm.addResource(hall);

            Event e1 = new Event("EB1", "Morning Session", EventType.SEMINAR, date, LocalTime.of(9, 0), LocalTime.of(11, 0), "H1", "Dept A", 40, 50.0, EventStatus.PLANNED);
            Event e2 = new Event("EB2", "Afternoon Session", EventType.SEMINAR, date, LocalTime.of(11, 0), LocalTime.of(13, 0), "H1", "Dept B", 40, 50.0, EventStatus.PLANNED);

            em.addEvent(e1);
            em.addEvent(e2);

            am.allocateResource("EB1", "H1", 1);
            ResourceAllocation alloc2 = am.allocateResource("EB2", "H1", 1);

            assertTrue("Allow Back-to-Back Events", alloc2 != null, "Back-to-back event allocation should have succeeded.");
        } catch (Exception ex) {
            assertTrue("Back-to-Back Events Exception Check", false, ex.getMessage());
        }
    }

    private static void testReleaseResourcesAfterCancellation() {
        System.out.println("\n--- 7. Testing Resource Release After Event Deletion/Cancellation ---");
        EventManager em = new EventManager();
        ResourceManager rm = new ResourceManager();
        AllocationManager am = new AllocationManager(em, rm);

        try {
            LocalDate date = LocalDate.now().plusDays(6);
            Event e = new Event("EC1", "Cancelled Fest", EventType.CULTURAL, date, LocalTime.of(15, 0), LocalTime.of(18, 0), "R1", "Club", 50, 100.0, EventStatus.PLANNED);
            Resource proj = new Resource("P1", "Projector", ResourceType.PROJECTOR, 2, ResourceStatus.ACTIVE, 10.0, "Store");
            em.addEvent(e);
            rm.addResource(proj);

            am.allocateResource("EC1", "P1", 2);
            assertTrue("Allocated 2 projectors", am.getTotalAllocatedQuantity("P1") == 2, "Failed to allocate 2 projectors.");

            // Delete event & release allocations
            em.deleteEvent("EC1");
            am.releaseAllAllocationsForEvent("EC1");

            assertTrue("Release Allocations After Deletion", am.getTotalAllocatedQuantity("P1") == 0, "Allocations were not released.");
        } catch (Exception ex) {
            assertTrue("Release Resource Exception Check", false, ex.getMessage());
        }
    }

    private static void testHandlingInvalidInputAndMissingIds() {
        System.out.println("\n--- 8. Testing Error Handling for Missing IDs and Invalid Input ---");
        EventManager em = new EventManager();
        ResourceManager rm = new ResourceManager();
        AllocationManager am = new AllocationManager(em, rm);

        boolean nonExistentEventCaught = false;
        try {
            am.allocateResource("NON_EXISTENT_EID", "R1", 1);
        } catch (EventNotFoundException ex) {
            nonExistentEventCaught = true;
        } catch (Exception ex) {}

        assertTrue("Missing Event ID Error Handling", nonExistentEventCaught, "EventNotFoundException expected.");
    }

    private static void testCsvPersistenceLoadSave() {
        System.out.println("\n--- 9. Testing CSV Persistence Load and Save ---");
        String testDir = "scratch_test_data";
        DataPersistenceManager pm = new DataPersistenceManager(testDir);

        try {
            List<Event> sampleEvents = pm.loadEvents();
            List<Resource> sampleResources = pm.loadResources();
            List<ResourceAllocation> sampleAllocations = pm.loadAllocations();

            assertTrue("CSV Persistence Loaded Events", !sampleEvents.isEmpty(), "Events list should not be empty.");
            assertTrue("CSV Persistence Loaded Resources", !sampleResources.isEmpty(), "Resources list should not be empty.");
            assertTrue("CSV Persistence Loaded Allocations", !sampleAllocations.isEmpty(), "Allocations list should not be empty.");

            // Clean up test scratch dir
            File dir = new File(testDir);
            if (dir.exists()) {
                for (File f : dir.listFiles()) f.delete();
                dir.delete();
            }
        } catch (Exception ex) {
            assertTrue("CSV Persistence Exception Check", false, ex.getMessage());
        }
    }
}
