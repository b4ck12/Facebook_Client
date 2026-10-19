package com.facebook.client.data.model;

/**
 * DTO đại diện cho Lời mời kết bạn giữa 2 người dùng.
 * Ánh xạ tương ứng với bảng 'friendships' trong SQLite và JSON từ endpoint GET /api/friends/requests.
 */
public class FriendRequest {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";

    private String id;
    private String requesterId;
    private String requesterName;
    private String requesterAvatar;
    private String receiverId;
    private String status;
    private long createdAt;

    public FriendRequest() {
    }

    public FriendRequest(String id, String requesterId, String requesterName,
                         String requesterAvatar, String receiverId, String status, long createdAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.requesterAvatar = requesterAvatar;
        this.receiverId = receiverId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterAvatar() {
        return requesterAvatar;
    }

    public void setRequesterAvatar(String requesterAvatar) {
        this.requesterAvatar = requesterAvatar;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
