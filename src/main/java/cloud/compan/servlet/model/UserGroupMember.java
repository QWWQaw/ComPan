package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;
import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_group_member")
@Getter
@Setter
public class UserGroupMember {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "role")
    private String role;
} 