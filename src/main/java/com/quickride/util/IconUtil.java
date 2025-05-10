package com.quickride.util;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Utility class for handling application icons
 */
public class IconUtil {

    private IconUtil() {
        // Utility class, not meant to be instantiated
    }
    
    /**
     * Gets the application icon as an Image
     * First tries to load a PNG version, then falls back to built-in icon
     * 
     * @return Application icon as JavaFX Image
     */
    public static Image getAppIcon() {
        try {
            // First try to load existing PNG version if available
            InputStream pngStream = IconUtil.class.getResourceAsStream("/images/taxi_icon.png");
            if (pngStream != null) {
                Image icon = new Image(pngStream);
                if (!icon.isError()) {
                    return icon;
                }
            }
            
            // Try to load and convert SVG
            InputStream svgStream = IconUtil.class.getResourceAsStream("/images/taxi_icon.svg");
            if (svgStream != null) {
                try {
                    // Read the SVG content - not directly used but kept as reference for future work
                    // and demonstrate that we loaded the SVG file
                    svgStream.readAllBytes();
                    
                    // Convert SVG to Image - this is a simplified approach 
                    // that doesn't use WebView which would be more complex
                    return createIconFromSvg();
                } catch (IOException e) {
                    System.err.println("Failed to read SVG data: " + e.getMessage());
                }
            }
            
            // If all else fails, use a built-in fallback icon
            return createFallbackIcon();
        } catch (SecurityException e) {
            // Security exception can happen when trying to access resources
            System.err.println("Security error accessing icon: " + e.getMessage());
            return createFallbackIcon();
        } catch (Exception e) {
            // Catch all other exceptions
            System.err.println("Failed to load application icon: " + e.getMessage());
            return createFallbackIcon();
        }
    }
    
    /**
     * Create an icon from SVG 
     */
    private static Image createIconFromSvg() {
        // Since SVG rendering in JavaFX is complex and might require JavaFX WebView,
        // we'll use our fallback icon with taxi colors matching the SVG
        javafx.scene.shape.Circle background = new javafx.scene.shape.Circle(32, 32, 32);
        background.setFill(Color.web("#3498db"));
        
        javafx.scene.shape.Rectangle taxiBody = new javafx.scene.shape.Rectangle(12, 20, 40, 25);
        taxiBody.setFill(Color.web("#FFD700"));
        taxiBody.setStroke(Color.BLACK);
        taxiBody.setStrokeWidth(2);
        taxiBody.setArcWidth(5);
        taxiBody.setArcHeight(5);
        
        // Wheels
        javafx.scene.shape.Circle wheel1 = new javafx.scene.shape.Circle(22, 45, 6);
        wheel1.setFill(Color.BLACK);
        javafx.scene.shape.Circle wheel2 = new javafx.scene.shape.Circle(42, 45, 6);
        wheel2.setFill(Color.BLACK);
        
        // Windows
        javafx.scene.shape.Rectangle window = new javafx.scene.shape.Rectangle(24, 15, 16, 8);
        window.setFill(Color.web("#87CEFA"));
        window.setStroke(Color.BLACK);
        window.setStrokeWidth(1);
        
        // Group everything
        javafx.scene.Group taxiGroup = new javafx.scene.Group(background, taxiBody, wheel1, wheel2, window);
        
        // Create a StackPane to hold the taxi
        StackPane iconPane = new StackPane(taxiGroup);
        iconPane.setPrefSize(64, 64);
        iconPane.setMinSize(64, 64);
        iconPane.setMaxSize(64, 64);
        
        // Take a snapshot of the icon
        WritableImage iconImage = new WritableImage(64, 64);
        iconPane.snapshot(null, iconImage);
        
        return iconImage;
    }
    
    /**
     * Creates a simple fallback icon if no icon file is available
     * 
     * @return A simple yellow taxi icon
     */
    private static Image createFallbackIcon() {
        try {
            // Create a simple yellow taxi icon
            javafx.scene.shape.Rectangle taxiBody = new javafx.scene.shape.Rectangle(10, 15, 40, 25);
            taxiBody.setFill(javafx.scene.paint.Color.YELLOW);
            taxiBody.setStroke(javafx.scene.paint.Color.BLACK);
            taxiBody.setStrokeWidth(1.5);
            taxiBody.setArcWidth(5);
            taxiBody.setArcHeight(5);
            
            // Wheels
            javafx.scene.shape.Circle wheel1 = new javafx.scene.shape.Circle(20, 40, 5);
            wheel1.setFill(javafx.scene.paint.Color.BLACK);
            javafx.scene.shape.Circle wheel2 = new javafx.scene.shape.Circle(40, 40, 5);
            wheel2.setFill(javafx.scene.paint.Color.BLACK);
            
            // Taxi light
            javafx.scene.shape.Rectangle taxiLight = new javafx.scene.shape.Rectangle(25, 10, 10, 5);
            taxiLight.setFill(javafx.scene.paint.Color.ORANGE);
            
            // Group everything
            javafx.scene.Group taxiGroup = new javafx.scene.Group(taxiBody, wheel1, wheel2, taxiLight);
            
            // Create a StackPane to hold the taxi
            StackPane iconPane = new StackPane(taxiGroup);
            iconPane.setStyle("-fx-background-color: #3498db; -fx-background-radius: 50;");
            iconPane.setPrefSize(64, 64);
            iconPane.setMinSize(64, 64);
            iconPane.setMaxSize(64, 64);
            
            // Take a snapshot of the icon
            WritableImage iconImage = new WritableImage(64, 64);
            iconPane.snapshot(null, iconImage);
            
            return iconImage;
        } catch (Exception e) {
            System.err.println("Failed to create fallback icon: " + e.getMessage());
            return null;
        }
    }
} 