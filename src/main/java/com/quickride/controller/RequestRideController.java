package com.quickride.controller;

import com.quickride.exception.NoTaxiAvailableException;
import com.quickride.manager.RideManager;
import com.quickride.model.Location;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Request Ride dialog
 */
@SuppressWarnings("unused")
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
    private ComboBox<String> pickupLocationComboBox;
    
    @FXML
    private ComboBox<String> dropoffLocationComboBox;
    
    @FXML
    private CheckBox nearestTaxiCheckBox;
    
    @FXML
    private Button requestButton;
    
    @FXML
    private Button backButton;
    
    private RideManager rideManager;
    
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
        
        // Initialize location options
        initializeLocationOptions();
    }
    
    /**
     * Initialize location dropdown options with sample locations
     */
    private void initializeLocationOptions() {
        // Sample pickup locations
        if (pickupLocationComboBox != null) {
            pickupLocationComboBox.getItems().addAll(
                "Current Location",
                "Airport Terminal",
                "Downtown Hotel",
                "Shopping Mall",
                "Central Park",
                "Business District"
            );
            pickupLocationComboBox.setValue("Current Location");
        }
        
        // Sample dropoff locations
        if (dropoffLocationComboBox != null) {
            dropoffLocationComboBox.getItems().addAll(
                "Airport Terminal",
                "Downtown Hotel",
                "Shopping Mall",
                "Central Park",
                "Business District",
                "Suburban Neighborhood"
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
                alert.setContentText("Your ride has been requested and a taxi will be assigned shortly.");
                alert.showAndWait();
                
                // Close the dialog
                closeDialog();
                
            } catch (NoTaxiAvailableException e) {
                // Show specific error for no taxi available
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No Taxi Available");
                alert.setHeaderText("Cannot Request Ride");
                alert.setContentText("No taxis are currently available. Please try again later.");
                alert.showAndWait();
            } catch (IllegalArgumentException e) {
                // Show specific error for invalid input
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText("Cannot Request Ride");
                alert.setContentText("Invalid location format: " + e.getMessage());
                alert.showAndWait();
            } catch (Exception e) {
                // Show error message for other unexpected errors
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
        Stage stage = (Stage) requestButton.getScene().getWindow();
        stage.close();
    }
} 