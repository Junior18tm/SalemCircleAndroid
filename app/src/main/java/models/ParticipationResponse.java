package models;

public class ParticipationResponse {
    private String message;

    // Constructor
    public ParticipationResponse(String message) {
        this.message = message;
    }

    // Getter
    public String getMessage() {
        return message;
    }

    // Setter
    public void setMessage(String message) {
        this.message = message;
    }
}
