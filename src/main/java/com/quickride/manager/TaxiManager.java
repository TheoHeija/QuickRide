package com.quickride.manager;

import com.quickride.exception.InvalidTaxiException;
import com.quickride.exception.NoTaxiAvailableException;
import com.quickride.model.Location;
import com.quickride.model.Taxi;

import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Manager class for taxi operations
 * Demonstrates different data structures and their time complexities
 */
public class TaxiManager {
    // Use Queue for available taxis - demonstrate FIFO processing
    private final Queue<Taxi> availableTaxis;
    
    // Use ArrayList for assigned taxis - demonstrate dynamic arrays
    private final List<Taxi> assignedTaxis;
    
    // Use HashSet for all taxis - demonstrate unique collection with O(1) lookup
    private final Set<Taxi> allTaxis;
    
    // Observable lists for JavaFX UI binding
    private final ObservableList<Taxi> observableAvailableTaxis;
    private final ObservableList<Taxi> observableAssignedTaxis;
    
    public TaxiManager() {
        this.availableTaxis = new LinkedList<>();
        this.assignedTaxis = new ArrayList<>();
        this.allTaxis = new HashSet<>();
        
        this.observableAvailableTaxis = FXCollections.observableArrayList();
        this.observableAssignedTaxis = FXCollections.observableArrayList();
    }
    
    // Add a new taxi to the system - O(1)
    public void addTaxi(Taxi taxi) throws InvalidTaxiException {
        // Validate taxi
        if (taxi == null) {
            throw new InvalidTaxiException("Taxi cannot be null");
        }
        
        if (taxi.getDriverName() == null || taxi.getDriverName().trim().isEmpty()) {
            throw new InvalidTaxiException("Driver name cannot be empty");
        }
        
        if (taxi.getLicensePlate() == null || taxi.getLicensePlate().trim().isEmpty()) {
            throw new InvalidTaxiException("License plate cannot be empty");
        }
        
        // Check if taxi with same license plate already exists
        for (Taxi existingTaxi : allTaxis) {
            if (existingTaxi.getLicensePlate().equals(taxi.getLicensePlate())) {
                throw new InvalidTaxiException("A taxi with license plate " + 
                                               taxi.getLicensePlate() + " already exists");
            }
        }
        
        // Add to collections
        allTaxis.add(taxi);
        availableTaxis.add(taxi);
        
        // Update observable lists
        updateObservableLists();
    }
    
    // Get the next available taxi - O(1)
    public Taxi getNextAvailableTaxi() throws NoTaxiAvailableException {
        if (availableTaxis.isEmpty()) {
            throw new NoTaxiAvailableException();
        }
        
        Taxi taxi = availableTaxis.poll(); // Remove from queue
        taxi.setAvailable(false);
        assignedTaxis.add(taxi);
        
        // Update observable lists
        updateObservableLists();
        
        return taxi;
    }
    
    // Get the nearest available taxi to a location - O(n)
    public Taxi getNearestAvailableTaxi(Location location) throws NoTaxiAvailableException {
        if (availableTaxis.isEmpty()) {
            throw new NoTaxiAvailableException();
        }
        
        Taxi nearestTaxi = null;
        double minDistance = Double.MAX_VALUE;
        
        // Find the nearest taxi - O(n) where n is the number of available taxis
        for (Taxi taxi : availableTaxis) {
            double distance = taxi.getCurrentLocation().distanceTo(location);
            if (distance < minDistance) {
                minDistance = distance;
                nearestTaxi = taxi;
            }
        }
        
        // Remove from available queue and add to assigned list
        if (nearestTaxi != null) {
            availableTaxis.remove(nearestTaxi);
            nearestTaxi.setAvailable(false);
            assignedTaxis.add(nearestTaxi);
            
            // Update observable lists
            updateObservableLists();
            
            return nearestTaxi;
        } else {
            throw new NoTaxiAvailableException("Failed to find nearest taxi");
        }
    }
    
    // Release a taxi back to available pool - O(n)
    public void releaseTaxi(Taxi taxi) throws InvalidTaxiException {
        if (taxi == null) {
            throw new InvalidTaxiException("Taxi cannot be null");
        }
        
        if (!assignedTaxis.contains(taxi)) {
            throw new InvalidTaxiException("Taxi is not currently assigned");
        }
        
        assignedTaxis.remove(taxi);
        taxi.setAvailable(true);
        availableTaxis.add(taxi);
        
        // Update observable lists
        updateObservableLists();
    }
    
    // Get a taxi by ID - O(n)
    public Taxi getTaxiById(String id) {
        for (Taxi taxi : allTaxis) {
            if (taxi.getId().equals(id)) {
                return taxi;
            }
        }
        return null;
    }
    
    // Update observable lists for UI
    private void updateObservableLists() {
        observableAvailableTaxis.setAll(availableTaxis);
        observableAssignedTaxis.setAll(assignedTaxis);
    }
    
    // Get observable lists for UI binding
    public ObservableList<Taxi> getObservableAvailableTaxis() {
        return observableAvailableTaxis;
    }
    
    public ObservableList<Taxi> getObservableAssignedTaxis() {
        return observableAssignedTaxis;
    }
    
    // Get counts for statistics
    public int getAvailableTaxiCount() {
        return availableTaxis.size();
    }
    
    public int getAssignedTaxiCount() {
        return assignedTaxis.size();
    }
    
    public int getTotalTaxiCount() {
        return allTaxis.size();
    }
} 