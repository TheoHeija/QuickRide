package com.quickride.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.quickride.exception.InvalidTaxiException;
import com.quickride.manager.RideManager;
import com.quickride.manager.TaxiManager;
import com.quickride.model.Location;
import com.quickride.model.Ride;
import com.quickride.model.RideStatus;
import com.quickride.model.Taxi;
import com.quickride.util.RealMapViewer;
import com.quickride.util.TaxiFactory;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for the main view
 */
@SuppressWarnings("unused")
public class MainController {
    
    /**
     * Default constructor required by FXML loader.
     * Must be public for JavaFX to access it.
     */
    public MainController() {
        // Default constructor required for FXML
    }
    
    private final TaxiManager taxiManager = new TaxiManager();
    private final RideManager rideManager = new RideManager(taxiManager);
    
    @FXML
    private TextField pickupField;
    
    @FXML
    private TextField dropoffField;
    
    @FXML
    private Button requestRideButton;
    
    @FXML
    private Button addTaxiButton;
    
    @FXML
    private TableView<Taxi> availableTaxisTable;
    
    @FXML
    private TableColumn<Taxi, String> availDriverColumn;
    
    @FXML
    private TableColumn<Taxi, String> availLocationColumn;
    
    @FXML
    private TableView<Ride> ridesTable;
    
    @FXML
    private TableColumn<Ride, String> customerColumn;
    
    @FXML
    private TableColumn<Ride, RideStatus> statusColumn;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private StackPane mapContainer;
    
    @FXML
    private Button satelliteViewButton;
    
    @FXML
    private Button streetViewButton;
    
    @FXML
    private Button centerMapButton;
    
    private RealMapViewer mapViewer;
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        System.out.println("MainController initialized");
        
        // Initialize tables
        setupTaxiTable();
        setupRidesTable();
        setupRideContextMenu();
        
        // Make sure map container has proper size
        if (mapContainer != null) {
            mapContainer.setMinSize(400, 300);
            mapContainer.setPrefSize(600, 400);
            mapContainer.getStyleClass().add("map-container");
        }
        
        // Setup map rendering
        setupMapRendering();
        
        // Setup map control buttons if they exist
        setupMapControls();
        
        // Add some sample data for demo
        addDemoData();
        
        // Update status bar
        updateStatusBar();
    }
    
    private void setupTaxiTable() {
        availDriverColumn.setCellValueFactory(new PropertyValueFactory<>("driverName"));
        availLocationColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCurrentLocation().getAddress()));
        
        availableTaxisTable.setItems(taxiManager.getObservableAvailableTaxis());
    }
    
    private void setupRidesTable() {
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Add custom cell factory for status column
        statusColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Ride, RideStatus>() {
            @Override
            protected void updateItem(RideStatus status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Create a label with appropriate style class
                    Label statusLabel = new Label(status.toString());
                    statusLabel.getStyleClass().add("status-indicator");
                    
                    // Add specific status class based on status
                    String styleClass = switch(status) {
                        case REQUESTED -> "status-requested";
                        case ASSIGNED -> "status-assigned";
                        case IN_PROGRESS -> "status-in-progress";
                        case COMPLETED -> "status-completed";
                        case CANCELLED -> "status-cancelled";
                    };
                    statusLabel.getStyleClass().add(styleClass);
                    
                    setGraphic(statusLabel);
                    setText(null);
                    setStyle("");
                }
            }
        });
        
        // Add context menu for ride management
        setupRideContextMenu();
        
        ridesTable.setItems(rideManager.getObservableRides());
    }
    
    /**
     * Sets up the context menu for ride management
     */
    private void setupRideContextMenu() {
        // Create context menu and items
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem startRideItem = new MenuItem("Start Ride");
        MenuItem completeRideItem = new MenuItem("Complete Ride");
        MenuItem cancelRideItem = new MenuItem("Cancel Ride");
        
        // Add handlers
        startRideItem.setOnAction(event -> handleRideAction(RideStatus.IN_PROGRESS));
        completeRideItem.setOnAction(event -> handleRideAction(RideStatus.COMPLETED));
        cancelRideItem.setOnAction(event -> handleRideAction(RideStatus.CANCELLED));
        
        // Add to menu
        contextMenu.getItems().addAll(
            startRideItem, 
            completeRideItem, 
            new SeparatorMenuItem(),
            cancelRideItem
        );
        
        // Attach to table
        ridesTable.setContextMenu(contextMenu);
        
        // Add row factory to enable/disable menu items based on current ride status
        ridesTable.setRowFactory(tableView -> {
            final TableRow<Ride> row = new TableRow<>();
            
            // Show menu only when a ride is selected
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(javafx.beans.binding.Bindings.isNotNull(row.itemProperty()))
                    .then(contextMenu)
                    .otherwise((ContextMenu) null)
            );
            
            return row;
        });
    }
    
    /**
     * Handle ride action (start, complete, cancel)
     */
    private void handleRideAction(RideStatus newStatus) {
        // Add null check for newStatus parameter
        if (newStatus == null) {
            showError("Invalid Action", new IllegalArgumentException("Ride status cannot be null"));
            return;
        }
        
        Ride selectedRide = ridesTable.getSelectionModel().getSelectedItem();
        if (selectedRide != null) {
            try {
                // Update ride status based on action
                RideStatus currentStatus = selectedRide.getStatus();
                
                // Validate state transition
                boolean validTransition = switch (newStatus) {
                    case IN_PROGRESS -> currentStatus == RideStatus.ASSIGNED;
                    case COMPLETED -> currentStatus == RideStatus.IN_PROGRESS;
                    case CANCELLED -> currentStatus != RideStatus.COMPLETED && 
                                      currentStatus != RideStatus.CANCELLED;
                    default -> false;
                };
                
                if (!validTransition) {
                    showError("Invalid Status Change", 
                        new IllegalStateException("Cannot change status from " + 
                            currentStatus + " to " + newStatus));
                    return;
                }
                
                // Update ride
                rideManager.updateRideStatus(selectedRide, newStatus);
                
                // Show confirmation
                String actionName = switch (newStatus) {
                    case IN_PROGRESS -> "started";
                    case COMPLETED -> "completed";
                    case CANCELLED -> "cancelled";
                    default -> "updated";
                };
                
                // Show confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ride " + actionName.substring(0, 1).toUpperCase() + actionName.substring(1));
                alert.setHeaderText("Ride successfully " + actionName);
                alert.setContentText("The ride status has been updated to " + newStatus);
                alert.showAndWait();
                
                // Update UI
                updateStatusBar();
                
            } catch (Exception e) {
                showError("Error Updating Ride", e);
            }
        }
    }
    
    private void addDemoData() {
        try {
            // Add several taxis for better map visualization
            Taxi[] randomTaxis = TaxiFactory.createRandomTaxis(10);
            for (Taxi taxi : randomTaxis) {
                taxiManager.addTaxi(taxi);
            }
        } catch (InvalidTaxiException e) {
            showError("Error adding taxi", e);
        }
    }
    
    private void updateStatusBar() {
        int availableTaxis = taxiManager.getObservableAvailableTaxis().size();
        int totalRides = rideManager.getObservableRides().size();
        
        statusLabel.setText(String.format("Available Taxis: %d | Total Rides: %d", 
                availableTaxis, totalRides));
    }
    
    @FXML
    private void handleRequestRide() {
        try {
            // Load the request ride view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RequestRideView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the ride manager
            RequestRideController controller = loader.getController();
            controller.setRideManager(rideManager);
            
            // Create and show the stage
            Stage stage = new Stage();
            stage.setTitle("Request a Ride");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Update UI after dialog closes
            updateStatusBar();
        } catch (IOException e) {
            showError("Error loading request ride view", e);
        }
    }
    
    @FXML
    private void handleAddTaxi() {
        try {
            // Load the add taxi view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddTaxiView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the taxi manager
            AddTaxiController controller = loader.getController();
            controller.setTaxiManager(taxiManager);
            
            // Create and show the stage
            Stage stage = new Stage();
            stage.setTitle("Add New Taxi");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Update UI after dialog closes
            updateStatusBar();
        } catch (IOException e) {
            showError("Error loading add taxi view", e);
        }
    }
    
    /**
     * Set up map rendering
     */
    private void setupMapRendering() {
        if (mapContainer != null) {
            // Create a pane for map rendering
            Pane mapPane = new Pane();
            mapPane.getStyleClass().add("map-view");
            
            // Fill the stack pane
            mapPane.prefWidthProperty().bind(mapContainer.widthProperty());
            mapPane.prefHeightProperty().bind(mapContainer.heightProperty());
            
            // Add to container
            mapContainer.getChildren().clear();
            mapContainer.getChildren().add(mapPane);
            
            // Create and initialize the real map viewer
            mapViewer = new RealMapViewer(mapPane);
            
            // Initial update with available taxis
            updateMapWithTaxis();
            
            // Listen for changes in available taxis
            taxiManager.getObservableAvailableTaxis().addListener(
                (ListChangeListener<Taxi>) change -> updateMapWithTaxis());
        }
    }
    
    /**
     * Update the map with current taxis
     */
    private void updateMapWithTaxis() {
        List<Taxi> taxis = new ArrayList<>(taxiManager.getObservableAvailableTaxis());
        if (mapViewer != null) {
            mapViewer.updateTaxis(taxis);
        }
    }
    
    /**
     * Set up map control buttons
     */
    private void setupMapControls() {
        // Setup satellite view button
        if (satelliteViewButton != null) {
            satelliteViewButton.setOnAction(e -> {
                if (mapViewer != null) {
                    mapViewer.setMapType("satellite");
                    satelliteViewButton.getStyleClass().add("active");
                    if (streetViewButton != null) {
                        streetViewButton.getStyleClass().remove("active");
                    }
                }
            });
        }
        
        // Setup street view button
        if (streetViewButton != null) {
            streetViewButton.setOnAction(e -> {
                if (mapViewer != null) {
                    mapViewer.setMapType("street");
                    streetViewButton.getStyleClass().add("active");
                    if (satelliteViewButton != null) {
                        satelliteViewButton.getStyleClass().remove("active");
                    }
                }
            });
            // Street view is default, mark as active
            streetViewButton.getStyleClass().add("active");
        }
        
        // Setup center map button
        if (centerMapButton != null) {
            centerMapButton.setOnAction(e -> {
                centerMapOnAvailableTaxis();
            });
        }
    }
    
    /**
     * Center the map on available taxis
     */
    private void centerMapOnAvailableTaxis() {
        if (mapViewer != null) {
            List<Taxi> availableTaxis = taxiManager.getObservableAvailableTaxis();
            if (!availableTaxis.isEmpty()) {
                // Calculate the average position of all taxis
                double avgLat = 0.0;
                double avgLon = 0.0;
                int count = 0;
                
                for (Taxi taxi : availableTaxis) {
                    Location location = taxi.getCurrentLocation();
                    if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                        avgLat += location.getLatitude();
                        avgLon += location.getLongitude();
                        count++;
                    }
                }
                
                if (count > 0) {
                    avgLat /= count;
                    avgLon /= count;
                    mapViewer.centerMap(avgLat, avgLon, 14);
                }
            }
        }
    }
    
    private void showError(String title, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
} 