package com.campusplacement.screens;

import com.campusplacement.concurrency.BackgroundTasks;
import com.campusplacement.utils.AlertUtil;
import java.io.File;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * Report Generation and Export Screen.
 * Demonstrates FEATURE 2: Background Multithreaded CSV & PDF Report Generation
 * using JavaFX Task and ExecutorService.
 */
public class ReportScreen {

    public static Parent createContent() {
        BorderPane root = new BorderPane();
        root.setLeft(DashboardScreen.createSidebar("Reports"));

        VBox contentBox = new VBox(20);
        contentBox.getStyleClass().add("content-area");

        // Header
        VBox titleBox = new VBox(3);
        Label title = new Label("Placement Reports & Document Export");
        title.getStyleClass().add("view-header");
        Label subtitle = new Label("Generate official audit reports, selection summaries, and corporate reports in CSV and PDF formats.");
        subtitle.getStyleClass().add("view-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // Form Card for Report Configuration
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-padding: 25px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        Label reportTypeLbl = new Label("Select Report Type:");
        reportTypeLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        ComboBox<String> reportTypeBox = new ComboBox<>();
        reportTypeBox.getItems().addAll(
                "Complete Student List",
                "Selected Students Report",
                "Rejected Applications Report",
                "Company Placement Report"
        );
        reportTypeBox.setValue("Selected Students Report");
        reportTypeBox.setPrefWidth(280);

        Label formatLbl = new Label("Export File Format:");
        formatLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton pdfRadio = new RadioButton("PDF Document (.pdf)");
        pdfRadio.setToggleGroup(formatGroup);
        pdfRadio.setSelected(true);

        RadioButton csvRadio = new RadioButton("CSV Spreadsheet (.csv)");
        csvRadio.setToggleGroup(formatGroup);

        HBox formatBox = new HBox(15, pdfRadio, csvRadio);

        grid.add(reportTypeLbl, 0, 0);
        grid.add(reportTypeBox, 1, 0);
        grid.add(formatLbl, 0, 1);
        grid.add(formatBox, 1, 1);

        // Progress Section
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);

        Label progressStatus = new Label();
        progressStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #2563eb; -fx-font-weight: bold;");
        progressStatus.setVisible(false);

        // Action Button
        Button generateBtn = new Button("Generate & Export Report");
        generateBtn.getStyleClass().add("btn-primary");
        generateBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10px 24px;");

        generateBtn.setOnAction(e -> {
            String selectedType = reportTypeBox.getValue();
            String format = pdfRadio.isSelected() ? "PDF" : "CSV";

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Generated Placement Report");
            String defaultFileName = selectedType.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ("PDF".equalsIgnoreCase(format) ? ".pdf" : ".csv");
            fileChooser.setInitialFileName(defaultFileName);

            if ("PDF".equalsIgnoreCase(format)) {
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents (*.pdf)", "*.pdf"));
            } else {
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Spreadsheets (*.csv)", "*.csv"));
            }

            File chosenFile = fileChooser.showSaveDialog(null);
            if (chosenFile == null) return;

            // Multithreaded Execution (FEATURE 2)
            progressBar.setVisible(true);
            progressStatus.setVisible(true);
            generateBtn.setDisable(true);

            Task<String> reportTask = BackgroundTasks.createReportGenerationTask(selectedType, format, chosenFile);

            // Bind UI Progress Bar to Task Progress Property
            progressBar.progressProperty().bind(reportTask.progressProperty());
            progressStatus.textProperty().bind(reportTask.messageProperty());

            reportTask.setOnSucceeded(event -> {
                progressBar.progressProperty().unbind();
                progressStatus.textProperty().unbind();
                progressBar.setVisible(false);
                generateBtn.setDisable(false);
                progressStatus.setText("File exported: " + chosenFile.getName());

                AlertUtil.showInfo(
                        "Report Export Complete",
                        "The report has been successfully generated in the background without blocking the UI!\nSaved to: " + chosenFile.getAbsolutePath()
                );
            });

            reportTask.setOnFailed(event -> {
                progressBar.progressProperty().unbind();
                progressStatus.textProperty().unbind();
                progressBar.setVisible(false);
                generateBtn.setDisable(false);
                progressStatus.setText("Failed to generate report.");

                AlertUtil.showError(
                        "Generation Failed",
                        "An error occurred during report export: " + reportTask.getException().getMessage()
                );
            });

            // Run in background thread pool
            BackgroundTasks.execute(reportTask);
        });

        card.getChildren().addAll(grid, progressBar, progressStatus, generateBtn);

        // Information Callout Box
        VBox infoBox = new VBox(6);
        infoBox.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 8px; -fx-padding: 15px; -fx-border-color: #bfdbfe; -fx-border-radius: 8px;");
        Label infoTitle = new Label("Multithreading in Action (Viva Topic):");
        infoTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e40af;");
        Label infoDesc = new Label("When 'Generate & Export Report' is clicked, a JavaFX Task is submitted to an ExecutorService worker thread.\n"
                + "Heavy database queries and PDF page generation execute completely off the JavaFX Application Thread.\n"
                + "The UI remains fully responsive: you can interact with other screens or controls while generation finishes.");
        infoDesc.setStyle("-fx-text-fill: #1e3a8a; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(infoTitle, infoDesc);

        contentBox.getChildren().addAll(titleBox, card, infoBox);
        root.setCenter(contentBox);

        return root;
    }
}
