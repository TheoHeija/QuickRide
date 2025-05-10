package com.quickride.model;

/**
 * A simple class to represent locations
 */
public class Location {
    private final double latitude;
    private final double longitude;
    private final String address;
    
    public Location(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
    
    /**
     * Constructor with just an address.
     * Sets default coordinates (0,0)
     */
    public Location(String address) {
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.address = address;
    }
    
    // Getters
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public String getAddress() {
        return address;
    }
    
    // Calculate distance between two locations (using Haversine formula)
    public double distanceTo(Location other) {
        // Earth radius in kilometers
        final int R = 6371;
        
        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    @Override
    public String toString() {
        return address;
    }
} 