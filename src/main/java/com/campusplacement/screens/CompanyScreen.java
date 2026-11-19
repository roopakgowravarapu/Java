package com.campusplacement.screens;

import com.campusplacement.management.CompanyManagement;
import com.campusplacement.model.Company;
import com.campusplacement.utils.AlertUtil;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Recruiting Company Management Screen.
 * Demonstrates CRUD operations and event handling in JavaFX.
 */
public class CompanyScreen {

    private static TableView<Company> companyTable;
    private static ObservableList<Company> companyList = FXCollections.observableArrayList();
    private static TextField searchField;
    private static ComboBox<String> industryFilter;

    public static Parent createContent() {
        BorderPane root = new BorderPane();
        root.setLeft(DashboardScreen.createSidebar("Companies"));

        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("content-area");

        // Header Title
        VBox titleBox = new VBox(3);
        Label title = new Label("Recruiting Corporate Partners");
        title.getStyleClass().add("view-header");
        Label subtitle = new Label("Manage visiting recruiters, industry domains, and corporate HR contacts.");
        subtitle.getStyleClass().add("view-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search Company, Location or HR...");
        searchField.getStyleClass().add("search-field");

        industryFilter = new ComboBox<>();
        industryFilter.getItems().addAll(
                "All Industries",
                "Technology",
                "Software",
                "IT Services",
                "Consulting & IT",
                "Cloud & E-Commerce",
                "Finance"
        );
        industryFilter.setValue("All Industries");

        Button addBtn = new Button("Add Company");
        addBtn.getStyleClass().add("btn-primary");

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-secondary");

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-danger");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");

        toolbar.getChildren().addAll(searchField, industryFilter, addBtn, editBtn, deleteBtn, refreshBtn);

        // TableView Configuration
        companyTable = new TableView<>();
        companyTable.setItems(companyList);
        VBox.setVgrow(companyTable, Priority.ALWAYS);

        TableColumn<Company, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCompanyId()));
        idCol.setPrefWidth(50);

        TableColumn<Company, String> nameCol = new TableColumn<>("Company Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName()));
        nameCol.setPrefWidth(180);

        TableColumn<Company, String> indCol = new TableColumn<>("Industry");
        indCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIndustry()));
        indCol.setPrefWidth(140);

        TableColumn<Company, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));
        locCol.setPrefWidth(140);

        TableColumn<Company, String> webCol = new TableColumn<>("Website");
        webCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getWebsite()));
        webCol.setPrefWidth(180);

        TableColumn<Company, String> hrCol = new TableColumn<>("HR Contact");
        hrCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHrContact()));
        hrCol.setPrefWidth(130);

        TableColumn<Company, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        emailCol.setPrefWidth(180);

        companyTable.getColumns().addAll(idCol, nameCol, indCol, locCol, webCol, hrCol, emailCol);

        // EVENT HANDLERS
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadCompanies());
        industryFilter.setOnAction(e -> loadCompanies());
        refreshBtn.setOnAction(e -> loadCompanies());

        addBtn.setOnAction(e -> showCompanyForm(null));

        editBtn.setOnAction(e -> {
            Company selected = companyTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a company to edit.");
                return;
            }
            showCompanyForm(selected);
        });

        deleteBtn.setOnAction(e -> {
            Company selected = companyTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a company to delete.");
                return;
            }
            boolean confirm = AlertUtil.showConfirmation(
                    "Delete Company",
                    "Are you sure you want to delete company: " + selected.getCompanyName() + "?\nAll drives and applications for this company will be removed."
            );
            if (confirm) {
                try {
                    CompanyManagement.deleteCompany(selected.getCompanyId());
                    AlertUtil.showInfo("Deleted", "Company removed successfully.");
                    loadCompanies();
                } catch (Exception ex) {
                    AlertUtil.showError("Delete Error", ex.getMessage());
                }
            }
        });

        contentBox.getChildren().addAll(titleBox, toolbar, companyTable);
        root.setCenter(contentBox);

        loadCompanies();
        return root;
    }

    private static void loadCompanies() {
        try {
            String kw = searchField != null ? searchField.getText() : "";
            String ind = industryFilter != null ? industryFilter.getValue() : "All Industries";
            List<Company> list = CompanyManagement.searchCompanies(kw, ind);
            companyList.setAll(list);
        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Failed to load companies: " + e.getMessage());
        }
    }

    private static void showCompanyForm(Company companyToEdit) {
        boolean isEdit = (companyToEdit != null);
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Company Details" : "Register New Recruiting Partner");
        dialog.setHeaderText(isEdit ? "Modify corporate partner information." : "Enter new recruiting company details.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Amazon Web Services");

        TextField indField = new TextField();
        indField.setPromptText("e.g. Technology / Cloud");

        TextField locField = new TextField();
        locField.setPromptText("e.g. Bangalore, India");

        TextField webField = new TextField();
        webField.setPromptText("https://careers.example.com");

        TextField hrField = new TextField();
        hrField.setPromptText("HR Lead Name");

        TextField emailField = new TextField();
        emailField.setPromptText("campus-recruitment@company.com");

        if (isEdit) {
            nameField.setText(companyToEdit.getCompanyName());
            indField.setText(companyToEdit.getIndustry());
            locField.setText(companyToEdit.getLocation());
            webField.setText(companyToEdit.getWebsite());
            hrField.setText(companyToEdit.getHrContact());
            emailField.setText(companyToEdit.getEmail());
        }

        grid.add(new Label("Company Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Industry Domain:"), 0, 1);
        grid.add(indField, 1, 1);
        grid.add(new Label("Location / City:"), 0, 2);
        grid.add(locField, 1, 2);
        grid.add(new Label("Website URL:"), 0, 3);
        grid.add(webField, 1, 3);
        grid.add(new Label("HR Contact Person:"), 0, 4);
        grid.add(hrField, 1, 4);
        grid.add(new Label("HR Contact Email:"), 0, 5);
        grid.add(emailField, 1, 5);

        Label errorMsg = new Label();
        errorMsg.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");
        errorMsg.setWrapText(true);
        errorMsg.setVisible(false);
        grid.add(errorMsg, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            errorMsg.setVisible(false);
            String email = emailField.getText();

            // Validate Email format
            if (!com.campusplacement.utils.ValidationUtil.isValidEmail(email)) {
                errorMsg.setText("Invalid email format! Please enter a valid email address (e.g. contact@company.com).");
                errorMsg.setVisible(true);
                emailField.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px;");
                AlertUtil.showError("Invalid Email Format", "The email address is not in a valid format!\nExample of valid format: name@company.com\n\nPlease correct it to continue.");
                emailField.requestFocus();
                event.consume(); // PREVENTS DIALOG FROM CLOSING!
                return;
            } else {
                emailField.setStyle("");
            }

            try {
                Company c = isEdit ? companyToEdit : new Company();
                c.setCompanyName(nameField.getText());
                c.setIndustry(indField.getText());
                c.setLocation(locField.getText());
                c.setWebsite(webField.getText());
                c.setHrContact(hrField.getText());
                c.setEmail(emailField.getText());

                boolean ok = isEdit ? CompanyManagement.updateCompany(c) : CompanyManagement.addCompany(c);
                if (!ok) {
                    errorMsg.setText("Could not save company record to database.");
                    errorMsg.setVisible(true);
                    event.consume();
                }
            } catch (Exception ex) {
                errorMsg.setText("Error: " + ex.getMessage());
                errorMsg.setVisible(true);
                AlertUtil.showError("Validation Error", ex.getMessage());
                event.consume(); // KEEP DIALOG OPEN!
            }
        });

        dialog.setResultConverter(btnType -> btnType == javafx.scene.control.ButtonType.OK);

        dialog.showAndWait().ifPresent(confirmed -> {
            if (Boolean.TRUE.equals(confirmed)) {
                AlertUtil.showInfo("Success", isEdit ? "Company profile updated." : "New company registered.");
                loadCompanies();
            }
        });
    }
}
