//package cloud.compan.servlet.repository;
//
//import cloud.compan.servlet.model.Folder;
//import cloud.compan.servlet.utils.JdbcExecutor;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//
//import java.util.List;
//import java.util.Optional;
//
//@Singleton
//public class FolderRepository extends BaseRepository<Folder, Long> {
//
//    @Inject
//    public FolderRepository(JdbcExecutor executor) {
//        super(executor);
//    }
//
//    /**
//     * 查找指定用户在特定父文件夹下的所有文件夹。
//     * @param ownerId 用户ID
//     * @param parentFolderId 父文件夹ID (如果为null, 则查找根目录)
//     * @return 文件夹列表
//     */
//    public Optional<List<Folder>> findByOwnerIdAndParentFolderId(Long ownerId, Long parentFolderId) {
//        String sql;
//        if (parentFolderId == null) {
//            sql = "SELECT * FROM `folder` WHERE `owner_id` = ? AND `parent_folder_id` IS NULL AND `status` = 'active'";
//            List<Folder> folders = executor.queryForList(entityClass, sql, rowMapper, ownerId);
//            return Optional.of(folders);
//        } else {
//            sql = "SELECT * FROM `folder` WHERE `owner_id` = ? AND `parent_folder_id` = ? AND `status` = 'active'";
//            List<Folder> folders = executor.queryForList(entityClass, sql, rowMapper, ownerId, parentFolderId);
//            return Optional.of(folders);
//        }
//    }
//
//    /**
//     * 在特定父文件夹下，按名称查找文件夹 (用于检查重名)。
//     * @param parentFolderId 父文件夹ID
//     * @param folderName 文件夹名称
//     * @param ownerId 用户ID
//     * @return 文件夹 Optional
//     */
//    public Optional<Folder> findByParentAndName(Long parentFolderId, String folderName, Long ownerId) {
//        String sql;
//        if (parentFolderId == null) {
//            sql = "SELECT * FROM `folder` WHERE `parent_folder_id` IS NULL AND `folder_name` = ? AND `owner_id` = ? AND `status` = 'active'";
//            Optional<Folder> folder = executor.queryForObject(entityClass, sql, rowMapper, folderName, ownerId);
//            return folder;
//        } else {
//            sql = "SELECT * FROM `folder` WHERE `parent_folder_id` = ? AND `folder_name` = ? AND `owner_id` = ? AND `status` = 'active'";
//            Optional<Folder> folder = executor.queryForObject(entityClass, sql, rowMapper, parentFolderId, folderName, ownerId);
//            return folder;
//        }
//    }
//}
package cloud.compan.servlet.repository;

import cloud.compan.servlet.entity.Folder;
import cloud.compan.servlet.service.DatabaseService;
import cloud.compan.servlet.annotations.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件夹数据访问层
 * 对应数据库表：folder
 */
@Repository
public class FolderRepository {

    private final DatabaseService databaseService;

    public FolderRepository() {
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 创建文件夹
     */
    public Folder createFolder(Folder folder) {
        String sql = "INSERT INTO folder (owner_id, parent_folder_id, folder_name, size, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setLong(1, folder.getOwnerId());
            stmt.setObject(2, folder.getParentFolderId());
            stmt.setString(3, folder.getFolderName());
            stmt.setLong(4, folder.getSize());
            stmt.setString(5, folder.getStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    folder.setFolderId(rs.getLong(1));
                    return folder;
                }
            }
        } catch (SQLException e) {
            System.err.println("创建文件夹失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
        return null;
    }

    /**
     * 根据用户ID和父文件夹ID获取文件夹列表
     */
    public List<Folder> getFoldersByUserAndParent(Long userId, Long parentFolderId) {
        String sql = "SELECT * FROM folder WHERE owner_id = ? AND parent_folder_id ";
        if (parentFolderId == null) {
            sql += "IS NULL";
        } else {
            sql += "= ?";
        }
        sql += " AND status = 'active' ORDER BY folder_name";

        List<Folder> folders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            if (parentFolderId != null) {
                stmt.setLong(2, parentFolderId);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                folders.add(mapResultSetToFolder(rs));
            }
        } catch (SQLException e) {
            System.err.println("获取文件夹列表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
        return folders;
    }

    /**
     * 根据ID获取文件夹
     */
    public Folder getFolderById(Long folderId, Long userId) {
        String sql = "SELECT * FROM folder WHERE folder_id = ? AND owner_id = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, folderId);
            stmt.setLong(2, userId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToFolder(rs);
            }
        } catch (SQLException e) {
            System.err.println("获取文件夹详情失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
        return null;
    }

    /**
     * 更新文件夹名称
     */
    public boolean updateFolderName(Long folderId, String newName, Long userId) {
        String sql = "UPDATE folder SET folder_name = ?, updated_at = CURRENT_TIMESTAMP WHERE folder_id = ? AND owner_id = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, newName);
            stmt.setLong(2, folderId);
            stmt.setLong(3, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新文件夹名称失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * 移动文件夹
     */
    public boolean moveFolder(Long folderId, Long newParentId, Long userId) {
        String sql = "UPDATE folder SET parent_folder_id = ?, updated_at = CURRENT_TIMESTAMP WHERE folder_id = ? AND owner_id = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setObject(1, newParentId);
            stmt.setLong(2, folderId);
            stmt.setLong(3, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("移动文件夹失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * 删除文件夹（软删除）
     */
    public boolean deleteFolder(Long folderId, Long userId) {
        String sql = "UPDATE folder SET status = 'deleted', deleted_at = CURRENT_TIMESTAMP WHERE folder_id = ? AND owner_id = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, folderId);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除文件夹失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * 检查文件夹名称是否存在
     */
    public boolean isFolderNameExists(String folderName, Long parentFolderId, Long userId) {
        String sql = "SELECT COUNT(*) FROM folder WHERE folder_name = ? AND parent_folder_id ";
        if (parentFolderId == null) {
            sql += "IS NULL";
        } else {
            sql += "= ?";
        }
        sql += " AND owner_id = ? AND status = 'active'";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, folderName);
            int paramIndex = 2;
            if (parentFolderId != null) {
                stmt.setLong(paramIndex++, parentFolderId);
            }
            stmt.setLong(paramIndex, userId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查文件夹名称是否存在失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
        return false;
    }

    /**
     * 将ResultSet映射为Folder对象
     */
    private Folder mapResultSetToFolder(ResultSet rs) throws SQLException {
        Folder folder = new Folder();
        folder.setFolderId(rs.getLong("folder_id"));
        folder.setOwnerId(rs.getLong("owner_id"));
        folder.setParentFolderId(rs.getObject("parent_folder_id", Long.class));
        folder.setFolderName(rs.getString("folder_name"));
        folder.setSize(rs.getLong("size"));
        folder.setStatus(rs.getString("status"));
        folder.setCreatedAt(rs.getTimestamp("created_at"));
        folder.setUpdatedAt(rs.getTimestamp("updated_at"));
        folder.setDeletedAt(rs.getTimestamp("deleted_at"));
        return folder;
    }
}
