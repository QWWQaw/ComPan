//package cloud.compan.servlet.repository;
//
//import cloud.compan.servlet.model.File;
//import cloud.compan.servlet.utils.JdbcExecutor;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//
//import java.util.List;
//import java.util.Optional;
//
//@Singleton
//public class FileRepository extends BaseRepository<File, Long> {
//
//    @Inject
//    public FileRepository(JdbcExecutor executor) {
//        super(executor);
//    }
//
//    /**
//     * 查找指定文件夹下的所有文件。
//     * @param folderId 文件夹ID
//     * @return 文件列表
//     */
//    public Optional<List<File>> findByFolderId(Long folderId) {
//        String sql = "SELECT * FROM `file` WHERE `folder_id` = ? AND `status` = 'active'";
//        List<File> files = executor.queryForList(entityClass, sql, rowMapper, folderId);
//        return Optional.of(files);
//    }
//
//    /**
//     * 在特定文件夹下，按名称查找文件 (用于检查重名)。
//     * @param folderId 文件夹ID
//     * @param fileName 文件名
//     * @param uploaderId 上传者ID
//     * @return 文件 Optional
//     */
//    public Optional<File> findByFolderAndName(Long folderId, String fileName, Long uploaderId) {
//        String sql = "SELECT * FROM `file` WHERE `folder_id` = ? AND `file_name` = ? AND `uploader_id` = ? AND `status` = 'active'";
//        return executor.queryForObject(entityClass, sql, rowMapper, folderId, fileName, uploaderId);
//    }
//
//    /**
//     * 根据文件哈希查找文件记录。
//     * @param objectHash 文件内容的哈希值
//     * @return 文件列表
//     */
//    public Optional<List<File>> findByObjectHash(String objectHash) {
//        String sql = "SELECT * FROM `file` WHERE `object_hash` = ?";
//        List<File> files = executor.queryForList(entityClass, sql, rowMapper, objectHash);
//        return Optional.of(files);
//    }
//}
package cloud.compan.servlet.repository;

import cloud.compan.servlet.entity.FileEntity;
import cloud.compan.servlet.entity.StorageObject;
import cloud.compan.servlet.service.DatabaseService;
import cloud.compan.servlet.annotations.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件数据访问层
 * 对应数据库表：file 和 storage_object
 */
@Repository
public class FileRepository {

    private final DatabaseService databaseService;

    public FileRepository() {
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 创建存储对象（用于文件去重）
     */
    public StorageObject createStorageObject(StorageObject storageObject) {
        String sql = "INSERT INTO storage_object (hash, size, storage_path, ref_count) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE ref_count = ref_count + 1";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, storageObject.getHash());
            stmt.setLong(2, storageObject.getSize());
            stmt.setString(3, storageObject.getStoragePath());
            stmt.setInt(4, storageObject.getRefCount());

            stmt.executeUpdate();
            return storageObject;
        } catch (SQLException e) {
            System.err.println("创建存储对象失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, null);
        }
        return null;
    }

    /**
     * 检查存储对象是否存在（用于秒传）
     */
    public StorageObject getStorageObjectByHash(String hash) {
        String sql = "SELECT * FROM storage_object WHERE hash = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, hash);

            rs = stmt.executeQuery();
            if (rs.next()) {
                StorageObject obj = new StorageObject();
                obj.setHash(rs.getString("hash"));
                obj.setSize(rs.getLong("size"));
                obj.setStoragePath(rs.getString("storage_path"));
                obj.setRefCount(rs.getInt("ref_count"));
                obj.setCreatedAt(rs.getTimestamp("created_at"));
                return obj;
            }
        } catch (SQLException e) {
            System.err.println("获取存储对象失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
        return null;
    }

    /**
     * 创建文件记录
     */
    public FileEntity createFile(FileEntity file) {
        String sql = "INSERT INTO file (uploader_id, folder_id, file_name, mime_type, object_hash, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setLong(1, file.getUploaderId());
            stmt.setLong(2, file.getFolderId());
            stmt.setString(3, file.getFileName());
            stmt.setString(4, file.getMimeType());
            stmt.setString(5, file.getObjectHash());
            stmt.setString(6, file.getStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    file.setFileId(rs.getLong(1));
                    return file;
                }
            }
        } catch (SQLException e) {
            System.err.println("创建文件记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
        return null;
    }

    /**
     * 根据文件夹ID获取文件列表
     */
    public List<FileEntity> getFilesByFolder(Long folderId, Long userId) {
        String sql = "SELECT f.*, so.size, so.storage_path FROM file f " +
                "JOIN storage_object so ON f.object_hash = so.hash " +
                "WHERE f.folder_id = ? AND f.uploader_id = ? AND f.status = 'active' " +
                "ORDER BY f.file_name";

        List<FileEntity> files = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, folderId);
            stmt.setLong(2, userId);

            rs = stmt.executeQuery();
            while (rs.next()) {
                files.add(mapResultSetToFile(rs));
            }
        } catch (SQLException e) {
            System.err.println("获取文件列表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
        return files;
    }

    /**
     * 根据文件ID获取文件详情
     */
    public FileEntity getFileById(Long fileId, Long userId) {
        String sql = "SELECT f.*, so.size, so.storage_path FROM file f " +
                "JOIN storage_object so ON f.object_hash = so.hash " +
                "WHERE f.file_id = ? AND f.uploader_id = ? AND f.status = 'active'";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            stmt.setLong(2, userId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToFile(rs);
            }
        } catch (SQLException e) {
            System.err.println("获取文件详情失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
        return null;
    }

    /**
     * 重命名文件
     */
    public boolean renameFile(Long fileId, String newName, Long userId) {
        String sql = "UPDATE file SET file_name = ?, updated_at = CURRENT_TIMESTAMP WHERE file_id = ? AND uploader_id = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, newName);
            stmt.setLong(2, fileId);
            stmt.setLong(3, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("重命名文件失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * 移动文件
     */
    public boolean moveFile(Long fileId, Long targetFolderId, Long userId) {
        String sql = "UPDATE file SET folder_id = ?, updated_at = CURRENT_TIMESTAMP WHERE file_id = ? AND uploader_id = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, targetFolderId);
            stmt.setLong(2, fileId);
            stmt.setLong(3, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("移动文件失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * 删除文件（软删除）
     */
    public boolean deleteFile(Long fileId, Long userId) {
        String sql = "UPDATE file SET status = 'deleted', deleted_at = CURRENT_TIMESTAMP WHERE file_id = ? AND uploader_id = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除文件失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * 检查文件名是否存在
     */
    public boolean isFileNameExists(String fileName, Long folderId, Long userId) {
        String sql = "SELECT COUNT(*) FROM file WHERE file_name = ? AND folder_id = ? AND uploader_id = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, fileName);
            stmt.setLong(2, folderId);
            stmt.setLong(3, userId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查文件名是否存在失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
        return false;
    }

    /**
     * 将ResultSet映射为FileEntity对象
     */
    private FileEntity mapResultSetToFile(ResultSet rs) throws SQLException {
        FileEntity file = new FileEntity();
        file.setFileId(rs.getLong("file_id"));
        file.setUploaderId(rs.getLong("uploader_id"));
        file.setFolderId(rs.getLong("folder_id"));
        file.setFileName(rs.getString("file_name"));
        file.setMimeType(rs.getString("mime_type"));
        file.setObjectHash(rs.getString("object_hash"));
        file.setStatus(rs.getString("status"));
        file.setCreatedAt(rs.getTimestamp("created_at"));
        file.setUpdatedAt(rs.getTimestamp("updated_at"));
        file.setDeletedAt(rs.getTimestamp("deleted_at"));

        // 从storage_object表获取的信息
        file.setFileSize(rs.getLong("size"));
        file.setStoragePath(rs.getString("storage_path"));

        return file;
    }
}
