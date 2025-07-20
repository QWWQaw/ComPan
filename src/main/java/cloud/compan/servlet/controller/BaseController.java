package cloud.compan.servlet.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * 基础控制器接口
 * 定义控制器层的通用方法和响应格式规范
 * 响应格式遵循API文档v1.2标准
 */
public interface BaseController {
    
    // ============ 标准响应格式 ============
    
    /**
     * 成功响应
     * @param data 响应数据
     * @return 标准响应格式
     */
    default ApiResponse<Object> success(Object data) {
        return new ApiResponse<>(true, 200, "操作成功", data);
    }
    
    /**
     * 成功响应（无数据）
     * @param message 成功消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> success(String message) {
        return new ApiResponse<>(true, 200, message, null);
    }
    
    /**
     * 创建成功响应
     * @param data 创建的数据
     * @return 标准响应格式
     */
    default ApiResponse<Object> created(Object data) {
        return new ApiResponse<>(true, 201, "创建成功", data);
    }
    
    /**
     * 创建成功响应
     * @param message 创建成功消息
     * @param data 创建的数据
     * @return 标准响应格式
     */
    default ApiResponse<Object> created(String message, Object data) {
        return new ApiResponse<>(true, 201, message, data);
    }
    
    /**
     * 失败响应
     * @param code 错误码
     * @param message 错误消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> error(int code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
    
    /**
     * 失败响应（带错误详情）
     * @param code 错误码
     * @param message 错误消息
     * @param errorData 错误详情数据
     * @return 标准响应格式
     */
    default ApiResponse<Object> error(int code, String message, Object errorData) {
        return new ApiResponse<>(false, code, message, errorData);
    }
    
    /**
     * 参数验证失败 - 400
     * @param message 验证失败消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> badRequest(String message) {
        return error(400, message);
    }
    
    /**
     * 参数验证失败 - 400（带验证错误详情）
     * @param message 验证失败消息
     * @param validationErrors 验证错误列表
     * @return 标准响应格式
     */
    default ApiResponse<Object> badRequest(String message, List<ValidationError> validationErrors) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("errors", validationErrors);
        return error(400, message, errorData);
    }
    
    /**
     * 未认证 - 401
     * @param message 错误消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> unauthorized(String message) {
        return error(401, message != null ? message : "未认证");
    }
    
    /**
     * 权限不足 - 403
     * @param message 错误消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> forbidden(String message) {
        return error(403, message != null ? message : "权限不足");
    }
    
    /**
     * 资源不存在 - 404
     * @param message 错误消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> notFound(String message) {
        return error(404, message != null ? message : "资源不存在");
    }
    
    /**
     * 资源冲突 - 409
     * @param message 冲突消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> conflict(String message) {
        return error(409, message);
    }
    
    /**
     * 请求实体过大 - 413
     * @param message 错误消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> payloadTooLarge(String message) {
        return error(413, message != null ? message : "请求实体过大");
    }
    
    /**
     * 参数验证失败 - 422
     * @param message 验证失败消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> unprocessableEntity(String message) {
        return error(422, message != null ? message : "参数验证失败");
    }
    
    /**
     * 请求过于频繁 - 429
     * @param message 限流消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> tooManyRequests(String message) {
        return error(429, message != null ? message : "请求过于频繁");
    }
    
    /**
     * 服务器内部错误 - 500
     * @param message 错误消息
     * @return 标准响应格式
     */
    default ApiResponse<Object> internalError(String message) {
        return error(500, message != null ? message : "服务器内部错误");
    }
    
    // ============ 分页响应 ============
    
    /**
     * 分页响应
     * @param items 数据列表
     * @param currentPage 当前页码
     * @param perPage 每页大小
     * @param totalItems 总记录数
     * @return 分页响应格式
     */
    default ApiResponse<Object> pageResponse(Object items, int currentPage, int perPage, long totalItems) {
        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("pagination", new PaginationInfo(currentPage, perPage, totalItems));
        return success(data);
    }
    
    /**
     * 分页响应（带消息）
     * @param message 成功消息
     * @param items 数据列表
     * @param currentPage 当前页码
     * @param perPage 每页大小
     * @param totalItems 总记录数
     * @return 分页响应格式
     */
    default ApiResponse<Object> pageResponse(String message, Object items, int currentPage, int perPage, long totalItems) {
        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("pagination", new PaginationInfo(currentPage, perPage, totalItems));
        return new ApiResponse<>(true, 200, message, data);
    }
    
    // ============ 工具方法 ============
    
    /**
     * 获取当前用户ID（从请求中）
     * @param request HTTP请求
     * @return 用户ID，如果未登录返回null
     */
    default Long getCurrentUserId(HttpServletRequest request) {
        // 优先从JWT token中获取（后续实现）
        // TODO: 从JWT token或session中获取用户ID
        
        // 临时从header中获取
        String userIdHeader = request.getHeader("X-User-ID");
        if (userIdHeader != null) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 获取当前用户名（从请求中）
     * @param request HTTP请求
     * @return 用户名，如果未登录返回null
     */
    default String getCurrentUsername(HttpServletRequest request) {
        // TODO: 从JWT token中获取用户名
        return request.getHeader("X-Username");
    }
    
    /**
     * 检查用户是否已登录
     * @param request HTTP请求
     * @return 是否已登录
     */
    default boolean isAuthenticated(HttpServletRequest request) {
        return getCurrentUserId(request) != null;
    }
    
    /**
     * 检查必需参数
     * @param params 参数map
     * @param requiredFields 必需字段
     * @throws IllegalArgumentException 如果参数缺失
     */
    default void validateRequiredFields(Map<String, Object> params, String... requiredFields) {
        for (String field : requiredFields) {
            if (params == null || !params.containsKey(field) || params.get(field) == null) {
                throw new IllegalArgumentException("缺少必需参数: " + field);
            }
        }
    }
    
    /**
     * 检查分页参数
     * @param page 页码
     * @param perPage 每页大小
     * @return 标准化的分页参数
     */
    default PageParams validatePageParams(Integer page, Integer perPage) {
        int validPage = (page == null || page < 1) ? 1 : page;
        int validPerPage = (perPage == null || perPage < 1) ? 20 : Math.min(perPage, 100); // 最大100条
        return new PageParams(validPage, validPerPage);
    }
    
    /**
     * 设置HTTP响应状态码
     * @param response HTTP响应
     * @param statusCode 状态码
     */
    default void setResponseStatus(HttpServletResponse response, int statusCode) {
        response.setStatus(statusCode);
    }
    
    // ============ 响应数据类 ============
    
    /**
     * 标准API响应格式（符合API文档v1.2）
     */
    class ApiResponse<T> {
        private boolean success;
        private int code;
        private String message;
        private T data;
        private String timestamp;
        
        public ApiResponse(boolean success, int code, String message, T data) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.data = data;
            this.timestamp = Instant.now().toString();
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public int getCode() { return code; }
        public String getMessage() { return message; }
        public T getData() { return data; }
        public String getTimestamp() { return timestamp; }
    }
    
    /**
     * 分页信息（符合API文档格式）
     */
    class PaginationInfo {
        private int currentPage;
        private int perPage;
        private long totalItems;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrev;
        private Integer nextPage;
        private Integer prevPage;
        
        public PaginationInfo(int currentPage, int perPage, long totalItems) {
            this.currentPage = currentPage;
            this.perPage = perPage;
            this.totalItems = totalItems;
            this.totalPages = (int) Math.ceil((double) totalItems / perPage);
            this.hasNext = currentPage < totalPages;
            this.hasPrev = currentPage > 1;
            this.nextPage = hasNext ? currentPage + 1 : null;
            this.prevPage = hasPrev ? currentPage - 1 : null;
        }
        
        // Getters
        public int getCurrentPage() { return currentPage; }
        public int getPerPage() { return perPage; }
        public long getTotalItems() { return totalItems; }
        public int getTotalPages() { return totalPages; }
        public boolean isHasNext() { return hasNext; }
        public boolean isHasPrev() { return hasPrev; }
        public Integer getNextPage() { return nextPage; }
        public Integer getPrevPage() { return prevPage; }
    }
    
    /**
     * 验证错误信息
     */
    class ValidationError {
        private String field;
        private String message;
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        // Getters
        public String getField() { return field; }
        public String getMessage() { return message; }
    }
    
    /**
     * 分页参数
     */
    class PageParams {
        private int page;
        private int perPage;
        
        public PageParams(int page, int perPage) {
            this.page = page;
            this.perPage = perPage;
        }
        
        // Getters
        public int getPage() { return page; }
        public int getPerPage() { return perPage; }
        
        /**
         * 计算数据库查询的偏移量
         * @return 偏移量
         */
        public int getOffset() {
            return (page - 1) * perPage;
        }
        
        /**
         * 获取查询限制数量
         * @return 限制数量
         */
        public int getLimit() {
            return perPage;
        }
    }
} 