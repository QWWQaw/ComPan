package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;
import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "share")
@Getter
@Setter
public class Share {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "share_link")
    private String shareLink;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "password")
    private String password;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
} 