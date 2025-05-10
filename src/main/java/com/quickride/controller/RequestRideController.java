package com.quickride.controller;

import com.quickride.exception.NoTaxiAvailableException;
import com.quickride.manager.RideManager;
import com.quickride.model.Location;
import com.quickride.util.RealMapViewer;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Controller for the Request Ride dialog
 */
public class RequestRideController {
    /**
     * Default constructor required by FXML loader.
     * Must be public for JavaFX to access it.
     */
    public RequestRideController() {
        // Default constructor required for FXML
    }

    @FXML
    private TextField customerNameField;
    
    @FXML
    private TextField phoneNumberField;
    
    @FXML
    private ComboBox<String> pickupLocationComboBox;
    
    @FXML
    private ComboBox<String> dropoffLocationComboBox;
    
    @FXML
    private CheckBox nearestTaxiCheckBox;
    
    @FXML
    private CheckBox scheduleRideCheckBox;
    
    @FXML
    private Button requestButton;
    
    @FXML
    private Button economyButton;
    
    @FXML
    private Button comfortButton;
    
    @FXML
    private Button premiumButton;
    
    @FXML
    private StackPane mapContainer;
    
    private RideManager rideManager;
    private RealMapViewer mapViewer;
    private String selectedRideType = "Economy"; // Default ride type
    
    /**
     * Set the ride manager for this controller
     * @param rideManager The ride manager
     */
    public void setRideManager(RideManager rideManager) {
        this.rideManager = rideManager;
    }
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize checkbox
        if (nearestTaxiCheckBox != null) {
            nearestTaxiCheckBox.setSelected(true);
        }
        
        if (scheduleRideCheckBox != null) {
            scheduleRideCheckBox.setSelected(false);
        }
        
        // Initialize location options
        initializeLocationOptions();
        
        // Initialize the map if the container exists
        if (mapContainer != null) {
            setupMapView();
        }
    }
    
    /**
     * Set up the map view
     */
    private void setupMapView() {
        // Create a map viewer in the container
        mapViewer = new RealMapViewer(mapContainer);
        
        // Center the map on Switzerland
        mapViewer.centerMap(46.8182, 8.2275, 8);
    }
    
    /**
     * Handle ride type selection
     */
    @FXML
    public void handleRideTypeSelection() {
        // Reset all buttons first
        economyButton.getStyleClass().remove("active");
        comfortButton.getStyleClass().remove("active");
        premiumButton.getStyleClass().remove("active");
        
        // Determine which button was clicked and update selection
        if (economyButton.isFocused()) {
            economyButton.getStyleClass().add("active");
            selectedRideType = "Economy";
            updateFareEstimate(12, 15);
        } else if (comfortButton.isFocused()) {
            comfortButton.getStyleClass().add("active");
            selectedRideType = "Comfort";
            updateFareEstimate(18, 22);
        } else if (premiumButton.isFocused()) {
            premiumButton.getStyleClass().add("active");
            selectedRideType = "Premium";
            updateFareEstimate(25, 30);
        }
    }
    
    /**
     * Update the fare estimate based on ride type
     */
    private void updateFareEstimate(int minFare, int maxFare) {
        // This would update the fare estimate label text
        // Since we're just setting text in the FXML file for now, this is a placeholder
        System.out.println("Estimated fare: CHF " + minFare + " - " + maxFare);
    }
    
    /**
     * Initialize location dropdown options with Swiss locations
     */
    private void initializeLocationOptions() {
        // Sample Swiss pickup locations
        if (pickupLocationComboBox != null) {
            pickupLocationComboBox.getItems().addAll(
                "Current Location",
                "Zürich Hauptbahnhof",
                "Zürich Airport",
                "Bern Bahnhof",
                "Lausanne Gare",
                "Geneva Airport",
                "Basel SBB",
                "Lucerne Station"
            );
            pickupLocationComboBox.setValue("Current Location");
        }
        
        // Sample Swiss dropoff locations
        if (dropoffLocationComboBox != null) {
            dropoffLocationComboBox.getItems().addAll(
                "Zürich Hauptbahnhof",
                "Zürich Airport",
                "Bern Bahnhof",
                "Lausanne Gare",
                "Geneva Airport", 
                "Basel SBB",
                "Lucerne Station",
                "Interlaken Ost"
            );
        }
    }
    
    /**
     * Handle request button click
     */
    @FXML
    public void handleRequestRide() {
        // Validate fields
        if (validateFields()) {
            try {
                // Determine if we should use nearest taxi
                boolean useNearest = nearestTaxiCheckBox != null ? 
                    nearestTaxiCheckBox.isSelected() : true;
                
                // Get pickup and dropoff locations
                String pickup = pickupLocationComboBox.getValue();
                String dropoff = dropoffLocationComboBox.getValue();
                
                // Log ride details
                System.out.println("Requesting ride with: Customer=" + customerNameField.getText().trim() + 
                                  ", Phone=" + (phoneNumberField != null ? phoneNumberField.getText() : "N/A") +
                                  ", Pickup=" + pickup + ", Dropoff=" + dropoff + 
                                  ", RideType=" + selectedRideType +
                                  ", UseNearest=" + useNearest +
                                  ", Scheduled=" + (scheduleRideCheckBox != null && scheduleRideCheckBox.isSelected()));
                
                // Check if ride manager is properly set
                if (rideManager == null) {
                    throw new IllegalStateException("Ride manager is not initialized");
                }
                
                // Create new ride
                rideManager.requestRide(
                    customerNameField.getText().trim(),
                    new Location(pickup),
                    new Location(dropoff),
                    useNearest
                );
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ride Requested");
                alert.setHeaderText("Ride Successfully Requested");
                
                String confirmationMessage = "Your " + selectedRideType + " ride has been requested and a taxi will arrive shortly.";
                if (scheduleRideCheckBox != null && scheduleRideCheckBox.isSelected()) {
                    confirmationMessage = "Your " + selectedRideType + " ride has been scheduled.";
                }
                
                alert.setContentText(confirmationMessage);
                alert.showAndWait();
                
                // Close the dialog
                closeDialog();
                
            } catch (NoTaxiAvailableException e) {
                // Show specific error for no taxi available
                System.err.println("NoTaxiAvailableException: " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No Taxi Available");
                alert.setHeaderText("Cannot Request Ride");
                alert.setContentText("No taxis are currently available. Please try again later.");
                alert.showAndWait();
            } catch (IllegalArgumentException e) {
                // Show specific error for invalid input
                System.err.println("IllegalArgumentException: " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText("Cannot Request Ride");
                alert.setContentText("Invalid input: " + e.getMessage());
                alert.showAndWait();
            } catch (IllegalStateException e) {
                // Show specific error for state issues
                System.err.println("IllegalStateException: " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("System Error");
                alert.setHeaderText("Cannot Request Ride");
                alert.setContentText("System error: " + e.getMessage());
                alert.showAndWait();
            } catch (Exception e) {
                // Show error message for other unexpected errors
                System.err.println("Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Request Error");
                alert.setHeaderText("Failed to request ride");
                alert.setContentText("Unexpected error: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    /**
     * Validate form fields
     * @return true if fields are valid
     */
    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (customerNameField.getText().trim().isEmpty()) {
            errorMessage.append("Customer name is required\n");
        }
        
        if (phoneNumberField != null && phoneNumberField.getText().trim().isEmpty()) {
            errorMessage.append("Phone number is required\n");
        }
        
        if (pickupLocationComboBox.getValue() == null || pickupLocationComboBox.getValue().trim().isEmpty()) {
            errorMessage.append("Pickup location is required\n");
        }
        
        if (dropoffLocationComboBox.getValue() == null || dropoffLocationComboBox.getValue().trim().isEmpty()) {
            errorMessage.append("Drop-off location is required\n");
        }
        
        if (errorMessage.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    /**
     * Handle cancel button click
     */
    @FXML
    public void handleCancel() {
        closeDialog();
    }
    
    /**
     * Close the dialog
     */
    private void closeDialog() {
        if (mapViewer != null) {
            mapViewer.dispose();
        }
        Stage stage = (Stage) requestButton.getScene().getWindow();
        stage.close();
    }
} 