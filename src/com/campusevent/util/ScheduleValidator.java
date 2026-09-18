package com.campusevent.util;

import com.campusevent.model.Event;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Utility for verifying event schedules and checking time overlaps.
 */
public class ScheduleValidator {

    /**
     * Checks if a given start time and end time form a valid time range.
     */
    public static boolean isValidTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) return false;
        return startTime.isBefore(endTime);
    }

    /**
     * Checks if two event time slots on the same date overlap.
     * Back-to-back events (where end time of one equals start time of another) do NOT overlap.
     *
     * @return true if time slots overlap; false otherwise.
     */
    public static boolean doTimesOverlap(LocalDate date1, LocalTime start1, LocalTime end1,
                                         LocalDate date2, LocalTime start2, LocalTime end2) {
        if (!date1.equals(date2)) {
            return false;
        }
        // Overlap condition: Start1 < End2 AND End1 > Start2
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * Checks if two events overlap in time.
     */
    public static boolean doEventsOverlap(Event e1, Event e2) {
        if (e1 == null || e2 == null) return false;
        // Skip comparing event against itself or cancelled events
        if (e1.getId().equalsIgnoreCase(e2.getId())) return false;
        
        return doTimesOverlap(e1.getDate(), e1.getStartTime(), e1.getEndTime(),
                e2.getDate(), e2.getStartTime(), e2.getEndTime());
    }
}
