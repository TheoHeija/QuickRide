package com.quickride.util;

/**
 * Constants used throughout the application
 */
public class Constants {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Constants() {
        // Utility class, not meant to be instantiated
    }

    // Application
    public static final String APP_TITLE = "QuickRide - Taxi Booking System";
    public static final double APP_WIDTH = 1000;
    public static final double APP_HEIGHT = 700;
    
    // Styling
    public static final String CSS_FILE = "/css/modern_style.css";
    
    // Taxi types
    public static final String[] CAR_MODELS = {
        "Toyota Camry", "Honda Accord", "Ford Fusion", 
        "Hyundai Sonata", "Tesla Model 3", "BMW 5 Series"
    };
    
    // Sample locations
    public static final String[] SAMPLE_LOCATIONS = {
        "Airport Terminal", "Central Station", "Downtown Mall",
        "University Campus", "Business District", "Shopping Center",
        "Riverfront Park", "Hotel Zone", "Convention Center", "Hospital"
    };
    
    // UI messages
    public static final String NO_TAXI_AVAILABLE = "No taxi available at the moment";
    public static final String RIDE_REQUESTED = "Ride requested successfully";
    public static final String RIDE_STARTED = "Ride started successfully";
    public static final String RIDE_COMPLETED = "Ride completed successfully";
    public static final String RIDE_CANCELLED = "Ride cancelled successfully";
    public static final String TAXI_ADDED = "Taxi added successfully";
    
    // Big O time complexity descriptions
    public static final String ADD_TAXI_COMPLEXITY = "O(1) - Constant time";
    public static final String GET_NEXT_TAXI_COMPLEXITY = "O(1) - Constant time (queue poll operation)";
    public static final String GET_NEAREST_TAXI_COMPLEXITY = "O(n) - Linear time where n is the number of available taxis";
    public static final String RELEASE_TAXI_COMPLEXITY = "O(n) - Linear time to find and remove from assigned list";
} 