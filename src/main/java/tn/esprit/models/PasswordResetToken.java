package tn.esprit.models;

import java.time.LocalDateTime;

public class PasswordResetToken {
    private int id;
    private String token;
    private int userId;
    private LocalDateTime expiryDate;
    private boolean used;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String token, int userId) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = LocalDateTime.now().plusHours(1); // Token expires in 1 hour
        this.used = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}