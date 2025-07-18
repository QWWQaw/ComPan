package cloud.compan.servlet.entity;

/**
 * 用户组成员实体类
 * 对应数据库表：user_group_member
 */
public class UserGroupMember {
    private Integer id;               // id INT PRIMARY KEY AUTO_INCREMENT
    private Long userId;              // user_id BIGINT UNSIGNED NOT NULL
    private Long groupId;             // group_id BIGINT UNSIGNED NOT NULL
    private String role;              // role VARCHAR(50) NOT NULL DEFAULT 'member'

    // 构造函数
    public UserGroupMember() {}

    public UserGroupMember(Long userId, Long groupId, String role) {
        this.userId = userId;
        this.groupId = groupId;
        this.role = role;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "UserGroupMember{" +
                "id=" + id +
                ", userId=" + userId +
                ", groupId=" + groupId +
                ", role='" + role + '\'' +
                '}';
    }
}
