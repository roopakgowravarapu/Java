package com.campusplacement;

import com.campusplacement.concurrency.BackgroundTasks;
import com.campusplacement.concurrency.DeadlineMonitor;
import com.campusplacement.management.ApplicationManagement;
import com.campusplacement.management.CompanyManagement;
import com.campusplacement.management.DriveManagement;
import com.campusplacement.management.LoginManagement;
import com.campusplacement.management.ReportManagement;
import com.campusplacement.management.StudentManagement;
import com.campusplacement.model.Company;
import com.campusplacement.model.PlacementApplication;
import com.campusplacement.model.PlacementDrive;
import com.campusplacement.model.Student;
import com.campusplacement.utils.ValidationUtil;
import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Comprehensive System Integration Test Suite.
 * Validates Database operations, Transactions, Authentication, Multithreading,
 * Eligibility calculations, and File Exports (CSV & PDF).
 */
public class SystemIntegrationTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("CAMPUS PLACEMENT MANAGEMENT SYSTEM - AUTOMATED VERIFICATION SUITE");
        System.out.println("================================================================");

        // Initialize JavaFX Toolkit for Background Task testing
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already started
        } catch (Exception e) {
            System.out.println("JavaFX Platform init: " + e.getMessage());
        }

        runTest("1. Database Connection & Schema Init", SystemIntegrationTest::testDatabase);
        runTest("2. Password Hashing & Authentication", SystemIntegrationTest::testAuthentication);
        runTest("3. Student CRUD & Duplicate Constraints", SystemIntegrationTest::testStudentManagement);
        runTest("4. Company Management & Search", SystemIntegrationTest::testCompanyManagement);
        runTest("5. Placement Drive & Eligibility Logic", SystemIntegrationTest::testDriveAndEligibility);
        runTest("6. Application Workflow, Transactions & History", SystemIntegrationTest::testApplicationWorkflow);
        runTest("7. Multithreading (ExecutorService & Task)", SystemIntegrationTest::testMultithreading);
        runTest("8. Multithreading (Scheduled Deadline Monitor)", SystemIntegrationTest::testDeadlineMonitor);
        runTest("9. CSV & PDF Report Generation", SystemIntegrationTest::testReportExports);
        runTest("10. Officer Add Student -> Auto User Login -> Student Apply -> Clean Delete", SystemIntegrationTest::testEndToEndOfficerStudentWorkflow);

        System.out.println("================================================================");
        System.out.printf("TEST SUMMARY: %d PASSED, %d FAILED\n", totalPassed, totalFailed);
        System.out.println("================================================================");

        // Cleanup
        DeadlineMonitor.stopMonitoring();
        BackgroundTasks.shutdown();

        if (totalFailed > 0) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    private static void runTest(String name, TestCase test) {
        System.out.printf("[RUNNING] %s ... ", name);
        try {
            test.execute();
            System.out.println("PASSED");
            totalPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            totalFailed++;
        }
    }

    @FunctionalInterface
    interface TestCase {
        void execute() throws Exception;
    }

    // -------------------------------------------------------------
    // TEST 1: Database Initialization
    // -------------------------------------------------------------
    private static void testDatabase() throws Exception {
        DBConnection.initializeDatabase();
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertTrue(!conn.isClosed(), "Connection should be active");
        }
    }

    // -------------------------------------------------------------
    // TEST 2: Authentication
    // -------------------------------------------------------------
    private static void testAuthentication() throws Exception {
        // Valid Placement Officer login
        LoginManagement.UserSession officerSession = LoginManagement.authenticate("officer", "officer123");
        assertNotNull(officerSession, "Officer login should succeed");
        assertTrue(officerSession.isOfficer(), "Role should be OFFICER");

        // Valid Student login
        LoginManagement.UserSession studentSession = LoginManagement.authenticate("student", "student123");
        assertNotNull(studentSession, "Student login should succeed");
        assertTrue(studentSession.isStudent(), "Role should be STUDENT");
        assertNotNull(studentSession.getStudentId(), "Student ID should be linked");

        // Invalid login test
        LoginManagement.UserSession failedSession = LoginManagement.authenticate("officer", "wrong_password");
        assertNull(failedSession, "Invalid password should return null session");
    }

    // -------------------------------------------------------------
    // TEST 3: Student CRUD & Validation
    // -------------------------------------------------------------
    private static void testStudentManagement() throws Exception {
        String testRoll = "TEST_ROLL_" + System.currentTimeMillis() % 10000;
        String testEmail = "test_" + System.currentTimeMillis() % 10000 + "@college.edu";

        Student s = new Student(0, testRoll, "Automated Test Student", testEmail, 
                                "9998887776", "Computer Science", 8.85, 2026, "Java, JDBC", 0);

        // Add
        boolean added = StudentManagement.addStudent(s);
        assertTrue(added, "Student should be added");
        assertTrue(s.getStudentId() > 0, "Generated ID should be set");

        // Verify that newly registered student can immediately log in
        LoginManagement.UserSession newStudentSession = LoginManagement.authenticate(testRoll.toLowerCase(), "student123");
        assertNotNull(newStudentSession, "Newly registered student should be able to log in");
        assertTrue(newStudentSession.isStudent(), "Role should be STUDENT");

        // Duplicate Roll Number Rejection
        Student dup = new Student(0, testRoll, "Duplicate Roll Student", "unique_" + System.currentTimeMillis() + "@college.edu",
                                 "9998887775", "Information Tech", 8.0, 2026, "Java", 0);
        try {
            StudentManagement.addStudent(dup);
            throw new AssertionError("Duplicate roll number should throw exception");
        } catch (IllegalArgumentException expected) {
            // Expected
        }

        // Search
        List<Student> searchResults = StudentManagement.searchStudents("Automated", "Computer Science");
        assertTrue(!searchResults.isEmpty(), "Search should find test student");

        // Update
        s.setFullName("Updated Test Student");
        boolean updated = StudentManagement.updateStudent(s);
        assertTrue(updated, "Student update should succeed");

        // Delete
        boolean deleted = StudentManagement.deleteStudent(s.getStudentId());
        assertTrue(deleted, "Student deletion should succeed");
    }

    // -------------------------------------------------------------
    // TEST 4: Company Management
    // -------------------------------------------------------------
    private static void testCompanyManagement() throws Exception {
        String compName = "TestCorp_" + System.currentTimeMillis() % 10000;
        Company c = new Company(0, compName, "Software", "Pune, India", "https://test.com", "HR Lead", "hr@test.com");

        boolean added = CompanyManagement.addCompany(c);
        assertTrue(added, "Company should be added");
        assertTrue(c.getCompanyId() > 0, "Company ID should be assigned");

        // Search
        List<Company> list = CompanyManagement.searchCompanies(compName, "Software");
        assertTrue(!list.isEmpty(), "Company should be found in search");

        // Delete
        boolean deleted = CompanyManagement.deleteCompany(c.getCompanyId());
        assertTrue(deleted, "Company deletion should succeed");
    }

    // -------------------------------------------------------------
    // TEST 5: Placement Drive & Eligibility Logic
    // -------------------------------------------------------------
    private static void testDriveAndEligibility() throws Exception {
        // High Criteria Drive: Min CGPA 8.5, Max Backlogs 0, Dept: CSE, IT
        PlacementDrive drive = new PlacementDrive(99, 1, "Google", "Lead Engineer", "Desc",
                25.0, 8.50, "Computer Science,Information Tech", 0, "Java", "2026-12-31 23:59:00", "OPEN");

        // Student A: Eligible (CGPA 9.0, Dept CSE, Backlogs 0)
        Student eligibleStudent = new Student(101, "24CS999", "Eligible Student", "el@col.edu", "9876543210",
                "Computer Science", 9.0, 2026, "Java", 0);
        DriveManagement.EligibilityResult resA = DriveManagement.checkEligibility(eligibleStudent, drive);
        assertTrue(resA.isEligible(), "Student A should be eligible");

        // Student B: Low CGPA (CGPA 7.5 < 8.5)
        Student lowCgpaStudent = new Student(102, "24CS998", "Low CGPA", "low@col.edu", "9876543210",
                "Computer Science", 7.5, 2026, "Java", 0);
        DriveManagement.EligibilityResult resB = DriveManagement.checkEligibility(lowCgpaStudent, drive);
        assertTrue(!resB.isEligible(), "Student B should be ineligible due to CGPA");

        // Student C: Backlog Violation (Backlogs 1 > 0)
        Student backlogStudent = new Student(103, "24CS997", "Backlog Student", "bk@col.edu", "9876543210",
                "Computer Science", 8.8, 2026, "Java", 1);
        DriveManagement.EligibilityResult resC = DriveManagement.checkEligibility(backlogStudent, drive);
        assertTrue(!resC.isEligible(), "Student C should be ineligible due to backlogs");

        // Student D: Ineligible Department (Mechanical Eng)
        Student mechStudent = new Student(104, "24ME996", "Mech Student", "me@col.edu", "9876543210",
                "Mechanical Eng", 9.2, 2026, "AutoCAD", 0);
        DriveManagement.EligibilityResult resD = DriveManagement.checkEligibility(mechStudent, drive);
        assertTrue(!resD.isEligible(), "Student D should be ineligible due to Department");
    }

    // -------------------------------------------------------------
    // TEST 6: Application Workflow, Transactions & History
    // -------------------------------------------------------------
    private static void testApplicationWorkflow() throws Exception {
        // Priya (Student 2) has pre-existing application for Drive 1
        boolean duplicate = ApplicationManagement.hasAlreadyApplied(1, 2);
        assertTrue(duplicate, "Priya (Student 2) application (drive 1, student 2) should exist");

        // Student 1 (Rahul Sharma) has NOT applied to Drive 1 yet in initial demo data
        boolean s1AppliedBefore = ApplicationManagement.hasAlreadyApplied(1, 1);
        assertTrue(!s1AppliedBefore, "Student 1 should NOT be initially applied to Drive 1");

        // Test Student 1 applying fresh to Drive 1
        boolean applied = ApplicationManagement.applyForDrive(1, 1, "Automated Test Submission");
        assertTrue(applied, "Student 1 applying to Drive 1 should succeed");

        // Test duplicate detection now blocks applying a second time
        boolean s1AppliedAfter = ApplicationManagement.hasAlreadyApplied(1, 1);
        assertTrue(s1AppliedAfter, "Student 1 application should now be detected as already applied");

        // Find Student 1's newly created application
        List<PlacementApplication> s1Apps = ApplicationManagement.getApplicationsByStudentId(1);
        assertTrue(!s1Apps.isEmpty(), "Student 1 should have applications");
        int newAppId = s1Apps.get(0).getApplicationId();

        // Test Status Update & Audit History Logging
        boolean updated = ApplicationManagement.updateApplicationStatus(
                newAppId, "Interview Scheduled", "2026-11-26 11:00 AM", "Cleared technical coding round", "OfficerTest"
        );
        assertTrue(updated, "Application status update should succeed");

        // Verify History Audit
        List<ApplicationManagement.HistoryEntry> history = ApplicationManagement.getApplicationHistory(newAppId);
        assertTrue(!history.isEmpty(), "Application history should contain entries");
        ApplicationManagement.HistoryEntry latest = history.get(history.size() - 1);
        assertEquals("Interview Scheduled", latest.getNewStatus(), "Latest status should match update");
        assertEquals("OfficerTest", latest.getUpdatedBy(), "UpdatedBy audit should match");

        // Test Deleting Application
        boolean deleted = ApplicationManagement.deleteApplication(newAppId);
        assertTrue(deleted, "Deleting test application should succeed");
    }

    // -------------------------------------------------------------
    // TEST 7: Multithreading - ExecutorService & JavaFX Task
    // -------------------------------------------------------------
    private static void testMultithreading() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] taskExecuted = {false};

        Task<ReportManagement.DashboardStats> task = BackgroundTasks.createDashboardStatsTask();
        task.setOnSucceeded(e -> {
            ReportManagement.DashboardStats s = task.getValue();
            assertNotNull(s, "Dashboard stats should not be null");
            assertTrue(s.totalStudents > 0, "Total students should be > 0");
            taskExecuted[0] = true;
            latch.countDown();
        });

        task.setOnFailed(e -> latch.countDown());

        BackgroundTasks.execute(task);
        boolean finished = latch.await(4, TimeUnit.SECONDS);
        assertTrue(finished, "Background task should complete within 4 seconds");
        assertTrue(taskExecuted[0], "Task succeeded callback should execute");
    }

    // -------------------------------------------------------------
    // TEST 8: Multithreading - Scheduled Deadline Monitor
    // -------------------------------------------------------------
    private static void testDeadlineMonitor() throws Exception {
        // Run periodic deadline monitor to auto-close expired drives
        DeadlineMonitor.checkDeadlines();
        List<PlacementDrive> allDrives = DriveManagement.getAllDrives();
        assertTrue(!allDrives.isEmpty(), "Drives should exist and be monitored");
    }

    // -------------------------------------------------------------
    // TEST 9: CSV & PDF Report Exports
    // -------------------------------------------------------------
    private static void testReportExports() throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "CampusPlacementTests");
        tempDir.mkdirs();

        File csvFile = new File(tempDir, "test_report.csv");
        File pdfFile = new File(tempDir, "test_report.pdf");

        // Test CSV export directly
        String[] headers = {"Student ID", "Roll Number", "Full Name", "Department", "CGPA"};
        List<String[]> rows = List.of(
                new String[]{"1", "24CS101", "Rahul Sharma", "Computer Science", "8.75"},
                new String[]{"2", "24IT102", "Priya Patel", "Information Tech", "9.10"}
        );
        ReportManagement.exportToCSV(csvFile, headers, rows);
        assertTrue(csvFile.exists(), "CSV file should be created");
        assertTrue(csvFile.length() > 0, "CSV file should not be empty");

        // Test PDF export directly
        ReportManagement.exportToPDF(pdfFile, "Placed Students Test Report", headers, rows);
        assertTrue(pdfFile.exists(), "PDF file should be created");
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");

        // Clean up temp test files
        csvFile.delete();
        pdfFile.delete();
        tempDir.delete();
    }

    // -------------------------------------------------------------
    // TEST 10: Complete Officer & Student Portal End-to-End Workflow
    // -------------------------------------------------------------
    private static void testEndToEndOfficerStudentWorkflow() throws Exception {
        String testRoll = "24TEST" + (System.currentTimeMillis() % 10000);
        String testEmail = "testuser" + (System.currentTimeMillis() % 10000) + "@college.edu";
        
        // 1. Officer adds new student (auto-provisions student login credentials)
        Student s = new Student(0, testRoll, "EndToEnd Test Student", testEmail, "9876543299",
                "Computer Science", 8.90, 2026, "Java, Python, SQL", 0);
        boolean added = StudentManagement.addStudent(s);
        assertTrue(added, "Student record should be added successfully");
        int newStudentId = s.getStudentId();
        assertTrue(newStudentId > 0, "Student ID should be assigned");

        // 2. Student logs in through Student Portal
        String username = testRoll.toLowerCase();
        String password = "student123";
        LoginManagement.UserSession session = LoginManagement.authenticate(username, password);
        assertNotNull(session, "Student should be able to log in with auto-provisioned credentials");
        assertEquals("STUDENT", session.getRole(), "User role should be STUDENT");
        assertEquals(newStudentId, session.getStudentId().intValue(), "Session studentId should match");

        // 3. Student checks eligibility and applies for Drive 3 (TCS)
        PlacementDrive drive3 = DriveManagement.getDriveById(3);
        assertNotNull(drive3, "Drive 3 should exist");
        Student currentStudent = StudentManagement.getStudentById(newStudentId);
        DriveManagement.EligibilityResult el = DriveManagement.checkEligibility(currentStudent, drive3);
        assertTrue(el.isEligible(), "Student should be eligible for Drive 3");

        boolean applied = ApplicationManagement.applyForDrive(3, newStudentId, "Applied via student portal test");
        assertTrue(applied, "Student should successfully apply for Drive 3");

        // 4. Verify already applied detection
        boolean duplicate = ApplicationManagement.hasAlreadyApplied(3, newStudentId);
        assertTrue(duplicate, "Application should now be detected as already applied");

        // 5. Officer updates application status
        List<PlacementApplication> apps = ApplicationManagement.getApplicationsByStudentId(newStudentId);
        assertTrue(!apps.isEmpty(), "Student should have 1 active application");
        int appId = apps.get(0).getApplicationId();

        boolean statusUpdated = ApplicationManagement.updateApplicationStatus(
                appId, "Shortlisted", null, "Cleared online coding assessment", "officer"
        );
        assertTrue(statusUpdated, "Status update to Shortlisted should succeed");

        // 6. Officer deletes student (testing cascade delete of student, user, and applications)
        boolean deleted = StudentManagement.deleteStudent(newStudentId);
        assertTrue(deleted, "Deleting student should succeed");

        // 7. Verify clean deletion
        Student afterDelete = StudentManagement.getStudentById(newStudentId);
        assertTrue(afterDelete == null, "Student record should no longer exist");

        LoginManagement.UserSession afterDeleteSession = LoginManagement.authenticate(username, password);
        assertTrue(afterDeleteSession == null, "Deleted student account should not be able to log in");
    }

    // -------------------------------------------------------------
    // Assertion Helpers
    // -------------------------------------------------------------
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError("Assertion Failed (Null): " + message);
        }
    }

    private static void assertNull(Object obj, String message) {
        if (obj != null) {
            throw new AssertionError("Assertion Failed (Expected Null): " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion Failed: " + message + " (Expected: " + expected + ", Got: " + actual + ")");
    }
}
