package com.quickride.model;

import java.util.UUID;

/**
 * Class representing a taxi with various properties
 */
public class Taxi {
    private final String id;
    private final String driverName;
    private final String licensePlate;
    private final String carModel;
    private Location currentLocation;
    private boolean available;
    
    public Taxi(String driverName, String licensePlate, String carModel, Location currentLocation) {
        this.id = UUID.randomUUID().toString();
        this.driverName = driverName;
        this.licensePlate = licensePlate;
        this.carModel = carModel;
        this.currentLocation = currentLocation;
        this.available = true;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public String getDriverName() {
        return driverName;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public String getCarModel() {
        return carModel;
    }
    
    public Location getCurrentLocation() {
        return currentLocation;
    }
    
    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    @Override
    public String toString() {
        return carModel + " (" + licensePlate + ") - Driver: " + driverName;
    }
} 