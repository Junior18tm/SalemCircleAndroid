package models;

public class CommentModel {
    private String id;
    private String eventId;
    private String text;
    private UserModel user; // Assuming you have a User model that includes username and profileImagePath
    private String createdAt;

    // Constructor, getters and setters

    public CommentModel(String id, String eventId, String text, UserModel user, String createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public String getText() { return text; }
    public UserModel getUser() { return user; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    // Implement setters if necessary
}

