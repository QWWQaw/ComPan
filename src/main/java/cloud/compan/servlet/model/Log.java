package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;
import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "log")
@Getter
@Setter
public class Log {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "operation")
    private String operation;

    @Column(name = "details")
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;
} 