package cloud.compan.servlet.dto.example;

import cloud.compan.servlet.dto.common.ResponseDTO;
import cloud.compan.servlet.dto.converter.DTOConverter;
import cloud.compan.servlet.dto.file.FileInfoDTO;
import cloud.compan.servlet.dto.file.FileListQueryDTO;
import cloud.compan.servlet.dto.auth.LoginResponseDTO;
import cloud.compan.servlet.dto.UserLoginDTO;
import cloud.compan.servlet.dto.UserRegisterDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;

/**
 * DTO使用示例类
 * 展示如何在Handler与Service层之间使用DTO进行数据传输
 */
public class DTOUsageExample {

    /**
     * 示例1：在AuthHandler中使用DTO处理登录请求
     */
    public static class AuthHandlerExample {

        public void handleLogin(HttpServletRequest request) {
            // 1. 从请求中创建DTO
            UserLoginDTO loginDTO = new UserLoginDTO();
            loginDTO.setUsername(request.getParameter("username"));
            loginDTO.setPassword(request.getParameter("password"));

            // 2. 验证DTO
            if (!loginDTO.isValid()) {
                ResponseDTO<String> response = DTOConverter.createErrorResponse(
                    "登录参数验证失败", 400, "INVALID_LOGIN_PARAMS");
                // 返回响应...
                return;
            }

            // 3. 调用Service层（这里假设有AuthService）
            // Map<String, Object> serviceResult = authService.login(loginDTO.getUsername(), loginDTO.getPassword(), session);

            // 4. 转换Service响应为标准DTO响应
            // ResponseDTO<LoginResponseDTO> response = DTOConverter.convertServiceResponse(serviceResult);

            System.out.println("登录请求处理完成");
        }

        public void handleRegister(HttpServletRequest request) {
            // 创建注册DTO
            UserRegisterDTO registerDTO = new UserRegisterDTO();
            registerDTO.setUsername(request.getParameter("username"));
            registerDTO.setEmail(request.getParameter("email"));
            registerDTO.setPassword(request.getParameter("password"));

            // 验证DTO
            if (!registerDTO.isValid()) {
                ResponseDTO<String> response = DTOConverter.createErrorResponse(
                    "注册参数验证失败", 400, "INVALID_REGISTER_PARAMS");
                return;
            }

            System.out.println("注册请求处理完成");
        }
    }

    /**
     * 示例2：在FileHandler中使用DTO处理文件列表请求
     */
    public static class FileHandlerExample {

        public void handleGetFileList(HttpServletRequest request) {
            // 1. 创建查询DTO
            FileListQueryDTO queryDTO = new FileListQueryDTO();

            // 2. 从请求参数填充DTO
            String folderIdStr = request.getParameter("folder_id");
            if (folderIdStr != null && !folderIdStr.trim().isEmpty()) {
                try {
                    queryDTO.setFolderId(Long.parseLong(folderIdStr));
                } catch (NumberFormatException e) {
                    ResponseDTO<String> response = DTOConverter.createErrorResponse(
                        "无效的文件夹ID", 400, "INVALID_FOLDER_ID");
                    return;
                }
            }

            queryDTO.setPage(parseIntParam(request.getParameter("page"), 1));
            queryDTO.setPerPage(parseIntParam(request.getParameter("per_page"), 20));
            queryDTO.setSortBy(request.getParameter("sort_by"));
            queryDTO.setSortOrder(request.getParameter("sort_order"));
            queryDTO.setSearch(request.getParameter("search"));
            queryDTO.setFileType(request.getParameter("file_type"));

            // 3. 验证DTO
            if (!queryDTO.isValid()) {
                ResponseDTO<String> response = DTOConverter.createErrorResponse(
                    "查询参数验证失败", 400, "INVALID_QUERY_PARAMS");
                return;
            }

            // 4. 调用Service层处理
            // Map<String, Object> serviceResult = fileService.handleGetFileList(...);

            // 5. 转换响应
            // ResponseDTO<Map<String, Object>> response = DTOConverter.convertServiceResponse(serviceResult);

            System.out.println("文件列表查询完成");
        }

        private Integer parseIntParam(String param, int defaultValue) {
            if (param == null || param.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                int value = Integer.parseInt(param);
                return value > 0 ? value : defaultValue;
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    /**
     * 示例3：在Service层中使用DTO转换器
     */
    public static class ServiceLayerExample {

        public Map<String, Object> processFileListResult(List<Map<String, Object>> fileMaps,
                                                        Map<String, Object> paginationMap) {
            // 1. 转换文件列表
            List<FileInfoDTO> files = DTOConverter.convertToFileInfoDTOList(fileMaps);

            // 2. 转换分页信息
            // PaginationDTO pagination = DTOConverter.convertToPaginationDTO(paginationMap);

            // 3. 创建响应
            // ResponseDTO<Map<String, Object>> response = DTOConverter.createFileListResponse(files, pagination);

            // 4. 返回给Handler层
            return Map.of(
                "success", true,
                "message", "处理成功",
                "data", Map.of("files", files),
                "status_code", 200
            );
        }
    }

    /**
     * 示例4：错误处理的最佳实践
     */
    public static class ErrorHandlingExample {

        public ResponseDTO<String> handleValidationError(String field, String message) {
            return DTOConverter.<String>createErrorResponse(
                "参数验证失败: " + field + " - " + message,
                400,
                "VALIDATION_ERROR"
            ).addMetadata("field", field);
        }

        public ResponseDTO<String> handleNotFoundError(String resourceType, Long resourceId) {
            return DTOConverter.<String>createErrorResponse(
                resourceType + "不存在",
                404,
                "RESOURCE_NOT_FOUND"
            ).addMetadata("resource_type", resourceType)
             .addMetadata("resource_id", resourceId);
        }

        public ResponseDTO<String> handlePermissionError(String operation) {
            return DTOConverter.<String>createErrorResponse(
                "无权限执行操作: " + operation,
                403,
                "PERMISSION_DENIED"
            ).addMetadata("operation", operation);
        }
    }
}
