package com.campusplacement.concurrency;

import com.campusplacement.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FEATURE 3: Genuine Multithreading - Automatic Placement Drive Deadline Monitor.
 * 
 * Uses ScheduledExecutorService to run periodic background checks on recruitment drives
 * independently of the JavaFX UI thread.
 * 
 * Responsibilities:
 * 1. Checks open placement drives every 30 seconds.
 * 2. Automatically updates expired placement drives to 'CLOSED' status in the database.
 */
public class DeadlineMonitor {

    private static ScheduledExecutorService scheduler = null;

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
        String queryDrives = "SELECT d.drive_id, d.application_deadline, d.drive_status "
                           + "FROM placement_drives d "
                           + "WHERE d.drive_status = 'OPEN'";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryDrives)) {

            LocalDateTime now = LocalDateTime.now();

            while (rs.next()) {
                int driveId = rs.getInt("drive_id");
                String deadlineStr = rs.getString("application_deadline");

                LocalDateTime deadline;
                try {
                    deadline = LocalDateTime.parse(deadlineStr.replace(" ", "T"));
                } catch (Exception e) {
                    continue; // Skip if parse fails
                }

                Duration duration = Duration.between(now, deadline);

                // Deadline has expired -> Auto-close drive
                if (duration.isNegative()) {
                    autoCloseDrive(conn, driveId);
                }
            }

        } catch (Exception e) {
            System.err.println("[DeadlineMonitor] Warning during deadline check: " + e.getMessage());
        }
    }

    private static void autoCloseDrive(Connection conn, int driveId) {
        String sql = "UPDATE placement_drives SET drive_status = 'CLOSED' WHERE drive_id = ? AND drive_status = 'OPEN'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driveId);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                System.out.println("[DeadlineMonitor] Drive #" + driveId + " automatically marked as CLOSED (Deadline expired).");
            }
        } catch (Exception e) {
            // Ignore
        }
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
