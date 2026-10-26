package com.facebook.client.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * DTO đại diện cho Bài viết trên Bảng tin Feed và Profile.
 * Ánh xạ tương ứng với bảng 'posts' (JOIN 'users') và JSON từ endpoint GET /api/posts.
 */
public class Post {
    private String id;
    @SerializedName(value = "authorId", alternate = {"author_id"})
    private String authorId;
    @SerializedName(value = "authorName", alternate = {"author_name"})
    private String authorName;
    @SerializedName(value = "authorAvatar", alternate = {"author_avatar"})
    private String authorAvatar;
    private String content;
    @SerializedName(value = "mediaUrl", alternate = {"media_url"})
    private String mediaUrl;
    @SerializedName(value = "isPublic", alternate = {"is_public", "public"})
    private boolean isPublic;
    @SerializedName(value = "likesCount", alternate = {"likes_count"})
    private int likesCount;
    @SerializedName(value = "commentsCount", alternate = {"comments_count"})
    private int commentsCount;
    @SerializedName(value = "isLiked", alternate = {"is_liked", "liked"})
    private boolean isLiked;
    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private long createdAt;

    public Post() {
    }

    public Post(String id, String authorId, String authorName, String authorAvatar, String content, String mediaUrl,
                boolean isPublic, int likesCount, int commentsCount, boolean isLiked, long createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorAvatar = authorAvatar;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.isPublic = isPublic;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.isLiked = isLiked;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
