package cloud.compan.servlet.entity;

import java.sql.Timestamp;

/**
 * 日志实体类
 * 对应数据库表：log
 */
public class Log {
    private Integer id;               // id INT PRIMARY KEY AUTO_INCREMENT
    private Long userId;              // user_id BIGINT UNSIGNED NOT NULL
    private String operation;         // operation VARCHAR(255) NOT NULL
    private String details;           // details TEXT
    private String ipAddress;         // ip_address VARCHAR(45)
    private Timestamp performedAt;    // performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

    // 构造函数
    public Log() {}

    public Log(Long userId, String operation, String details, String ipAddress) {
        this.userId = userId;
        this.operation = operation;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Timestamp getPerformedAt() { return performedAt; }
    public void setPerformedAt(Timestamp performedAt) { this.performedAt = performedAt; }

    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", userId=" + userId +
                ", operation='" + operation + '\'' +
                ", details='" + details + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", performedAt=" + performedAt +
                '}';
    }
}
