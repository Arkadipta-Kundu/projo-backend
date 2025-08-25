package org.arkadipta.projobackend.dto.response;

public class LoginResponse {
    private boolean success;
    private String token;
    private UserResponse user;

    // Constructors
    public LoginResponse() {
    }

    public LoginResponse(boolean success, String token, UserResponse user) {
        this.success = success;
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
