@echo off
echo Building QuickRide...
call mvn clean package

echo Starting QuickRide...

REM Set paths to modules
set MODULES_PATH=%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\17.0.6\javafx-base-17.0.6-win.jar;^
%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\17.0.6\javafx-controls-17.0.6-win.jar;^
%USERPROFILE%\.m2\repository\org\openjfx\javafx-fxml\17.0.6\javafx-fxml-17.0.6-win.jar;^
%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\17.0.6\javafx-graphics-17.0.6-win.jar;^
%USERPROFILE%\.m2\repository\org\controlsfx\controlsfx\11.1.2\controlsfx-11.1.2.jar

REM Run the application with proper module arguments
java --module-path "%MODULES_PATH%" ^
     --add-modules javafx.controls,javafx.fxml ^
     --add-exports javafx.graphics/com.sun.javafx.css=org.controlsfx.controls ^
     --add-exports javafx.graphics/com.sun.javafx.scene=org.controlsfx.controls ^
     --add-exports javafx.graphics/com.sun.javafx.scene.traversal=org.controlsfx.controls ^
     --add-exports javafx.controls/com.sun.javafx.scene.control=org.controlsfx.controls ^
     --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=org.controlsfx.controls ^
     --add-exports javafx.controls/com.sun.javafx.scene.control.inputmap=org.controlsfx.controls ^
     --add-exports javafx.base/com.sun.javafx.event=org.controlsfx.controls ^
     --add-exports javafx.base/com.sun.javafx.collections=org.controlsfx.controls ^
     --add-exports javafx.base/com.sun.javafx.runtime=org.controlsfx.controls ^
     --add-opens javafx.controls/javafx.scene.control.skin=org.controlsfx.controls ^
     -cp target\QuickRide-1.0-SNAPSHOT.jar com.quickride.QuickRideApp 