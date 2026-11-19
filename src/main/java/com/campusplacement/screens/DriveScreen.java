package com.campusplacement.screens;

import com.campusplacement.management.CompanyManagement;
import com.campusplacement.management.DriveManagement;
import com.campusplacement.management.StudentManagement;
import com.campusplacement.model.Company;
import com.campusplacement.model.PlacementDrive;
import com.campusplacement.model.Student;
import com.campusplacement.utils.AlertUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Placement Drive Management Screen.
 * Includes drive lifecycle management, filtering, and student eligibility computation.
 */
public class DriveScreen {

    private static TableView<PlacementDrive> driveTable;
    private static ObservableList<PlacementDrive> driveList = FXCollections.observableArrayList();
    private static TextField searchField;
    private static ComboBox<String> statusFilter;

    public static Parent createContent() {
        BorderPane root = new BorderPane();
        root.setLeft(DashboardScreen.createSidebar("Drives"));

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-area");

        // Header Title
        VBox titleBox = new VBox(3);
        Label title = new Label("Campus Recruitment Drives");
        title.getStyleClass().add("view-header");
        Label subtitle = new Label("Schedule placement drives, configure CGPA & backlog cutoffs, and monitor deadlines.");
        subtitle.getStyleClass().add("view-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search Drive by Company, Role or Skills...");
        searchField.getStyleClass().add("search-field");

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Statuses", "OPEN", "IN_PROGRESS", "CLOSED");
        statusFilter.setValue("All Statuses");

        Button addBtn = new Button("Schedule New Drive");
        addBtn.getStyleClass().add("btn-primary");

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-secondary");

        Button eligibleBtn = new Button("View Eligible Students");
        eligibleBtn.getStyleClass().add("btn-success");

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-danger");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");

        toolbar.getChildren().addAll(searchField, statusFilter, addBtn, editBtn, eligibleBtn, deleteBtn, refreshBtn);

        // TableView Configuration
        driveTable = new TableView<>();
        driveTable.setItems(driveList);
        VBox.setVgrow(driveTable, Priority.ALWAYS);

        TableColumn<PlacementDrive, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getDriveId()));
        idCol.setPrefWidth(45);

        TableColumn<PlacementDrive, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName()));
        compCol.setPrefWidth(160);

        TableColumn<PlacementDrive, String> roleCol = new TableColumn<>("Job Role");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getJobRole()));
        roleCol.setPrefWidth(170);

        TableColumn<PlacementDrive, Number> pkgCol = new TableColumn<>("Package (LPA)");
        pkgCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPackageLpa()));
        pkgCol.setPrefWidth(100);

        TableColumn<PlacementDrive, Number> cgpaCol = new TableColumn<>("Min CGPA");
        cgpaCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getMinCgpa()));
        cgpaCol.setPrefWidth(80);

        TableColumn<PlacementDrive, String> deptCol = new TableColumn<>("Eligible Depts");
        deptCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEligibleDepartments()));
        deptCol.setPrefWidth(160);

        TableColumn<PlacementDrive, Number> backlogsCol = new TableColumn<>("Max Backlogs");
        backlogsCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getMaxBacklogs()));
        backlogsCol.setPrefWidth(90);

        TableColumn<PlacementDrive, String> deadlineCol = new TableColumn<>("Application Deadline");
        deadlineCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApplicationDeadline()));
        deadlineCol.setPrefWidth(150);

        TableColumn<PlacementDrive, String> statusCol = new TableColumn<>("Drive Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDriveStatus()));
        statusCol.setPrefWidth(95);

        driveTable.getColumns().addAll(idCol, compCol, roleCol, pkgCol, cgpaCol, deptCol, backlogsCol, deadlineCol, statusCol);

        // EVENT HANDLERS
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadDrives());
        statusFilter.setOnAction(e -> loadDrives());
        refreshBtn.setOnAction(e -> loadDrives());

        addBtn.setOnAction(e -> showDriveForm(null));

        editBtn.setOnAction(e -> {
            PlacementDrive selected = driveTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a drive to edit.");
                return;
            }
            showDriveForm(selected);
        });

        eligibleBtn.setOnAction(e -> {
            PlacementDrive selected = driveTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a placement drive to view eligible candidates.");
                return;
            }
            showEligibleStudentsDialog(selected);
        });

        deleteBtn.setOnAction(e -> {
            PlacementDrive selected = driveTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a placement drive to delete.");
                return;
            }
            boolean confirm = AlertUtil.showConfirmation(
                    "Delete Placement Drive",
                    "Are you sure you want to delete Drive #" + selected.getDriveId() + " (" + selected.getCompanyName() + " - " + selected.getJobRole() + ")?"
            );
            if (confirm) {
                try {
                    DriveManagement.deleteDrive(selected.getDriveId());
                    AlertUtil.showInfo("Deleted", "Placement drive deleted successfully.");
                    loadDrives();
                } catch (Exception ex) {
                    AlertUtil.showError("Delete Error", ex.getMessage());
                }
            }
        });

        contentBox.getChildren().addAll(titleBox, toolbar, driveTable);
        root.setCenter(contentBox);

        loadDrives();
        return root;
    }

    private static void loadDrives() {
        try {
            String kw = searchField != null ? searchField.getText() : "";
            List<PlacementDrive> list = DriveManagement.searchDrives(kw, null, null);
            String filter = statusFilter != null ? statusFilter.getValue() : "All Statuses";

            if (!"All Statuses".equalsIgnoreCase(filter)) {
                list.removeIf(d -> !filter.equalsIgnoreCase(d.getDriveStatus()));
            }

            driveList.setAll(list);
        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Failed to load drives: " + e.getMessage());
        }
    }

    private static void showEligibleStudentsDialog(PlacementDrive drive) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Eligible Students - " + drive.getCompanyName() + " (" + drive.getJobRole() + ")");
        dialog.setHeaderText("Criteria: Min CGPA: " + drive.getMinCgpa() + " | Max Backlogs: " + drive.getMaxBacklogs() 
                           + " | Departments: " + drive.getEligibleDepartments());

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setPrefWidth(550);

        ListView<String> listView = new ListView<>();
        try {
            List<Student> allStudents = StudentManagement.getAllStudents();
            int eligibleCount = 0;
            for (Student s : allStudents) {
                DriveManagement.EligibilityResult result = DriveManagement.checkEligibility(s, drive);
                if (result.isEligible()) {
                    listView.getItems().add("[ELIGIBLE] " + s.getFullName() + " (" + s.getRollNumber() + ") - Dept: " + s.getDepartment() + " | CGPA: " + s.getCgpa());
                    eligibleCount++;
                } else {
                    listView.getItems().add("[NOT ELIGIBLE] " + s.getFullName() + " (" + s.getRollNumber() + ") - Reason: " + result.getSummary());
                }
            }
            Label countLbl = new Label("Total Eligible Candidates: " + eligibleCount + " / " + allStudents.size());
            countLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #15803d;");
            box.getChildren().addAll(countLbl, listView);
        } catch (Exception ex) {
            box.getChildren().add(new Label("Error calculating eligibility: " + ex.getMessage()));
        }

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private static void showDriveForm(PlacementDrive driveToEdit) {
        boolean isEdit = (driveToEdit != null);
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Placement Drive" : "Schedule New Placement Drive");
        dialog.setHeaderText(isEdit ? "Update drive criteria and schedule." : "Enter placement drive job details and eligibility rules.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        ComboBox<Company> companyBox = new ComboBox<>();
        try {
            companyBox.getItems().addAll(CompanyManagement.getAllCompanies());
            if (!companyBox.getItems().isEmpty()) companyBox.setValue(companyBox.getItems().get(0));
        } catch (Exception ignored) {}

        TextField roleField = new TextField();
        roleField.setPromptText("e.g. Software Development Engineer");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Job description and expectations...");
        descArea.setPrefRowCount(3);

        TextField pkgField = new TextField();
        pkgField.setPromptText("Package in LPA (e.g. 14.50)");

        TextField cgpaField = new TextField("6.50");
        cgpaField.setPromptText("Minimum required CGPA");

        TextField deptsField = new TextField("Computer Science,Information Tech");
        deptsField.setPromptText("Comma-separated eligible departments");

        TextField backlogsField = new TextField("0");

        TextField skillsField = new TextField();
        skillsField.setPromptText("Comma-separated skills (e.g. Java, Cloud, SQL)");

        DatePicker deadlinePicker = new DatePicker(LocalDate.now().plusDays(7));

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("OPEN", "IN_PROGRESS", "CLOSED");
        statusBox.setValue("OPEN");

        if (isEdit) {
            for (Company c : companyBox.getItems()) {
                if (c.getCompanyId() == driveToEdit.getCompanyId()) {
                    companyBox.setValue(c);
                    break;
                }
            }
            roleField.setText(driveToEdit.getJobRole());
            descArea.setText(driveToEdit.getJobDescription());
            pkgField.setText(String.valueOf(driveToEdit.getPackageLpa()));
            cgpaField.setText(String.valueOf(driveToEdit.getMinCgpa()));
            deptsField.setText(driveToEdit.getEligibleDepartments());
            backlogsField.setText(String.valueOf(driveToEdit.getMaxBacklogs()));
            skillsField.setText(driveToEdit.getRequiredSkills());
            statusBox.setValue(driveToEdit.getDriveStatus());
            try {
                deadlinePicker.setValue(LocalDate.parse(driveToEdit.getApplicationDeadline().substring(0, 10)));
            } catch (Exception ignored) {}
        }

        grid.add(new Label("Recruiting Company:"), 0, 0);
        grid.add(companyBox, 1, 0);
        grid.add(new Label("Job Role:"), 0, 1);
        grid.add(roleField, 1, 1);
        grid.add(new Label("Job Description:"), 0, 2);
        grid.add(descArea, 1, 2);
        grid.add(new Label("Package (LPA):"), 0, 3);
        grid.add(pkgField, 1, 3);
        grid.add(new Label("Minimum CGPA:"), 0, 4);
        grid.add(cgpaField, 1, 4);
        grid.add(new Label("Eligible Departments:"), 0, 5);
        grid.add(deptsField, 1, 5);
        grid.add(new Label("Max Backlogs Allowed:"), 0, 6);
        grid.add(backlogsField, 1, 6);
        grid.add(new Label("Required Skills:"), 0, 7);
        grid.add(skillsField, 1, 7);
        grid.add(new Label("Application Deadline:"), 0, 8);
        grid.add(deadlinePicker, 1, 8);
        grid.add(new Label("Drive Status:"), 0, 9);
        grid.add(statusBox, 1, 9);

        Label errorMsg = new Label();
        errorMsg.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
        errorMsg.setWrapText(true);
        errorMsg.setVisible(false);
        grid.add(errorMsg, 0, 10, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            errorMsg.setVisible(false);
            try {
                Company selComp = companyBox.getValue();
                if (selComp == null) {
                    errorMsg.setText("Please select a recruiting company.");
                    errorMsg.setVisible(true);
                    AlertUtil.showError("Validation Error", "Please select a recruiting company.");
                    event.consume();
                    return;
                }

                double pkg = Double.parseDouble(pkgField.getText().trim());
                double cgpa = Double.parseDouble(cgpaField.getText().trim());
                int backlogs = Integer.parseInt(backlogsField.getText().trim());
                LocalDate dDate = deadlinePicker.getValue();
                String deadlineStr = (dDate != null ? dDate.toString() : LocalDate.now().plusDays(7).toString()) + " 23:59:00";

                PlacementDrive d = isEdit ? driveToEdit : new PlacementDrive();
                d.setCompanyId(selComp.getCompanyId());
                d.setJobRole(roleField.getText());
                d.setJobDescription(descArea.getText());
                d.setPackageLpa(pkg);
                d.setMinCgpa(cgpa);
                d.setEligibleDepartments(deptsField.getText());
                d.setMaxBacklogs(backlogs);
                d.setRequiredSkills(skillsField.getText());
                d.setApplicationDeadline(deadlineStr);
                d.setDriveStatus(statusBox.getValue());

                boolean ok = isEdit ? DriveManagement.updateDrive(d) : DriveManagement.addDrive(d);
                if (!ok) {
                    errorMsg.setText("Could not save placement drive to database.");
                    errorMsg.setVisible(true);
                    event.consume();
                }
            } catch (NumberFormatException nfe) {
                errorMsg.setText("Package, CGPA, and Backlogs must be valid numeric values.");
                errorMsg.setVisible(true);
                AlertUtil.showError("Invalid Number", "Package, CGPA, and Backlogs must be valid numbers.");
                event.consume(); // KEEP DIALOG OPEN!
            } catch (Exception ex) {
                errorMsg.setText("Error: " + ex.getMessage());
                errorMsg.setVisible(true);
                AlertUtil.showError("Save Error", ex.getMessage());
                event.consume(); // KEEP DIALOG OPEN!
            }
        });

        dialog.setResultConverter(btnType -> btnType == javafx.scene.control.ButtonType.OK);

        dialog.showAndWait().ifPresent(confirmed -> {
            if (Boolean.TRUE.equals(confirmed)) {
                AlertUtil.showInfo("Success", isEdit ? "Placement drive updated." : "New placement drive scheduled.");
                loadDrives();
            }
        });
    }
}
