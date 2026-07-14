package com.shopwise.app.dto.response;

public class LoginResponse {

    private String token;
    private String tokenType;
    private String username;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }
}