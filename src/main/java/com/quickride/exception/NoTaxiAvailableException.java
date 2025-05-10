package com.quickride.exception;

/**
 * Exception thrown when no taxis are available
 */
public class NoTaxiAvailableException extends Exception {
    public NoTaxiAvailableException() {
        super("No taxis are currently available.");
    }
    
    public NoTaxiAvailableException(String message) {
        super(message);
    }
} 