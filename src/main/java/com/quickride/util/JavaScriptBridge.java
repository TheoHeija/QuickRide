package com.quickride.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bridge class for communication between JavaScript and Java
 * This class serves as a proxy that JavaFX WebEngine can call safely
 */
public class JavaScriptBridge {
    private static final Logger LOGGER = Logger.getLogger(JavaScriptBridge.class.getName());
    private final RealMapViewer mapViewer;
    
    /**
     * Create a new JavaScript bridge linked to a map viewer
     * @param mapViewer The map viewer to link to
     */
    public JavaScriptBridge(RealMapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }
    
    /**
     * Show information for a taxi
     * Called from JavaScript when a taxi marker is clicked
     * @param taxiId The ID of the taxi
     */
    public void showTaxiInfo(String taxiId) {
        try {
            LOGGER.info(() -> "JavaScript called showTaxiInfo for taxi: " + taxiId);
            if (mapViewer != null) {
                mapViewer.showTaxiInfo(taxiId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in showTaxiInfo for taxi " + taxiId, e);
        }
    }
    
    /**
     * Clear the currently selected taxi
     * Called from JavaScript when the map is clicked
     */
    public void clearSelectedTaxi() {
        try {
            LOGGER.info("JavaScript called clearSelectedTaxi");
            if (mapViewer != null) {
                mapViewer.clearSelectedTaxi();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in clearSelectedTaxi", e);
        }
    }
    
    /**
     * Log a message from JavaScript to Java
     * @param message The message to log
     */
    public void log(String message) {
        LOGGER.info(() -> "JavaScript log: " + message);
    }
    
    /**
     * Report an error from JavaScript to Java
     * @param error The error message
     */
    public void reportError(String error) {
        LOGGER.severe(() -> "JavaScript error: " + error);
    }
} 