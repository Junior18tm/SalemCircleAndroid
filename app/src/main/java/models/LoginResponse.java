package models;

public class LoginResponse {
    private String accessToken;

    // Constructor
    public LoginResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    // Getter and Setter
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
