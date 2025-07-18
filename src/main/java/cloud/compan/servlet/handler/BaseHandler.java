package cloud.compan.servlet.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;

/**
 * 基础处理器类 - 所有Handler的基类
 *
 * 设计思路：
 * 1. 提供通用的HTTP响应处理方法
 * 2. 提供JSON序列化/反序列化工具
 * 3. 提供统一的错误处理机制
 * 4. 定义Handler的基本生命周期方法
 */
public abstract class BaseHandler {

    /**
     * 处理请求的抽象方法 - 子类必须实现
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws Exception 处理过程中的异常
     */
    public abstract void handle(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 获取Handler支持的HTTP方法
     * @return 支持的HTTP方法数组
     */
    public abstract String[] getSupportedMethods();

    /**
     * 获取Handler处理的路径模式
     * @return 路径模式字符串
     */
    public abstract String getPathPattern();

    /**
     * 发送JSON响应
     * @param response HTTP响应对象
     * @param data 要序列化的数据
     * @param statusCode HTTP状态码
     */
    protected void sendJsonResponse(HttpServletResponse response, Object data, int statusCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(statusCode);

        PrintWriter writer = response.getWriter();
        if (data != null) {
            String json = "{\"success\":true,\"message\":\"操作成功\",\"data\":" + data + "}";
            writer.write(json);
        }
        writer.flush();
    }

    /**
     * 发送成功响应
     * @param response HTTP响应对象
     * @param data 响应数据
     */
    protected void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        ApiResponse<Object> apiResponse = new ApiResponse<>(true, "操作成功", data);
        sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
    }

    /**
     * 发送错误响应
     * @param response HTTP响应对象
     * @param message 错误消息
     * @param statusCode HTTP状态码
     */
    protected void sendError(HttpServletResponse response, String message, int statusCode) throws IOException {
        ApiResponse<Object> apiResponse = new ApiResponse<>(false, message, null);
        sendJsonResponse(response, apiResponse, statusCode);
    }

    /**
     * 发送400错误响应
     */
    protected void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_BAD_REQUEST);
    }

    /**
     * 发送404错误响应
     */
    protected void sendNotFound(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * 发送500错误响应
     */
    protected void sendInternalServerError(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * 从请求体中读取JSON并转换为指定类型 - 简化版本
     * @param request HTTP请求对象
     * @param clazz 目标类型
     * @return JSON字符串，需要手动解析
     */
    protected String readJsonFromRequest(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * 从路径中提取参数
     * 例如：/api/users/123 -> 提取出 123
     */
    protected String extractPathVariable(String requestPath, String pattern, String variableName) {
        String[] pathParts = requestPath.split("/");
        String[] patternParts = pattern.split("/");

        if (pathParts.length != patternParts.length) {
            return null; // 路径不匹配
        }

        for (int i = 0; i < patternParts.length; i++) {
            if (patternParts[i].equals("{" + variableName + "}")) {
                return pathParts[i];
            }
        }
        return null;
    }

    /**
     * 发送标准成功响应（符合API文档格式）
     */
    protected void sendSuccessResponse(HttpServletResponse response, int code, String message, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);

        String jsonResponse = String.format(
                "{\"success\":true,\"code\":%d,\"message\":\"%s\",\"data\":%s,\"timestamp\":\"%s\"}",
                code, message,
                data != null ? objectToJson(data) : "null",
                java.time.Instant.now().toString()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * 发送标准错误响应（符合API文档格式）
     */
    protected void sendErrorResponse(HttpServletResponse response, int code, String message, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);

        String jsonResponse = String.format(
                "{\"success\":false,\"code\":%d,\"message\":\"%s\",\"data\":%s,\"timestamp\":\"%s\"}",
                code, message,
                data != null ? objectToJson(data) : "null",
                java.time.Instant.now().toString()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * 简单的对象转JSON方法（后续可以替换为Jackson）
     */
    private String objectToJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + obj + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                sb.append(objectToJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        // 简化处理，返回字符串表示
        return "\"" + obj.toString() + "\"";
    }

    /**
     * 统一的API响应格式
     */
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }
}
