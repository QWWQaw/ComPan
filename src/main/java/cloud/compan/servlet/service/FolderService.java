package cloud.compan.servlet.service;

import cloud.compan.servlet.entity.Folder;
import cloud.compan.servlet.repository.FolderRepository;
import cloud.compan.servlet.annotations.component.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 文件夹业务逻辑层
 * 处理文件夹相关的业务逻辑
 */
@Service
public class FolderService {

    private final FolderRepository folderRepository;

    public FolderService() {
        this.folderRepository = new FolderRepository();
    }

    /**
     * 创建文件夹
     */
    public Map<String, Object> createFolder(String folderName, Long parentFolderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证输入参数
            if (folderName == null || folderName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件夹名称不能为空");
                return result;
            }

            // 2. 检查文件夹名称是否已存在
            if (folderRepository.isFolderNameExists(folderName, parentFolderId, userId)) {
                result.put("success", false);
                result.put("message", "文件夹名称已存在");
                return result;
            }

            // 3. 创建文件夹
            Folder folder = new Folder(folderName, userId, parentFolderId);
            Folder createdFolder = folderRepository.createFolder(folder);

            if (createdFolder != null) {
                result.put("success", true);
                result.put("message", "文件夹创建成功");
                result.put("data", createdFolder);
            } else {
                result.put("success", false);
                result.put("message", "文件夹创建失败");
            }

        } catch (Exception e) {
            System.err.println("创建文件夹业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误，请稍后重试");
        }

        return result;
    }

    /**
     * 获取文件夹列表
     */
    public Map<String, Object> getFolderList(Long userId, Long parentFolderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Folder> folders = folderRepository.getFoldersByUserAndParent(userId, parentFolderId);

            result.put("success", true);
            result.put("message", "获取文件夹列表成功");
            result.put("data", Map.of(
                "folders", folders,
                "total", folders.size(),
                "parent_folder_id", parentFolderId
            ));

        } catch (Exception e) {
            System.err.println("获取文件夹列表业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取文件夹列表失败");
        }

        return result;
    }

    /**
     * 获取文件夹详情
     */
    public Map<String, Object> getFolderInfo(Long folderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Folder folder = folderRepository.getFolderById(folderId, userId);

            if (folder != null) {
                result.put("success", true);
                result.put("message", "获取文件夹详情成功");
                result.put("data", folder);
            } else {
                result.put("success", false);
                result.put("message", "文件夹不存在或无权限访问");
            }

        } catch (Exception e) {
            System.err.println("获取文件夹详情业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取文件夹详情失败");
        }

        return result;
    }

    /**
     * 重命名文件夹
     */
    public Map<String, Object> renameFolder(Long folderId, String newName, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证输入参数
            if (newName == null || newName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件夹名称不能为空");
                return result;
            }

            // 2. 获取原文件夹信息
            Folder folder = folderRepository.getFolderById(folderId, userId);
            if (folder == null) {
                result.put("success", false);
                result.put("message", "文件夹不存在或无权限访问");
                return result;
            }

            // 3. 检查新名称是否与其他文件夹冲突
            if (folderRepository.isFolderNameExists(newName, folder.getParentFolderId(), userId)) {
                result.put("success", false);
                result.put("message", "文件夹名称已存在");
                return result;
            }

            // 4. 执行重命名
            boolean success = folderRepository.updateFolderName(folderId, newName, userId);

            if (success) {
                folder.setFolderName(newName);
                result.put("success", true);
                result.put("message", "文件夹重命名成功");
                result.put("data", folder);
            } else {
                result.put("success", false);
                result.put("message", "文件夹重命名失败");
            }

        } catch (Exception e) {
            System.err.println("重命名文件夹业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "重命名文件夹失败");
        }

        return result;
    }

    /**
     * 移动文件夹
     */
    public Map<String, Object> moveFolder(Long folderId, Long targetParentId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取原文件夹信息
            Folder folder = folderRepository.getFolderById(folderId, userId);
            if (folder == null) {
                result.put("success", false);
                result.put("message", "文件夹不存在或无权限访问");
                return result;
            }

            // 2. 检查目标文件夹是否存在（如果不是移动到根目录）
            if (targetParentId != null) {
                Folder targetFolder = folderRepository.getFolderById(targetParentId, userId);
                if (targetFolder == null) {
                    result.put("success", false);
                    result.put("message", "目标文件夹不存在");
                    return result;
                }
            }

            // 3. 检查目标位置是否有同名文件夹
            if (folderRepository.isFolderNameExists(folder.getFolderName(), targetParentId, userId)) {
                result.put("success", false);
                result.put("message", "目标位置已存在同名文件夹");
                return result;
            }

            // 4. 执行移动
            boolean success = folderRepository.moveFolder(folderId, targetParentId, userId);

            if (success) {
                result.put("success", true);
                result.put("message", "文件夹移动成功");
                result.put("data", Map.of(
                    "folder_id", folderId,
                    "old_parent_id", folder.getParentFolderId(),
                    "new_parent_id", targetParentId
                ));
            } else {
                result.put("success", false);
                result.put("message", "文件夹移动失败");
            }

        } catch (Exception e) {
            System.err.println("移动文件夹业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "移动文件夹失败");
        }

        return result;
    }

    /**
     * 删除文件夹
     */
    public Map<String, Object> deleteFolder(Long folderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取文件夹信息
            Folder folder = folderRepository.getFolderById(folderId, userId);
            if (folder == null) {
                result.put("success", false);
                result.put("message", "文件夹不存在或无权限访问");
                return result;
            }

            // 2. 执行删除（软删除）
            boolean success = folderRepository.deleteFolder(folderId, userId);

            if (success) {
                result.put("success", true);
                result.put("message", "文件夹已移入回收站");
                result.put("data", Map.of(
                    "folder_id", folderId,
                    "folder_name", folder.getFolderName()
                ));
            } else {
                result.put("success", false);
                result.put("message", "删除文件夹失败");
            }

        } catch (Exception e) {
            System.err.println("删除文件夹业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除文件夹失败");
        }

        return result;
    }
}
