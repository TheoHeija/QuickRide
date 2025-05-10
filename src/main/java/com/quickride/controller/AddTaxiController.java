package com.quickride.controller;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quickride.exception.InvalidTaxiException;
import com.quickride.manager.TaxiManager;
import com.quickride.model.Location;
import com.quickride.model.Taxi;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Add Taxi dialog
 */
@SuppressWarnings("unused")
public class AddTaxiController {
    private static final Logger LOGGER = Logger.getLogger(AddTaxiController.class.getName());
    
    /**
     * Default constructor required by FXML loader.
     * Must be public for JavaFX to access it.
     */
    public AddTaxiController() {
        // Default constructor required for FXML
    }
    
    private TaxiManager taxiManager;
    private final Random random = new Random();
    
    // San Francisco area coordinates
    private static final double SF_LAT_MIN = 37.7075;
    private static final double SF_LAT_MAX = 37.8075;
    private static final double SF_LON_MIN = -122.5125;
    private static final double SF_LON_MAX = -122.3525;
    
    private static final String[] SF_NEIGHBORHOODS = {
        "Downtown", "Mission District", "SoMa", "Marina", "North Beach", 
        "Castro", "Haight-Ashbury", "Chinatown", "Sunset District", "Richmond District"
    };

    private static final String[] STREET_NAMES = {
        "Market St", "Valencia St", "Van Ness Ave", "Mission St", "Geary Blvd",
        "Fillmore St", "Divisadero St", "Columbus Ave", "Folsom St", "Bryant St"
    };
    
    @FXML
    private TextField driverNameField;
    
    @FXML
    private TextField licensePlateField;
    
    @FXML
    private ComboBox<String> carModelComboBox;
    
    @FXML
    private ComboBox<String> locationComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Populate car model dropdown
        carModelComboBox.setItems(FXCollections.observableArrayList(
            "Toyota Prius", "Honda Civic", "Tesla Model 3", "Ford Fusion", "Nissan Leaf",
            "Chevrolet Bolt", "Hyundai Ioniq", "Kia Niro", "Toyota Camry", "Honda Accord"
        ));
        
        // Populate location dropdown with SF neighborhoods
        locationComboBox.setItems(FXCollections.observableArrayList(Arrays.asList(SF_NEIGHBORHOODS)));
        
        // Default values
        carModelComboBox.getSelectionModel().selectFirst();
        locationComboBox.getSelectionModel().selectFirst();
    }
    
    /**
     * Set the taxi manager reference
     */
    public void setTaxiManager(TaxiManager taxiManager) {
        this.taxiManager = taxiManager;
    }
    
    /**
     * Handle save button click
     */
    @FXML
    private void handleSave() {
        try {
            // Validate inputs
            String driverName = driverNameField.getText().trim();
            String licensePlate = licensePlateField.getText().trim();
            String carModel = carModelComboBox.getValue();
            String neighborhood = locationComboBox.getValue();
            
            if (driverName.isEmpty()) {
                showError("Driver name is required");
                return;
            }
            
            if (licensePlate.isEmpty()) {
                showError("License plate is required");
                return;
            }
            
            if (carModel == null || carModel.isEmpty()) {
                showError("Car model is required");
                return;
            }
            
            if (neighborhood == null || neighborhood.isEmpty()) {
                showError("Location is required");
                return;
            }
            
            // Generate realistic address with coordinates
            String streetName = STREET_NAMES[random.nextInt(STREET_NAMES.length)];
            String streetNumber = (100 + random.nextInt(1900)) + "";
            String address = streetNumber + " " + streetName + ", " + neighborhood + ", San Francisco";
            
            // Generate random coordinates within SF
            double latitude = SF_LAT_MIN + (SF_LAT_MAX - SF_LAT_MIN) * random.nextDouble();
            double longitude = SF_LON_MIN + (SF_LON_MAX - SF_LON_MIN) * random.nextDouble();
            
            // Create location with coordinates
            Location location = new Location(latitude, longitude, address);
            
            // Create and add the taxi
            Taxi newTaxi = new Taxi(driverName, licensePlate, carModel, location);
            taxiManager.addTaxi(newTaxi);
            
            // Show success message
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Taxi Added");
            alert.setHeaderText("Taxi Successfully Added");
            alert.setContentText("Taxi with driver " + driverName + " has been added to the system.");
            alert.showAndWait();
            
            // Close the dialog
            closeDialog();
            
        } catch (InvalidTaxiException e) {
            showError("Could not add taxi: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding taxi", e);
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    /**
     * Close the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Show an error dialog
     */
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error Adding Taxi");
        alert.setContentText(message);
        alert.showAndWait();
    }
} 