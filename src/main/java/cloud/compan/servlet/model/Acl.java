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
@Table(name = "acl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Acl {

    @Id
    @Column(name = "acl_id")
    private Long aclId;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "target_group_id")
    private Long targetGroupId;

    @Column(name = "permission")
    private String permission;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
