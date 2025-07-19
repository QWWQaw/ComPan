package cloud.compan.servlet.repository;

import cloud.compan.servlet.model.File;
import cloud.compan.servlet.utils.JdbcExecutor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
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
    public Optional<List<File>> findByFolderId(Long folderId) {
        String sql = "SELECT * FROM `file` WHERE `folder_id` = ? AND `status` = 'active'";
        List<File> files = executor.queryForList(entityClass, sql, rowMapper, folderId);
        return Optional.of(files);
    }

    /**
     * 在特定文件夹下，按名称查找文件 (用于检查重名)。
     * @param folderId 文件夹ID
     * @param fileName 文件名
     * @param uploaderId 上传者ID
     * @return 文件 Optional
     */
    public Optional<File> findByFolderAndName(Long folderId, String fileName, Long uploaderId) {
        String sql = "SELECT * FROM `file` WHERE `folder_id` = ? AND `file_name` = ? AND `uploader_id` = ? AND `status` = 'active'";
        return executor.queryForObject(entityClass, sql, rowMapper, folderId, fileName, uploaderId);
    }

    /**
     * 根据文件哈希查找文件记录。
     * @param objectHash 文件内容的哈希值
     * @return 文件列表
     */ 
    public Optional<List<File>> findByObjectHash(String objectHash) {
        String sql = "SELECT * FROM `file` WHERE `object_hash` = ?";
        List<File> files = executor.queryForList(entityClass, sql, rowMapper, objectHash);
        return Optional.of(files);
    }
} 