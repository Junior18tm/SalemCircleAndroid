package models;

import android.util.Log;

public class UserRoleResponse {
    private String role;

    // Constructor
    public UserRoleResponse(String role) {
        this.role = role;
    }

    // Getter
    public String getRole() {
        Log.d("urr", "Retrieved role: " + role);
        return role;
    }

    // Setter
    public void setRole(String role) {
        this.role = role;
    }
}
