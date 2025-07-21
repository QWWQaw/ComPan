package cloud.compan.servlet.repository;

import java.util.List;
import java.util.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Repository;
import cloud.compan.servlet.model.UserGroupMember;
import cloud.compan.servlet.utils.JdbcExecutor;

@Singleton
@Repository
public class UserGroupMemberRepository extends BaseRepository<UserGroupMember, Integer> {

    @Inject
    public UserGroupMemberRepository(JdbcExecutor executor) {
        super(executor);
    }

    /**
     * 根据用户ID查找所有相关的组成员记录。
     * @param userId 用户ID
     * @return 组成员记录列表
     */
    public Optional<List<UserGroupMember>> findByUserId(Long userId) {
        String sql = "SELECT * FROM `user_group_member` WHERE `user_id` = ?";
        List<UserGroupMember> userGroupMembers = executor.queryForList(entityClass, sql, rowMapper, userId);
        return Optional.of(userGroupMembers);
    }

    /**
     * 根据组ID查找所有相关的组成员记录。
     * @param groupId 组ID
     * @return 组成员记录列表
     */
    public Optional<List<UserGroupMember>> findByGroupId(Long groupId) {
        String sql = "SELECT * FROM `user_group_member` WHERE `group_id` = ?";
        List<UserGroupMember> userGroupMembers = executor.queryForList(entityClass, sql, rowMapper, groupId);
        return Optional.of(userGroupMembers);
    }
} 