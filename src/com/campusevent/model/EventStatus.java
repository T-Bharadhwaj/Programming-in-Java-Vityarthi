package com.campusevent.model;

/**
 * Status lifecycle of an event.
 */
public enum EventStatus {
    PLANNED,
    ONGOING,
    COMPLETED,
    CANCELLED;

    public static EventStatus fromString(String text) {
        if (text == null) return PLANNED;
        for (EventStatus s : EventStatus.values()) {
            if (s.name().equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        return PLANNED;
    }
}
