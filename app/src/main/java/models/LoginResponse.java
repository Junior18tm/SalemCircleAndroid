package models;

public class LoginResponse {
    private String accessToken;

    private String userRole;
    // Constructor
    public LoginResponse(String accessToken, String userRole) {

        this.accessToken = accessToken;
        this.userRole = userRole;
    }



    // Getter and Setter
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserRole() { return userRole; }

    public void setUserRole(String userRole) { this.userRole = userRole; }
}
