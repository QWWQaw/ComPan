package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;
import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "share")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Share {

    @Id
    @Column(name = "share_id")
    private Long shareId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "share_link")
    private String shareLink;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "password")
    private String password;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "allow_download")
    private Boolean allowDownload;

    @Column(name = "allow_preview")
    private Boolean allowPreview;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
