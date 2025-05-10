package com.quickride.exception;

/**
 * Exception thrown when invalid taxi information is provided
 */
public class InvalidTaxiException extends Exception {
    public InvalidTaxiException() {
        super("The taxi information provided is invalid.");
    }
    
    public InvalidTaxiException(String message) {
        super(message);
    }
} 