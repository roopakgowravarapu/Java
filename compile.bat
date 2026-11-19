@echo off
echo ==========================================================
echo Compiling Campus Placement Management System...
echo ==========================================================

set JARS=C:\Users\roopa\.m2\repository\org\openjfx\javafx-base\17.0.10\javafx-base-17.0.10-win.jar;C:\Users\roopa\.m2\repository\org\openjfx\javafx-controls\17.0.10\javafx-controls-17.0.10-win.jar;C:\Users\roopa\.m2\repository\org\openjfx\javafx-graphics\17.0.10\javafx-graphics-17.0.10-win.jar;C:\Users\roopa\.m2\repository\org\openjfx\javafx-fxml\17.0.10\javafx-fxml-17.0.10-win.jar;C:\Users\roopa\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar;C:\Users\roopa\.m2\repository\org\apache\pdfbox\pdfbox\2.0.31\pdfbox-2.0.31.jar;C:\Users\roopa\.m2\repository\org\apache\pdfbox\fontbox\2.0.31\fontbox-2.0.31.jar;C:\Users\roopa\.m2\repository\commons-logging\commons-logging\1.2\commons-logging-1.2.jar;C:\Users\roopa\.m2\repository\org\xerial\sqlite-jdbc\3.45.3.0\sqlite-jdbc-3.45.3.0.jar;C:\Users\roopa\.m2\repository\org\slf4j\slf4j-api\2.0.13\slf4j-api-2.0.13.jar

if not exist "target\classes" mkdir target\classes

javac -cp "%JARS%;src\main\resources" -d target\classes src\main\java\com\campusplacement\*.java src\main\java\com\campusplacement\model\*.java src\main\java\com\campusplacement\management\*.java src\main\java\com\campusplacement\screens\*.java src\main\java\com\campusplacement\concurrency\*.java src\main\java\com\campusplacement\utils\*.java

xcopy /E /I /Y src\main\resources\* target\classes\

echo Compilation completed successfully!
if "%1" neq "nopause" pause
