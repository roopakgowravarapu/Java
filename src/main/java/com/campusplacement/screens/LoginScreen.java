package com.campusplacement.screens;

import com.campusplacement.MainApp;
import com.campusplacement.management.LoginManagement;
import com.campusplacement.utils.AlertUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * JavaFX Login Screen with database-backed authentication and role-based navigation.
 */
public class LoginScreen {

    public static Parent createContent() {
        StackPane root = new StackPane();
        root.getStyleClass().add("login-root");

        VBox card = new VBox(15);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER);

        // Header
        Label titleLabel = new Label("CAMPUS PLACEMENT SYSTEM");
        titleLabel.getStyleClass().add("login-title");

        Label subtitleLabel = new Label("Sign in with your institutional credentials");
        subtitleLabel.getStyleClass().add("login-subtitle");

        // Form Fields
        Label userLabel = new Label("Username or Registered Email");
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField userField = new TextField();
        userField.setPromptText("Enter username (e.g. officer or student)");

        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 11px;");
        errorLabel.setVisible(false);

        // Login Action Button
        Button loginBtn = new Button("SIGN IN");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        // Event Handler for Login
        Runnable performLogin = () -> {
            errorLabel.setVisible(false);
            String inputUser = userField.getText();
            String inputPass = passField.getText();

            if (inputUser == null || inputUser.trim().isEmpty() || inputPass == null || inputPass.trim().isEmpty()) {
                errorLabel.setText("Please enter both username/email and password.");
                errorLabel.setVisible(true);
                return;
            }

            try {
                loginBtn.setDisable(true);
                LoginManagement.UserSession session = LoginManagement.authenticate(inputUser, inputPass);
                loginBtn.setDisable(false);

                if (session != null) {
                    // Role-Based Navigation
                    if (session.isStudent()) {
                        MainApp.showStudentDashboard();
                    } else {
                        MainApp.showDashboard();
                    }
                } else {
                    errorLabel.setText("Invalid username/email or password.");
                    errorLabel.setVisible(true);
                }
            } catch (Exception ex) {
                loginBtn.setDisable(false);
                errorLabel.setText("Database error: " + ex.getMessage());
                errorLabel.setVisible(true);
            }
        };

        loginBtn.setOnAction(e -> performLogin.run());
        passField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                performLogin.run();
            }
        });
        userField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                passField.requestFocus();
            }
        });

        // Quick Demo Credential Fill Buttons (Great for College Evaluation Viva)
        Label demoHint = new Label("Quick Demo Logins (Viva Evaluation):");
        demoHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        HBox demoBtnBox = new HBox(8);
        demoBtnBox.setAlignment(Pos.CENTER);

        Button officerFill = new Button("Placement Officer");
        officerFill.getStyleClass().add("btn-secondary");
        officerFill.setStyle("-fx-font-size: 11px; -fx-padding: 5px 12px;");
        officerFill.setOnAction(e -> {
            userField.setText("officer");
            passField.setText("officer123");
        });

        Button studentFill = new Button("Student");
        studentFill.getStyleClass().add("btn-secondary");
        studentFill.setStyle("-fx-font-size: 11px; -fx-padding: 5px 12px;");
        studentFill.setOnAction(e -> {
            userField.setText("student");
            passField.setText("student123");
        });

        demoBtnBox.getChildren().addAll(officerFill, studentFill);

        // Layout Assembly
        VBox fieldsBox = new VBox(6);
        fieldsBox.setAlignment(Pos.CENTER_LEFT);
        fieldsBox.getChildren().addAll(userLabel, userField, passLabel, passField);

        card.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                fieldsBox,
                errorLabel,
                loginBtn,
                new Separator(),
                demoHint,
                demoBtnBox
        );

        root.getChildren().add(card);
        return root;
    }
}
