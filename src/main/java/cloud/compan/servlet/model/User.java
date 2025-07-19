package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Setter
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
    }
}
