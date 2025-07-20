package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;
import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "acl")
@Getter
@Setter
public class Acl {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "permission")
    private String permission;
}
