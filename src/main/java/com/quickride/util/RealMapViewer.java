package com.quickride.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quickride.model.Location;
import com.quickride.model.Taxi;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Utility class for displaying a real OpenStreetMap with taxi positions
 */
public class RealMapViewer {

    private static final Logger LOGGER = Logger.getLogger(RealMapViewer.class.getName());
    private static final DecimalFormat COORD_FORMAT = new DecimalFormat("##.######");
    
    // Default center location for the map (San Francisco)
    private static final double DEFAULT_LAT = 37.7749;
    private static final double DEFAULT_LON = -122.4194;
    private static final int DEFAULT_ZOOM = 13;
    
    private WebView webView;
    private WebEngine webEngine;
    private final Pane parentPane;
    private List<Taxi> taxis = new ArrayList<>();
    private VBox infoBox;
    
    // Static initializer to avoid "this" escape
    private static final class MapInitializer {
        private final RealMapViewer viewer;
        
        MapInitializer(RealMapViewer viewer) {
            this.viewer = viewer;
        }
        
        void initialize() {
            // Create WebView and get its engine
            viewer.webView = new WebView();
            viewer.webEngine = viewer.webView.getEngine();
            
            // Make WebView resize with parent pane
            viewer.webView.prefWidthProperty().bind(viewer.parentPane.widthProperty());
            viewer.webView.prefHeightProperty().bind(viewer.parentPane.heightProperty());
            
            // Load the HTML content for the map
            String mapHTML = viewer.loadMapHTML();
            viewer.webEngine.loadContent(mapHTML);
            
            // Add the map to the parent pane
            viewer.parentPane.getChildren().clear();
            viewer.parentPane.getChildren().add(viewer.webView);
            
            // When the map is loaded, initialize JavaScript bridge
            viewer.webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    viewer.updateTaxis(viewer.taxis);
                }
            });
        }
    }
    
    /**
     * Constructs a new map viewer
     * @param parentPane the JavaFX pane to add the map to
     */
    public RealMapViewer(Pane parentPane) {
        this.parentPane = parentPane;
        
        // Initialize the web view properly without "this" escape
        MapInitializer initializer = new MapInitializer(this);
        Platform.runLater(initializer::initialize);
    }

    /**
     * Loads the HTML content for the map from resources
     */
    private String loadMapHTML() {
        try {
            // Load the HTML template using traditional string concatenation
            StringBuilder html = new StringBuilder();
            
            html.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("    <meta charset=\"utf-8\">\n")
                .append("    <title>QuickRide Map</title>\n")
                .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                .append("    <style>\n")
                .append("        html, body, #map {\n")
                .append("            width: 100%;\n")
                .append("            height: 100%;\n")
                .append("            margin: 0;\n")
                .append("            padding: 0;\n")
                .append("        }\n")
                .append("        .map-layer-control {\n")
                .append("            padding: 6px 8px;\n")
                .append("            background: white;\n")
                .append("            box-shadow: 0 0 15px rgba(0,0,0,0.2);\n")
                .append("            border-radius: 5px;\n")
                .append("        }\n")
                .append("    </style>\n")
                .append("    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />\n")
                .append("    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("    <div id=\"map\"></div>\n")
                .append("    <script>\n")
                // Fixed string concatenation in StringBuilder by separating into multiple append calls
                .append("        // Initialize the map\n")
                .append("        var map = L.map('map').setView([")
                .append(DEFAULT_LAT)
                .append(", ")
                .append(DEFAULT_LON)
                .append("], ")
                .append(DEFAULT_ZOOM)
                .append(");\n")
                .append("        \n")
                .append("        // Define base maps\n")
                .append("        var street = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n")
                .append("            attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors',\n")
                .append("            maxZoom: 19\n")
                .append("        });\n")
                .append("        \n")
                .append("        // Using a more reliable satellite imagery source\n")
                .append("        var satellite = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {\n")
                .append("            attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community',\n")
                .append("            maxZoom: 18\n")
                .append("        });\n")
                .append("        \n")
                .append("        // Alternative satellite source in case the first one fails\n")
                .append("        var satelliteAlt = L.tileLayer('https://mt1.google.com/vt/lyrs=s&x={x}&y={y}&z={z}', {\n")
                .append("            attribution: 'Map data &copy; Google',\n")
                .append("            maxZoom: 20\n")
                .append("        });\n")
                .append("        \n")
                .append("        // Add satellite view by default with fallback\n")
                .append("        satelliteAlt.addTo(map);\n")
                .append("        \n")
                .append("        // Create layer control\n")
                .append("        var baseMaps = {\n")
                .append("            \"Satellite\": satelliteAlt,\n")
                .append("            \"Street\": street\n")
                .append("        };\n")
                .append("        \n")
                .append("        L.control.layers(baseMaps, null, {position: 'topright'}).addTo(map);\n")
                .append("        \n")
                .append("        // Object to store markers\n")
                .append("        var taxiMarkers = {};\n")
                .append("        \n")
                .append("        // Function to add or update taxi markers\n")
                .append("        function updateTaxis(taxiData) {\n")
                .append("            // Parse the JSON data\n")
                .append("            var taxis = JSON.parse(taxiData);\n")
                .append("            \n")
                .append("            // Keep track of processed taxis to remove stale markers\n")
                .append("            var processedIds = [];\n")
                .append("            \n")
                .append("            // Add/update markers for each taxi\n")
                .append("            taxis.forEach(function(taxi) {\n")
                .append("                processedIds.push(taxi.id);\n")
                .append("                \n")
                .append("                // Skip taxis without valid coordinates\n")
                .append("                if (taxi.lat === 0 && taxi.lon === 0) return;\n")
                .append("                \n")
                .append("                // Check if marker already exists\n")
                .append("                if (taxiMarkers[taxi.id]) {\n")
                .append("                    // Update existing marker\n")
                .append("                    taxiMarkers[taxi.id].setLatLng([taxi.lat, taxi.lon]);\n")
                .append("                } else {\n")
                .append("                    // Create new marker with car icon\n")
                .append("                    var taxiIcon = L.divIcon({\n")
                .append("                        html: '<svg width=\"32\" height=\"32\" viewBox=\"0 0 32 32\" xmlns=\"http://www.w3.org/2000/svg\"><circle cx=\"16\" cy=\"16\" r=\"16\" fill=\"#3498db\"/><circle cx=\"16\" cy=\"16\" r=\"13.5\" fill=\"#2980b9\"/><g transform=\"scale(0.09) translate(30, 30)\"><path d=\"M232,124l-16-40H40L24,124L8,140v96h24v16c0,13.2,10.8,24,24,24h16c13.2,0,24-10.8,24-24v-16h64v16c0,13.2,10.8,24,24,24h16c13.2,0,24-10.8,24-24v-16h24V140L232,124z\" fill=\"#FFD700\" stroke=\"#222\" stroke-width=\"8\"/><circle cx=\"72\" cy=\"212\" r=\"24\" fill=\"#333\" stroke=\"#222\" stroke-width=\"4\"/><circle cx=\"184\" cy=\"212\" r=\"24\" fill=\"#333\" stroke=\"#222\" stroke-width=\"4\"/><path d=\"M24,124h208\" stroke=\"#333\" stroke-width=\"8\"/><path d=\"M44,88l12-24h144l12,24\" stroke=\"#333\" stroke-width=\"6\" fill=\"none\"/><rect x=\"96\" y=\"84\" width=\"64\" height=\"24\" fill=\"#87CEFA\" stroke=\"#333\" stroke-width=\"4\"/></g></svg>',\n")
                .append("                        className: '',  // Important: empty to avoid default styling\n")
                .append("                        iconSize: [32, 32],\n")
                .append("                        iconAnchor: [16, 16]\n")
                .append("                    });\n")
                .append("                    \n")
                .append("                    var marker = L.marker([taxi.lat, taxi.lon], {icon: taxiIcon})\n")
                .append("                        .addTo(map)\n")
                .append("                        .bindTooltip(taxi.driver);\n")
                .append("                        \n")
                .append("                    marker.on('click', function() {\n")
                .append("                        // Call Java method when taxi is clicked\n")
                .append("                        if (window.javaConnector) {\n")
                .append("                            window.javaConnector.showTaxiInfo(taxi.id);\n")
                .append("                        }\n")
                .append("                    });\n")
                .append("                    \n")
                .append("                    taxiMarkers[taxi.id] = marker;\n")
                .append("                }\n")
                .append("            });\n")
                .append("            \n")
                .append("            // Remove markers for taxis that are no longer present\n")
                .append("            Object.keys(taxiMarkers).forEach(function(id) {\n")
                .append("                if (!processedIds.includes(id)) {\n")
                .append("                    map.removeLayer(taxiMarkers[id]);\n")
                .append("                    delete taxiMarkers[id];\n")
                .append("                }\n")
                .append("            });\n")
                .append("        }\n")
                .append("        \n")
                .append("        // Function to center the map on a specific location\n")
                .append("        function centerMap(lat, lon, zoom) {\n")
                .append("            map.setView([lat, lon], zoom);\n")
                .append("        }\n")
                .append("        \n")
                .append("        // Function to switch map type\n")
                .append("        function setMapType(type) {\n")
                .append("            if (type === 'satellite') {\n")
                .append("                street.remove();\n")
                .append("                satelliteAlt.addTo(map);\n")
                .append("            } else {\n")
                .append("                satelliteAlt.remove();\n")
                .append("                street.addTo(map);\n")
                .append("            }\n")
                .append("        }\n")
                .append("    </script>\n")
                .append("</body>\n")
                .append("</html>\n");
            
            return html.toString();
        } catch (Exception e) {
            // Use logger instead of printStackTrace
            LOGGER.log(Level.SEVERE, "Error loading map HTML", e);
            
            // Return a simple error page
            return "<html><body><h1>Error loading map</h1><p>" + e.getMessage() + "</p></body></html>";
        }
    }
    
    /**
     * Update the map with a list of taxis to display
     * @param taxis list of taxis to show on the map
     */
    public void updateTaxis(List<Taxi> taxis) {
        this.taxis = new ArrayList<>(taxis);
        
        if (webEngine == null) return;
        
        // Convert taxis to JSON format
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        
        for (Taxi taxi : taxis) {
            if (!first) json.append(",");
            first = false;
            
            Location location = taxi.getCurrentLocation();
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            
            json.append("{")
                .append("\"id\":\"").append(taxi.getId()).append("\",")
                .append("\"driver\":\"").append(taxi.getDriverName()).append("\",")
                .append("\"lat\":").append(lat).append(",")
                .append("\"lon\":").append(lon).append(",")
                .append("\"carModel\":\"").append(taxi.getCarModel()).append("\",")
                .append("\"licensePlate\":\"").append(taxi.getLicensePlate()).append("\",")
                .append("\"address\":\"").append(escapeJavaScript(location.getAddress())).append("\"")
                .append("}");
        }
        
        json.append("]");
        
        // Execute JavaScript to update the markers
        Platform.runLater(() -> {
            webEngine.executeScript("updateTaxis('" + json.toString() + "')");
        });
    }
    
    /**
     * Escape special characters for JavaScript
     */
    private String escapeJavaScript(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("'", "\\'")
                 .replace("\"", "\\\"")
                 .replace("\r", "\\r")
                 .replace("\n", "\\n");
    }
    
    /**
     * Display taxi information when a taxi marker is clicked
     */
    public void showTaxiInfo(String taxiId) {
        // Find the taxi with the given ID
        for (Taxi taxi : taxis) {
            if (taxi.getId().equals(taxiId)) {
                Platform.runLater(() -> {
                    // Get the location
                    Location taxiLocation = taxi.getCurrentLocation();
                    double taxiLat = taxiLocation.getLatitude();
                    double taxiLon = taxiLocation.getLongitude();
                    
                    // Show the info box
                    showTaxiInfoBox(taxi, taxiLat, taxiLon);
                });
                break;
            }
        }
    }
    
    /**
     * Show the taxi info box
     */
    private void showTaxiInfoBox(Taxi taxi, double taxiLat, double taxiLon) {
        // Remove previous info box if exists
        if (infoBox != null && infoBox.getParent() != null) {
            parentPane.getChildren().remove(infoBox);
        }
        
        // Create a new info box with taxi details
        infoBox = createTaxiInfoBox(taxi, taxiLat, taxiLon);
        
        // Position it near the center
        double x = parentPane.getWidth() / 2 - 100;
        double y = parentPane.getHeight() / 2 - 100;
        
        infoBox.setLayoutX(x);
        infoBox.setLayoutY(y);
        
        // Make sure info box is within map bounds
        if (infoBox.getLayoutX() < 10) infoBox.setLayoutX(10);
        if (infoBox.getLayoutY() < 10) infoBox.setLayoutY(10);
        if (infoBox.getLayoutX() + 200 > parentPane.getWidth()) {
            infoBox.setLayoutX(parentPane.getWidth() - 210);
        }
        if (infoBox.getLayoutY() + 180 > parentPane.getHeight()) {
            infoBox.setLayoutY(parentPane.getHeight() - 190);
        }
        
        parentPane.getChildren().add(infoBox);
    }
    
    /**
     * Create an info box for a taxi with detailed information
     */
    private VBox createTaxiInfoBox(Taxi taxi, double taxiLat, double taxiLon) {
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
     * Center the map on a specific location
     * @param latitude the latitude to center on
     * @param longitude the longitude to center on
     * @param zoom the zoom level (1-19, where 19 is closest)
     */
    public void centerMap(double latitude, double longitude, int zoom) {
        if (webEngine != null) {
            Platform.runLater(() -> {
                webEngine.executeScript("centerMap(" + latitude + ", " + longitude + ", " + zoom + ")");
            });
        }
    }
    
    /**
     * Cleanup resources
     */
    public void dispose() {
        Platform.runLater(() -> {
            if (webView != null) {
                webEngine.load("about:blank");
                webView = null;
                webEngine = null;
            }
        });
    }
    
    /**
     * Switch the map type
     * @param mapType the map type ("satellite" or "street")
     */
    public void setMapType(String mapType) {
        if (webEngine != null) {
            Platform.runLater(() -> {
                webEngine.executeScript("setMapType('" + mapType + "')");
            });
        }
    }
} 