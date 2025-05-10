package com.quickride.util;

import java.util.List;

import com.quickride.model.Taxi;

import javafx.scene.web.WebView;

/**
 * Wrapper for a Google Maps implementation that might be used in the future
 * This is a placeholder class for compatibility with potential future map providers
 */
public class GoogleMapWrapper {
    
    // Make field final per linter suggestion
    private final WebView mapView;
    
    // This field is intentionally not used yet but kept for future implementation
    @SuppressWarnings("unused")
    private final List<Taxi> taxis;
    
    /**
     * Creates a new GoogleMapWrapper around a WebView
     * @param mapView The WebView to display the map in
     * @param taxis Initial list of taxis
     */
    public GoogleMapWrapper(WebView mapView, List<Taxi> taxis) {
        this.mapView = mapView;
        this.taxis = taxis;
    }
    
    /**
     * Get the WebView used for the map
     * @return WebView control
     */
    public WebView getMapView() {
        return mapView;
    }
} 