package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String passwordHash; // 修正字段名以匹配服务层代码

    @Column(name = "storage_limit")
    private Long storageLimit = 10737418240L; // 10GB

    @Column(name = "storage_used")
    private Long storageUsed = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 添加角色字段以支持管理员权限检查
    @Column(name = "role")
    private String role = "USER";
}
