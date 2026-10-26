package com.facebook.client.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * DTO đại diện cho Bình luận bài viết (Chuẩn Text-only theo đặc tả PDF).
 * Ánh xạ tương ứng với bảng 'comments' trong SQLite và JSON từ endpoint GET /api/posts/{id}/comments.
 */
public class Comment {
    private String id;
    @SerializedName(value = "postId", alternate = {"post_id"})
    private String postId;
    @SerializedName(value = "authorId", alternate = {"author_id"})
    private String authorId;
    @SerializedName(value = "authorName", alternate = {"author_name"})
    private String authorName;
    @SerializedName(value = "authorAvatar", alternate = {"author_avatar"})
    private String authorAvatar;
    private String content;
    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private long createdAt;

    public Comment() {
    }

    public Comment(String id, String postId, String authorId, String authorName,
                   String authorAvatar, String content, long createdAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorAvatar = authorAvatar;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
