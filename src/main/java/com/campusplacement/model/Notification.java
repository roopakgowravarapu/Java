package com.campusplacement.model;

/**
 * Model class representing an internal System Notification.
 */
public class Notification {
    private int notificationId;
    private Integer userId; // null indicates global/broadcast notification
    private String title;
    private String message;
    private String notificationType; // 'NEW_DRIVE', 'STATUS_CHANGE', 'DEADLINE_ALERT', 'GENERAL'
    private boolean read;
    private String createdAt;

    public Notification() {
    }

    public Notification(int notificationId, Integer userId, String title, 
                        String message, String notificationType, boolean read, String createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.read = read;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return title + ": " + message;
    }
}
