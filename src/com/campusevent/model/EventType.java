package com.campusevent.model;

/**
 * Supported types of campus events.
 */
public enum EventType {
    WORKSHOP,
    SEMINAR,
    HACKATHON,
    CULTURAL,
    SPORTS,
    OTHER;

    public static EventType fromString(String text) {
        if (text == null) return OTHER;
        for (EventType b : EventType.values()) {
            if (b.name().equalsIgnoreCase(text.trim())) {
                return b;
            }
        }
        return OTHER;
    }
}
