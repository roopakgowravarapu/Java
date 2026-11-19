package com.campusplacement;

import com.campusplacement.concurrency.BackgroundTasks;
import com.campusplacement.concurrency.DeadlineMonitor;
import com.campusplacement.screens.ApplicationScreen;
import com.campusplacement.screens.CompanyScreen;
import com.campusplacement.screens.DashboardScreen;
import com.campusplacement.screens.DriveScreen;
import com.campusplacement.screens.LoginScreen;
import com.campusplacement.screens.ReportScreen;
import com.campusplacement.screens.StudentDashboardScreen;
import com.campusplacement.screens.StudentScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Entry Point for Campus Placement Management System.
 * Advanced Java Desktop Application utilizing JavaFX, JDBC, MySQL, and Multithreading.
 */
public class MainApp extends Application {

    private static Stage primaryStage;
    private static Scene primaryScene;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Campus Placement Management System");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);

        // 1. Initialize Database & Seed Demo Data
        DBConnection.initializeDatabase();

        // 2. Start Automatic Deadline Monitoring Background Thread (FEATURE 3)
        DeadlineMonitor.startMonitoring();

        // 3. Load Login View
        showLoginScreen();

        // 4. Clean Application Shutdown Handler
        primaryStage.setOnCloseRequest(e -> {
            System.out.println("Shutting down Campus Placement System services...");
            DeadlineMonitor.stopMonitoring();
            BackgroundTasks.shutdown();
        });

        primaryStage.show();
    }

    // ==========================================
    // ROLE-BASED NAVIGATION CONTROLLERS
    // ==========================================

    public static void showLoginScreen() {
        switchScene(LoginScreen.createContent(), 850, 600);
    }

    public static void showDashboard() {
        switchScene(DashboardScreen.createContent(), 1200, 780);
    }

    public static void showStudentScreen() {
        switchScene(StudentScreen.createContent(), 1200, 780);
    }

    public static void showCompanyScreen() {
        switchScene(CompanyScreen.createContent(), 1200, 780);
    }

    public static void showDriveScreen() {
        switchScene(DriveScreen.createContent(), 1200, 780);
    }

    public static void showApplicationScreen() {
        switchScene(ApplicationScreen.createContent(), 1200, 780);
    }

    public static void showReportScreen() {
        switchScene(ReportScreen.createContent(), 1150, 750);
    }

    public static void showStudentDashboard() {
        switchScene(StudentDashboardScreen.createContent(), 1150, 750);
    }

    private static void switchScene(javafx.scene.Parent root, double width, double height) {
        if (primaryScene == null) {
            primaryScene = new Scene(root, width, height);
            applyStylesheet(primaryScene);
            primaryStage.setScene(primaryScene);
        } else {
            primaryScene.setRoot(root);
        }
    }

    private static void applyStylesheet(Scene scene) {
        try {
            String css = MainApp.class.getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ex) {
            System.err.println("Could not load style.css: " + ex.getMessage());
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
