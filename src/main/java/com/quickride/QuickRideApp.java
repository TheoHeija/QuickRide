package com.quickride;

import java.io.IOException;

import com.quickride.util.Constants;
import com.quickride.util.IconUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main application class for QuickRide
 */
public class QuickRideApp extends Application {
    
    /**
     * Default constructor required for JavaFX Application.
     */
    public QuickRideApp() {
        // Default constructor required for JavaFX
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load main view from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            Parent root = loader.load();
            
            // Create scene
            Scene scene = new Scene(root, Constants.APP_WIDTH, Constants.APP_HEIGHT);
            
            // Load stylesheet safely
            java.net.URL cssResource = getClass().getResource(Constants.CSS_FILE);
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                System.err.println("Warning: CSS file not found: " + Constants.CSS_FILE);
            }
            
            // Set up stage
            primaryStage.setTitle(Constants.APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(Constants.APP_WIDTH);
            primaryStage.setMinHeight(Constants.APP_HEIGHT);
            
            // Add application icon using our icon utility
            Image appIcon = IconUtil.getAppIcon();
            if (appIcon != null) {
                primaryStage.getIcons().add(appIcon);
            }
            
            primaryStage.show();
            
        } catch (IOException e) {
            // Log the full stack trace for developers
            System.err.println("Failed to start application: " + e.getMessage());
            
            // Display error dialog to the user
            showErrorDialog("Application Error", 
                "Failed to start the QuickRide application", 
                "The application could not be started due to a resource loading error: " + e.getMessage());
            
            // Exit the application
            System.exit(1);
        }
    }
    
    /**
     * Shows an error dialog with the given information
     */
    private void showErrorDialog(String title, String header, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Main method - entry point for the application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
} 