package com.campusplacement.utils;

import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Utility class to display user-friendly JavaFX dialogs and alerts.
 * Ensures all dialogs execute safely on the JavaFX Application Thread.
 */
public class AlertUtil {

    public static void showInfo(String title, String message) {
        if (Platform.isFxApplicationThread()) {
            displayAlert(Alert.AlertType.INFORMATION, title, message);
        } else {
            Platform.runLater(() -> displayAlert(Alert.AlertType.INFORMATION, title, message));
        }
    }

    public static void showWarning(String title, String message) {
        if (Platform.isFxApplicationThread()) {
            displayAlert(Alert.AlertType.WARNING, title, message);
        } else {
            Platform.runLater(() -> displayAlert(Alert.AlertType.WARNING, title, message));
        }
    }

    public static void showError(String title, String message) {
        if (Platform.isFxApplicationThread()) {
            displayAlert(Alert.AlertType.ERROR, title, message);
        } else {
            Platform.runLater(() -> displayAlert(Alert.AlertType.ERROR, title, message));
        }
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void displayAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
