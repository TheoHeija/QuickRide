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
    
    // Default center location for the map (Switzerland - Center near Bern)
    private static final double DEFAULT_LAT = 46.8182;
    private static final double DEFAULT_LON = 8.2275;
    private static final int DEFAULT_ZOOM = 8;
    
    private WebView webView;
    private WebEngine webEngine;
    private final Pane parentPane;
    private List<Taxi> taxis = new ArrayList<>();
    private VBox infoBox;
    
    // Store reference to currently selected taxi
    private Taxi selectedTaxi;
    
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
                    try {
                        // Create JavaScript bridge using safe method
                        viewer.webEngine.executeScript(
                            "window.javaConnector = {"
                            + "  showTaxiInfo: function(taxiId) {"
                            + "    window.location.href = 'java-callback:showTaxiInfo:' + taxiId;"
                            + "  },"
                            + "  clearSelectedTaxi: function() {"
                            + "    window.location.href = 'java-callback:clearSelectedTaxi';"
                            + "  },"
                            + "  log: function(message) {"
                            + "    window.location.href = 'java-callback:log:' + message;"
                            + "  },"
                            + "  reportError: function(error) {"
                            + "    window.location.href = 'java-callback:error:' + error;"
                            + "  }"
                            + "};"
                        );
                        
                        // Set location change handler to capture JavaScript callbacks
                        viewer.webEngine.locationProperty().addListener((loc, oldLoc, newLoc) -> {
                            if (newLoc != null && newLoc.startsWith("java-callback:")) {
                                String command = newLoc.substring("java-callback:".length());
                                
                                if (command.startsWith("showTaxiInfo:")) {
                                    String taxiId = command.substring("showTaxiInfo:".length());
                                    viewer.showTaxiInfo(taxiId);
                                } else if (command.equals("clearSelectedTaxi")) {
                                    viewer.clearSelectedTaxi();
                                } else if (command.startsWith("log:")) {
                                    String message = command.substring("log:".length());
                                    LOGGER.info(() -> "JavaScript log: " + message);
                                } else if (command.startsWith("error:")) {
                                    String error = command.substring("error:".length());
                                    LOGGER.severe(() -> "JavaScript error: " + error);
                                }
                                
                                // Reset location to avoid loops
                                Platform.runLater(() -> viewer.webEngine.getLoadWorker().cancel());
                            }
                        });
                        
                        // Add global error handler
                        viewer.webEngine.executeScript(
                            "window.onerror = function(message, source, lineno, colno, error) { " +
                            "  if (window.javaConnector) { " +
                            "    window.javaConnector.reportError(message); " +
                            "  } " +
                            "  console.error('JavaScript error: ' + message); " +
                            "  return true; " +
                            "};"
                        );
                        
                        // Test if JavaScript bridge is working
                        viewer.webEngine.executeScript("if (window.javaConnector) { window.javaConnector.log('JavaScript bridge initialized'); }");
                        
                        viewer.updateTaxis(viewer.taxis);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error initializing JavaScript bridge", e);
                    }
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
                .append("        .selected-taxi {\n")
                .append("            z-index: 1000 !important;\n")
                .append("            filter: drop-shadow(0 0 10px #ff9800);\n")
                .append("            transform: scale(1.2);\n")
                .append("            transition: all 0.3s ease;\n")
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
                .append("        // Add street view by default\n")
                .append("        street.addTo(map);\n")
                .append("        \n")
                .append("        // Create layer control\n")
                .append("        var baseMaps = {\n")
                .append("            \"Street\": street,\n")
                .append("            \"Satellite\": satelliteAlt\n")
                .append("        };\n")
                .append("        \n")
                .append("        L.control.layers(baseMaps, null, {position: 'topright'}).addTo(map);\n")
                .append("        \n")
                .append("        // Object to store markers\n")
                .append("        var taxiMarkers = {};\n")
                .append("        var selectedTaxiId = null;\n")
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
                .append("                        html: '<img src=\"data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHN2ZyB3aWR0aD0iNTEyIiBoZWlnaHQ9IjUxMiIgdmlld0JveD0iMCAwIDUxMiA1MTIiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgICA8IS0tIE9yYW5nZSBDYXIgU1ZHIHJlY3JlYXRpb24gb2YgdGhlIGlzb21ldHJpYyBjYXIgLS0+CiAgICA8ZyB0cmFuc2Zvcm09InRyYW5zbGF0ZSg1NiwgMTA2KSBzY2FsZSgwLjgpIj4KICAgICAgICA8IS0tIENhciBCb2R5IC0tPgogICAgICAgIDxwYXRoIGQ9Ik0yNTAgMjAwTDEwMCAzMDBMNDAwIDMwMEw1MDAgMjAwTDI1MCAyMDBaIiBmaWxsPSIjRkY1NzIyIi8+CiAgICAgICAgPCEtLSBDYXIgVG9wIC0tPgogICAgICAgIDxwYXRoIGQ9Ik0yNTAgMjAwTDM3MCAxMDBMNTAwIDIwMEwyNTAgMjAwWiIgZmlsbD0iI0U2NEExOSIvPgogICAgICAgIDwhLS0gTGVmdCBTaWRlIEJvZHkgLS0+CiAgICAgICAgPHBhdGggZD0iTTEwMCAzMDBMMTAwIDM1MEwyMDAgNDAwTDQwMCA0MDBMNDAwIDMwMEwxMDAgMzAwWiIgZmlsbD0iI0ZGNTcyMiIvPgogICAgICAgIDwhLS0gRnJvbnQgQnVtcGVyIC0tPgogICAgICAgIDxwYXRoIGQ9Ik00MDAgMzAwTDQwMCA0MDBMNTAwIDMyMEw1MDAgMjAwTDQwMCAzMDBaIiBmaWxsPSIjQkYzNjBDIi8+CiAgICAgICAgPCEtLSBXaGVlbHMgLS0+CiAgICAgICAgPGNpcmNsZSBjeD0iMTc1IiBjeT0iMzgwIiByPSI0MCIgZmlsbD0iIzQyNDI0MiIvPgogICAgICAgIDxjaXJjbGUgY3g9IjE3NSIgY3k9IjM4MCIgcj0iMjAiIGZpbGw9IiNCREJEQkQiLz4KICAgICAgICA8Y2lyY2xlIGN4PSIzNTAiIGN5PSIzODAiIHI9IjQwIiBmaWxsPSIjNDI0MjQyIi8+CiAgICAgICAgPGNpcmNsZSBjeD0iMzUwIiBjeT0iMzgwIiByPSIyMCIgZmlsbD0iI0JEQkRCRCIvPgogICAgICAgIDwhLS0gV2luZG93cyAtLT4KICAgICAgICA8cGF0aCBkPSJNMjYwIDIyMEwzNzAgMTMwTDQ2MCAyMDBMMjYwIDIyMFoiIGZpbGw9IiM3OTU1NDgiLz4KICAgICAgICA8IS0tIEhlYWRsaWdodHMgLS0+CiAgICAgICAgPHJlY3QgeD0iNDgwIiB5PSIyNTAiIHdpZHRoPSIzMCIgaGVpZ2h0PSIyMCIgZmlsbD0iI0ZGRkZGRiIvPgogICAgICAgIDxyZWN0IHg9IjQ4MCIgeT0iMjkwIiB3aWR0aD0iMzAiIGhlaWdodD0iMjAiIGZpbGw9IiNGRkZGRkYiLz4KICAgICAgICA8IS0tIFNpZGUgRGV0YWlsIC0tPgogICAgICAgIDxyZWN0IHg9IjEyMCIgeT0iMzIwIiB3aWR0aD0iMTAwIiBoZWlnaHQ9IjIwIiBmaWxsPSIjQkYzNjBDIi8+CiAgICA8L2c+Cjwvc3ZnPg==\" width=\"36\" height=\"36\" style=\"filter: drop-shadow(2px 2px 2px rgba(0,0,0,0.5));\">',\n")
                .append("                        className: taxi.id === selectedTaxiId ? 'selected-taxi' : '', \n")
                .append("                        iconSize: [36, 36],\n")
                .append("                        iconAnchor: [18, 18]\n")
                .append("                    });\n")
                .append("                    \n")
                .append("                    var marker = L.marker([taxi.lat, taxi.lon], {icon: taxiIcon})\n")
                .append("                        .addTo(map)\n")
                .append("                        .bindTooltip(taxi.driver);\n")
                .append("                        \n")
                .append("                    marker.on('click', function() {\n")
                .append("                        // Call Java method when taxi is clicked\n")
                .append("                        try {\n")
                .append("                            if (window.javaConnector) {\n")
                .append("                                window.javaConnector.showTaxiInfo(taxi.id);\n")
                .append("                                selectTaxi(taxi.id);\n")
                .append("                            }\n")
                .append("                        } catch (err) {\n")
                .append("                            console.error('Error calling Java: ' + err);\n")
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
                .append("        // Function to select a taxi and highlight it\n")
                .append("        function selectTaxi(taxiId) {\n")
                .append("            // Remove highlight from previously selected taxi\n")
                .append("            if (selectedTaxiId && taxiMarkers[selectedTaxiId]) {\n")
                .append("                var oldIcon = taxiMarkers[selectedTaxiId].getIcon();\n")
                .append("                oldIcon.options.className = '';\n")
                .append("                taxiMarkers[selectedTaxiId].setIcon(oldIcon);\n")
                .append("            }\n")
                .append("            \n")
                .append("            // Set new selected taxi\n")
                .append("            selectedTaxiId = taxiId;\n")
                .append("            \n")
                .append("            // Highlight the selected taxi\n")
                .append("            if (taxiMarkers[taxiId]) {\n")
                .append("                var newIcon = taxiMarkers[taxiId].getIcon();\n")
                .append("                newIcon.options.className = 'selected-taxi';\n")
                .append("                taxiMarkers[taxiId].setIcon(newIcon);\n")
                .append("                \n")
                .append("                // Center map on the selected taxi\n")
                .append("                map.panTo(taxiMarkers[taxiId].getLatLng(), {\n")
                .append("                    animate: true,\n")
                .append("                    duration: 0.5\n")
                .append("                });\n")
                .append("            }\n")
                .append("        }\n")
                .append("        \n")
                .append("        // Function to clear the selected taxi\n")
                .append("        function clearSelectedTaxi() {\n")
                .append("            if (selectedTaxiId && taxiMarkers[selectedTaxiId]) {\n")
                .append("                var oldIcon = taxiMarkers[selectedTaxiId].getIcon();\n")
                .append("                oldIcon.options.className = '';\n")
                .append("                taxiMarkers[selectedTaxiId].setIcon(oldIcon);\n")
                .append("                selectedTaxiId = null;\n")
                .append("            }\n")
                .append("        }\n")
                .append("        \n")
                .append("        // Add map click handler to unselect taxi\n")
                .append("        map.on('click', function(e) {\n")
                .append("            // Only call Java if we had something selected previously\n")
                .append("            if (selectedTaxiId) {\n")
                .append("                try {\n")
                .append("                    if (window.javaConnector) {\n")
                .append("                        window.javaConnector.clearSelectedTaxi();\n")
                .append("                    }\n")
                .append("                } catch (err) {\n")
                .append("                    console.error('Error calling Java: ' + err);\n")
                .append("                }\n")
                .append("            }\n")
                .append("            clearSelectedTaxi();\n")
                .append("        });\n")
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
     * Focus on a specific taxi
     * @param taxi The taxi to focus on
     */
    public void focusOnTaxi(Taxi taxi) {
        if (taxi == null) return;
        
        // Update selected taxi reference
        selectedTaxi = taxi;
        
        // Call JavaScript to highlight and focus on taxi
        Platform.runLater(() -> {
            try {
                // Check if selectTaxi function exists
                Boolean functionExists = (Boolean) webEngine.executeScript(
                    "typeof selectTaxi === 'function'"
                );
                
                if (Boolean.TRUE.equals(functionExists)) {
                    String script = "selectTaxi('" + escapeJavaScript(taxi.getId()) + "');";
                    webEngine.executeScript(script);
                } else {
                    LOGGER.warning("selectTaxi function not available yet, skipping taxi focus");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error focusing on taxi", e);
            }
        });
    }
    
    /**
     * Update the map with a list of taxis to display
     */
    public void updateTaxis(List<Taxi> taxis) {
        this.taxis = new ArrayList<>(taxis); // Store a copy of the taxis
        
        Platform.runLater(() -> {
            try {
                if (webEngine == null) {
                    return; // WebEngine not initialized yet
                }
                
                // Check if map is loaded and updateTaxis function exists
                Boolean functionExists = false;
                try {
                    functionExists = (Boolean) webEngine.executeScript(
                        "typeof updateTaxis === 'function'"
                    );
                } catch (Exception e) {
                    // Function check failed, assume function doesn't exist
                    LOGGER.log(Level.WARNING, "Failed to check if updateTaxis function exists", e);
                }
                
                if (!Boolean.TRUE.equals(functionExists)) {
                    LOGGER.warning("updateTaxis function not available yet, skipping update");
                    return;
                }
                
                StringBuilder taxiJson = new StringBuilder("[");
                boolean first = true;
                
                for (Taxi taxi : taxis) {
                    if (!first) {
                        taxiJson.append(",");
                    }
                    
                    // Format location for taxi
                    Location location = taxi.getCurrentLocation();
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    
                    // Create JSON object for this taxi
                    taxiJson.append("{")
                           .append("\"id\":\"").append(escapeJavaScript(taxi.getId())).append("\",")
                           .append("\"driver\":\"").append(escapeJavaScript(taxi.getDriverName())).append("\",")
                           .append("\"lat\":").append(COORD_FORMAT.format(lat)).append(",")
                           .append("\"lon\":").append(COORD_FORMAT.format(lon)).append("}")
                    ;
                    
                    first = false;
                }
                
                taxiJson.append("]");
                
                // Update map with taxi data
                String script = "updateTaxis('" + taxiJson.toString() + "');";
                webEngine.executeScript(script);
                
                // If there's a selected taxi, keep it selected
                if (selectedTaxi != null) {
                    focusOnTaxi(selectedTaxi);
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error updating taxis on map", e);
            }
        });
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
                try {
                    // Check if centerMap function exists
                    Boolean functionExists = (Boolean) webEngine.executeScript(
                        "typeof centerMap === 'function'"
                    );
                    
                    if (Boolean.TRUE.equals(functionExists)) {
                        webEngine.executeScript("centerMap(" + latitude + ", " + longitude + ", " + zoom + ")");
                    } else {
                        LOGGER.warning("centerMap function not available yet, skipping map centering");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error centering map", e);
                }
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
                try {
                    // Check if setMapType function exists
                    Boolean functionExists = (Boolean) webEngine.executeScript(
                        "typeof setMapType === 'function'"
                    );
                    
                    if (Boolean.TRUE.equals(functionExists)) {
                        webEngine.executeScript("setMapType('" + mapType + "')");
                    } else {
                        LOGGER.warning("setMapType function not available yet, skipping map type change");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error setting map type", e);
                }
            });
        }
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
     * Clear the currently selected taxi
     */
    public void clearSelectedTaxi() {
        // Clear the selection
        selectedTaxi = null;
        
        // Remove info box if present
        if (infoBox != null && infoBox.getParent() != null) {
            Platform.runLater(() -> {
                parentPane.getChildren().remove(infoBox);
            });
        }
        
        // Call JavaScript to clear selection state
        Platform.runLater(() -> {
            try {
                // Check if clearSelectedTaxi function exists
                Boolean functionExists = (Boolean) webEngine.executeScript(
                    "typeof clearSelectedTaxi === 'function'"
                );
                
                if (Boolean.TRUE.equals(functionExists)) {
                    String script = "clearSelectedTaxi();";
                    webEngine.executeScript(script);
                } else {
                    LOGGER.warning("clearSelectedTaxi function not available yet, skipping taxi deselection");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error clearing selected taxi", e);
            }
        });
    }
} 