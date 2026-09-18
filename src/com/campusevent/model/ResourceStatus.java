package com.campusevent.model;

/**
 * Status condition of a resource.
 */
public enum ResourceStatus {
    ACTIVE,
    UNDER_MAINTENANCE,
    DECOMMISSIONED;

    public static ResourceStatus fromString(String text) {
        if (text == null) return ACTIVE;
        for (ResourceStatus s : ResourceStatus.values()) {
            if (s.name().equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        return ACTIVE;
    }
}
