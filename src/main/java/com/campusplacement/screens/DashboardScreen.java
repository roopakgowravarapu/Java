package com.campusplacement.screens;

import com.campusplacement.MainApp;
import com.campusplacement.concurrency.BackgroundTasks;
import com.campusplacement.concurrency.DeadlineMonitor;
import com.campusplacement.management.ApplicationManagement;
import com.campusplacement.management.LoginManagement;
import com.campusplacement.management.ReportManagement;
import com.campusplacement.utils.AlertUtil;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Modern Admin & Placement Officer Dashboard.
 * Displays dynamic live MySQL KPIs, 4 interactive JavaFX charts,
 * and multithreaded asynchronous data loading.
 */
public class DashboardScreen {

    /**
     * Creates the shared navigation sidebar for administrative screens.
     */
    public static VBox createSidebar(String activeScreen) {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");

        Label brand = new Label("CAMPUS PLACEMENT");
        brand.getStyleClass().add("sidebar-brand");

        // User info box
        VBox userBox = new VBox(3);
        userBox.getStyleClass().add("sidebar-user-box");
        LoginManagement.UserSession user = LoginManagement.getCurrentUser();
        String uName = user != null ? user.getUsername() : "Placement Officer";
        String uRole = user != null ? user.getRole() : "OFFICER";
        Label nameLbl = new Label(uName.toUpperCase());
        nameLbl.getStyleClass().add("sidebar-user-name");
        Label roleLbl = new Label("Role: " + uRole);
        roleLbl.getStyleClass().add("sidebar-user-role");
        userBox.getChildren().addAll(nameLbl, roleLbl);

        // Navigation Buttons
        Button navDash = new Button("Dashboard");
        Button navStudents = new Button("Students");
        Button navCompanies = new Button("Companies");
        Button navDrives = new Button("Placement Drives");
        Button navApps = new Button("Applications");
        Button navReports = new Button("Reports & Analytics");

        Button[] buttons = {navDash, navStudents, navCompanies, navDrives, navApps, navReports};
        for (Button btn : buttons) {
            btn.getStyleClass().add("sidebar-btn");
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        // Highlight active button
        if ("Dashboard".equalsIgnoreCase(activeScreen)) navDash.getStyleClass().add("sidebar-btn-active");
        if ("Students".equalsIgnoreCase(activeScreen)) navStudents.getStyleClass().add("sidebar-btn-active");
        if ("Companies".equalsIgnoreCase(activeScreen)) navCompanies.getStyleClass().add("sidebar-btn-active");
        if ("Drives".equalsIgnoreCase(activeScreen)) navDrives.getStyleClass().add("sidebar-btn-active");
        if ("Applications".equalsIgnoreCase(activeScreen)) navApps.getStyleClass().add("sidebar-btn-active");
        if ("Reports".equalsIgnoreCase(activeScreen)) navReports.getStyleClass().add("sidebar-btn-active");

        navDash.setOnAction(e -> MainApp.showDashboard());
        navStudents.setOnAction(e -> MainApp.showStudentScreen());
        navCompanies.setOnAction(e -> MainApp.showCompanyScreen());
        navDrives.setOnAction(e -> MainApp.showDriveScreen());
        navApps.setOnAction(e -> MainApp.showApplicationScreen());
        navReports.setOnAction(e -> MainApp.showReportScreen());

        // Logout Button
        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("sidebar-logout-btn");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            boolean confirm = AlertUtil.showConfirmation("Confirm Logout", "Are you sure you want to sign out?");
            if (confirm) {
                LoginManagement.logout();
                MainApp.showLoginScreen();
            }
        });

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                brand,
                userBox,
                navDash,
                navStudents,
                navCompanies,
                navDrives,
                navApps,
                navReports,
                spacer,
                logoutBtn
        );

        return sidebar;
    }

    public static Parent createContent() {
        BorderPane root = new BorderPane();
        root.setLeft(createSidebar("Dashboard"));

        VBox contentBox = new VBox(20);
        contentBox.getStyleClass().add("content-area");

        // Header and Refresh Button
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);
        Label title = new Label("Executive Placement Dashboard");
        title.getStyleClass().add("view-header");
        Label subtitle = new Label("Live placement statistics, academic breakdown, and recruiter analytics from MySQL.");
        subtitle.getStyleClass().add("view-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        ProgressIndicator loadingSpinner = new ProgressIndicator();
        loadingSpinner.setPrefSize(28, 28);
        loadingSpinner.setVisible(false);

        Button refreshBtn = new Button("Refresh Data");
        refreshBtn.getStyleClass().add("btn-secondary");

        headerBox.getChildren().addAll(titleBox, loadingSpinner, refreshBtn);

        // 9 Statistic KPI Cards in a Responsive Grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(15);

        Label totalStudentsVal = new Label("...");
        Label totalCompaniesVal = new Label("...");
        Label totalDrivesVal = new Label("...");
        Label totalAppsVal = new Label("...");
        Label totalSelectedVal = new Label("...");
        Label totalRejectedVal = new Label("...");
        Label placementRateVal = new Label("...");
        Label avgPkgVal = new Label("...");
        Label highPkgVal = new Label("...");

        statsGrid.add(createStatCard("TOTAL STUDENTS", totalStudentsVal, "stat-card-accent-blue"), 0, 0);
        statsGrid.add(createStatCard("COMPANIES", totalCompaniesVal, "stat-card-accent-blue"), 1, 0);
        statsGrid.add(createStatCard("PLACEMENT DRIVES", totalDrivesVal, "stat-card-accent-blue"), 2, 0);

        statsGrid.add(createStatCard("TOTAL APPLICATIONS", totalAppsVal, "stat-card-accent-purple"), 0, 1);
        statsGrid.add(createStatCard("OFFERS / SELECTED", totalSelectedVal, "stat-card-accent-green"), 1, 1);
        statsGrid.add(createStatCard("REJECTED", totalRejectedVal, "stat-card-accent-amber"), 2, 1);

        statsGrid.add(createStatCard("PLACEMENT RATE", placementRateVal, "stat-card-accent-green"), 0, 2);
        statsGrid.add(createStatCard("AVERAGE PACKAGE", avgPkgVal, "stat-card-accent-purple"), 1, 2);
        statsGrid.add(createStatCard("HIGHEST PACKAGE", highPkgVal, "stat-card-accent-green"), 2, 2);

        // Four JavaFX Charts
        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(20);
        chartsGrid.setVgap(20);

        // Chart 1: Department Distribution PieChart
        PieChart deptChart = new PieChart();
        deptChart.setTitle("Department-Wise Students");
        deptChart.setPrefHeight(300);

        // Chart 2: Application Status Distribution PieChart
        PieChart statusChart = new PieChart();
        statusChart.setTitle("Application Status Breakdown");
        statusChart.setPrefHeight(300);

        // Chart 3: Company-wise Placements BarChart
        CategoryAxis cXAxis = new CategoryAxis();
        cXAxis.setLabel("Company");
        NumberAxis cYAxis = new NumberAxis();
        cYAxis.setLabel("Students Placed");
        BarChart<String, Number> companyChart = new BarChart<>(cXAxis, cYAxis);
        companyChart.setTitle("Recruiter Placement Count");
        companyChart.setLegendVisible(false);
        companyChart.setPrefHeight(300);

        // Chart 4: Package Comparison BarChart
        CategoryAxis pXAxis = new CategoryAxis();
        pXAxis.setLabel("Drive Role");
        NumberAxis pYAxis = new NumberAxis();
        pYAxis.setLabel("Package (LPA)");
        BarChart<String, Number> pkgChart = new BarChart<>(pXAxis, pYAxis);
        pkgChart.setTitle("Top Offered Packages (LPA)");
        pkgChart.setLegendVisible(false);
        pkgChart.setPrefHeight(300);

        chartsGrid.add(deptChart, 0, 0);
        chartsGrid.add(statusChart, 1, 0);
        chartsGrid.add(companyChart, 0, 1);
        chartsGrid.add(pkgChart, 1, 1);

        // Multithreaded Asynchronous Data Loader (FEATURE 1)
        Runnable loadDataAsync = () -> {
            loadingSpinner.setVisible(true);
            refreshBtn.setDisable(true);

            Task<ReportManagement.DashboardStats> statsTask = BackgroundTasks.createDashboardStatsTask();
            statsTask.setOnSucceeded(e -> {
                ReportManagement.DashboardStats s = statsTask.getValue();
                totalStudentsVal.setText(String.valueOf(s.totalStudents));
                totalCompaniesVal.setText(String.valueOf(s.totalCompanies));
                totalDrivesVal.setText(String.valueOf(s.totalDrives));
                totalAppsVal.setText(String.valueOf(s.totalApplications));
                totalSelectedVal.setText(String.valueOf(s.totalSelected));
                totalRejectedVal.setText(String.valueOf(s.totalRejected));
                placementRateVal.setText(String.format("%.1f%%", s.placementPercentage));
                avgPkgVal.setText(String.format("%.2f LPA", s.averagePackage));
                highPkgVal.setText(String.format("%.2f LPA", s.highestPackage));

                // Load Charts Data
                loadChartsAsync(deptChart, statusChart, companyChart, pkgChart);

                loadingSpinner.setVisible(false);
                refreshBtn.setDisable(false);
            });

            statsTask.setOnFailed(e -> {
                loadingSpinner.setVisible(false);
                refreshBtn.setDisable(false);
                AlertUtil.showError("Dashboard Load Failed", "Unable to retrieve dashboard statistics from database.");
            });

            BackgroundTasks.execute(statsTask);
        };

        refreshBtn.setOnAction(e -> loadDataAsync.run());

        // Initial Load
        loadDataAsync.run();

        contentBox.getChildren().addAll(headerBox, statsGrid, chartsGrid);

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        return root;
    }

    private static void loadChartsAsync(PieChart deptChart, PieChart statusChart, 
                                        BarChart<String, Number> companyChart, BarChart<String, Number> pkgChart) {
        new Thread(() -> {
            try {
                Map<String, Integer> depts = ReportManagement.getDepartmentDistribution();
                Map<String, Integer> statuses = ReportManagement.getApplicationStatusDistribution();
                Map<String, Integer> comps = ReportManagement.getCompanyPlacements();
                Map<String, Double> pkgs = ReportManagement.getPackageComparison();

                Platform.runLater(() -> {
                    // Update Dept Chart
                    deptChart.getData().clear();
                    for (Map.Entry<String, Integer> entry : depts.entrySet()) {
                        deptChart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                    }

                    // Update Status Chart
                    statusChart.getData().clear();
                    for (Map.Entry<String, Integer> entry : statuses.entrySet()) {
                        statusChart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                    }

                    // Update Company Placements Chart
                    companyChart.getData().clear();
                    XYChart.Series<String, Number> cSeries = new XYChart.Series<>();
                    for (Map.Entry<String, Integer> entry : comps.entrySet()) {
                        cSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    companyChart.getData().add(cSeries);

                    // Update Package Comparison Chart
                    pkgChart.getData().clear();
                    XYChart.Series<String, Number> pSeries = new XYChart.Series<>();
                    for (Map.Entry<String, Double> entry : pkgs.entrySet()) {
                        pSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    pkgChart.getData().add(pSeries);
                });
            } catch (Exception ex) {
                System.err.println("Error loading chart data: " + ex.getMessage());
            }
        }).start();
    }

    private static VBox createStatCard(String title, Label valueLabel, String accentClass) {
        VBox card = new VBox(4);
        card.getStyleClass().addAll("stat-card", accentClass);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-card-title");

        valueLabel.getStyleClass().add("stat-card-value");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
}
