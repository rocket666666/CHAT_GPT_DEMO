package com.example.chatsales.dto;

public class AuthResponse {
    private boolean success;
    private String token;
    private String message;
    private String username;
    private boolean passwordExpiring;
    private Integer daysUntilExpiry;

    public AuthResponse() {
    }

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static AuthResponse success(String token, String username) {
        AuthResponse response = new AuthResponse(true, "登录成功");
        response.setToken(token);
        response.setUsername(username);
        return response;
    }

    public static AuthResponse fail(String message) {
        return new AuthResponse(false, message);
    }

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isPasswordExpiring() {
        return passwordExpiring;
    }

    public void setPasswordExpiring(boolean passwordExpiring) {
        this.passwordExpiring = passwordExpiring;
    }

    public Integer getDaysUntilExpiry() {
        return daysUntilExpiry;
    }

    public void setDaysUntilExpiry(Integer daysUntilExpiry) {
        this.daysUntilExpiry = daysUntilExpiry;
    }
} 