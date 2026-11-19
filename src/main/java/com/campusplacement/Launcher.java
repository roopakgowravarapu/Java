package com.campusplacement;

/**
 * Standard JavaFX Application Launcher.
 * 
 * In JavaFX 11+, launching a class that directly extends Application from the classpath
 * causes the JVM to check for modular runtime components.
 * This Launcher class bypasses that check and delegates to MainApp.main(args).
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
