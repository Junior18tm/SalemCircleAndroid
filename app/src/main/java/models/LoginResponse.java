package models;

public class LoginResponse {
    private String accessToken;

    private String userId;
    // Constructor
    public LoginResponse(String accessToken, String userId) {

        this.accessToken = accessToken;
        this.userId = userId;
    }



    // Getter and Setter
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }
}
