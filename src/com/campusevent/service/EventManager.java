package com.campusevent.service;

import com.campusevent.exception.EventNotFoundException;
import com.campusevent.exception.InvalidInputException;
import com.campusevent.exception.ScheduleConflictException;
import com.campusevent.model.Event;
import com.campusevent.model.EventStatus;
import com.campusevent.util.ScheduleValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing Event lifecycle and CRUD operations.
 */
public class EventManager {
    private final List<Event> events;

    public EventManager() {
        this.events = new ArrayList<>();
    }

    public EventManager(List<Event> initialEvents) {
        this.events = initialEvents != null ? new ArrayList<>(initialEvents) : new ArrayList<>();
    }

    public void addEvent(Event event) throws InvalidInputException, ScheduleConflictException {
        if (event == null) {
            throw new InvalidInputException("Event details cannot be null.");
        }
        if (event.getId() == null || event.getId().trim().isEmpty()) {
            throw new InvalidInputException("Event ID cannot be empty.");
        }
        if (getEventById(event.getId()) != null) {
            throw new InvalidInputException("An event with ID '" + event.getId() + "' already exists.");
        }
        if (!ScheduleValidator.isValidTimeRange(event.getStartTime(), event.getEndTime())) {
            throw new InvalidInputException("Invalid time range! Event start time (" + event.getStartTime() +
                    ") must be strictly before end time (" + event.getEndTime() + ").");
        }

        events.add(event);
    }

    public void updateEvent(Event updatedEvent) throws EventNotFoundException, InvalidInputException {
        if (updatedEvent == null) {
            throw new InvalidInputException("Updated event cannot be null.");
        }
        Event existing = getEventById(updatedEvent.getId());
        if (existing == null) {
            throw new EventNotFoundException("Event with ID '" + updatedEvent.getId() + "' not found.");
        }
        if (!ScheduleValidator.isValidTimeRange(updatedEvent.getStartTime(), updatedEvent.getEndTime())) {
            throw new InvalidInputException("Invalid time range! Start time must be before end time.");
        }

        existing.setName(updatedEvent.getName());
        existing.setType(updatedEvent.getType());
        existing.setDate(updatedEvent.getDate());
        existing.setStartTime(updatedEvent.getStartTime());
        existing.setEndTime(updatedEvent.getEndTime());
        existing.setVenueId(updatedEvent.getVenueId());
        existing.setOrganizer(updatedEvent.getOrganizer());
        existing.setExpectedAttendees(updatedEvent.getExpectedAttendees());
        existing.setBudget(updatedEvent.getBudget());
        existing.setStatus(updatedEvent.getStatus());
    }

    public void deleteEvent(String eventId) throws EventNotFoundException {
        Event event = getEventById(eventId);
        if (event == null) {
            throw new EventNotFoundException("Event with ID '" + eventId + "' not found.");
        }
        events.remove(event);
    }

    public Event getEventById(String eventId) {
        if (eventId == null) return null;
        for (Event e : events) {
            if (e.getId().equalsIgnoreCase(eventId.trim())) {
                return e;
            }
        }
        return null;
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }

    public List<Event> searchEventsByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllEvents();
        }
        String q = query.toLowerCase().trim();
        return events.stream()
                .filter(e -> e.getName().toLowerCase().contains(q) || e.getId().toLowerCase().contains(q) || e.getOrganizer().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Event> getEventsByDate(LocalDate date) {
        if (date == null) return new ArrayList<>();
        return events.stream()
                .filter(e -> date.equals(e.getDate()) && e.getStatus() != EventStatus.CANCELLED)
                .collect(Collectors.toList());
    }
}
