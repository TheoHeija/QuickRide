module com.quickride {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires javafx.base;
    requires org.controlsfx.controls;
    requires java.logging;
    
    // Note on jxmapviewer2:
    // We acknowledge that using an automatic module is generally not recommended
    // for production applications. For this educational project, it's acceptable
    // since:
    // 1. We've moved away from using this library to using web-based maps
    // 2. The library does not have a proper modular version available
    // 3. This is a self-contained application not intended for redistribution as a library
    //
    // In a production environment, proper modularization would be required or 
    // the library would need to be replaced entirely.
    requires static jxmapviewer2;  // Automatic module, acceptable for this demo project
    
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