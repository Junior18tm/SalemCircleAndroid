package models;

public class UserRoleResponse {
    private String role;

    // Constructor
    public UserRoleResponse(String role) {
        this.role = role;
    }

    // Getter
    public String getRole() {
        return role;
    }

    // Setter
    public void setRole(String role) {
        this.role = role;
    }
}
