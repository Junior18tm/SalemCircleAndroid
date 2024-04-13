package models;

public class CommentModel {
    private String _id;
    private String eventId;
    private String text;
    private UserModel user;
    private String createdAt;

    // Constructor, getters and setters

    public CommentModel(String _id, String eventId, String text, UserModel user, String createdAt) {
        this._id = _id;
        this.eventId = eventId;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return _id; }
    public String getEventId() { return eventId; }
    public String getText() { return text; }
    public UserModel getUser() { return user; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    // Implement setters if necessary
}

