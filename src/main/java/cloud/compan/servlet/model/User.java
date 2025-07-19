package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "storage_limit")
    private Long storageLimit = 10737418240L;//10GB

    @Column(name = "storage_used")
    private Long storageUsed = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User() {
        // no-arg constructor
    }

    public User(Long userId, String username, String email, String hashedPassword) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = hashedPassword;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Long getStorageLimit() { return storageLimit; }
    public Long getStorageUsed() { return storageUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setStorageLimit(Long storageLimit) { this.storageLimit = storageLimit; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", storageLimit=" + storageLimit +
                ", storageUsed=" + storageUsed +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
