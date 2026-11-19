package com.campusplacement.screens;

import com.campusplacement.management.ApplicationManagement;
import com.campusplacement.management.LoginManagement;
import com.campusplacement.model.PlacementApplication;
import com.campusplacement.utils.AlertUtil;
import java.time.LocalDate;
import java.util.ArrayList;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Application Management Screen.
 * Implements status workflow transitions, interview scheduling, offer releases,
 * and immutable audit history logging.
 */
public class ApplicationScreen {

    private static TableView<PlacementApplication> appTable;
    private static ObservableList<PlacementApplication> appList = FXCollections.observableArrayList();
    private static TextField searchField;
    private static ComboBox<String> statusFilter;

    public static Parent createContent() {
        BorderPane root = new BorderPane();
        root.setLeft(DashboardScreen.createSidebar("Applications"));

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-area");

        // Header Title
        VBox titleBox = new VBox(3);
        Label title = new Label("Student Placement Applications");
        title.getStyleClass().add("view-header");
        Label subtitle = new Label("Review student candidacy, transition recruitment stages, schedule interviews, and log audit history.");
        subtitle.getStyleClass().add("view-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by Student, Roll, Company, Role...");
        searchField.getStyleClass().add("search-field");

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Statuses", "Applied", "Under Review", "Shortlisted", "Interview Scheduled", "Selected", "Rejected");
        statusFilter.setValue("All Statuses");

        Button updateStatusBtn = new Button("Update Status");
        updateStatusBtn.getStyleClass().add("btn-primary");

        Button historyBtn = new Button("View Audit History");
        historyBtn.getStyleClass().add("btn-secondary");

        Button deleteAppBtn = new Button("Delete Application");
        deleteAppBtn.getStyleClass().add("btn-danger");

        Button selectAllBtn = new Button("Select All");
        selectAllBtn.getStyleClass().add("btn-secondary");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");

        toolbar.getChildren().addAll(searchField, statusFilter, updateStatusBtn, historyBtn, deleteAppBtn, selectAllBtn, refreshBtn);

        // TableView Configuration
        appTable = new TableView<>();
        appTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        appTable.setItems(appList);
        VBox.setVgrow(appTable, Priority.ALWAYS);

        TableColumn<PlacementApplication, Number> idCol = new TableColumn<>("App ID");
        idCol.setCellValueFactory(a -> new SimpleIntegerProperty(a.getValue().getApplicationId()));
        idCol.setPrefWidth(60);

        TableColumn<PlacementApplication, String> studentCol = new TableColumn<>("Student Name");
        studentCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getStudentName()));
        studentCol.setPrefWidth(150);

        TableColumn<PlacementApplication, String> rollCol = new TableColumn<>("Roll No");
        rollCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getStudentRollNumber()));
        rollCol.setPrefWidth(90);

        TableColumn<PlacementApplication, String> deptCol = new TableColumn<>("Dept");
        deptCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getStudentDepartment()));
        deptCol.setPrefWidth(120);

        TableColumn<PlacementApplication, Number> cgpaCol = new TableColumn<>("CGPA");
        cgpaCol.setCellValueFactory(a -> new SimpleDoubleProperty(a.getValue().getStudentCgpa()));
        cgpaCol.setPrefWidth(65);

        TableColumn<PlacementApplication, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getCompanyName()));
        compCol.setPrefWidth(160);

        TableColumn<PlacementApplication, String> roleCol = new TableColumn<>("Job Role");
        roleCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getJobRole()));
        roleCol.setPrefWidth(160);

        TableColumn<PlacementApplication, Number> pkgCol = new TableColumn<>("Package (LPA)");
        pkgCol.setCellValueFactory(a -> new SimpleDoubleProperty(a.getValue().getPackageLpa()));
        pkgCol.setPrefWidth(95);

        TableColumn<PlacementApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getStatus()));
        statusCol.setPrefWidth(130);

        TableColumn<PlacementApplication, String> dateCol = new TableColumn<>("Applied Date");
        dateCol.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getApplicationDate()));
        dateCol.setPrefWidth(130);

        TableColumn<PlacementApplication, String> remarksCol = new TableColumn<>("Remarks / Interview");
        remarksCol.setCellValueFactory(a -> new SimpleStringProperty(
                (a.getValue().getInterviewDate() != null ? "[Interview: " + a.getValue().getInterviewDate() + "] " : "") 
                + (a.getValue().getRemarks() != null ? a.getValue().getRemarks() : "")
        ));
        remarksCol.setPrefWidth(220);

        appTable.getColumns().addAll(idCol, studentCol, rollCol, deptCol, cgpaCol, compCol, roleCol, pkgCol, statusCol, dateCol, remarksCol);

        // EVENT HANDLERS
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadApplications());
        statusFilter.setOnAction(e -> loadApplications());
        refreshBtn.setOnAction(e -> loadApplications());

        updateStatusBtn.setOnAction(e -> {
            PlacementApplication selected = appTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select an application to update its recruitment status.");
                return;
            }
            showStatusDialog(selected);
        });

        historyBtn.setOnAction(e -> {
            PlacementApplication selected = appTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select an application to view its status history audit trail.");
                return;
            }
            showHistoryDialog(selected);
        });

        selectAllBtn.setOnAction(e -> {
            if (appTable.getSelectionModel().getSelectedItems().size() == appList.size() && !appList.isEmpty()) {
                appTable.getSelectionModel().clearSelection();
                selectAllBtn.setText("Select All");
            } else {
                appTable.getSelectionModel().selectAll();
                selectAllBtn.setText("Deselect All");
            }
        });

        deleteAppBtn.setOnAction(e -> {
            ObservableList<PlacementApplication> selected = appTable.getSelectionModel().getSelectedItems();
            if (selected.isEmpty()) {
                AlertUtil.showWarning("No Selection", "Please select one or more applications from the table to delete.");
                return;
            }
            int count = selected.size();
            String confirmPrompt = (count == 1)
                    ? "Are you sure you want to delete application #" + selected.get(0).getApplicationId() + " for " + selected.get(0).getStudentName() + " (" + selected.get(0).getCompanyName() + " - " + selected.get(0).getJobRole() + ")?\nThis action cannot be undone."
                    : "Are you sure you want to delete ALL " + count + " selected application records?\nThis action cannot be undone.";

            boolean confirm = AlertUtil.showConfirmation("Delete Application Record(s)", confirmPrompt);
            if (confirm) {
                try {
                    List<PlacementApplication> toDelete = new ArrayList<>(selected);
                    int delCount = 0;
                    for (PlacementApplication a : toDelete) {
                        if (ApplicationManagement.deleteApplication(a.getApplicationId())) {
                            delCount++;
                        }
                    }
                    AlertUtil.showInfo("Application(s) Deleted", "Successfully removed " + delCount + " application(s).");
                    selectAllBtn.setText("Select All");
                    loadApplications();
                } catch (Exception ex) {
                    AlertUtil.showError("Delete Error", "Cannot delete application(s): " + ex.getMessage());
                }
            }
        });

        contentBox.getChildren().addAll(titleBox, toolbar, appTable);
        root.setCenter(contentBox);

        loadApplications();
        return root;
    }

    private static void loadApplications() {
        try {
            String kw = searchField != null ? searchField.getText() : "";
            String status = statusFilter != null ? statusFilter.getValue() : "All Statuses";
            List<PlacementApplication> list = ApplicationManagement.searchApplications(kw, status);
            appList.setAll(list);
        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Failed to load applications: " + e.getMessage());
        }
    }

    private static void showStatusDialog(PlacementApplication app) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Update Application Status - " + app.getStudentName());
        dialog.setHeaderText("Current Status: " + app.getStatus() + " | Company: " + app.getCompanyName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        ComboBox<String> newStatusBox = new ComboBox<>();
        newStatusBox.getItems().addAll("Under Review", "Shortlisted", "Interview Scheduled", "Selected", "Rejected");
        newStatusBox.setValue(app.getStatus());

        DatePicker interviewDatePicker = new DatePicker(LocalDate.now().plusDays(2));
        TextField timeField = new TextField("10:00 AM");

        TextField commentsField = new TextField();
        commentsField.setPromptText("Enter stage notes or feedback...");

        HBox dtBox = new HBox(8, interviewDatePicker, timeField);

        Label dtLabel = new Label("Interview Date & Time:");
        dtLabel.setVisible("Interview Scheduled".equalsIgnoreCase(app.getStatus()));
        dtBox.setVisible("Interview Scheduled".equalsIgnoreCase(app.getStatus()));

        newStatusBox.setOnAction(e -> {
            boolean isSched = "Interview Scheduled".equalsIgnoreCase(newStatusBox.getValue());
            dtLabel.setVisible(isSched);
            dtBox.setVisible(isSched);
        });

        grid.add(new Label("New Status:"), 0, 0);
        grid.add(newStatusBox, 1, 0);
        grid.add(dtLabel, 0, 1);
        grid.add(dtBox, 1, 1);
        grid.add(new Label("Comments / Remarks:"), 0, 2);
        grid.add(commentsField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                String nStatus = newStatusBox.getValue();
                String dtStr = null;
                if ("Interview Scheduled".equalsIgnoreCase(nStatus)) {
                    LocalDate d = interviewDatePicker.getValue();
                    dtStr = (d != null ? d.toString() : LocalDate.now().toString()) + " " + timeField.getText().trim();
                }
                String comments = commentsField.getText();
                LoginManagement.UserSession user = LoginManagement.getCurrentUser();
                String officer = (user != null ? user.getUsername() : "Placement Officer");

                boolean ok = ApplicationManagement.updateApplicationStatus(
                        app.getApplicationId(),
                        nStatus,
                        dtStr,
                        comments,
                        officer
                );
                if (!ok) {
                    AlertUtil.showError("Update Error", "Could not update application status.");
                    event.consume();
                }
            } catch (Exception ex) {
                AlertUtil.showError("Update Error", ex.getMessage());
                event.consume(); // KEEP DIALOG OPEN!
            }
        });

        dialog.setResultConverter(btnType -> btnType == javafx.scene.control.ButtonType.OK);

        dialog.showAndWait().ifPresent(confirmed -> {
            if (Boolean.TRUE.equals(confirmed)) {
                AlertUtil.showInfo("Status Updated", "Application status updated and student notified.");
                loadApplications();
            }
        });
    }

    private static void showHistoryDialog(PlacementApplication app) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Status Audit History - App #" + app.getApplicationId());
        dialog.setHeaderText("Candidate: " + app.getStudentName() + " (" + app.getStudentRollNumber() + ") | Role: " + app.getJobRole() + " at " + app.getCompanyName());

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setPrefWidth(550);

        ListView<String> historyList = new ListView<>();
        try {
            List<ApplicationManagement.HistoryEntry> entries = ApplicationManagement.getApplicationHistory(app.getApplicationId());
            for (ApplicationManagement.HistoryEntry h : entries) {
                String line = String.format("[%s] Status: %s -> %s (By: %s) | Note: %s",
                        h.getChangedAt(),
                        (h.getOldStatus() != null ? h.getOldStatus() : "Initial"),
                        h.getNewStatus(),
                        h.getUpdatedBy(),
                        (h.getComments() != null ? h.getComments() : "None"));
                historyList.getItems().add(line);
            }
        } catch (Exception ex) {
            historyList.getItems().add("Error loading history: " + ex.getMessage());
        }

        box.getChildren().add(historyList);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
