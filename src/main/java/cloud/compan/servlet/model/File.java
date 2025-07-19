package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;
import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "file")
@Getter
@Setter
public class File {

    @Id
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "object_hash")
    private String objectHash;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
} 