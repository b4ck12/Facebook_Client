package com.facebook.client.data.model;

/**
 * DTO đại diện cho Bài viết trên Bảng tin Feed và Profile.
 * Ánh xạ tương ứng với bảng 'posts' (JOIN 'users') và JSON từ endpoint GET /api/posts.
 */
public class Post {
    private String id;
    private String authorId;
    private String authorName;
    private String authorAvatar;
    private String content;
    private String mediaUrl;
    private boolean isPublic;
    private int likesCount;
    private int commentsCount;
    private boolean isLiked;
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
