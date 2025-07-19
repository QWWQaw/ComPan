package cloud.compan.servlet.repository;

import cloud.compan.servlet.model.Acl;
import cloud.compan.servlet.utils.JdbcExecutor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class AclRepository extends BaseRepository<Acl, Integer> {

    @Inject
    public AclRepository(JdbcExecutor executor) {
        super(executor);
    }

    /**
     * 根据文件ID查找所有相关的ACL记录。
     * @param fileId 文件ID
     * @return ACL记录列表
     */
    public Optional<List<Acl>> findByFileId(Long fileId) {
        String sql = "SELECT * FROM `acl` WHERE `file_id` = ?";
        List<Acl> acls = executor.queryForList(entityClass, sql, rowMapper, fileId);
        return Optional.of(acls);
    }

    /**
     * 根据文件夹ID查找所有相关的ACL记录。
     * @param folderId 文件夹ID
     * @return ACL记录列表
     */
    public Optional<List<Acl>> findByFolderId(Long folderId) {
        String sql = "SELECT * FROM `acl` WHERE `folder_id` = ?";
        List<Acl> acls = executor.queryForList(entityClass, sql, rowMapper, folderId);
        return Optional.of(acls);
    }

    /**
     * 根据用户ID查找所有相关的ACL记录。
     * @param userId 用户ID
     * @return ACL记录列表
     */
    public Optional<List<Acl>> findByUserId(Long userId) {
        String sql = "SELECT * FROM `acl` WHERE `user_id` = ?";
        List<Acl> acls = executor.queryForList(entityClass, sql, rowMapper, userId);
        return Optional.of(acls);
    }
} 