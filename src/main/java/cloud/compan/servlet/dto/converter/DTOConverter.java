package cloud.compan.servlet.dto.converter;

import cloud.compan.servlet.dto.common.ResponseDTO;
import cloud.compan.servlet.dto.common.PaginationDTO;
import cloud.compan.servlet.dto.file.FileInfoDTO;
import cloud.compan.servlet.dto.user.UserStorageStatsDTO;
import cloud.compan.servlet.dto.auth.LoginResponseDTO;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;

/**
 * DTO转换工具类
 * 用于简化Handler与Service层之间的数据转换
 */
public class DTOConverter {

    /**
     * 将Service层返回的Map结构转换为标准ResponseDTO
     */
    public static <T> ResponseDTO<T> convertServiceResponse(Map<String, Object> serviceResult) {
        if (serviceResult == null) {
            return ResponseDTO.error("服务返回空结果", 500);
        }

        boolean success = (Boolean) serviceResult.getOrDefault("success", false);
        String message = (String) serviceResult.getOrDefault("message", "");
        Object data = serviceResult.get("data");
        Integer statusCode = (Integer) serviceResult.getOrDefault("status_code", success ? 200 : 400);

        ResponseDTO<T> response;
        if (success) {
            response = ResponseDTO.success(message, (T) data, statusCode);
        } else {
            response = ResponseDTO.error(message, (T) data, statusCode);
        }

        // 添加时间戳
        response.addTimestamp();

        return response;
    }

    /**
     * 将Map转换为FileInfoDTO
     */
    public static FileInfoDTO convertToFileInfoDTO(Map<String, Object> fileMap) {
        if (fileMap == null) return null;

        FileInfoDTO dto = new FileInfoDTO();
        dto.setFileId((Long) fileMap.get("file_id"));
        dto.setFileName((String) fileMap.get("file_name"));
        dto.setFileSize((Long) fileMap.get("file_size"));
        dto.setMimeType((String) fileMap.get("mime_type"));
        dto.setFolderId((Long) fileMap.get("folder_id"));
        dto.setUserId((Long) fileMap.get("user_id"));
        dto.setFileHash((String) fileMap.get("file_hash"));
        dto.setCreatedAt((Timestamp) fileMap.get("created_at"));
        dto.setUpdatedAt((Timestamp) fileMap.get("updated_at"));

        // 生成下载链接
        Long fileId = dto.getFileId();
        if (fileId != null) {
            dto.setDownloadUrl("/api/v1/files/" + fileId + "/download");
        }

        return dto;
    }

    /**
     * 将文件Map列表转换为FileInfoDTO列表
     */
    public static List<FileInfoDTO> convertToFileInfoDTOList(List<Map<String, Object>> fileMaps) {
        if (fileMaps == null) return new ArrayList<>();

        List<FileInfoDTO> dtoList = new ArrayList<>();
        for (Map<String, Object> fileMap : fileMaps) {
            FileInfoDTO dto = convertToFileInfoDTO(fileMap);
            if (dto != null) {
                dtoList.add(dto);
            }
        }
        return dtoList;
    }

    /**
     * 将Map转换为PaginationDTO
     */
    public static PaginationDTO convertToPaginationDTO(Map<String, Object> paginationMap) {
        if (paginationMap == null) return null;

        PaginationDTO dto = new PaginationDTO();
        dto.setCurrentPage((Integer) paginationMap.get("current_page"));
        dto.setPerPage((Integer) paginationMap.get("per_page"));
        dto.setTotal((Integer) paginationMap.get("total"));
        dto.setTotalPages((Integer) paginationMap.get("total_pages"));
        dto.setHasNext((Boolean) paginationMap.get("has_next"));
        dto.setHasPrev((Boolean) paginationMap.get("has_prev"));

        return dto;
    }

    /**
     * 将Map转换为UserStorageStatsDTO
     */
    public static UserStorageStatsDTO convertToStorageStatsDTO(Map<String, Object> statsMap) {
        if (statsMap == null) return null;

        UserStorageStatsDTO dto = new UserStorageStatsDTO();
        dto.setStorageLimit((Long) statsMap.get("storage_limit"));
        dto.setStorageUsed((Long) statsMap.get("storage_used"));
        dto.setStorageAvailable((Long) statsMap.get("storage_available"));
        dto.setUsagePercentage((Double) statsMap.get("usage_percentage"));

        // 修复类型转换问题：Long转Integer
        Long fileCount = (Long) statsMap.get("file_count");
        Long folderCount = (Long) statsMap.get("folder_count");
        dto.setFileCount(fileCount != null ? fileCount.intValue() : 0);
        dto.setFolderCount(folderCount != null ? folderCount.intValue() : 0);

        return dto;
    }

    /**
     * 将Map转换为LoginResponseDTO
     */
    public static LoginResponseDTO convertToLoginResponseDTO(Map<String, Object> loginMap) {
        if (loginMap == null) return null;

        String token = (String) loginMap.get("token");
        Long expiresIn = ((Number) loginMap.get("expires_in")).longValue();
        Map<String, Object> userMap = (Map<String, Object>) loginMap.get("user");

        LoginResponseDTO.UserInfoDTO userInfo = LoginResponseDTO.UserInfoDTO.fromMap(userMap);

        return new LoginResponseDTO(token, expiresIn, userInfo);
    }

    /**
     * 创建成功的文件列表响应
     */
    public static ResponseDTO<Map<String, Object>> createFileListResponse(
            List<FileInfoDTO> files, PaginationDTO pagination) {

        Map<String, Object> data = Map.of(
            "files", files,
            "pagination", pagination
        );

        return ResponseDTO.success("获取文件列表成功", data)
                .addTimestamp();
    }

    /**
     * 创建错误响应并添加元数据
     */
    public static <T> ResponseDTO<T> createErrorResponse(String message, int statusCode, String errorCode) {
        ResponseDTO<T> response = ResponseDTO.error(message, statusCode);
        response.addMetadata("error_code", errorCode);
        response.addTimestamp();
        return response;
    }

    /**
     * 创建成功响应并添加元数据
     */
    public static <T> ResponseDTO<T> createSuccessResponse(String message, T data, String operationType) {
        ResponseDTO<T> response = ResponseDTO.success(message, data);
        response.addMetadata("operation", operationType);
        response.addTimestamp();
        return response;
    }

    /**
     * 验证并转换分页参数
     */
    public static Map<String, Object> convertPaginationParams(String pageStr, String perPageStr) {
        int page = 1;
        int perPage = 20;

        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }

        if (perPageStr != null && !perPageStr.trim().isEmpty()) {
            try {
                perPage = Integer.parseInt(perPageStr);
                if (perPage < 1) perPage = 20;
                if (perPage > 100) perPage = 100; // 限制最大值
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }

        return Map.of("page", page, "perPage", perPage);
    }

    /**
     * 验证并转换Long类型ID参数
     */
    public static Long convertIdParameter(String idStr, String paramName) throws IllegalArgumentException {
        if (idStr == null || idStr.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + "不能为空");
        }

        try {
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的" + paramName);
        }
    }

    /**
     * 安全地获取字符串参数
     */
    public static String getSafeStringParam(String param) {
        return param != null ? param.trim() : null;
    }

    /**
     * 检查字符串参数是否为空
     */
    public static boolean isEmptyParam(String param) {
        return param == null || param.trim().isEmpty();
    }
}
