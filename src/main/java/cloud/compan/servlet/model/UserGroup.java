package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Id;
import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_group")
@Getter
@Setter
public class UserGroup {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
} 