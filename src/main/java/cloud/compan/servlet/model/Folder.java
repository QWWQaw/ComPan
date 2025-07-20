package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;
import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "folder")
@Getter
@Setter
public class Folder {

    @Id
    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "parent_folder_id")
    private Long parentFolderId;

    @Column(name = "folder_name")
    private String folderName;

    @Column(name = "size")
    private Long size;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
