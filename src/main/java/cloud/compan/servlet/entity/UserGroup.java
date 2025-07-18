package cloud.compan.servlet.entity;

/**
 * 用户组实体类
 * 对应数据库表：user_group
 */
public class UserGroup {
    private Long id;                  // id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    private String name;              // name VARCHAR(100) NOT NULL UNIQUE
    private String description;       // description VARCHAR(255)

    // 构造函数
    public UserGroup() {}

    public UserGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "UserGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
