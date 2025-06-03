package com.quickride.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.quickride.exception.NoTaxiAvailableException;
import com.quickride.manager.RideManager;
import com.quickride.manager.TaxiManager;
import com.quickride.model.Location;
import com.quickride.model.Ride;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Request Ride View
 * Handles ride booking functionality with improved UI/UX
 */
public class RequestRideController implements Initializable {

    // Static map for known locations and their coordinates
    private static final Map<String, double[]> KNOWN_LOCATIONS = new HashMap<>();
    static {
        KNOWN_LOCATIONS.put("Zürich Hauptbahnhof", new double[]{47.3779, 8.5403});
        KNOWN_LOCATIONS.put("Zürich Airport", new double[]{47.4647, 8.5492});
        KNOWN_LOCATIONS.put("ETH Zurich", new double[]{47.3769, 8.5417});
        KNOWN_LOCATIONS.put("University of Zurich", new double[]{47.3739, 8.5494});
        KNOWN_LOCATIONS.put("Bahnhofstrasse", new double[]{47.3689, 8.5394});
        KNOWN_LOCATIONS.put("Zurich Old Town", new double[]{47.3717, 8.5422});
        KNOWN_LOCATIONS.put("Lake Zurich", new double[]{47.3667, 8.5500});
        KNOWN_LOCATIONS.put("Uetliberg", new double[]{47.3492, 8.4914});
    }

    // Phone number validation pattern
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    // FXML UI Components
    @FXML private Button backButton;
    @FXML private ProgressIndicator loadingIndicator;
    
    // Location inputs
    @FXML private ComboBox<String> pickupLocationComboBox;
    @FXML private ComboBox<String> dropoffLocationComboBox;
    
    // Passenger details
    @FXML private TextField customerNameField;
    @FXML private TextField phoneNumberField;
    
    // Ride type buttons
    @FXML private Button economyButton;
    @FXML private Button comfortButton;
    @FXML private Button premiumButton;
    
    // Individual price/ETA labels for each ride type
    @FXML private Label economyPrice;
    @FXML private Label economyEta;
    @FXML private Label comfortPrice;
    @FXML private Label comfortEta;
    @FXML private Label premiumPrice;
    @FXML private Label premiumEta;
    
    // Options
    @FXML private CheckBox nearestTaxiCheckBox;
    
    // Action button and fare display
    @FXML private Button requestButton;
    @FXML private Label fareEstimateLabel;
    @FXML private Label etaEstimateLabel;

    // Dependencies
    private RideManager rideManager;
    
    // State
    private String selectedRideType = "Economy"; // Default selection
    private Stage stage;

    /**
     * Default constructor required by FXML loader.
     */
    public RequestRideController() {
        // Default constructor
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupLocationComboBoxes();
        setupValidation();
        updateAllFareEstimates();
        
        // Set initial loading state
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
        
        // Add listeners for dynamic fare updates
        if (pickupLocationComboBox != null) {
            pickupLocationComboBox.setOnAction(e -> updateAllFareEstimates());
        }
        if (dropoffLocationComboBox != null) {
            dropoffLocationComboBox.setOnAction(e -> updateAllFareEstimates());
        }
    }

    /**
     * Setup location combo boxes with available locations
     */
    private void setupLocationComboBoxes() {
        if (pickupLocationComboBox != null) {
            pickupLocationComboBox.getItems().addAll(
                "Current Location", 
                "Zürich Hauptbahnhof", 
                "Zürich Airport", 
                "ETH Zurich",
                "University of Zurich", 
                "Bahnhofstrasse", 
                "Zurich Old Town",
                "Lake Zurich", 
                "Uetliberg"
            );
            pickupLocationComboBox.setValue("Current Location");
        }

        if (dropoffLocationComboBox != null) {
            dropoffLocationComboBox.getItems().addAll(
                "Zürich Hauptbahnhof", 
                "Zürich Airport", 
                "ETH Zurich",
                "University of Zurich", 
                "Bahnhofstrasse", 
                "Zurich Old Town",
                "Lake Zurich", 
                "Uetliberg"
            );
        }
    }

    /**
     * Setup input validation with visual feedback
     */
    private void setupValidation() {
        // Add real-time phone number validation
        if (phoneNumberField != null) {
            phoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.trim().isEmpty()) {
                    if (PHONE_PATTERN.matcher(newValue).matches()) {
                        phoneNumberField.setStyle("-fx-border-color: #34C759;"); // Green for valid
                    } else {
                        phoneNumberField.setStyle("-fx-border-color: #FF3B30;"); // Red for invalid
                    }
                } else {
                    phoneNumberField.setStyle(""); // Reset to default
                }
            });
        }
        
        // Add name validation
        if (customerNameField != null) {
            customerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.trim().length() >= 2) {
                    customerNameField.setStyle("-fx-border-color: #34C759;"); // Green for valid
                } else {
                    customerNameField.setStyle(""); // Reset to default
                }
            });
        }
    }

    /**
     * Handle ride type selection with improved visual feedback
     */
    @FXML
    public void handleRideTypeSelection(javafx.event.ActionEvent event) {
        // Reset all buttons
        resetRideTypeButtons();
        
        // Set active button and update selected type
        Button source = (Button) event.getSource();
        source.getStyleClass().add("active");
        
        if (source == economyButton) {
            selectedRideType = "Economy";
        } else if (source == comfortButton) {
            selectedRideType = "Comfort";
        } else if (source == premiumButton) {
            selectedRideType = "Premium";
        }
        
        updateMainFareEstimate();
    }

    /**
     * Reset all ride type buttons to inactive state
     */
    private void resetRideTypeButtons() {
        if (economyButton != null) economyButton.getStyleClass().remove("active");
        if (comfortButton != null) comfortButton.getStyleClass().remove("active");
        if (premiumButton != null) premiumButton.getStyleClass().remove("active");
    }

    /**
     * Update fare estimates for all ride types
     */
    private void updateAllFareEstimates() {
        String pickup = pickupLocationComboBox != null ? pickupLocationComboBox.getValue() : null;
        String dropoff = dropoffLocationComboBox != null ? dropoffLocationComboBox.getValue() : null;
        
        if (pickup == null || dropoff == null || pickup.equals(dropoff)) {
            return; // Skip if invalid selection
        }
        
        // Calculate base distance/fare (simplified)
        double distance = calculateDistance(pickup, dropoff);
        
        // Update individual ride type prices and ETAs
        updateRideTypePricing("Economy", distance, economyPrice, economyEta);
        updateRideTypePricing("Comfort", distance, comfortPrice, comfortEta);
        updateRideTypePricing("Premium", distance, premiumPrice, premiumEta);
        
        // Update main fare display
        updateMainFareEstimate();
    }

    /**
     * Update pricing for a specific ride type
     */
    private void updateRideTypePricing(String rideType, double distance, Label priceLabel, Label etaLabel) {
        if (priceLabel == null || etaLabel == null) return;
        
        double baseFare = Math.max(8.0, distance * 2.5); // Minimum CHF 8
        double multiplier = getRideTypeMultiplier(rideType);
        double fare = baseFare * multiplier;
        int eta = Math.max(5, (int)(distance * 2)); // Minimum 5 minutes
        
        priceLabel.setText(String.format("CHF %.0f-%.0f", fare, fare * 1.2));
        etaLabel.setText(eta + " min");
    }

    /**
     * Get pricing multiplier for ride type
     */
    private double getRideTypeMultiplier(String rideType) {
        return switch (rideType) {
            case "Economy" -> 1.0;
            case "Comfort" -> 1.4;
            case "Premium" -> 1.8;
            default -> 1.0;
        };
    }

    /**
     * Update the main fare estimate display based on selected ride type
     */
    private void updateMainFareEstimate() {
        String pickup = pickupLocationComboBox != null ? pickupLocationComboBox.getValue() : null;
        String dropoff = dropoffLocationComboBox != null ? dropoffLocationComboBox.getValue() : null;
        
        if (pickup == null || dropoff == null || pickup.equals(dropoff)) {
            if (fareEstimateLabel != null) fareEstimateLabel.setText("Select locations");
            if (etaEstimateLabel != null) etaEstimateLabel.setText("--");
            return;
        }
        
        double distance = calculateDistance(pickup, dropoff);
        double baseFare = Math.max(8.0, distance * 2.5);
        double multiplier = getRideTypeMultiplier(selectedRideType);
        double fare = baseFare * multiplier;
        int eta = Math.max(5, (int)(distance * 2));
        
        if (fareEstimateLabel != null) {
            fareEstimateLabel.setText(String.format("CHF %.0f-%.0f", fare, fare * 1.2));
        }
        if (etaEstimateLabel != null) {
            etaEstimateLabel.setText(eta + " min");
        }
    }

    /**
     * Calculate distance between two locations (simplified)
     */
    private double calculateDistance(String pickup, String dropoff) {
        // For "Current Location", use Zürich HB as default
        if ("Current Location".equals(pickup)) {
            pickup = "Zürich Hauptbahnhof";
        }
        
        double[] pickupCoords = KNOWN_LOCATIONS.getOrDefault(pickup, new double[]{47.3779, 8.5403});
        double[] dropoffCoords = KNOWN_LOCATIONS.getOrDefault(dropoff, new double[]{47.3779, 8.5403});
        
        // Simple distance calculation (not accurate, for demo purposes)
        double latDiff = pickupCoords[0] - dropoffCoords[0];
        double lonDiff = pickupCoords[1] - dropoffCoords[1];
        double distance = Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 100; // Rough conversion to km
        
        return Math.max(2.0, distance); // Minimum 2km
    }

    /**
     * Handle ride request with improved validation and error handling
     */
    @FXML
    public void handleRequestRide() {
        if (!validateInputs()) {
            return;
        }
        
        setLoadingState(true);
        
        // Create task for background processing
        Task<Void> rideRequestTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Simulate processing time
                    Thread.sleep(1500);
                    
                    // Create ride request
                    createRideRequest();
                    
                    Platform.runLater(() -> {
                        showSuccessAlert();
                        closeWindow();
                    });
                } catch (NoTaxiAvailableException e) {
                    Platform.runLater(() -> {
                        showErrorAlert("No taxi available: " + e.getMessage());
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> {
                        showErrorAlert("Request was interrupted");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showErrorAlert("Failed to request ride: " + e.getMessage());
                    });
                } finally {
                    Platform.runLater(() -> setLoadingState(false));
                }
                return null;
            }
        };
        
        new Thread(rideRequestTask).start();
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();
        
        // Validate locations
        if (pickupLocationComboBox == null || pickupLocationComboBox.getValue() == null) {
            errors.append("Please select a pickup location.\n");
        }
        if (dropoffLocationComboBox == null || dropoffLocationComboBox.getValue() == null) {
            errors.append("Please select a dropoff location.\n");
        }
        if (pickupLocationComboBox != null && dropoffLocationComboBox != null && 
            pickupLocationComboBox.getValue() != null && dropoffLocationComboBox.getValue() != null &&
            pickupLocationComboBox.getValue().equals(dropoffLocationComboBox.getValue())) {
            errors.append("Pickup and dropoff locations cannot be the same.\n");
        }
        
        // Validate customer name
        if (customerNameField == null || customerNameField.getText().trim().length() < 2) {
            errors.append("Please enter a valid name (at least 2 characters).\n");
        }
        
        // Validate phone number
        if (phoneNumberField == null || !PHONE_PATTERN.matcher(phoneNumberField.getText().trim()).matches()) {
            errors.append("Please enter a valid phone number (e.g., +41 XX XXX XX XX).\n");
        }
        
        if (errors.length() > 0) {
            showErrorAlert(errors.toString());
            return false;
        }
        
        return true;
    }

    /**
     * Create the actual ride request
     */
    private void createRideRequest() throws NoTaxiAvailableException {
        String pickup = pickupLocationComboBox.getValue();
        String dropoff = dropoffLocationComboBox.getValue();
        String customerName = customerNameField.getText().trim();
        String customerPhoneNumber = phoneNumberField.getText().trim();
        boolean useNearestTaxi = nearestTaxiCheckBox != null && nearestTaxiCheckBox.isSelected();
        
        // Convert pickup location to coordinates
        double[] pickupCoords = KNOWN_LOCATIONS.getOrDefault(
            "Current Location".equals(pickup) ? "Zürich Hauptbahnhof" : pickup,
            new double[]{47.3779, 8.5403}
        );
        
        double[] dropoffCoords = KNOWN_LOCATIONS.getOrDefault(dropoff, new double[]{47.3779, 8.5403});
        
        Location pickupLocation = new Location(pickupCoords[0], pickupCoords[1], pickup);
        Location dropoffLocation = new Location(dropoffCoords[0], dropoffCoords[1], dropoff);
        
        // Create ride using correct constructor
        new Ride(customerName, pickupLocation, dropoffLocation);
        
        // Store additional properties for future use
        System.out.println("Phone: " + customerPhoneNumber + ", Ride Type: " + selectedRideType);
        
        if (rideManager != null) {
            rideManager.requestRide(customerName, pickupLocation, dropoffLocation, useNearestTaxi);
        }
    }

    /**
     * Set loading state for UI
     */
    private void setLoadingState(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
        if (requestButton != null) {
            requestButton.setDisable(loading);
            requestButton.setText(loading ? "REQUESTING..." : "REQUEST RIDE");
        }
    }

    /**
     * Show success alert
     */
    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ride Requested");
        alert.setHeaderText("Success!");
        alert.setContentText("Your ride has been requested successfully. You will be contacted shortly.");
        alert.showAndWait();
    }

    /**
     * Show error alert
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Request Failed");
        alert.setHeaderText("Unable to Request Ride");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handle cancel/back button
     */
    @FXML
    public void handleCancel() {
        closeWindow();
    }

    /**
     * Close the window
     */
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        } else if (backButton != null) {
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();
        }
    }

    // Setters for dependency injection
    public void setRideManager(RideManager rideManager) {
        this.rideManager = rideManager;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
} 