package com.facebook.client.data.model;

/**
 * DTO đại diện cho Thông báo hoạt động tương tác (Like, Comment, Kết bạn).
 * Ánh xạ tương ứng với bảng 'notifications' trong SQLite và JSON từ endpoint GET /api/notifications.
 */
public class Notification {
    public static final String TYPE_LIKE = "LIKE";
    public static final String TYPE_COMMENT = "COMMENT";
    public static final String TYPE_FRIEND_REQUEST = "FRIEND_REQUEST";
    public static final String TYPE_FRIEND_ACCEPT = "FRIEND_ACCEPT";

    private String id;
    private String recipientId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String type;
    private String targetPostId;
    private String message;
    private boolean isRead;
    private long createdAt;

    public Notification() {
    }

    public Notification(String id, String recipientId, String senderId, String senderName, String senderAvatar, String type,
                        String targetPostId, String message, boolean isRead, long createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.type = type;
        this.targetPostId = targetPostId;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetPostId() {
        return targetPostId;
    }

    public void setTargetPostId(String targetPostId) {
        this.targetPostId = targetPostId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
