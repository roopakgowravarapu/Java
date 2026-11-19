package com.campusplacement.concurrency;

import com.campusplacement.management.ApplicationManagement;
import com.campusplacement.management.CompanyManagement;
import com.campusplacement.management.ReportManagement;
import com.campusplacement.management.StudentManagement;
import com.campusplacement.model.Company;
import com.campusplacement.model.PlacementApplication;
import com.campusplacement.model.Student;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.concurrent.Task;

/**
 * FEATURE 1 & 2: Genuine Java Multithreading Framework.
 * 
 * Manages background database queries and asynchronous report exports.
 * 
 * College Viva Explanation:
 * - The JavaFX Application Thread is responsible for rendering the UI and handling user input.
 * - If a long-running database query or file generation runs on the JavaFX Application Thread,
 *   the entire user interface freezes and becomes unresponsive ("Not Responding").
 * - We use ExecutorService with JavaFX Task to offload heavy JDBC calls and PDF rendering
 *   to background worker threads, keeping the UI silky smooth, while Task callbacks
 *   (setOnSucceeded, setOnFailed) automatically return results to the JavaFX Application Thread.
 */
public class BackgroundTasks {

    // Thread pool for background database tasks and file processing
    private static final ExecutorService taskExecutor = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("CampusPlacement-Worker-" + thread.getId());
        thread.setDaemon(true); // Daemon threads exit automatically when app closes
        return thread;
    });

    /**
     * Submits any JavaFX Task for asynchronous execution on the thread pool.
     */
    public static <T> void execute(Task<T> task) {
        taskExecutor.submit(task);
    }

    /**
     * FEATURE 1: Asynchronous Task to load or search students in the background.
     */
    public static Task<List<Student>> createStudentSearchTask(String keyword, String department) {
        return new Task<>() {
            @Override
            protected List<Student> call() throws Exception {
                updateMessage("Querying student records from database...");
                // Simulate slight network/query latency if dataset is small so progress bar is visible
                Thread.sleep(150);
                return StudentManagement.searchStudents(keyword, department);
            }
        };
    }

    /**
     * FEATURE 1: Asynchronous Task to load dashboard statistics from MySQL.
     */
    public static Task<ReportManagement.DashboardStats> createDashboardStatsTask() {
        return new Task<>() {
            @Override
            protected ReportManagement.DashboardStats call() throws Exception {
                updateMessage("Calculating placement metrics and KPIs...");
                return ReportManagement.getDashboardStatistics();
            }
        };
    }

    /**
     * FEATURE 2: Background Report Generation and File Export (CSV / PDF).
     */
    public static Task<String> createReportGenerationTask(String reportType, String format, File targetFile) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Fetching report data from database...");
                updateProgress(10, 100);
                Thread.sleep(200);

                String title = reportType;
                String[] headers;
                List<String[]> rows = new ArrayList<>();

                switch (reportType) {
                    case "Complete Student List":
                        title = "Complete Registered Students Report";
                        headers = new String[]{"Student ID", "Roll Number", "Full Name", "Department", "CGPA", "Year", "Backlogs"};
                        List<Student> students = StudentManagement.getAllStudents();
                        updateProgress(40, 100);
                        for (Student s : students) {
                            rows.add(new String[]{
                                String.valueOf(s.getStudentId()), s.getRollNumber(), s.getFullName(),
                                s.getDepartment(), String.format("%.2f", s.getCgpa()),
                                String.valueOf(s.getGraduationYear()), String.valueOf(s.getBacklogCount())
                            });
                        }
                        break;

                    case "Selected Students Report":
                        title = "Placed Students Selection Report";
                        headers = new String[]{"App ID", "Student Name", "Roll No", "Department", "Company", "Job Role", "Package (LPA)"};
                        List<PlacementApplication> allApps = ApplicationManagement.getAllApplications();
                        updateProgress(40, 100);
                        for (PlacementApplication app : allApps) {
                            if ("Selected".equalsIgnoreCase(app.getStatus())) {
                                rows.add(new String[]{
                                    String.valueOf(app.getApplicationId()), app.getStudentName(), app.getStudentRollNumber(),
                                    app.getStudentDepartment(), app.getCompanyName(), app.getJobRole(),
                                    String.format("%.2f", app.getPackageLpa())
                                });
                            }
                        }
                        break;

                    case "Rejected Applications Report":
                        title = "Rejected Applications Summary";
                        headers = new String[]{"App ID", "Student Name", "Roll No", "Company", "Job Role", "Remarks"};
                        List<PlacementApplication> apps = ApplicationManagement.getAllApplications();
                        updateProgress(40, 100);
                        for (PlacementApplication app : apps) {
                            if ("Rejected".equalsIgnoreCase(app.getStatus())) {
                                rows.add(new String[]{
                                    String.valueOf(app.getApplicationId()), app.getStudentName(), app.getStudentRollNumber(),
                                    app.getCompanyName(), app.getJobRole(),
                                    app.getRemarks() != null ? app.getRemarks() : "Criteria not met"
                                });
                            }
                        }
                        break;

                    case "Company Placement Report":
                    default:
                        title = "Registered Companies & Placement Summary";
                        headers = new String[]{"Company ID", "Company Name", "Industry", "Location", "HR Contact", "Email"};
                        List<Company> companies = CompanyManagement.getAllCompanies();
                        updateProgress(40, 100);
                        for (Company c : companies) {
                            rows.add(new String[]{
                                String.valueOf(c.getCompanyId()), c.getCompanyName(), c.getIndustry(),
                                c.getLocation(), c.getHrContact(), c.getEmail()
                            });
                        }
                        break;
                }

                updateMessage("Formatting and writing " + format.toUpperCase() + " file...");
                updateProgress(70, 100);
                Thread.sleep(300);

                if ("PDF".equalsIgnoreCase(format)) {
                    ReportManagement.exportToPDF(targetFile, title, headers, rows);
                } else {
                    ReportManagement.exportToCSV(targetFile, headers, rows);
                }

                updateProgress(100, 100);
                updateMessage("Report successfully exported to " + targetFile.getName());
                return targetFile.getAbsolutePath();
            }
        };
    }

    /**
     * Gracefully shuts down the background task executor service.
     */
    public static void shutdown() {
        taskExecutor.shutdown();
        try {
            if (!taskExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            taskExecutor.shutdownNow();
        }
    }
}
