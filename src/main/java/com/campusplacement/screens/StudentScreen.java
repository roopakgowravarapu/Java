package com.campusplacement.screens;

import com.campusplacement.concurrency.BackgroundTasks;
import com.campusplacement.management.StudentManagement;
import com.campusplacement.model.Student;
import com.campusplacement.utils.AlertUtil;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Complete Student Management Screen.
 * Demonstrates JavaFX TableView, Event Handling, and Asynchronous Background Querying.
 */
public class StudentScreen {

    private static TableView<Student> studentTable;
    private static ObservableList<Student> studentList = FXCollections.observableArrayList();
    private static ProgressBar loadingBar;
    private static Label statusLabel;
    private static TextField searchField;
    private static ComboBox<String> deptFilter;

    public static Parent createContent() {
        BorderPane root = new BorderPane();
        root.setLeft(DashboardScreen.createSidebar("Students"));

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-area");

        // Header Title
        VBox titleBox = new VBox(3);
        Label title = new Label("Student Information Directory");
        title.getStyleClass().add("view-header");
        Label subtitle = new Label("Manage registered student academic profiles, eligibility criteria, and skillsets.");
        subtitle.getStyleClass().add("view-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // Action Toolbar (Search, Filter, Add, Edit, Delete)
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by Name, Roll No or Skill...");
        searchField.getStyleClass().add("search-field");

        deptFilter = new ComboBox<>();
        deptFilter.getItems().addAll(
                "All Departments",
                "Computer Science",
                "Information Tech",
                "Electronics & Comm",
                "Electrical Eng",
                "Mechanical Eng"
        );
        deptFilter.setValue("All Departments");

        Button addBtn = new Button("Add Student");
        addBtn.getStyleClass().add("btn-primary");

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-secondary");

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-danger");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");

        toolbar.getChildren().addAll(searchField, deptFilter, addBtn, editBtn, deleteBtn, refreshBtn);

        // Loading and Status Bar
        loadingBar = new ProgressBar();
        loadingBar.setMaxWidth(Double.MAX_VALUE);
        loadingBar.setVisible(false);

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        // TableView Configuration
        studentTable = new TableView<>();
        studentTable.setItems(studentList);
        VBox.setVgrow(studentTable, Priority.ALWAYS);

        TableColumn<Student, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStudentId()));
        idCol.setPrefWidth(50);

        TableColumn<Student, String> rollCol = new TableColumn<>("Roll Number");
        rollCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRollNumber()));
        rollCol.setPrefWidth(100);

        TableColumn<Student, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        nameCol.setPrefWidth(150);

        TableColumn<Student, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        emailCol.setPrefWidth(180);

        TableColumn<Student, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhoneNumber()));
        phoneCol.setPrefWidth(100);

        TableColumn<Student, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartment()));
        deptCol.setPrefWidth(140);

        TableColumn<Student, Number> cgpaCol = new TableColumn<>("CGPA");
        cgpaCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCgpa()));
        cgpaCol.setPrefWidth(70);

        TableColumn<Student, Number> yearCol = new TableColumn<>("Grad Year");
        yearCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getGraduationYear()));
        yearCol.setPrefWidth(80);

        TableColumn<Student, String> skillsCol = new TableColumn<>("Skills");
        skillsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSkills()));
        skillsCol.setPrefWidth(180);

        TableColumn<Student, Number> backlogsCol = new TableColumn<>("Backlogs");
        backlogsCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getBacklogCount()));
        backlogsCol.setPrefWidth(70);

        studentTable.getColumns().addAll(idCol, rollCol, nameCol, emailCol, phoneCol, deptCol, cgpaCol, yearCol, skillsCol, backlogsCol);

        // EVENT HANDLERS
        // 1. Dynamic Search Listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadStudentsAsync());

        // 2. Department Filter Event
        deptFilter.setOnAction(e -> loadStudentsAsync());

        // 3. Refresh Action
        refreshBtn.setOnAction(e -> loadStudentsAsync());

        // 4. Add Student Action
        addBtn.setOnAction(e -> showStudentForm(null));

        // 5. Edit Student Action
        editBtn.setOnAction(e -> {
            Student selected = studentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a student from the table to edit.");
                return;
            }
            showStudentForm(selected);
        });

        // 6. Delete Student Action
        deleteBtn.setOnAction(e -> {
            Student selected = studentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a student to delete.");
                return;
            }

            boolean confirmed = AlertUtil.showConfirmation(
                    "Delete Student Record",
                    "Are you sure you want to delete student: " + selected.getFullName() + " (" + selected.getRollNumber() + ")?\nAll associated applications will also be removed."
            );

            if (confirmed) {
                try {
                    boolean success = StudentManagement.deleteStudent(selected.getStudentId());
                    if (success) {
                        AlertUtil.showInfo("Deleted", "Student record successfully deleted.");
                        loadStudentsAsync();
                    }
                } catch (Exception ex) {
                    AlertUtil.showError("Delete Error", "Cannot delete student: " + ex.getMessage());
                }
            }
        });

        contentBox.getChildren().addAll(titleBox, toolbar, loadingBar, statusLabel, studentTable);
        root.setCenter(contentBox);

        // Initial Data Load
        loadStudentsAsync();

        return root;
    }

    /**
     * FEATURE 1: Background Multithreaded Database Search.
     */
    private static void loadStudentsAsync() {
        loadingBar.setVisible(true);
        statusLabel.setText("Querying MySQL database...");

        String kw = searchField != null ? searchField.getText() : "";
        String dept = deptFilter != null ? deptFilter.getValue() : "All Departments";

        Task<List<Student>> task = BackgroundTasks.createStudentSearchTask(kw, dept);
        task.setOnSucceeded(e -> {
            List<Student> results = task.getValue();
            studentList.setAll(results);
            loadingBar.setVisible(false);
            statusLabel.setText("Displaying " + results.size() + " student records.");
        });

        task.setOnFailed(e -> {
            loadingBar.setVisible(false);
            statusLabel.setText("Error loading student data.");
            AlertUtil.showError("Database Error", "Failed to retrieve students: " + task.getException().getMessage());
        });

        BackgroundTasks.execute(task);
    }

    /**
     * Displays Modal Form to Add or Edit a Student.
     */
    private static void showStudentForm(Student studentToEdit) {
        boolean isEdit = (studentToEdit != null);
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Student Details" : "Add New Student");
        dialog.setHeaderText(isEdit ? "Modify student academic profile." : "Enter student registration details.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField rollField = new TextField();
        rollField.setPromptText("e.g. 24CS109");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("student@college.edu");

        TextField phoneField = new TextField();
        phoneField.setPromptText("10-digit number");

        ComboBox<String> deptBox = new ComboBox<>();
        deptBox.getItems().addAll("Computer Science", "Information Tech", "Electronics & Comm", "Electrical Eng", "Mechanical Eng");
        deptBox.setValue("Computer Science");

        TextField cgpaField = new TextField();
        cgpaField.setPromptText("0.00 to 10.00");

        TextField yearField = new TextField();
        yearField.setPromptText("e.g. 2026");

        TextField skillsField = new TextField();
        skillsField.setPromptText("Comma-separated skills (e.g. Java, Python, SQL)");

        TextField backlogsField = new TextField("0");

        if (isEdit) {
            rollField.setText(studentToEdit.getRollNumber());
            nameField.setText(studentToEdit.getFullName());
            emailField.setText(studentToEdit.getEmail());
            phoneField.setText(studentToEdit.getPhoneNumber());
            deptBox.setValue(studentToEdit.getDepartment());
            cgpaField.setText(String.valueOf(studentToEdit.getCgpa()));
            yearField.setText(String.valueOf(studentToEdit.getGraduationYear()));
            skillsField.setText(studentToEdit.getSkills());
            backlogsField.setText(String.valueOf(studentToEdit.getBacklogCount()));
        }

        grid.add(new Label("Roll Number:"), 0, 0);
        grid.add(rollField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Department:"), 0, 4);
        grid.add(deptBox, 1, 4);
        grid.add(new Label("CGPA:"), 0, 5);
        grid.add(cgpaField, 1, 5);
        grid.add(new Label("Graduation Year:"), 0, 6);
        grid.add(yearField, 1, 6);
        grid.add(new Label("Skills:"), 0, 7);
        grid.add(skillsField, 1, 7);
        grid.add(new Label("Backlogs:"), 0, 8);
        grid.add(backlogsField, 1, 8);

        Label errorMsg = new Label();
        errorMsg.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
        errorMsg.setWrapText(true);
        errorMsg.setVisible(false);
        grid.add(errorMsg, 0, 9, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            errorMsg.setVisible(false);

            String email = emailField.getText();
            if (!com.campusplacement.utils.ValidationUtil.isValidEmail(email)) {
                errorMsg.setText("Invalid email format! Example of valid format: student@college.edu");
                errorMsg.setVisible(true);
                emailField.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px;");
                AlertUtil.showError("Invalid Email Format", "The email address is invalid!\nPlease enter a valid email format (e.g. name@domain.com) to continue.");
                emailField.requestFocus();
                event.consume(); // PREVENTS DIALOG FROM CLOSING!
                return;
            } else {
                emailField.setStyle("");
            }

            try {
                double cgpa = Double.parseDouble(cgpaField.getText().trim());
                int year = Integer.parseInt(yearField.getText().trim());
                int backlogs = Integer.parseInt(backlogsField.getText().trim());

                Student s = isEdit ? studentToEdit : new Student();
                s.setRollNumber(rollField.getText());
                s.setFullName(nameField.getText());
                s.setEmail(emailField.getText());
                s.setPhoneNumber(phoneField.getText());
                s.setDepartment(deptBox.getValue());
                s.setCgpa(cgpa);
                s.setGraduationYear(year);
                s.setSkills(skillsField.getText());
                s.setBacklogCount(backlogs);

                boolean ok = isEdit ? StudentManagement.updateStudent(s) : StudentManagement.addStudent(s);
                if (!ok) {
                    errorMsg.setText("Could not save student record to database.");
                    errorMsg.setVisible(true);
                    event.consume();
                }
            } catch (NumberFormatException nfe) {
                errorMsg.setText("CGPA, Year, and Backlogs must be valid numeric values.");
                errorMsg.setVisible(true);
                AlertUtil.showError("Input Format Error", "CGPA, Graduation Year, and Backlogs must be valid numbers.");
                event.consume(); // PREVENTS DIALOG FROM CLOSING!
            } catch (Exception ex) {
                errorMsg.setText("Error: " + ex.getMessage());
                errorMsg.setVisible(true);
                AlertUtil.showError("Validation Error", ex.getMessage());
                event.consume(); // PREVENTS DIALOG FROM CLOSING!
            }
        });

        dialog.setResultConverter(btnType -> btnType == javafx.scene.control.ButtonType.OK);

        dialog.showAndWait().ifPresent(confirmed -> {
            if (Boolean.TRUE.equals(confirmed)) {
                AlertUtil.showInfo("Success", isEdit ? "Student record updated." : "New student registered successfully.");
                loadStudentsAsync();
            }
        });
    }
}
