package com.campusevent.service;

import com.campusevent.exception.EventNotFoundException;
import com.campusevent.exception.InsufficientResourceException;
import com.campusevent.exception.InvalidInputException;
import com.campusevent.exception.ResourceNotFoundException;
import com.campusevent.exception.ScheduleConflictException;
import com.campusevent.model.Event;
import com.campusevent.model.EventStatus;
import com.campusevent.model.Resource;
import com.campusevent.model.ResourceAllocation;
import com.campusevent.model.ResourceStatus;
import com.campusevent.model.ResourceType;
import com.campusevent.util.ScheduleValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service orchestrating resource allocation, venue locking, and capacity conflict validation.
 */
public class AllocationManager {

    private final List<ResourceAllocation> allocations;
    private final EventManager eventManager;
    private final ResourceManager resourceManager;

    public AllocationManager(EventManager eventManager, ResourceManager resourceManager) {
        this.allocations = new ArrayList<>();
        this.eventManager = eventManager;
        this.resourceManager = resourceManager;
    }

    public AllocationManager(List<ResourceAllocation> initialAllocations, EventManager eventManager, ResourceManager resourceManager) {
        this.allocations = initialAllocations != null ? new ArrayList<>(initialAllocations) : new ArrayList<>();
        this.eventManager = eventManager;
        this.resourceManager = resourceManager;
    }

    /**
     * Allocates a specific quantity of a resource to an event after full conflict validation.
     */
    public ResourceAllocation allocateResource(String eventId, String resourceId, int requestedQuantity)
            throws EventNotFoundException, ResourceNotFoundException, ScheduleConflictException, InsufficientResourceException, InvalidInputException {

        if (requestedQuantity <= 0) {
            throw new InvalidInputException("Allocation quantity must be greater than zero.");
        }

        Event event = eventManager.getEventById(eventId);
        if (event == null) {
            throw new EventNotFoundException("Cannot allocate! Event with ID '" + eventId + "' does not exist.");
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new InvalidInputException("Cannot allocate resources to a CANCELLED event.");
        }

        Resource resource = resourceManager.getResourceById(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException("Cannot allocate! Resource with ID '" + resourceId + "' does not exist.");
        }

        if (resource.getStatus() == ResourceStatus.UNDER_MAINTENANCE) {
            throw new InvalidInputException("Resource '" + resource.getName() + "' [" + resourceId + "] is currently UNDER MAINTENANCE and cannot be booked.");
        }

        if (resource.getStatus() == ResourceStatus.DECOMMISSIONED) {
            throw new InvalidInputException("Resource '" + resource.getName() + "' [" + resourceId + "] has been DECOMMISSIONED.");
        }

        // 1. Check venue/hall exclusive lock if the resource is an AUDITORIUM or CLASSROOM venue
        if (resource.getType() == ResourceType.AUDITORIUM || resource.getType() == ResourceType.CLASSROOM) {
            checkVenueConflict(resource, event);
        }

        // 2. Check quantity capacity availability across overlapping time slots
        int maxAvailableInSlot = getAvailableQuantityForTimeSlot(resource.getId(), event.getDate(), event.getStartTime(), event.getEndTime(), event.getId());
        if (requestedQuantity > maxAvailableInSlot) {
            throw new InsufficientResourceException(String.format(
                    "Conflict detected! Insufficient quantity for resource '%s' [%s].\n" +
                    "   Requested: %d unit(s) | Available for slot (%s %s-%s): %d unit(s).\n" +
                    "   (Total Inventory: %d)",
                    resource.getName(), resourceId, requestedQuantity,
                    event.getDate(), event.getStartTime(), event.getEndTime(),
                    maxAvailableInSlot, resource.getTotalQuantity()
            ));
        }

        // Generate unique Allocation ID
        String allocId = "A" + (1000 + allocations.size() + 1);
        ResourceAllocation allocation = new ResourceAllocation(allocId, event.getId(), resource.getId(), requestedQuantity, LocalDateTime.now());
        allocations.add(allocation);

        return allocation;
    }

    /**
     * Checks if a venue is already booked by another overlapping event.
     */
    private void checkVenueConflict(Resource venueResource, Event targetEvent) throws ScheduleConflictException {
        List<Event> sameDateEvents = eventManager.getEventsByDate(targetEvent.getDate());
        for (Event existingEvent : sameDateEvents) {
            if (existingEvent.getId().equalsIgnoreCase(targetEvent.getId())) continue;
            if (existingEvent.getStatus() == EventStatus.CANCELLED) continue;

            // Check if venue is already allocated to an overlapping event
            boolean venueUsedByExisting = isResourceAllocatedToEvent(existingEvent.getId(), venueResource.getId());

            if (venueUsedByExisting) {
                if (ScheduleValidator.doEventsOverlap(targetEvent, existingEvent)) {
                    throw new ScheduleConflictException(String.format(
                            "VENUE CONFLICT DETECTED!\n" +
                            "   Venue '%s' [%s] is already booked on %s for Event '%s' [%s] (%s - %s).\n" +
                            "   Your requested event '%s' [%s] (%s - %s) overlaps with this booking.\n" +
                            "   Note: Back-to-back events ending exactly when the next starts are allowed.",
                            venueResource.getName(), venueResource.getId(), targetEvent.getDate(),
                            existingEvent.getName(), existingEvent.getId(), existingEvent.getStartTime(), existingEvent.getEndTime(),
                            targetEvent.getName(), targetEvent.getId(), targetEvent.getStartTime(), targetEvent.getEndTime()
                    ));
                }
            }
        }
    }

    /**
     * Calculates available unallocated quantity of a resource for a specified date and time window.
     */
    public int getAvailableQuantityForTimeSlot(String resourceId, LocalDate date, LocalTime startTime, LocalTime endTime, String excludeEventId) {
        Resource resource = resourceManager.getResourceById(resourceId);
        if (resource == null || resource.getStatus() != ResourceStatus.ACTIVE) {
            return 0;
        }

        List<Event> sameDateEvents = eventManager.getEventsByDate(date);
        int totalAllocatedInSlot = 0;

        for (Event existingEvent : sameDateEvents) {
            if (excludeEventId != null && existingEvent.getId().equalsIgnoreCase(excludeEventId)) continue;
            if (existingEvent.getStatus() == EventStatus.CANCELLED) continue;

            if (ScheduleValidator.doTimesOverlap(date, startTime, endTime, date, existingEvent.getStartTime(), existingEvent.getEndTime())) {
                List<ResourceAllocation> eventAllocations = getAllocationsForEvent(existingEvent.getId());
                for (ResourceAllocation alloc : eventAllocations) {
                    if (alloc.getResourceId().equalsIgnoreCase(resourceId)) {
                        totalAllocatedInSlot += alloc.getAllocatedQuantity();
                    }
                }
            }
        }

        return Math.max(0, resource.getTotalQuantity() - totalAllocatedInSlot);
    }

    public boolean isResourceAllocatedToEvent(String eventId, String resourceId) {
        for (ResourceAllocation a : allocations) {
            if (a.getEventId().equalsIgnoreCase(eventId) && a.getResourceId().equalsIgnoreCase(resourceId)) {
                return true;
            }
        }
        return false;
    }

    public void releaseAllocation(String allocationId) throws InvalidInputException {
        ResourceAllocation found = null;
        for (ResourceAllocation a : allocations) {
            if (a.getAllocationId().equalsIgnoreCase(allocationId.trim())) {
                found = a;
                break;
            }
        }
        if (found == null) {
            throw new InvalidInputException("Allocation record '" + allocationId + "' not found.");
        }
        allocations.remove(found);
    }

    public int releaseAllAllocationsForEvent(String eventId) {
        List<ResourceAllocation> toRemove = allocations.stream()
                .filter(a -> a.getEventId().equalsIgnoreCase(eventId))
                .collect(Collectors.toList());
        allocations.removeAll(toRemove);
        return toRemove.size();
    }

    public List<ResourceAllocation> getAllocationsForEvent(String eventId) {
        return allocations.stream()
                .filter(a -> a.getEventId().equalsIgnoreCase(eventId))
                .collect(Collectors.toList());
    }

    public List<ResourceAllocation> getAllAllocations() {
        return new ArrayList<>(allocations);
    }

    public int getTotalAllocatedQuantity(String resourceId) {
        return allocations.stream()
                .filter(a -> a.getResourceId().equalsIgnoreCase(resourceId))
                .mapToInt(ResourceAllocation::getAllocatedQuantity)
                .sum();
    }
}
