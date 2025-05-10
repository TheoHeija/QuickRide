# Update the pom first to create an executable JAR
mvn clean package

# Set JAVAFX and ControlsFX paths
$env:JAVAFX_HOME = "$env:USERPROFILE\.m2\repository\org\openjfx"
$env:CONTROLSFX_HOME = "$env:USERPROFILE\.m2\repository\org\controlsfx\controlsfx\11.1.2"

# Create the modulepath
$modulepath = "$env:JAVAFX_HOME\javafx-base\17.0.6\javafx-base-17.0.6-win.jar;$env:JAVAFX_HOME\javafx-controls\17.0.6\javafx-controls-17.0.6-win.jar;$env:JAVAFX_HOME\javafx-fxml\17.0.6\javafx-fxml-17.0.6-win.jar;$env:JAVAFX_HOME\javafx-graphics\17.0.6\javafx-graphics-17.0.6-win.jar;$env:CONTROLSFX_HOME\controlsfx-11.1.2.jar"

Write-Host "Starting QuickRide App with JavaFX..." -ForegroundColor Green

# Run the application with JavaFX modules
java `
--module-path "$modulepath" `
--add-modules javafx.controls,javafx.fxml,org.controlsfx.controls `
--add-opens=javafx.base/com.sun.javafx.event=org.controlsfx.controls `
--add-exports=javafx.base/com.sun.javafx.event=org.controlsfx.controls `
--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=org.controlsfx.controls `
--add-exports=javafx.controls/com.sun.javafx.scene.control=org.controlsfx.controls `
-jar target/QuickRide-1.0-SNAPSHOT.jar 