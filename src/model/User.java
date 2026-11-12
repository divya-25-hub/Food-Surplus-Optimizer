package model;

public class User {

    private String username;
    private String password;
    private String role;

    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Setter for username
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter for password
    public String getPassword() {
        return password;
    }

    // Setter for password
    public void setPassword(String password) {
        this.password = password;
    }

    // Getter for role
    public String getRole() {
        return role;
    }

    // Setter for role
    public void setRole(String role) {
        this.role = role;
    }

    // Validate username
    public boolean validateUsername() {

    return username != null
            && !username.trim().isEmpty()
            && username.length() >= 3;
}

    // Validate password
    public boolean validatePassword() {

    return password != null
            && !password.trim().isEmpty()
            && password.length() >= 6;
}

    // Validate role
    public boolean validateRole() {

    return role != null
            && !role.trim().isEmpty();
}

    // Validate complete user
    public boolean validateUser() {

        return validateUsername()
                && validatePassword()
                && validateRole();
    }
}