package com.quickride.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Class to represent a ride
 */
public class Ride {
    private final String id;
    private final String customerName;
    private final Location pickupLocation;
    private final Location dropoffLocation;
    private final LocalDateTime requestTime;
    private LocalDateTime assignedTime;
    private LocalDateTime completedTime;
    private Taxi assignedTaxi;
    private RideStatus status;
    
    public Ride(String customerName, Location pickupLocation, Location dropoffLocation) {
        this.id = UUID.randomUUID().toString();
        this.customerName = customerName;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.requestTime = LocalDateTime.now();
        this.status = RideStatus.REQUESTED;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public Location getPickupLocation() {
        return pickupLocation;
    }
    
    public Location getDropoffLocation() {
        return dropoffLocation;
    }
    
    public LocalDateTime getRequestTime() {
        return requestTime;
    }
    
    public LocalDateTime getAssignedTime() {
        return assignedTime;
    }
    
    public void setAssignedTime(LocalDateTime assignedTime) {
        this.assignedTime = assignedTime;
    }
    
    public LocalDateTime getCompletedTime() {
        return completedTime;
    }
    
    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }
    
    public Taxi getAssignedTaxi() {
        return assignedTaxi;
    }
    
    public void setAssignedTaxi(Taxi assignedTaxi) {
        this.assignedTaxi = assignedTaxi;
    }
    
    public RideStatus getStatus() {
        return status;
    }
    
    public void setStatus(RideStatus status) {
        this.status = status;
    }
    
    // Calculate the distance of the ride
    public double calculateDistance() {
        return pickupLocation.distanceTo(dropoffLocation);
    }
    
    // Calculate the fare based on distance (simple formula)
    public double calculateFare() {
        return 5.0 + (calculateDistance() * 2.0); // Base fare + $2 per kilometer
    }
    
    @Override
    public String toString() {
        return customerName + " - From: " + pickupLocation + " To: " + dropoffLocation + " (" + status + ")";
    }
} 