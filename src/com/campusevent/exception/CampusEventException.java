package com.campusevent.exception;

/**
 * Base custom checked exception for Campus Event Resource Management system.
 */
public class CampusEventException extends Exception {
    public CampusEventException(String message) {
        super(message);
    }

    public CampusEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
