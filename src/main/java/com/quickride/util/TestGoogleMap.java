package com.quickride.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * Test application for GoogleMapWrapper
 * This is mainly for testing Google Maps integration
 */
public class TestGoogleMap extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(TestGoogleMap.class.getName());
    
    /**
     * Explicit constructor to fix warning about exposing default constructor
     */
    public TestGoogleMap() {
        // Explicit constructor to avoid warnings
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            
            // Load Google Maps API
            String mapHtml = "<!DOCTYPE html>" +
                    "<html><head>" +
                    "<meta charset=\"utf-8\">" +
                    "<title>Google Maps Test</title>" +
                    "<script src=\"https://maps.googleapis.com/maps/api/js?key=YOUR_API_KEY\"></script>" +
                    "<style>html, body, #map { height: 100%; margin: 0; padding: 0; }</style>" +
                    "</head><body>" +
                    "<div id=\"map\"></div>" +
                    "<script>" +
                    "var map = new google.maps.Map(document.getElementById('map'), {" +
                    "  center: {lat: 37.7749, lng: -122.4194}," +
                    "  zoom: 13" +
                    "});" +
                    "</script></body></html>";
            
            webEngine.loadContent(mapHtml);
            
            Scene scene = new Scene(webView, 800, 600);
            primaryStage.setTitle("Google Maps Test");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            // Use logger instead of stack trace
            LOGGER.log(Level.SEVERE, "Error loading map", e);
            
            // Show error message to user
            StackPane errorPane = new StackPane();
            errorPane.getChildren().add(new Label("Error loading map: " + e.getMessage()));
            Scene errorScene = new Scene(errorPane, 800, 600);
            primaryStage.setScene(errorScene);
            primaryStage.show();
        }
    }
    
    /**
     * Main method
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            // Use logger instead of stack trace
            LOGGER.log(Level.SEVERE, "Error launching application", e);
        }
    }
} 