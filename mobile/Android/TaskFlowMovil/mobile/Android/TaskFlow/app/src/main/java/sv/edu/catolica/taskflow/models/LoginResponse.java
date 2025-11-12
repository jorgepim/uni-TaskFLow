package sv.edu.catolica.taskflow.models;

import java.util.List;

public class LoginResponse {
    private User user;
    private String token;
    private List<String> roles;

    public LoginResponse() {}

    // Getters and setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
