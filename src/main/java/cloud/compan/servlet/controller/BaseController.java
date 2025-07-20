package cloud.compan.servlet.controller;

import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.SearchCriteria;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.web.exception.HttpExceptions;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
import cloud.compan.servlet.web.response.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 控制器基类
 * 提供通用的响应处理、分页、验证等功能
 */
public abstract class BaseController {
    
    /**
     * 处理ServiceResult并转换为ApiResponse
     */
    protected ApiResponseWrapper handleServiceResult(ServiceResult<?> serviceResult) {
        if (serviceResult.isSuccess()) {
            return ApiResponseWrapper.success(serviceResult.getMessage(), serviceResult.getData());
        } else {
            // 根据错误码选择合适的HTTP状态码
            int httpStatus = mapErrorCodeToHttpStatus(serviceResult.getErrorCode());
            return ApiResponseWrapper.error(httpStatus, serviceResult.getMessage());
        }
    }
    
    /**
     * 处理创建操作的ServiceResult
     */
    protected ApiResponseWrapper handleCreateResult(ServiceResult<?> serviceResult) {
        if (serviceResult.isSuccess()) {
            return ApiResponseWrapper.created(serviceResult.getMessage(), serviceResult.getData());
        } else {
            int httpStatus = mapErrorCodeToHttpStatus(serviceResult.getErrorCode());
            return ApiResponseWrapper.error(httpStatus, serviceResult.getMessage());
        }
    }
    
    /**
     * 处理分页查询结果
     */
    protected ApiResponseWrapper handlePageResult(ServiceResult<PageResultDTO<?>> serviceResult) {
        if (serviceResult.isSuccess()) {
            PageResultDTO<?> pageResult = serviceResult.getData();
            return ApiResponseWrapper.success("查询成功", pageResult);
        } else {
            int httpStatus = mapErrorCodeToHttpStatus(serviceResult.getErrorCode());
            return ApiResponseWrapper.error(httpStatus, serviceResult.getMessage());
        }
    }
    
    /**
     * 成功响应 - 无数据
     */
    protected ApiResponseWrapper success() {
        return ApiResponseWrapper.success("操作成功");
    }
    
    /**
     * 成功响应 - 带数据
     */
    protected ApiResponseWrapper success(Object data) {
        return ApiResponseWrapper.success(data);
    }
    
    /**
     * 成功响应 - 带消息和数据
     */
    protected ApiResponseWrapper success(String message, Object data) {
        return ApiResponseWrapper.success(message, data);
    }
    
    /**
     * 创建成功响应
     */
    protected ApiResponseWrapper created(Object data) {
        return ApiResponseWrapper.created(data);
    }
    
    /**
     * 创建成功响应 - 带消息
     */
    protected ApiResponseWrapper created(String message, Object data) {
        return ApiResponseWrapper.created(message, data);
    }
    
    /**
     * 错误响应
     */
    protected ApiResponseWrapper error(String message) {
        return ApiResponseWrapper.error(400, message);
    }
    
    /**
     * 错误响应 - 带状态码
     */
    protected ApiResponseWrapper error(int code, String message) {
        return ApiResponseWrapper.error(code, message);
    }
    
    /**
     * 验证错误响应
     */
    protected ApiResponseWrapper validationError(String message, List<ValidationError> errors) {
        return ApiResponseWrapper.validationError(message, errors);
    }
    
    /**
     * 验证单个字段错误
     */
    protected ApiResponseWrapper validationError(String field, String message) {
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError(field, message));
        return validationError("参数验证失败", errors);
    }
    
    /**
     * 解析分页参数
     */
    protected SearchCriteria parseSearchCriteria(HttpServletRequest request) {
        try {
            int page = parseIntParam(request, "page", 1);
            int size = parseIntParam(request, "size", 20);
            String search = request.getParameter("search");
            String sort = request.getParameter("sort");
            String order = request.getParameter("order");
            
            // 验证分页参数
            if (page < 1) {
                throw HttpExceptions.badRequest("页码必须大于0");
            }
            if (size < 1 || size > 100) {
                throw HttpExceptions.badRequest("每页大小必须在1-100之间");
            }
            
            return new SearchCriteria()
                .keyword(search)
                .sortBy(sort)
                .sortDirection(order)
                .page(page)
                .size(size);
            
        } catch (Exception e) {
            throw HttpExceptions.badRequest("分页参数解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析整数参数
     */
    protected int parseIntParam(HttpServletRequest request, String paramName, int defaultValue) {
        String value = request.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw HttpExceptions.badRequest("参数 '" + paramName + "' 必须是有效的整数");
        }
    }
    
    /**
     * 解析长整数参数
     */
    protected long parseLongParam(HttpServletRequest request, String paramName, long defaultValue) {
        String value = request.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw HttpExceptions.badRequest("参数 '" + paramName + "' 必须是有效的长整数");
        }
    }
    
    /**
     * 解析布尔参数
     */
    protected boolean parseBooleanParam(HttpServletRequest request, String paramName, boolean defaultValue) {
        String value = request.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * 验证必需参数
     */
    protected void requireNonNull(Object value, String paramName) {
        if (value == null) {
            throw HttpExceptions.badRequest("参数 '" + paramName + "' 不能为空");
        }
    }
    
    /**
     * 验证字符串参数
     */
    protected void requireNonEmpty(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw HttpExceptions.badRequest("参数 '" + paramName + "' 不能为空");
        }
    }
    
    /**
     * 验证字符串长度
     */
    protected void validateStringLength(String value, String paramName, int minLength, int maxLength) {
        if (value == null) return;
        
        int length = value.length();
        if (length < minLength || length > maxLength) {
            throw HttpExceptions.badRequest(
                String.format("参数 '%s' 长度必须在%d-%d之间，当前长度: %d", 
                    paramName, minLength, maxLength, length));
        }
    }
    
    /**
     * 验证邮箱格式
     */
    protected void validateEmail(String email, String paramName) {
        if (email == null || email.trim().isEmpty()) return;
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw HttpExceptions.badRequest("参数 '" + paramName + "' 不是有效的邮箱格式");
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    protected String getClientIP(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", 
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0];
                }
                return ip.trim();
            }
        }
        return request.getRemoteAddr();
    }
    
    /**
     * 获取User-Agent
     */
    protected String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
    
    /**
     * 将错误码映射为HTTP状态码
     */
    private int mapErrorCodeToHttpStatus(String errorCode) {
        if (errorCode == null) return 500;
        
        switch (errorCode) {
            case "VALIDATION_ERROR":
            case "INVALID_PARAMETER":
                return 400; // Bad Request
                
            case "UNAUTHORIZED":
            case "INVALID_CREDENTIALS":
                return 401; // Unauthorized
                
            case "FORBIDDEN":
            case "ACCESS_DENIED":
                return 403; // Forbidden
                
            case "NOT_FOUND":
            case "USER_NOT_FOUND":
            case "RESOURCE_NOT_FOUND":
                return 404; // Not Found
                
            case "CONFLICT":
            case "DUPLICATE_ENTRY":
            case "USERNAME_EXISTS":
            case "EMAIL_EXISTS":
                return 409; // Conflict
                
            case "QUOTA_EXCEEDED":
            case "STORAGE_FULL":
                return 413; // Payload Too Large
                
            case "UNSUPPORTED_OPERATION":
                return 415; // Unsupported Media Type
                
            case "RATE_LIMIT_EXCEEDED":
                return 429; // Too Many Requests
                
            default:
                return 500; // Internal Server Error
        }
    }
} 