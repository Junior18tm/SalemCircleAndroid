package models;

import android.util.Log;

public class UserModel {
    private String _id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role;
    private String profileImagePath;

    // Constructor for sign-up
    public UserModel(String username, String email, String password, String fullName, String role, String profileImagePath) {
        this(null, username, email, password, fullName, role, profileImagePath);
    }
    public UserModel(String _id, String username, String email, String password, String fullName, String role, String profileImagePath) {
        this._id = _id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = "";
        this.role = "user"; // Default role
        this.profileImagePath = profileImagePath;
    }

    // Getters and Setters
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = _id;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() {
        return fullName; }
    public void setName(String name) { this.fullName = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImagePath() {
        // Return the default profile image URL if none is set
        if (profileImagePath == null || profileImagePath.isEmpty()) {
            return "https://storage.googleapis.com/salem-circle/profile-pic/DefaultProfilePic.png";
        }
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}




