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

        // Self-Registration Button for New Students
        Button registerBtn = new Button("New Student? Register Here");
        registerBtn.getStyleClass().add("btn-secondary");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px;");
        registerBtn.setOnAction(e -> showRegistrationDialog(userField, passField));

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
                registerBtn,
                new Separator(),
                demoHint,
                demoBtnBox
        );

        root.getChildren().add(card);
        return root;
    }

    /**
     * Modal dialog allowing new students to self-register and create their placement account.
     */
    private static void showRegistrationDialog(TextField loginUserField, PasswordField loginPassField) {
        javafx.scene.control.Dialog<Boolean> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Student Self-Registration");
        dialog.setHeaderText("Create your Campus Placement Student Account");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField rollField = new TextField();
        rollField.setPromptText("e.g. 24CS110");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("student@college.edu");

        TextField phoneField = new TextField();
        phoneField.setPromptText("10-digit mobile number");

        javafx.scene.control.ComboBox<String> deptBox = new javafx.scene.control.ComboBox<>();
        deptBox.getItems().addAll("Computer Science", "Information Tech", "Electronics & Comm", "Electrical Eng", "Mechanical Eng");
        deptBox.setValue("Computer Science");

        TextField cgpaField = new TextField();
        cgpaField.setPromptText("Current CGPA (0.00 to 10.00)");

        TextField yearField = new TextField("2026");

        TextField skillsField = new TextField();
        skillsField.setPromptText("e.g. Java, Python, SQL");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Choose account password");

        Label errorMsg = new Label();
        errorMsg.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
        errorMsg.setWrapText(true);
        errorMsg.setVisible(false);

        grid.add(new Label("Roll Number:"), 0, 0);
        grid.add(rollField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Institutional Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Mobile Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Department:"), 0, 4);
        grid.add(deptBox, 1, 4);
        grid.add(new Label("Current CGPA:"), 0, 5);
        grid.add(cgpaField, 1, 5);
        grid.add(new Label("Graduation Year:"), 0, 6);
        grid.add(yearField, 1, 6);
        grid.add(new Label("Technical Skills:"), 0, 7);
        grid.add(skillsField, 1, 7);
        grid.add(new Label("Account Password:"), 0, 8);
        grid.add(passField, 1, 8);
        grid.add(errorMsg, 0, 9, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        okButton.setText("Register & Create Account");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            errorMsg.setVisible(false);

            String roll = rollField.getText();
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String pwd = passField.getText();

            if (!com.campusplacement.utils.ValidationUtil.isNotEmpty(roll)) {
                errorMsg.setText("Roll Number is required.");
                errorMsg.setVisible(true);
                event.consume();
                return;
            }
            if (!com.campusplacement.utils.ValidationUtil.isNotEmpty(name)) {
                errorMsg.setText("Full Name is required.");
                errorMsg.setVisible(true);
                event.consume();
                return;
            }
            if (!com.campusplacement.utils.ValidationUtil.isValidEmail(email)) {
                errorMsg.setText("Invalid email format! Example: name@college.edu");
                errorMsg.setVisible(true);
                emailField.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px;");
                AlertUtil.showError("Invalid Email Format", "Please enter a valid institutional email address.");
                event.consume();
                return;
            } else {
                emailField.setStyle("");
            }
            if (!com.campusplacement.utils.ValidationUtil.isValidPhone(phone)) {
                errorMsg.setText("Mobile Phone must be a valid 10-digit number.");
                errorMsg.setVisible(true);
                event.consume();
                return;
            }
            if (!com.campusplacement.utils.ValidationUtil.isNotEmpty(pwd) || pwd.trim().length() < 4) {
                errorMsg.setText("Password must be at least 4 characters.");
                errorMsg.setVisible(true);
                event.consume();
                return;
            }

            try {
                double cgpa = Double.parseDouble(cgpaField.getText().trim());
                int year = Integer.parseInt(yearField.getText().trim());

                com.campusplacement.model.Student s = new com.campusplacement.model.Student(
                        0,
                        roll.trim().toUpperCase(),
                        name.trim(),
                        email.trim(),
                        phone.trim(),
                        deptBox.getValue(),
                        cgpa,
                        year,
                        skillsField.getText().trim(),
                        0 // 0 initial backlogs
                );

                boolean registered = com.campusplacement.management.StudentManagement.addStudent(s, pwd.trim());
                if (!registered) {
                    errorMsg.setText("Registration could not be completed.");
                    errorMsg.setVisible(true);
                    event.consume();
                }
            } catch (NumberFormatException nfe) {
                errorMsg.setText("CGPA and Graduation Year must be valid numeric values.");
                errorMsg.setVisible(true);
                AlertUtil.showError("Input Format Error", "CGPA and Graduation Year must be valid numbers.");
                event.consume();
            } catch (Exception ex) {
                errorMsg.setText("Registration Error: " + ex.getMessage());
                errorMsg.setVisible(true);
                AlertUtil.showError("Registration Error", ex.getMessage());
                event.consume();
            }
        });

        dialog.setResultConverter(btnType -> btnType == javafx.scene.control.ButtonType.OK);

        dialog.showAndWait().ifPresent(confirmed -> {
            if (Boolean.TRUE.equals(confirmed)) {
                String registeredUser = rollField.getText().trim().toLowerCase();
                String enteredPass = passField.getText().trim();

                // Pre-populate login fields for immediate sign in
                loginUserField.setText(registeredUser);
                loginPassField.setText(enteredPass);

                AlertUtil.showInfo(
                        "Registration Successful!",
                        "Welcome to Campus Placement System, " + nameField.getText().trim() + "!\n\n"
                                + "Your account has been created successfully.\n"
                                + "• Username: " + registeredUser + " (or email: " + emailField.getText().trim() + ")\n\n"
                                + "The login fields have been pre-filled. Click 'SIGN IN' to open your Student Portal!"
                );
            }
        });
    }
}
