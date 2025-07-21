package cloud.compan.servlet.repository;

import java.util.List;
import java.util.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Repository;
import cloud.compan.servlet.model.Folder;
import cloud.compan.servlet.utils.JdbcExecutor;

@Singleton
@Repository
public class FolderRepository extends BaseRepository<Folder, Long> {

    @Inject
    public FolderRepository(JdbcExecutor executor) {
        super(executor);
    }

    /**
     * 查找指定用户在特定父文件夹下的所有文件夹。
     * @param ownerId 用户ID
     * @param parentFolderId 父文件夹ID (如果为null, 则查找根目录)
     * @return 文件夹列表
     */
    public Optional<List<Folder>> findByOwnerIdAndParentFolderId(Long ownerId, Long parentFolderId) {
        String sql;
        if (parentFolderId == null) {
            sql = "SELECT * FROM `folder` WHERE `owner_id` = ? AND `parent_folder_id` IS NULL AND `status` = 'active'";
            List<Folder> folders = executor.queryForList(entityClass, sql, rowMapper, ownerId);
            return Optional.of(folders);
        } else {
            sql = "SELECT * FROM `folder` WHERE `owner_id` = ? AND `parent_folder_id` = ? AND `status` = 'active'";
            List<Folder> folders = executor.queryForList(entityClass, sql, rowMapper, ownerId, parentFolderId);
            return Optional.of(folders);
        }
    }

    /**
     * 在特定父文件夹下，按名称查找文件夹 (用于检查重名)。
     * @param parentFolderId 父文件夹ID
     * @param folderName 文件夹名称
     * @param ownerId 用户ID
     * @return 文件夹 Optional
     */
    public Optional<Folder> findByParentAndName(Long parentFolderId, String folderName, Long ownerId) {
        String sql;
        if (parentFolderId == null) {
            sql = "SELECT * FROM `folder` WHERE `parent_folder_id` IS NULL AND `folder_name` = ? AND `owner_id` = ? AND `status` = 'active'";
            Optional<Folder> folder = executor.queryForObject(entityClass, sql, rowMapper, folderName, ownerId);
            return folder;
        } else {
            sql = "SELECT * FROM `folder` WHERE `parent_folder_id` = ? AND `folder_name` = ? AND `owner_id` = ? AND `status` = 'active'";
            Optional<Folder> folder = executor.queryForObject(entityClass, sql, rowMapper, parentFolderId, folderName, ownerId);
            return folder;
        }
    }
} 