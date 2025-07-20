package cloud.compan.servlet.web.response;

import java.time.Instant;
import java.util.List;

/**
 * 统一API响应包装器
 * 符合API 1.3文档规范的响应格式
 */
public class ApiResponseWrapper {
    
    private final boolean success;
    private final int code;
    private final String message;
    private final Object data;
    private final String timestamp;
    
    private ApiResponseWrapper(boolean success, int code, String message, Object data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().toString();
    }
    
    /**
     * 创建成功响应
     */
    public static ApiResponseWrapper success(Object data) {
        return new ApiResponseWrapper(true, 200, "操作成功", data);
    }
    
    public static ApiResponseWrapper success(String message, Object data) {
        return new ApiResponseWrapper(true, 200, message, data);
    }
    
    public static ApiResponseWrapper success(String message) {
        return new ApiResponseWrapper(true, 200, message, null);
    }
    
    /**
     * 创建成功响应 - 201 Created
     */
    public static ApiResponseWrapper created(Object data) {
        return new ApiResponseWrapper(true, 201, "创建成功", data);
    }
    
    public static ApiResponseWrapper created(String message, Object data) {
        return new ApiResponseWrapper(true, 201, message, data);
    }
    
    /**
     * 创建错误响应
     */
    public static ApiResponseWrapper error(int code, String message) {
        return new ApiResponseWrapper(false, code, message, null);
    }
    
    public static ApiResponseWrapper error(int code, String message, Object errorData) {
        return new ApiResponseWrapper(false, code, message, errorData);
    }
    
    /**
     * 创建带验证错误的响应
     */
    public static ApiResponseWrapper validationError(String message, List<ValidationError> errors) {
        ErrorData errorData = new ErrorData("VALIDATION_ERROR", errors);
        return new ApiResponseWrapper(false, 400, message, errorData);
    }
    
    /**
     * 从WebException创建错误响应
     */
    public static ApiResponseWrapper fromException(cloud.compan.servlet.web.exception.WebException ex) {
        if (ex.getErrorCode() != null) {
            ErrorData errorData = new ErrorData(ex.getErrorCode(), null);
            return new ApiResponseWrapper(false, ex.getStatusCode(), ex.getMessage(), errorData);
        } else {
            return new ApiResponseWrapper(false, ex.getStatusCode(), ex.getMessage(), null);
        }
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
    public String getTimestamp() { return timestamp; }
    
    /**
     * 错误数据结构
     */
    public static class ErrorData {
        private final String errorCode;
        private final List<ValidationError> errors;
        
        public ErrorData(String errorCode, List<ValidationError> errors) {
            this.errorCode = errorCode;
            this.errors = errors;
        }
        
        public String getErrorCode() { return errorCode; }
        public List<ValidationError> getErrors() { return errors; }
    }
} 