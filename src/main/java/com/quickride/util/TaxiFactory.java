package com.quickride.util;

import java.util.Random;

import com.quickride.model.Location;
import com.quickride.model.Taxi;

/**
 * Factory class to create sample taxis
 */
public class TaxiFactory {
    
    private static final Random random = new Random();
    
    // San Francisco area coordinates
    private static final double SF_LAT_MIN = 37.7075;
    private static final double SF_LAT_MAX = 37.8075;
    private static final double SF_LON_MIN = -122.5125;
    private static final double SF_LON_MAX = -122.3525;
    
    private static final String[] DRIVER_NAMES = {
        "Emily Williams", "John Smith", "Sarah Davis", "Michael Chen", "David Brown",
        "Kevin Jones", "Lisa Wilson", "Robert Lee", "Maria Garcia", "James Miller"
    };
    
    private static final String[] CAR_MODELS = {
        "Toyota Prius", "Honda Civic", "Tesla Model 3", "Ford Fusion", "Nissan Leaf",
        "Chevrolet Bolt", "Hyundai Ioniq", "Kia Niro", "Toyota Camry", "Honda Accord"
    };
    
    private static final String[] SF_NEIGHBORHOODS = {
        "Downtown", "Mission District", "SoMa", "Marina", "North Beach", 
        "Castro", "Haight-Ashbury", "Chinatown", "Sunset District", "Richmond District",
        "Nob Hill", "Pacific Heights", "Embarcadero", "Financial District", "Fisherman's Wharf"
    };

    private static final String[] STREET_NAMES = {
        "Market St", "Valencia St", "Van Ness Ave", "Mission St", "Geary Blvd",
        "Fillmore St", "Divisadero St", "Columbus Ave", "Folsom St", "Bryant St",
        "Howard St", "Harrison St", "Chestnut St", "Union St", "Grant Ave"
    };
    
    /**
     * Private constructor to prevent instantiation
     */
    private TaxiFactory() {
        // Utility class, not meant to be instantiated
    }
    
    /**
     * Creates a random array of taxis
     * @param count Number of taxis to create
     * @return Array of random taxis
     */
    public static Taxi[] createRandomTaxis(int count) {
        Taxi[] taxis = new Taxi[count];
        
        for (int i = 0; i < count; i++) {
            String driverName = DRIVER_NAMES[random.nextInt(DRIVER_NAMES.length)];
            String carModel = CAR_MODELS[random.nextInt(CAR_MODELS.length)];
            
            // Generate random license plate
            String licensePlate = "QR" + (1000 + random.nextInt(9000));
            
            // Generate random coordinates in San Francisco
            double latitude = SF_LAT_MIN + (SF_LAT_MAX - SF_LAT_MIN) * random.nextDouble();
            double longitude = SF_LON_MIN + (SF_LON_MAX - SF_LON_MIN) * random.nextDouble();
            
            // Generate address with neighborhood and street
            String neighborhood = SF_NEIGHBORHOODS[random.nextInt(SF_NEIGHBORHOODS.length)];
            String street = STREET_NAMES[random.nextInt(STREET_NAMES.length)];
            String streetNumber = (100 + random.nextInt(1900)) + "";
            String address = streetNumber + " " + street + ", " + neighborhood + ", San Francisco";
            
            // Create location with real coordinates
            Location location = new Location(latitude, longitude, address);
            
            taxis[i] = new Taxi(driverName, licensePlate, carModel, location);
        }
        
        return taxis;
    }
} 