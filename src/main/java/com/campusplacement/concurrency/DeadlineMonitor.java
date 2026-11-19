package com.campusplacement.concurrency;

import com.campusplacement.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * FEATURE 3: Genuine Multithreading - Automatic Deadline Monitoring.
 * 
 * Uses ScheduledExecutorService to run periodic background checks on placement drive deadlines
 * independently of the JavaFX UI thread.
 * 
 * Responsibilities:
 * 1. Checks open placement drives every 30 seconds.
 * 2. Detects drives approaching deadline within 48 hours.
 * 3. Prevents duplicate notifications by querying existing database records.
 * 4. Inserts system alerts into the notifications table.
 * 5. Automatically marks expired drives as 'CLOSED'.
 * 6. Safely notifies UI listeners on Platform.runLater().
 */
public class DeadlineMonitor {

    private static ScheduledExecutorService scheduler = null;
    private static Runnable onNotificationGeneratedCallback = null;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Starts the periodic deadline monitor background thread.
     */
    public static synchronized void startMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return; // Already running
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("CampusPlacement-DeadlineMonitor");
            t.setDaemon(true);
            return t;
        });

        // Run immediately after 3 seconds, then repeat every 30 seconds
        scheduler.scheduleAtFixedRate(DeadlineMonitor::checkDeadlines, 3, 30, TimeUnit.SECONDS);
        System.out.println("[DeadlineMonitor] Scheduled background service started (30s interval).");
    }

    /**
     * Periodic check executed by ScheduledExecutorService.
     */
    public static void checkDeadlines() {
        String queryDrives = "SELECT d.drive_id, c.company_name, d.job_role, d.application_deadline, d.drive_status "
                           + "FROM placement_drives d "
                           + "JOIN companies c ON d.company_id = c.company_id "
                           + "WHERE d.drive_status = 'OPEN'";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryDrives)) {

            LocalDateTime now = LocalDateTime.now();
            boolean newAlertCreated = false;

            while (rs.next()) {
                int driveId = rs.getInt("drive_id");
                String companyName = rs.getString("company_name");
                String jobRole = rs.getString("job_role");
                String deadlineStr = rs.getString("application_deadline");

                LocalDateTime deadline;
                try {
                    deadline = LocalDateTime.parse(deadlineStr.replace(" ", "T"));
                } catch (Exception e) {
                    continue; // Skip if parse fails
                }

                Duration duration = Duration.between(now, deadline);
                long hoursRemaining = duration.toHours();

                // Case 1: Deadline has expired -> Auto-close drive
                if (duration.isNegative()) {
                    autoCloseDrive(conn, driveId);
                    continue;
                }

                // Case 2: Deadline is approaching (within 48 hours)
                if (hoursRemaining >= 0 && hoursRemaining <= 48) {
                    String title = "Deadline Alert: Drive #" + driveId + " - " + companyName;
                    if (!notificationAlreadyExists(conn, title)) {
                        String message = "Application deadline for " + jobRole + " at " + companyName 
                                       + " is closing in " + (hoursRemaining == 0 ? "less than 1 hour" : hoursRemaining + " hours") 
                                       + "! Please complete and submit your application.";

                        insertNotification(conn, title, message);
                        newAlertCreated = true;
                        System.out.println("[DeadlineMonitor] Generated alert: " + title);
                    }
                }
            }

            // Trigger UI update if listener attached
            if (newAlertCreated && onNotificationGeneratedCallback != null) {
                Platform.runLater(onNotificationGeneratedCallback);
            }

        } catch (Exception e) {
            System.err.println("[DeadlineMonitor] Warning during deadline check: " + e.getMessage());
        }
    }

    private static boolean notificationAlreadyExists(Connection conn, String title) {
        String sql = "SELECT notification_id FROM notifications WHERE title = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static void insertNotification(Connection conn, String title, String message) {
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, is_read) VALUES (NULL, ?, ?, 'DEADLINE_ALERT', 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("[DeadlineMonitor] Error inserting notification: " + e.getMessage());
        }
    }

    private static void autoCloseDrive(Connection conn, int driveId) {
        String sql = "UPDATE placement_drives SET drive_status = 'CLOSED' WHERE drive_id = ? AND drive_status = 'OPEN'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driveId);
            stmt.executeUpdate();
            System.out.println("[DeadlineMonitor] Drive #" + driveId + " marked as CLOSED (Deadline expired).");
        } catch (Exception e) {
            // Ignore
        }
    }

    public static void setOnNotificationGenerated(Runnable callback) {
        onNotificationGeneratedCallback = callback;
    }

    /**
     * Stops the deadline monitor background scheduler.
     */
    public static synchronized void stopMonitoring() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            System.out.println("[DeadlineMonitor] Background scheduler stopped.");
        }
    }
}
