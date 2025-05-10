package com.quickride.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.quickride.exception.InvalidTaxiException;
import com.quickride.exception.NoTaxiAvailableException;
import com.quickride.model.Location;
import com.quickride.model.Ride;
import com.quickride.model.RideStatus;
import com.quickride.model.Taxi;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Manager class for ride operations
 * Demonstrates different data structures and their time complexities
 */
public class RideManager {
    // List to store all rides - demonstrate ordered collection
    private final List<Ride> allRides;
    
    // Map to store rides by status - demonstrate map usage for O(1) lookup by key
    private final Map<RideStatus, List<Ride>> ridesByStatus;
    
    // Reference to the taxi manager
    private final TaxiManager taxiManager;
    
    // Observable list for JavaFX UI binding
    private final ObservableList<Ride> observableRides;
    
    public RideManager(TaxiManager taxiManager) {
        this.taxiManager = taxiManager;
        this.allRides = new ArrayList<>();
        this.ridesByStatus = new HashMap<>();
        
        // Initialize the lists for each status
        for (RideStatus status : RideStatus.values()) {
            ridesByStatus.put(status, new ArrayList<>());
        }
        
        this.observableRides = FXCollections.observableArrayList();
    }
    
    // Request a new ride - O(1) or O(n) depending on taxi selection method
    public Ride requestRide(String customerName, Location pickupLocation, Location dropoffLocation, 
                           boolean useNearestTaxi) throws NoTaxiAvailableException {
        // Create a new ride
        Ride ride = new Ride(customerName, pickupLocation, dropoffLocation);
        
        // Get a taxi (either next in queue or nearest)
        Taxi assignedTaxi;
        if (useNearestTaxi) {
            assignedTaxi = taxiManager.getNearestAvailableTaxi(pickupLocation);
        } else {
            assignedTaxi = taxiManager.getNextAvailableTaxi();
        }
        
        // Assign taxi to ride
        ride.setAssignedTaxi(assignedTaxi);
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedTime(LocalDateTime.now());
        
        // Add to collections
        allRides.add(ride);
        ridesByStatus.get(RideStatus.ASSIGNED).add(ride);
        
        // Update observable list
        updateObservableList();
        
        return ride;
    }
    
    // Start a ride - O(1)
    public void startRide(Ride ride) {
        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new IllegalStateException("Ride must be in ASSIGNED status to start");
        }
        
        // Update ride status
        ridesByStatus.get(ride.getStatus()).remove(ride);
        ride.setStatus(RideStatus.IN_PROGRESS);
        ridesByStatus.get(RideStatus.IN_PROGRESS).add(ride);
        
        // Update observable list
        updateObservableList();
    }
    
    // Complete a ride - O(n)
    public void completeRide(Ride ride) {
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new IllegalStateException("Ride must be in IN_PROGRESS status to complete");
        }
        
        // Update ride status
        ridesByStatus.get(ride.getStatus()).remove(ride);
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedTime(LocalDateTime.now());
        ridesByStatus.get(RideStatus.COMPLETED).add(ride);
        
        // Release the taxi back to the available pool
        try {
            taxiManager.releaseTaxi(ride.getAssignedTaxi());
        } catch (IllegalArgumentException e) {
            // Log error specifically for invalid taxi
            System.err.println("Invalid taxi during release: " + e.getMessage());
        } catch (IllegalStateException e) {
            // Log error specifically for taxi state issues
            System.err.println("Taxi state error during release: " + e.getMessage());
        } catch (InvalidTaxiException e) {
            // Log error for taxi validation issues
            System.err.println("Invalid taxi exception during release: " + e.getMessage());
        }
        
        // Update observable list
        updateObservableList();
    }
    
    // Cancel a ride - O(n)
    public void cancelRide(Ride ride) {
        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel a completed or already cancelled ride");
        }
        
        // Update ride status
        ridesByStatus.get(ride.getStatus()).remove(ride);
        ride.setStatus(RideStatus.CANCELLED);
        ridesByStatus.get(RideStatus.CANCELLED).add(ride);
        
        // Release the taxi back to the available pool if it was assigned
        if (ride.getAssignedTaxi() != null) {
            try {
                taxiManager.releaseTaxi(ride.getAssignedTaxi());
            } catch (IllegalArgumentException e) {
                // Log error specifically for invalid taxi
                System.err.println("Invalid taxi during release: " + e.getMessage());
            } catch (IllegalStateException e) {
                // Log error specifically for taxi state issues
                System.err.println("Taxi state error during release: " + e.getMessage());
            } catch (InvalidTaxiException e) {
                // Log error for taxi validation issues
                System.err.println("Invalid taxi exception during release: " + e.getMessage());
            }
        }
        
        // Update observable list
        updateObservableList();
    }
    
    // Get a ride by ID - O(n)
    public Ride getRideById(String id) {
        for (Ride ride : allRides) {
            if (ride.getId().equals(id)) {
                return ride;
            }
        }
        return null;
    }
    
    // Get rides by status - O(1)
    public List<Ride> getRidesByStatus(RideStatus status) {
        return new ArrayList<>(ridesByStatus.get(status));
    }
    
    // Update observable list for UI
    private void updateObservableList() {
        observableRides.setAll(allRides);
    }
    
    // Get observable list for UI binding
    public ObservableList<Ride> getObservableRides() {
        return observableRides;
    }
    
    // Get statistics
    public int getTotalRideCount() {
        return allRides.size();
    }
    
    public int getRideCountByStatus(RideStatus status) {
        return ridesByStatus.get(status).size();
    }
    
    // Update ride status - general method to handle any status change
    public void updateRideStatus(Ride ride, RideStatus newStatus) {
        if (ride == null) {
            throw new IllegalArgumentException("Ride cannot be null");
        }
        
        // Validate state transition
        validateStatusTransition(ride.getStatus(), newStatus);
        
        // Update collections
        ridesByStatus.get(ride.getStatus()).remove(ride);
        ride.setStatus(newStatus);
        ridesByStatus.get(newStatus).add(ride);
        
        // Handle specific transition side effects
        switch (newStatus) {
            case ASSIGNED -> {
                ride.setAssignedTime(LocalDateTime.now());
            }
            case IN_PROGRESS -> {
                // Start the ride
            }
            case COMPLETED -> {
                ride.setCompletedTime(LocalDateTime.now());
                
                // Release the taxi back to the available pool
                releaseAssignedTaxi(ride);
            }
            case CANCELLED -> {
                // Release the taxi if one was assigned
                if (ride.getAssignedTaxi() != null) {
                    releaseAssignedTaxi(ride);
                }
            }
            default -> { /* No action needed */ }
        }
        
        // Update observable list
        updateObservableList();
    }
    
    // Helper method to validate status transitions
    private void validateStatusTransition(RideStatus currentStatus, RideStatus newStatus) {
        switch (newStatus) {
            case REQUESTED -> 
                // Can only be set during creation
                throw new IllegalStateException("Cannot change ride status back to REQUESTED");
            case ASSIGNED -> {
                if (currentStatus != RideStatus.REQUESTED) {
                    throw new IllegalStateException("Only REQUESTED rides can be ASSIGNED");
                }
            }
            case IN_PROGRESS -> {
                if (currentStatus != RideStatus.ASSIGNED) {
                    throw new IllegalStateException("Only ASSIGNED rides can be started (IN_PROGRESS)");
                }
            }
            case COMPLETED -> {
                if (currentStatus != RideStatus.IN_PROGRESS) {
                    throw new IllegalStateException("Only IN_PROGRESS rides can be COMPLETED");
                }
            }
            case CANCELLED -> {
                if (currentStatus == RideStatus.COMPLETED || currentStatus == RideStatus.CANCELLED) {
                    throw new IllegalStateException("Cannot cancel a COMPLETED or already CANCELLED ride");
                }
            }
            default -> throw new IllegalStateException("Unknown ride status: " + newStatus);
        }
    }
    
    // Helper method to handle taxi release with proper error handling
    private void releaseAssignedTaxi(Ride ride) {
        try {
            taxiManager.releaseTaxi(ride.getAssignedTaxi());
        } catch (IllegalArgumentException e) {
            // Log error specifically for invalid taxi
            System.err.println("Invalid taxi during release: " + e.getMessage());
        } catch (IllegalStateException e) {
            // Log error specifically for taxi state issues
            System.err.println("Taxi state error during release: " + e.getMessage());
        } catch (InvalidTaxiException e) {
            // Log error for taxi validation issues
            System.err.println("Invalid taxi exception during release: " + e.getMessage());
        }
    }
} 