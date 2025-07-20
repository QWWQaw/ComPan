package cloud.compan.servlet.dto.mapper;

import cloud.compan.servlet.model.User;
import cloud.compan.servlet.dto.UserDTO;
import cloud.compan.servlet.dto.UserRegistrationDTO;
import cloud.compan.servlet.dto.LoginResultDTO;
import cloud.compan.servlet.dto.StorageStatsDTO;

import java.time.LocalDateTime;

/**
 * 用户实体与DTO之间的映射器
 * 负责Entity和DTO之间的数据转换
 */
public class UserMapper {
    
    /**
     * 将User实体转换为UserDTO
     */
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setStorageLimit(user.getStorageLimit());
        dto.setStorageUsed(user.getStorageUsed());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * 将UserDTO转换为User实体
     */
    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setStorageLimit(dto.getStorageLimit());
        user.setStorageUsed(dto.getStorageUsed());
        user.setCreatedAt(dto.getCreatedAt());
        user.setUpdatedAt(dto.getUpdatedAt());
        
        return user;
    }
    
    /**
     * 将UserRegistrationDTO转换为User实体
     */
    public static User toEntity(UserRegistrationDTO registrationDTO) {
        if (registrationDTO == null) {
            return null;
        }
        
        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        // 密码在服务层处理加密
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setStorageLimit(10737418240L); // 默认10GB
        user.setStorageUsed(0L);
        
        return user;
    }
    
    /**
     * 创建LoginResultDTO
     */
    public static LoginResultDTO createLoginResultDTO(User user, String token, long expiresIn) {
        if (user == null) {
            return null;
        }
        
        UserDTO userDTO = toDTO(user);
        return new LoginResultDTO(userDTO, token, expiresIn);
    }
    
    /**
     * 创建StorageStatsDTO
     */
    public static StorageStatsDTO createStorageStatsDTO(long storageLimit, long storageUsed, int fileCount, int folderCount) {
        return new StorageStatsDTO(storageLimit, storageUsed, fileCount, folderCount);
    }
    
    /**
     * 更新User实体的部分字段（用于更新操作）
     */
    public static void updateEntity(User target, UserDTO source) {
        if (target == null || source == null) {
            return;
        }
        
        if (source.getUsername() != null) {
            target.setUsername(source.getUsername());
        }
        if (source.getEmail() != null) {
            target.setEmail(source.getEmail());
        }
        if (source.getStorageLimit() != null) {
            target.setStorageLimit(source.getStorageLimit());
        }
        if (source.getStorageUsed() != null) {
            target.setStorageUsed(source.getStorageUsed());
        }
        
        target.setUpdatedAt(LocalDateTime.now());
    }
} 