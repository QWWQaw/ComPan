package cloud.compan.servlet.repository;

import java.util.List;
import java.util.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Repository;
import cloud.compan.servlet.model.File;
import cloud.compan.servlet.utils.JdbcExecutor;

@Singleton
@Repository
public class FileRepository extends BaseRepository<File, Long> {

    @Inject
    public FileRepository(JdbcExecutor executor) {
        super(executor);
    }

    /**
     * 查找指定文件夹下的所有文件。
     * @param folderId 文件夹ID
     * @return 文件列表
     */
    public List<File> findByFolderId(Long folderId) {
        String sql = "SELECT * FROM `file` WHERE `folder_id` " +
                    (folderId == null ? "IS NULL" : "= ?") +
                    " AND `status` = 'ACTIVE'";
        if (folderId == null) {
            return executor.queryForList(entityClass, sql, rowMapper);
        } else {
            return executor.queryForList(entityClass, sql, rowMapper, folderId);
        }
    }

    /**
     * 在特定文件夹下，按名称查找文件 (用于检查重名)。
     * @param folderId 文件夹ID
     * @param fileName 文件名
     * @param uploaderId 上传者ID
     * @return 文件对象，如果不存在返回null
     */
    public File findByFolderAndName(Long folderId, String fileName, Long uploaderId) {
        String sql = "SELECT * FROM `file` WHERE `folder_id` = ? AND `file_name` = ? AND `uploader_id` = ? AND `status` = 'ACTIVE'";
        Optional<File> result = executor.queryForObject(entityClass, sql, rowMapper, folderId, fileName, uploaderId);
        return result.orElse(null);
    }

    /**
     * 根据上传者ID查找文件
     * @param uploaderId 上传者ID
     * @return 文件列表
     */
    public List<File> findByUploaderId(Long uploaderId) {
        String sql = "SELECT * FROM `file` WHERE `uploader_id` = ? AND `status` = 'ACTIVE'";
        return executor.queryForList(entityClass, sql, rowMapper, uploaderId);
    }

    /**
     * 根据文件哈希查找文件记录。
     * @param objectHash 文件内容的哈希值
     * @return 文件列表
     */ 
    public List<File> findByObjectHash(String objectHash) {
        String sql = "SELECT * FROM `file` WHERE `object_hash` = ? AND `status` = 'ACTIVE'";
        return executor.queryForList(entityClass, sql, rowMapper, objectHash);
    }

    /**
     * 根据文件名模糊搜索
     * @param keyword 搜索关键词
     * @return 文件列表
     */
    public List<File> findByFileNameContaining(String keyword) {
        String sql = "SELECT * FROM `file` WHERE `file_name` LIKE ? AND `status` = 'ACTIVE'";
        return executor.queryForList(entityClass, sql, rowMapper, "%" + keyword + "%");
    }

    /**
     * 计算用户存储使用量
     * @param uploaderId 用户ID
     * @return 存储使用量（字节）
     */
    public Long calculateStorageUsedByUploaderId(Long uploaderId) {
        String sql = "SELECT COALESCE(SUM(`file_size`), 0) FROM `file` WHERE `uploader_id` = ? AND `status` = 'ACTIVE'";
        return executor.queryForObject(Long.class, sql, rs -> rs.getLong(1), uploaderId).orElse(0L);
    }

    /**
     * 计算用户文件数量
     * @param uploaderId 用户ID
     * @return 文件数量
     */
    public Long countByUploaderId(Long uploaderId) {
        String sql = "SELECT COUNT(*) FROM `file` WHERE `uploader_id` = ? AND `status` = 'ACTIVE'";
        return executor.queryForObject(Long.class, sql, rs -> rs.getLong(1), uploaderId).orElse(0L);
    }

    /**
     * 计算文件夹内文件数量
     * @param folderId 文件夹ID
     * @return 文件数量
     */
    public Long countByFolderId(Long folderId) {
        String sql = "SELECT COUNT(*) FROM `file` WHERE `folder_id` " +
                    (folderId == null ? "IS NULL" : "= ?") +
                    " AND `status` = 'ACTIVE'";
        if (folderId == null) {
            return executor.queryForObject(Long.class, sql, rs -> rs.getLong(1)).orElse(0L);
        } else {
            return executor.queryForObject(Long.class, sql, rs -> rs.getLong(1), folderId).orElse(0L);
        }
    }
}
