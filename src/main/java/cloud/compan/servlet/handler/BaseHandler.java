package cloud.compan.servlet.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

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
            if (data instanceof Map) {
                writer.write(mapToJson((Map<?, ?>) data));
            } else {
                writer.write(objectToJson(data));
            }
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
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("status_code", statusCode);
        errorResponse.put("timestamp", java.time.Instant.now().toString());

        sendJsonResponse(response, errorResponse, statusCode);
    }

    /**
     * 发送错误响应 - 带额外信息
     * @param response HTTP响应对象
     * @param statusCode HTTP状态码
     * @param message 错误消息
     * @param extraInfo 额外信息
     */
    protected void sendErrorResponse(HttpServletResponse response, int statusCode, String message, Map<String, String> extraInfo) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("status_code", statusCode);
        errorResponse.put("timestamp", java.time.Instant.now().toString());

        if (extraInfo != null && !extraInfo.isEmpty()) {
            errorResponse.put("details", extraInfo);
        }

        sendJsonResponse(response, errorResponse, statusCode);
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
     * 从请求体中获取参数 - 核心方法
     */
    protected String getParameterFromRequestBody(HttpServletRequest request, String paramName) throws IOException {
        String requestBody = readJsonFromRequest(request);
        return parseJsonParameter(requestBody, paramName);
    }

    /**
     * 从请求体中读取JSON字符串
     * @param request HTTP请求对象
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
     * 简单的JSON参数解析 - 解析形如 {"key":"value"} 的JSON
     */
    private String parseJsonParameter(String json, String paramName) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        // 简单的JSON解析，查找 "paramName":"value" 模式
        String searchPattern = "\"" + paramName + "\"";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex == -1) {
            return null;
        }

        // 找到冒号
        int colonIndex = json.indexOf(":", startIndex);
        if (colonIndex == -1) {
            return null;
        }

        // 跳过空格，找到值的开始
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length()) {
            return null;
        }

        // 判断值的类型（字符串、数字、布尔值等）
        char firstChar = json.charAt(valueStart);
        if (firstChar == '"') {
            // 字符串值
            int valueEnd = json.indexOf('"', valueStart + 1);
            if (valueEnd == -1) {
                return null;
            }
            return json.substring(valueStart + 1, valueEnd);
        } else {
            // 数字、布尔值或null
            int valueEnd = valueStart;
            while (valueEnd < json.length()) {
                char c = json.charAt(valueEnd);
                if (c == ',' || c == '}' || Character.isWhitespace(c)) {
                    break;
                }
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).trim();
        }
    }

    /**
     * 将Map转换为JSON字符串
     */
    private String mapToJson(Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(objectToJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 将对象转换为JSON字符串
     */
    private String objectToJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map) {
            return mapToJson((Map<?, ?>) obj);
        }
        if (obj instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(objectToJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        // 其他类型转为字符串
        return "\"" + escapeJson(obj.toString()) + "\"";
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 从URL路径中提取ID参数
     */
    protected Long extractIdFromPath(String requestURI, String pattern) {
        try {
            String[] uriParts = requestURI.split("/");
            String[] patternParts = pattern.split("/");

            for (int i = 0; i < Math.min(uriParts.length, patternParts.length); i++) {
                if (patternParts[i].contains("{") && patternParts[i].contains("}")) {
                    return Long.parseLong(uriParts[i]);
                }
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
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
