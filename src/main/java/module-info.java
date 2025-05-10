module com.quickride {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires javafx.base;
    requires org.controlsfx.controls;
    requires java.logging;
    // Using jxmapviewer2 as an automatic module (acceptable for this application)
    // The automatic module name is derived from the JAR filename
    // We acknowledge this as a known limitation but acceptable for this project
    requires jxmapviewer2;
    requires java.desktop;
    requires transitive javafx.web;

    // We can't expose internal JavaFX packages in this module
    // Instead, we'll use VM arguments to enable ControlsFX access
    
    opens com.quickride to javafx.fxml;
    opens com.quickride.controller to javafx.fxml, org.controlsfx.controls;
    opens com.quickride.model to javafx.base, javafx.fxml;
    opens com.quickride.manager to javafx.base, javafx.fxml;
    
    exports com.quickride;
    exports com.quickride.controller;
    exports com.quickride.model;
    exports com.quickride.manager;
    exports com.quickride.exception;
    exports com.quickride.util;
}