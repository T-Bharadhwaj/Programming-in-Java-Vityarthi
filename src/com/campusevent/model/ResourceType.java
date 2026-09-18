package com.campusevent.model;

/**
 * Supported categories of campus resources.
 */
public enum ResourceType {
    CLASSROOM,
    AUDITORIUM,
    PROJECTOR,
    MICROPHONE,
    CHAIRS,
    SPEAKERS,
    OTHER;

    public static ResourceType fromString(String text) {
        if (text == null) return OTHER;
        for (ResourceType t : ResourceType.values()) {
            if (t.name().equalsIgnoreCase(text.trim())) {
                return t;
            }
        }
        return OTHER;
    }
}
