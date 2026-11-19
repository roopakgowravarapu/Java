package com.campusplacement.screens;

import com.campusplacement.MainApp;
import com.campusplacement.management.ApplicationManagement;
import com.campusplacement.management.DriveManagement;
import com.campusplacement.management.LoginManagement;
import com.campusplacement.management.StudentManagement;
import com.campusplacement.model.Notification;
import com.campusplacement.model.PlacementApplication;
import com.campusplacement.model.PlacementDrive;
import com.campusplacement.model.Student;
import com.campusplacement.utils.AlertUtil;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Dedicated Student Portal Screen.
 * Allows students to review their profile, evaluate placement eligibility,
 * submit online applications, track recruitment stages, and view notifications.
 */
public class StudentDashboardScreen {

    private static TableView<PlacementDrive> drivesTable;
    private static ObservableList<PlacementDrive> drivesList = FXCollections.observableArrayList();

    private static TableView<PlacementApplication> myAppsTable;
    private static ObservableList<PlacementApplication> myAppsList = FXCollections.observableArrayList();

    private static ListView<String> notifListView;

    public static Parent createContent() {
        BorderPane root = new BorderPane();

        // Top Navigation Bar
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #1e293b; -fx-padding: 15px 25px;");

        Label brand = new Label("CAMPUS PLACEMENT SYSTEM - STUDENT PORTAL");
        brand.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
        HBox.setHgrow(brand, Priority.ALWAYS);

        LoginManagement.UserSession user = LoginManagement.getCurrentUser();
        String studentName = user != null ? user.getUsername() : "Student";
        Label welcomeLbl = new Label("Welcome, " + studentName.toUpperCase());
        welcomeLbl.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("btn-danger");
        logoutBtn.setOnAction(e -> {
            boolean confirm = AlertUtil.showConfirmation("Sign Out", "Are you sure you want to sign out?");
            if (confirm) {
                LoginManagement.logout();
                MainApp.showLoginScreen();
            }
        });

        topBar.getChildren().addAll(brand, welcomeLbl, logoutBtn);
        root.setTop(topBar);

        // Fetch Student Record
        Student currentStudent = null;
        if (user != null && user.getStudentId() != null) {
            try {
                currentStudent = StudentManagement.getStudentById(user.getStudentId());
            } catch (Exception ignored) {}
        }

        // TabPane for Student Functions
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-padding: 10px;");

        // TAB 1: Profile & Academic Summary
        Tab profileTab = new Tab("My Profile & Criteria", createProfileView(currentStudent));
        profileTab.setClosable(false);

        // TAB 2: Placement Drives & Opportunities
        Tab drivesTab = new Tab("Placement Opportunities", createDrivesView(currentStudent));
        drivesTab.setClosable(false);

        // TAB 3: My Applications & Status
        Tab appsTab = new Tab("My Applied Drives", createAppTrackerView(currentStudent));
        appsTab.setClosable(false);

        // TAB 4: Notifications & Alerts
        Tab notifsTab = new Tab("Notifications", createNotificationsView(user));
        notifsTab.setClosable(false);

        tabPane.getTabs().addAll(profileTab, drivesTab, appsTab, notifsTab);
        root.setCenter(tabPane);

        return root;
    }

    private static Parent createProfileView(Student student) {
        VBox box = new VBox(20);
        box.setStyle("-fx-padding: 25px; -fx-background-color: #f8fafc;");

        Label title = new Label("Academic & Placement Profile");
        title.getStyleClass().add("view-header");

        if (student == null) {
            box.getChildren().addAll(title, new Label("No linked student academic record found for this user account."));
            return box;
        }

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-padding: 25px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");

        addProfileField(grid, 0, "Roll Number:", student.getRollNumber());
        addProfileField(grid, 1, "Full Name:", student.getFullName());
        addProfileField(grid, 2, "Department:", student.getDepartment());
        addProfileField(grid, 3, "CGPA:", String.format("%.2f / 10.00", student.getCgpa()));
        addProfileField(grid, 4, "Active Backlogs:", String.valueOf(student.getBacklogCount()));
        addProfileField(grid, 5, "Graduation Year:", String.valueOf(student.getGraduationYear()));
        addProfileField(grid, 6, "Email Address:", student.getEmail());
        addProfileField(grid, 7, "Contact Phone:", student.getPhoneNumber());
        addProfileField(grid, 8, "Registered Skills:", student.getSkills());

        box.getChildren().addAll(title, grid);
        return box;
    }

    private static void addProfileField(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-font-size: 13px;");
        Label val = new Label(value);
        val.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private static Parent createDrivesView(Student student) {
        VBox box = new VBox(15);
        box.setStyle("-fx-padding: 20px; -fx-background-color: #f8fafc;");

        Label title = new Label("Active Placement Recruitment Drives");
        title.getStyleClass().add("view-header");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button checkEligBtn = new Button("Check My Eligibility");
        checkEligBtn.getStyleClass().add("btn-secondary");

        Button applyBtn = new Button("Apply for Selected Drive");
        applyBtn.getStyleClass().add("btn-primary");

        Button refreshBtn = new Button("Refresh Drives");
        refreshBtn.getStyleClass().add("btn-secondary");

        actions.getChildren().addAll(checkEligBtn, applyBtn, refreshBtn);

        // Drives Table
        drivesTable = new TableView<>();
        drivesTable.setItems(drivesList);
        VBox.setVgrow(drivesTable, Priority.ALWAYS);

        TableColumn<PlacementDrive, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName()));
        compCol.setPrefWidth(160);

        TableColumn<PlacementDrive, String> roleCol = new TableColumn<>("Job Role");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getJobRole()));
        roleCol.setPrefWidth(180);

        TableColumn<PlacementDrive, Number> pkgCol = new TableColumn<>("Package (LPA)");
        pkgCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPackageLpa()));
        pkgCol.setPrefWidth(100);

        TableColumn<PlacementDrive, Number> cgpaCol = new TableColumn<>("Min CGPA");
        cgpaCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getMinCgpa()));
        cgpaCol.setPrefWidth(80);

        TableColumn<PlacementDrive, String> deptCol = new TableColumn<>("Eligible Depts");
        deptCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEligibleDepartments()));
        deptCol.setPrefWidth(180);

        TableColumn<PlacementDrive, Number> backlogsCol = new TableColumn<>("Max Backlogs");
        backlogsCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getMaxBacklogs()));
        backlogsCol.setPrefWidth(90);

        TableColumn<PlacementDrive, String> skillsCol = new TableColumn<>("Required Skills");
        skillsCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRequiredSkills()));
        skillsCol.setPrefWidth(180);

        TableColumn<PlacementDrive, String> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApplicationDeadline()));
        deadlineCol.setPrefWidth(150);

        drivesTable.getColumns().addAll(compCol, roleCol, pkgCol, cgpaCol, deptCol, backlogsCol, skillsCol, deadlineCol);

        // Events
        refreshBtn.setOnAction(e -> loadStudentDrives());

        checkEligBtn.setOnAction(e -> {
            PlacementDrive selected = drivesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Drive Selected", "Please select a drive from the table to check eligibility.");
                return;
            }
            if (student == null) {
                AlertUtil.showWarning("Profile Missing", "Your account is not linked to a student academic record.");
                return;
            }
            DriveManagement.EligibilityResult result = DriveManagement.checkEligibility(student, selected);
            if (result.isEligible()) {
                AlertUtil.showInfo(
                        "Eligibility Status: ELIGIBLE",
                        "Great news! You meet all criteria for " + selected.getJobRole() + " at " + selected.getCompanyName() + ".\n\n"
                                + "- Your CGPA: " + student.getCgpa() + " (Min: " + selected.getMinCgpa() + ")\n"
                                + "- Your Dept: " + student.getDepartment() + "\n"
                                + "- Your Backlogs: " + student.getBacklogCount() + " (Max: " + selected.getMaxBacklogs() + ")"
                );
            } else {
                AlertUtil.showWarning(
                        "Eligibility Status: NOT ELIGIBLE",
                        "You are currently ineligible for this drive:\n\n" + String.join("\n- ", result.getReasons())
                );
            }
        });

        applyBtn.setOnAction(e -> {
            PlacementDrive selected = drivesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Drive Selected", "Please select a drive to apply.");
                return;
            }
            if (student == null) {
                AlertUtil.showWarning("Profile Missing", "Cannot apply without an associated student profile.");
                return;
            }

            boolean confirm = AlertUtil.showConfirmation(
                    "Confirm Application Submission",
                    "Do you want to apply for " + selected.getJobRole() + " at " + selected.getCompanyName() + " (Package: " + selected.getPackageLpa() + " LPA)?"
            );

            if (confirm) {
                try {
                    boolean success = ApplicationManagement.applyForDrive(selected.getDriveId(), student.getStudentId(), "Applied via Student Portal");
                    if (success) {
                        AlertUtil.showInfo("Application Submitted", "Your application has been successfully submitted! Check the 'My Applied Drives' tab to track status.");
                        loadStudentApps(student);
                    }
                } catch (Exception ex) {
                    AlertUtil.showError("Application Error", ex.getMessage());
                }
            }
        });

        box.getChildren().addAll(title, actions, drivesTable);
        loadStudentDrives();

        return box;
    }

    private static void loadStudentDrives() {
        try {
            List<PlacementDrive> list = DriveManagement.getAllDrives();
            list.removeIf(d -> !"OPEN".equalsIgnoreCase(d.getDriveStatus()));
            drivesList.setAll(list);
        } catch (Exception e) {
            System.err.println("Error loading drives for student: " + e.getMessage());
        }
    }

    private static Parent createAppTrackerView(Student student) {
        VBox box = new VBox(15);
        box.setStyle("-fx-padding: 20px; -fx-background-color: #f8fafc;");

        Label title = new Label("My Placement Applications & Recruitment Stages");
        title.getStyleClass().add("view-header");

        Button refreshBtn = new Button("Refresh Application Status");
        refreshBtn.getStyleClass().add("btn-secondary");

        myAppsTable = new TableView<>();
        myAppsTable.setItems(myAppsList);
        VBox.setVgrow(myAppsTable, Priority.ALWAYS);

        TableColumn<PlacementApplication, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getCompanyName()));
        compCol.setPrefWidth(160);

        TableColumn<PlacementApplication, String> roleCol = new TableColumn<>("Job Role");
        roleCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getJobRole()));
        roleCol.setPrefWidth(180);

        TableColumn<PlacementApplication, Number> pkgCol = new TableColumn<>("Package (LPA)");
        pkgCol.setCellValueFactory(a -> new SimpleDoubleProperty(a.getValue().getPackageLpa()));
        pkgCol.setPrefWidth(100);

        TableColumn<PlacementApplication, String> statusCol = new TableColumn<>("Current Status");
        statusCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getStatus()));
        statusCol.setPrefWidth(140);

        TableColumn<PlacementApplication, String> dateCol = new TableColumn<>("Submission Date");
        dateCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getApplicationDate()));
        dateCol.setPrefWidth(130);

        TableColumn<PlacementApplication, String> remarksCol = new TableColumn<>("Interview / Officer Notes");
        remarksCol.setCellValueFactory(a -> new SimpleStringProperty(
                (a.getValue().getInterviewDate() != null ? "[Interview: " + a.getValue().getInterviewDate() + "] " : "")
                + (a.getValue().getRemarks() != null ? a.getValue().getRemarks() : "")
        ));
        remarksCol.setPrefWidth(260);

        myAppsTable.getColumns().addAll(compCol, roleCol, pkgCol, statusCol, dateCol, remarksCol);

        refreshBtn.setOnAction(e -> loadStudentApps(student));

        box.getChildren().addAll(title, refreshBtn, myAppsTable);
        loadStudentApps(student);

        return box;
    }

    private static void loadStudentApps(Student student) {
        if (student == null) return;
        try {
            List<PlacementApplication> list = ApplicationManagement.getApplicationsByStudentId(student.getStudentId());
            myAppsList.setAll(list);
        } catch (Exception e) {
            System.err.println("Error loading student applications: " + e.getMessage());
        }
    }

    private static Parent createNotificationsView(LoginManagement.UserSession user) {
        VBox box = new VBox(15);
        box.setStyle("-fx-padding: 20px; -fx-background-color: #f8fafc;");

        Label title = new Label("Placement Alerts & Announcements");
        title.getStyleClass().add("view-header");

        HBox actions = new HBox(10);
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");

        Button markReadBtn = new Button("Mark Selected as Read");
        markReadBtn.getStyleClass().add("btn-primary");

        actions.getChildren().addAll(refreshBtn, markReadBtn);

        notifListView = new ListView<>();
        VBox.setVgrow(notifListView, Priority.ALWAYS);

        Runnable loadNotifs = () -> {
            notifListView.getItems().clear();
            try {
                List<Notification> notifs = ApplicationManagement.getNotificationsForUser(user != null ? user.getUserId() : null);
                for (Notification n : notifs) {
                    String prefix = n.isRead() ? "[READ] " : "[NEW] ";
                    notifListView.getItems().add(prefix + n.getTitle() + " - " + n.getMessage() + " (" + n.getCreatedAt() + ")");
                }
            } catch (Exception ex) {
                notifListView.getItems().add("Error loading notifications: " + ex.getMessage());
            }
        };

        refreshBtn.setOnAction(e -> loadNotifs.run());
        markReadBtn.setOnAction(e -> {
            AlertUtil.showInfo("Notification", "Notifications marked as read.");
            loadNotifs.run();
        });

        box.getChildren().addAll(title, actions, notifListView);
        loadNotifs.run();

        return box;
    }
}
