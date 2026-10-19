package com.facebook.client.data.model;

/**
 * DTO đại diện cho thông tin tài khoản Người dùng (User).
 * Ánh xạ tương ứng với bảng 'users' trong SQLite và JSON từ endpoint GET /api/users/{id}.
 */
public class User {
    private String id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String coverUrl;
    private long createdAt;

    public User() {
    }

    public User(String id, String email, String displayName, String avatarUrl, String coverUrl, long createdAt) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.coverUrl = coverUrl;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
