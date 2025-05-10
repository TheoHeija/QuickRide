package com.quickride.util;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import com.quickride.model.Location;
import com.quickride.model.Taxi;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Utility class to render a simple mock map with taxis
 */
public class MapRenderer {
    
    private static final Random random = new Random();
    private static final DecimalFormat COORD_FORMAT = new DecimalFormat("##.######");
    
    // Map center coordinates (San Francisco)
    private static double MAP_CENTER_LAT = 37.7749;
    private static double MAP_CENTER_LON = -122.4194;
    
    // Map zoom level (degrees per pixel)
    private static double MAP_ZOOM = 0.0005;
    
    // For panning
    private static double lastMouseX = 0;
    private static double lastMouseY = 0;
    private static boolean isPanning = false;
    
    // For displaying taxi info
    private static VBox infoBox = null;
    
    /**
     * Private constructor to prevent instantiation
     */
    private MapRenderer() {
        // Utility class, not meant to be instantiated
    }
    
    /**
     * Renders a simple mock map with available taxis
     * @param mapPane The pane to render the map on
     * @param availableTaxis List of available taxis to show on the map
     */
    public static void renderMap(Pane mapPane, List<Taxi> availableTaxis) {
        // Clear previous content
        mapPane.getChildren().clear();
        
        // Get pane dimensions
        double width = mapPane.getWidth();
        double height = mapPane.getHeight();
        
        if (width <= 0 || height <= 0) {
            // Pane not yet sized, we'll render later
            return;
        }
        
        // Add mouse handlers for panning if not already set
        setupMapControls(mapPane);
        
        // Draw grid lines (road network)
        drawMapGrid(mapPane, width, height);
        
        // Draw main roads
        drawMainRoads(mapPane, width, height);
        
        // Add map coordinates labels (GPS-like)
        addCoordinateLabels(mapPane, width, height);
        
        // Draw available taxis
        for (Taxi taxi : availableTaxis) {
            Location taxiLocation = taxi.getCurrentLocation();
            
            // If the taxi doesn't have a real location with coordinates,
            // assign realistic coordinates near the map center
            double taxiLat = MAP_CENTER_LAT + (random.nextDouble() - 0.5) * 0.1;
            double taxiLon = MAP_CENTER_LON + (random.nextDouble() - 0.5) * 0.1;
            
            if (taxiLocation.getLatitude() != 0 && taxiLocation.getLongitude() != 0) {
                taxiLat = taxiLocation.getLatitude();
                taxiLon = taxiLocation.getLongitude();
            }
            
            // Convert geo coordinates to screen coordinates
            double x = convertLongitudeToX(taxiLon, width);
            double y = convertLatitudeToY(taxiLat, height);
            
            // Draw taxi marker
            Circle taxiMarker = new Circle(x, y, 8);
            taxiMarker.getStyleClass().add("taxi-marker");
            taxiMarker.setUserData(taxi); // Store taxi for potential click handlers
            
            // Add click handler for the taxi marker
            setupTaxiMarkerInteraction(mapPane, taxiMarker, taxi, taxiLat, taxiLon);
            
            // Add tooltip with info
            String tooltipText = String.format("%s - %s\nLocation: %s\nCoordinates: %s, %s", 
                    taxi.getDriverName(), 
                    taxi.getCarModel(),
                    taxiLocation.getAddress(),
                    COORD_FORMAT.format(taxiLat),
                    COORD_FORMAT.format(taxiLon));
            
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(tooltipText);
            javafx.scene.control.Tooltip.install(taxiMarker, tooltip);
            
            mapPane.getChildren().add(taxiMarker);
            
            // Add driver name label
            Label driverLabel = new Label(taxi.getDriverName());
            driverLabel.setLayoutX(x + 12);
            driverLabel.setLayoutY(y - 8);
            driverLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
            driverLabel.setTextFill(Color.rgb(20, 20, 20));
            driverLabel.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-padding: 2px; -fx-background-radius: 3px;");
            mapPane.getChildren().add(driverLabel);
        }
        
        // Add instructions for map interaction
        addMapInstructions(mapPane, width, height);
    }
    
    /**
     * Setup interaction for taxi markers
     */
    private static void setupTaxiMarkerInteraction(Pane mapPane, Circle taxiMarker, Taxi taxi, double taxiLat, double taxiLon) {
        // Change cursor on hover
        taxiMarker.setCursor(Cursor.HAND);
        
        // Highlight on hover
        taxiMarker.setOnMouseEntered(e -> {
            taxiMarker.setEffect(new DropShadow(10, Color.GOLD));
            taxiMarker.setRadius(10); // Make it slightly bigger
        });
        
        taxiMarker.setOnMouseExited(e -> {
            taxiMarker.setEffect(null);
            taxiMarker.setRadius(8); // Reset size
        });
        
        // Handle click to show detailed info
        taxiMarker.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // Remove previous info box if it exists
                if (infoBox != null && infoBox.getParent() != null) {
                    if (infoBox.getParent() instanceof Pane parent) {
                        parent.getChildren().remove(infoBox);
                    }
                }
                
                // Create a detailed info box
                infoBox = createTaxiInfoBox(taxi, taxiLat, taxiLon);
                infoBox.setLayoutX(e.getSceneX() - 100); // Position near cursor
                infoBox.setLayoutY(e.getSceneY() - 200);
                
                // Make sure info box is within map bounds
                if (infoBox.getLayoutX() < 10) infoBox.setLayoutX(10);
                if (infoBox.getLayoutY() < 10) infoBox.setLayoutY(10);
                if (infoBox.getLayoutX() + 200 > mapPane.getWidth()) {
                    infoBox.setLayoutX(mapPane.getWidth() - 210);
                }
                if (infoBox.getLayoutY() + 180 > mapPane.getHeight()) {
                    infoBox.setLayoutY(mapPane.getHeight() - 190);
                }
                
                mapPane.getChildren().add(infoBox);
            }
        });
    }
    
    /**
     * Create an info box for a taxi with detailed information
     */
    private static VBox createTaxiInfoBox(Taxi taxi, double taxiLat, double taxiLon) {
        VBox taxiInfoBox = new VBox(5);
        taxiInfoBox.setPadding(new Insets(10));
        taxiInfoBox.setBackground(new Background(new BackgroundFill(
                Color.rgb(255, 255, 255, 0.9), 
                new CornerRadii(5), 
                Insets.EMPTY)));
        taxiInfoBox.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        taxiInfoBox.setMinWidth(200);
        taxiInfoBox.setMaxWidth(200);
        
        // Create header
        Label headerLabel = new Label("Taxi Details");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Driver info
        Label driverLabel = new Label("Driver: " + taxi.getDriverName());
        driverLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        
        // Car info
        Label carLabel = new Label("Car: " + taxi.getCarModel());
        Label licenseLabel = new Label("License: " + taxi.getLicensePlate());
        
        // Location info
        Label locationLabel = new Label("Location:");
        locationLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        Label addressLabel = new Label(taxi.getCurrentLocation().getAddress());
        addressLabel.setWrapText(true);
        
        // Coordinates
        Label coordsLabel = new Label(String.format("GPS: %s, %s", 
                COORD_FORMAT.format(taxiLat),
                COORD_FORMAT.format(taxiLon)));
        
        // Add a close button
        javafx.scene.control.Button closeButton = new javafx.scene.control.Button("Close");
        closeButton.setOnAction(e -> {
            // Remove the info box
            if (taxiInfoBox.getParent() != null) {
                ((Pane)taxiInfoBox.getParent()).getChildren().remove(taxiInfoBox);
            }
        });
        
        // Add all to VBox
        taxiInfoBox.getChildren().addAll(
            headerLabel,
            new javafx.scene.control.Separator(),
            driverLabel,
            carLabel,
            licenseLabel,
            new javafx.scene.control.Separator(),
            locationLabel,
            addressLabel,
            coordsLabel,
            closeButton
        );
        
        return taxiInfoBox;
    }
    
    /**
     * Add map control handlers for panning and zooming
     */
    @SuppressWarnings("unchecked")
    private static void setupMapControls(Pane mapPane) {
        // For panning: track mouse press, drag, release
        mapPane.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            
            // Only start panning on right mouse button
            if (e.getButton() == MouseButton.SECONDARY) {
                isPanning = true;
                mapPane.setCursor(Cursor.MOVE);
            }
        });
        
        mapPane.setOnMouseDragged(e -> {
            if (isPanning) {
                // Calculate drag distance
                double deltaX = e.getX() - lastMouseX;
                double deltaY = e.getY() - lastMouseY;
                
                // Convert pixel movement to coordinate change
                double lonChange = -deltaX * MAP_ZOOM;
                double latChange = deltaY * MAP_ZOOM;
                
                // Move map center
                MAP_CENTER_LON += lonChange;
                MAP_CENTER_LAT += latChange;
                
                // Update last position
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                
                // Redraw map
                renderMap(mapPane, (List<Taxi>) mapPane.getUserData());
            }
        });
        
        mapPane.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                isPanning = false;
                mapPane.setCursor(Cursor.DEFAULT);
            }
        });
        
        // For zooming: use scroll wheel
        mapPane.setOnScroll(e -> {
            // Adjust zoom based on scroll direction
            if (e.getDeltaY() > 0) {
                // Zoom in
                MAP_ZOOM *= 0.9;
            } else {
                // Zoom out
                MAP_ZOOM *= 1.1;
            }
            
            // Constrain zoom level
            MAP_ZOOM = Math.max(0.0001, Math.min(0.001, MAP_ZOOM));
            
            // Redraw map
            renderMap(mapPane, (List<Taxi>) mapPane.getUserData());
        });
        
        // Store taxi list in map pane user data for redrawing
        mapPane.setUserData(mapPane.getUserData());
    }
    
    /**
     * Add instructions for map interaction
     */
    private static void addMapInstructions(Pane mapPane, double width, double height) {
        Label instructionsLabel = new Label("Map Controls: Right-click & drag to pan | Scroll to zoom | Click taxi for details");
        instructionsLabel.setLayoutX(width / 2 - 230);
        instructionsLabel.setLayoutY(height - 30);
        instructionsLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        instructionsLabel.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-padding: 5px; -fx-background-radius: 5px;");
        mapPane.getChildren().add(instructionsLabel);
    }
    
    /**
     * Add coordinate labels to show GPS-like positioning
     */
    private static void addCoordinateLabels(Pane mapPane, double width, double height) {
        // Bottom right coordinates
        String coordinates = COORD_FORMAT.format(MAP_CENTER_LAT) + ", " + 
                            COORD_FORMAT.format(MAP_CENTER_LON);
        
        Label coordLabel = new Label("Center: " + coordinates);
        coordLabel.setLayoutX(width - 220);
        coordLabel.setLayoutY(height - 60);
        coordLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 12));
        coordLabel.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-padding: 5px; -fx-background-radius: 5px;");
        mapPane.getChildren().add(coordLabel);
        
        // Top left zoom indicator
        Label zoomLabel = new Label("QuickRide Map • Zoom: " + Math.round(1/MAP_ZOOM/200) + "x");
        zoomLabel.setLayoutX(10);
        zoomLabel.setLayoutY(10);
        zoomLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        zoomLabel.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-padding: 5px; -fx-background-radius: 5px;");
        mapPane.getChildren().add(zoomLabel);
    }
    
    /**
     * Convert longitude to X coordinate
     */
    private static double convertLongitudeToX(double longitude, double width) {
        double centerX = width / 2;
        return centerX + (longitude - MAP_CENTER_LON) / MAP_ZOOM;
    }
    
    /**
     * Convert latitude to Y coordinate
     */
    private static double convertLatitudeToY(double latitude, double height) {
        double centerY = height / 2;
        // Negative because latitude increases northward but Y decreases
        return centerY - (latitude - MAP_CENTER_LAT) / MAP_ZOOM;
    }
    
    /**
     * Draws a simple grid to represent roads
     */
    private static void drawMapGrid(Pane mapPane, double width, double height) {
        // Draw vertical lines (north-south roads)
        for (int x = 0; x < width; x += 50) {
            Line road = new Line(x, 0, x, height);
            road.getStyleClass().add("map-grid");
            mapPane.getChildren().add(road);
        }
        
        // Draw horizontal lines (east-west roads)
        for (int y = 0; y < height; y += 50) {
            Line road = new Line(0, y, width, y);
            road.getStyleClass().add("map-grid");
            mapPane.getChildren().add(road);
        }
    }
    
    /**
     * Draws main roads
     */
    private static void drawMainRoads(Pane mapPane, double width, double height) {
        // Draw two main roads (one vertical, one horizontal)
        
        // Vertical main road
        Line mainRoadV = new Line(width * 0.3, 0, width * 0.3, height);
        mainRoadV.getStyleClass().add("map-main-road");
        mapPane.getChildren().add(mainRoadV);
        
        // Horizontal main road
        Line mainRoadH = new Line(0, height * 0.6, width, height * 0.6);
        mainRoadH.getStyleClass().add("map-main-road");
        mapPane.getChildren().add(mainRoadH);
        
        // Few diagonal roads
        Line diagonalRoad1 = new Line(width * 0.7, 0, width, height * 0.4);
        diagonalRoad1.getStyleClass().add("map-road");
        mapPane.getChildren().add(diagonalRoad1);
        
        Line diagonalRoad2 = new Line(0, height * 0.3, width * 0.6, height);
        diagonalRoad2.getStyleClass().add("map-road");
        mapPane.getChildren().add(diagonalRoad2);
    }
} 