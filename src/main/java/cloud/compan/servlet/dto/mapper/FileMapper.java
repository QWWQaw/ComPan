package cloud.compan.servlet.dto.mapper;

import cloud.compan.servlet.dto.FileDTO;
import cloud.compan.servlet.model.File;
import com.google.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件映射器
 * 实现File实体和FileDTO之间的转换
 */
@Singleton
public class FileMapper {
    
    /**
     * 将File实体转换为FileDTO
     * @param file File实体
     * @return FileDTO对象
     */
    public static FileDTO toDTO(File file) {
        if (file == null) {
            return null;
        }
        
        // TODO: 实现File实体到FileDTO的转换
        // 现在返回空的FileDTO，待File实体的getter方法可用后完善
        FileDTO dto = new FileDTO();
        
        // 设置基本扩展字段
        dto.extractFileExtension();
        dto.setFileIconByMimeType();
        dto.generateDownloadUrl();
        dto.generatePreviewUrl();
        dto.generateThumbnailUrl("small");
        
        return dto;
    }
    
    /**
     * 将File实体转换为FileDTO，包含扩展信息
     * @param file File实体
     * @param fileSize 文件大小（从其他来源获取）
     * @param uploaderName 上传者名称
     * @param folderName 文件夹名称
     * @param folderPath 文件夹路径
     * @return FileDTO对象
     */
    public static FileDTO toDTOWithExtendedInfo(File file, Long fileSize, String uploaderName, 
                                               String folderName, String folderPath) {
        FileDTO dto = toDTO(file);
        if (dto != null) {
            dto.setFileSize(fileSize);
            dto.setUploaderName(uploaderName);
            dto.setFolderName(folderName);
            dto.setFolderPath(folderPath);
        }
        return dto;
    }
    
    /**
     * 将File实体转换为FileDTO，包含权限信息
     * @param file File实体
     * @param canRead 是否可读
     * @param canWrite 是否可写
     * @param canDelete 是否可删除
     * @param isShared 是否已分享
     * @return FileDTO对象
     */
    public static FileDTO toDTOWithPermissions(File file, Boolean canRead, Boolean canWrite, 
                                              Boolean canDelete, Boolean isShared) {
        FileDTO dto = toDTO(file);
        if (dto != null) {
            dto.setCanRead(canRead);
            dto.setCanWrite(canWrite);
            dto.setCanDelete(canDelete);
            dto.setIsShared(isShared);
        }
        return dto;
    }
    
    /**
     * 将FileDTO转换为File实体
     * @param dto FileDTO对象
     * @return File实体
     */
    public static File toEntity(FileDTO dto) {
        if (dto == null) {
            return null;
        }
        
        // TODO: 实现FileDTO到File实体的转换
        // 现在返回空的File实体，待File实体的setter方法可用后完善
        File file = new File();
        
        return file;
    }
    
    /**
     * 将File实体列表转换为FileDTO列表
     * @param files File实体列表
     * @return FileDTO列表
     */
    public static List<FileDTO> toDTOList(List<File> files) {
        if (files == null) {
            return null;
        }
        
        return files.stream()
                .map(FileMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将FileDTO列表转换为File实体列表
     * @param dtos FileDTO列表
     * @return File实体列表
     */
    public static List<File> toEntityList(List<FileDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
                .map(FileMapper::toEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新FileDTO的权限信息
     * @param dto FileDTO对象
     * @param canRead 是否可读
     * @param canWrite 是否可写
     * @param canDelete 是否可删除
     */
    public static void updatePermissions(FileDTO dto, Boolean canRead, Boolean canWrite, Boolean canDelete) {
        if (dto != null) {
            dto.setCanRead(canRead);
            dto.setCanWrite(canWrite);
            dto.setCanDelete(canDelete);
        }
    }
    
    /**
     * 更新FileDTO的扩展信息
     * @param dto FileDTO对象
     * @param fileSize 文件大小
     * @param uploaderName 上传者名称
     * @param folderPath 文件夹路径
     */
    public static void updateExtendedInfo(FileDTO dto, Long fileSize, String uploaderName, String folderPath) {
        if (dto != null) {
            dto.setFileSize(fileSize);
            dto.setUploaderName(uploaderName);
            dto.setFolderPath(folderPath);
        }
    }
    
    /**
     * 创建简化的FileDTO（仅包含基本信息）
     * @param file File实体
     * @return 简化的FileDTO对象
     */
    public static FileDTO toSimpleDTO(File file) {
        if (file == null) {
            return null;
        }
        
        // TODO: 实现简化的File实体到FileDTO的转换
        FileDTO dto = new FileDTO();
        
        // 设置基本扩展字段
        dto.extractFileExtension();
        dto.setFileIconByMimeType();
        
        return dto;
    }
    
    /**
     * 批量更新FileDTO列表的权限信息
     * @param dtos FileDTO列表
     * @param userId 用户ID（用于权限检查）
     */
    public static void batchUpdatePermissions(List<FileDTO> dtos, Long userId) {
        if (dtos == null || userId == null) {
            return;
        }
        
        for (FileDTO dto : dtos) {
            // 这里可以根据实际业务逻辑设置权限
            // 目前设置默认权限
            boolean isOwner = userId.equals(dto.getUploaderId());
            dto.setCanRead(true);
            dto.setCanWrite(isOwner);
            dto.setCanDelete(isOwner);
        }
    }
} 