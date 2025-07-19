package cloud.compan.servlet.dto.example;

import cloud.compan.servlet.dto.common.ResponseDTO;
import cloud.compan.servlet.dto.converter.DTOConverter;
import cloud.compan.servlet.dto.file.FileInfoDTO;
import cloud.compan.servlet.dto.file.FileListQueryDTO;
import cloud.compan.servlet.dto.auth.LoginResponseDTO;
import cloud.compan.servlet.dto.UserLoginDTO;
import cloud.compan.servlet.dto.UserRegisterDTO;

import javax.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * DTO使用示例类
 * 展示如何在Handler与Service层之间使用DTO进行数据传输
 */
public class DTOUsageExample {

    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();

    /**
     * 示例1：在AuthHandler中使用DTO处理登录请求
     */
    public static class AuthHandlerExample {

        public void handleLogin(HttpServletRequest request) {
            // 1. 从请求中创建DTO
            UserLoginDTO loginDTO = new UserLoginDTO(
                request.getParameter("username"),
                request.getParameter("password")
            );

            // 2. 使用Hibernate Validator验证DTO
            Set<ConstraintViolation<UserLoginDTO>> violations = validator.validate(loginDTO);
            if (!violations.isEmpty()) {
                String errorMessage = violations.iterator().next().getMessage();
                ResponseDTO<String> response = DTOConverter.createErrorResponse(
                    "登录参数验证失败: " + errorMessage, 400, "INVALID_LOGIN_PARAMS");
                // 返回响应...
                return;
            }

            // 3. 调用Service层（这里假设有AuthService）
            // Map<String, Object> serviceResult = authService.login(loginDTO.getUsername(), loginDTO.getPassword());

            // 4. 转换Service响应为标准DTO响应
            // ResponseDTO<LoginResponseDTO> response = DTOConverter.convertServiceResponse(serviceResult);

            System.out.println("登录请求处理完成");
        }

        public void handleRegister(HttpServletRequest request) {
            // 创建注册DTO
            UserRegisterDTO registerDTO = new UserRegisterDTO(
                request.getParameter("username"),
                request.getParameter("email"),
                request.getParameter("password")
            );

            // 使用Hibernate Validator验证DTO
            Set<ConstraintViolation<UserRegisterDTO>> violations = validator.validate(registerDTO);
            if (!violations.isEmpty()) {
                String errorMessage = violations.iterator().next().getMessage();
                ResponseDTO<String> response = DTOConverter.createErrorResponse(
                    "注册参数验证失败: " + errorMessage, 400, "INVALID_REGISTER_PARAMS");
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

            // 3. 使用Hibernate Validator验证DTO
            Set<ConstraintViolation<FileListQueryDTO>> violations = validator.validate(queryDTO);
            if (!violations.isEmpty()) {
                String errorMessage = violations.iterator().next().getMessage();
                ResponseDTO<String> response = DTOConverter.createErrorResponse(
                    "查询参数验证失败: " + errorMessage, 400, "INVALID_QUERY_PARAMS");
                return;
            }

            // 4. 调用Service层获取文件列表
            // Map<String, Object> serviceResult = fileService.getFileList(queryDTO);
            // ResponseDTO<Map<String, Object>> response = DTOConverter.convertServiceResponse(serviceResult);

            System.out.println("文件列表查询完成");
        }

        /**
         * 辅助方法：安全地解析整数参数
         */
        private Integer parseIntParam(String param, Integer defaultValue) {
            if (param == null || param.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(param);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    /**
     * 示例3：展示如何使用DTOConverter转换复杂响应
     */
    public static class ResponseConversionExample {

        public void demonstrateResponseConversion() {
            // 模拟Service层返回的Map结构
            Map<String, Object> serviceResult = Map.of(
                "success", true,
                "message", "操作成功",
                "data", Map.of(
                    "user_id", 1L,
                    "username", "testuser",
                    "email", "test@example.com"
                ),
                "status_code", 200
            );

            // 使用DTOConverter转换为标准响应格式
            ResponseDTO<Map<String, Object>> response = DTOConverter.convertServiceResponse(serviceResult);

            System.out.println("转换后的响应: " + response.getMessage());
            System.out.println("是否成功: " + response.getSuccess());
            System.out.println("状态码: " + response.getStatusCode());
        }

        public void demonstrateFileInfoConversion() {
            // 模拟数据库返回的文件信息Map
            Map<String, Object> fileMap = Map.of(
                "file_id", 1L,
                "file_name", "example.pdf",
                "file_size", 1024000L,
                "mime_type", "application/pdf",
                "folder_id", 10L,
                "user_id", 1L,
                "file_hash", "abc123hash"
            );

            // 转换为FileInfoDTO
            FileInfoDTO fileInfoDTO = DTOConverter.convertToFileInfoDTO(fileMap);

            System.out.println("文件名: " + fileInfoDTO.getFileName());
            System.out.println("文件大小: " + fileInfoDTO.getFileSize() + " bytes");
            System.out.println("下载链接: " + fileInfoDTO.getDownloadUrl());
        }
    }

    /**
     * 示例4：展示Hibernate Validator的高级用法
     */
    public static class ValidationExample {

        public boolean validateDTO(Object dto) {
            Set<ConstraintViolation<Object>> violations = validator.validate(dto);

            if (violations.isEmpty()) {
                System.out.println("DTO验证通过");
                return true;
            }

            System.out.println("DTO验证失败:");
            for (ConstraintViolation<Object> violation : violations) {
                System.out.println("- " + violation.getPropertyPath() + ": " + violation.getMessage());
            }
            return false;
        }

        public void demonstrateValidation() {
            // 测试无效的登录DTO
            UserLoginDTO invalidLogin = new UserLoginDTO("", ""); // 空用户名和密码
            validateDTO(invalidLogin);

            // 测试有效的登录DTO
            UserLoginDTO validLogin = new UserLoginDTO("testuser", "password123");
            validateDTO(validLogin);

            // 测试无效的注册DTO
            UserRegisterDTO invalidRegister = new UserRegisterDTO("ab", "invalid-email", "123"); // 用户名太短，邮箱格式错误，密码太短
            validateDTO(invalidRegister);
        }
    }
}
