package cloud.compan.servlet.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 基础控制器类，提供通用的响应处理方法, 主要包括api响应格式（成功响应，失败响应），分页响应格式，json的构建发送， 以及参数相关方法
 */
public abstract class BaseController {

    /**
     * 统一的API响应格式
     *
    */
    public static class ApiResponse<T> {
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
            this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT); // 它将当前的时间（带时区）格式化为 ISO 8601 标准的字符串
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 分页响应数据
     */
    public static class PaginationResponse<T> {
        private java.util.List<T> items;
        private PaginationInfo pagination;

        public PaginationResponse(java.util.List<T> items, PaginationInfo pagination) {
            this.items = items;
            this.pagination = pagination;
        }

        public java.util.List<T> getItems() { return items; }
        public void setItems(java.util.List<T> items) { this.items = items; }

        public PaginationInfo getPagination() { return pagination; }
        public void setPagination(PaginationInfo pagination) { this.pagination = pagination; }
    }

    /**
     * 分页信息
     */
    public static class PaginationInfo {
        private int currentPage;
        private int perPage;
        private int totalItems;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrev;
        private Integer nextPage;
        private Integer prevPage;

        public PaginationInfo(int currentPage, int perPage, int totalItems) {
            this.currentPage = currentPage;
            this.perPage = perPage;
            this.totalItems = totalItems;
            this.totalPages = (int) Math.ceil((double) totalItems / perPage);
            this.hasNext = currentPage < totalPages;
            this.hasPrev = currentPage > 1;
            this.nextPage = hasNext ? currentPage + 1 : null;
            this.prevPage = hasPrev ? currentPage - 1 : null;
        }

        // Getters and Setters
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

        public int getPerPage() { return perPage; }
        public void setPerPage(int perPage) { this.perPage = perPage; }

        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

        public boolean isHasPrev() { return hasPrev; }
        public void setHasPrev(boolean hasPrev) { this.hasPrev = hasPrev; }

        public Integer getNextPage() { return nextPage; }
        public void setNextPage(Integer nextPage) { this.nextPage = nextPage; }

        public Integer getPrevPage() { return prevPage; }
        public void setPrevPage(Integer prevPage) { this.prevPage = prevPage; }
    }

    /**
     * 返回JSON响应
     */
    protected void sendJsonResponse(HttpServletResponse response, ApiResponse<?> apiResponse) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(apiResponse.getCode());

        try {
            // 使用简单的JSON序列化
            String jsonResponse = buildJsonResponse(apiResponse);
            response.getWriter().write(jsonResponse);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500错误
            response.getWriter().write("{\"success\":false,\"code\":500,\"message\":\"JSON序列化失败\",\"data\":null}");
        }
    }

    /**
     * 简单的JSON构建方法
     */
    private String buildJsonResponse(ApiResponse<?> apiResponse) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\":").append(apiResponse.isSuccess()).append(",");
        json.append("\"code\":").append(apiResponse.getCode()).append(",");
        json.append("\"message\":\"").append(escapeJson(apiResponse.getMessage())).append("\",");
        json.append("\"data\":").append(apiResponse.getData() != null ? "\"" + apiResponse.getData().toString() + "\"" : "null").append(",");
        json.append("\"timestamp\":\"").append(apiResponse.getTimestamp()).append("\"");
        json.append("}");
        return json.toString();
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * 返回成功响应
     */
    protected void sendSuccessResponse(HttpServletResponse response, Object data, String message) throws IOException {
        ApiResponse<Object> apiResponse = new ApiResponse<>(true, HttpServletResponse.SC_OK, message, data);
        sendJsonResponse(response, apiResponse);
    }

    /**
     * 返回创建成功响应
     */
    protected void sendCreatedResponse(HttpServletResponse response, Object data, String message) throws IOException {
        ApiResponse<Object> apiResponse = new ApiResponse<>(true, HttpServletResponse.SC_CREATED, message, data);
        sendJsonResponse(response, apiResponse);
    }

    /**
     * 返回错误响应
     */
    protected void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        sendErrorResponse(response, statusCode, message, null);
    }

    /**
     * 返回错误响应（带数据）
     */
    protected void sendErrorResponse(HttpServletResponse response, int statusCode, String message, Object errorData) throws IOException {
        ApiResponse<Object> apiResponse = new ApiResponse<>(false, statusCode, message, errorData);
        sendJsonResponse(response, apiResponse);
    }

    /**
     * 获取请求参数
     */
    protected String getParameter(HttpServletRequest request, String paramName) {
        return request.getParameter(paramName);
    }

    /**
     * 获取请求参数，如果为空则返回默认值
     */
    protected String getParameter(HttpServletRequest request, String paramName, String defaultValue) {
        String value = request.getParameter(paramName);
        return value != null && !value.trim().isEmpty() ? value : defaultValue;
    }

    /**
     * 获取整型参数
     */
    protected Integer getIntParameter(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        if (value != null && !value.trim().isEmpty()) { // 检查是否为null或空字符串
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取整型参数，带默认值
     */
    protected int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        Integer value = getIntParameter(request, paramName);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取长整型参数
     */
    protected Long getLongParameter(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        if (value != null && !value.trim().isEmpty()) { // 检查是否为null或空字符串
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 验证必需参数
     */
    protected boolean isParameterMissing(String... params) { // 接受可变参数， 如果参数为null或空字符串，则返回true，否则返回false
        for (String param : params) {
            if (param == null || param.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
