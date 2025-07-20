package cloud.compan.servlet.converter;

import cloud.compan.servlet.utils.JsonUtils;
import cloud.compan.servlet.web.exception.HttpExceptions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JSON HTTP消息转换器
 * 使用JsonUtils进行JSON序列化和反序列化
 */
@Singleton
public class JsonHttpMessageConverter implements HttpMessageConverter<Object> {
    
    private final JsonUtils jsonUtils;
    
    // 支持的Content-Type
    private static final String[] SUPPORTED_READ_TYPES = {
        "application/json",
        "application/*+json",
        "text/json"
    };
    
    private static final String[] SUPPORTED_WRITE_TYPES = {
        "application/json",
        "application/*+json",
        "text/json",
        "*/*"
    };
    
    @Inject
    public JsonHttpMessageConverter(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }
    
    @Override
    public boolean canRead(Class<?> clazz, String mediaType) {
        if (mediaType == null) return false;
        
        String normalizedType = normalizeMediaType(mediaType);
        for (String supportedType : SUPPORTED_READ_TYPES) {
            if (isMediaTypeMatch(normalizedType, supportedType)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean canWrite(Class<?> clazz, String mediaType) {
        if (mediaType == null) return true; // 默认支持JSON输出
        
        String normalizedType = normalizeMediaType(mediaType);
        for (String supportedType : SUPPORTED_WRITE_TYPES) {
            if (isMediaTypeMatch(normalizedType, supportedType)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String[] getSupportedMediaTypes() {
        return SUPPORTED_READ_TYPES.clone();
    }
    
    @Override
    public Object read(Class<?> clazz, HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        if (!canRead(clazz, contentType)) {
            throw HttpExceptions.badRequest("不支持的Content-Type: " + contentType);
        }
        
        try {
            // 读取请求体
            String requestBody = readRequestBody(request);
            
            if (requestBody == null || requestBody.trim().isEmpty()) {
                if (clazz == String.class) {
                    return "";
                }
                throw HttpExceptions.badRequest("请求体为空");
            }
            
            // JSON反序列化
            if (clazz == String.class) {
                return requestBody;
            } else if (clazz == java.util.Map.class || clazz == java.util.HashMap.class) {
                // 使用JsonUtils的专门方法解析为Map
                return jsonUtils.fromJsonMap(requestBody);
            } else if (clazz == java.util.List.class || clazz == java.util.ArrayList.class) {
                // 对于List类型，解析为Object的List（如果需要特定类型的List，控制器应该明确指定）
                return jsonUtils.fromJsonList(requestBody, Object.class);
            } else {
                return jsonUtils.fromJson(requestBody, clazz);
            }
            
        } catch (Exception e) {
            throw HttpExceptions.requestBodyParseError("JSON解析失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void write(Object object, String mediaType, HttpServletResponse response) throws IOException {
        // 设置响应头
        setupResponseHeaders(response, mediaType);
        
        if (object == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        
        try {
            String jsonResult = convertToJson(object);
            response.getWriter().write(jsonResult);
            
        } catch (Exception e) {
            System.err.println("JSON序列化失败: " + e.getMessage());
            e.printStackTrace();
            
            // 使用fallback策略
            String errorJson = createErrorJson("JSON序列化失败", e.getMessage());
            response.getWriter().write(errorJson);
        }
    }
    
    /**
     * 读取请求体内容
     * 使用JsonUtils.readJsonFromReader进行统一的读取操作
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = StandardCharsets.UTF_8.name();
        }
        
        try (BufferedReader reader = request.getReader()) {
            return jsonUtils.readJsonFromReader(reader);
        } catch (RuntimeException e) {
            // 将JsonUtils的RuntimeException转换为IOException以符合方法签名
            throw new IOException("读取请求体失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将对象转换为JSON字符串
     */
    private String convertToJson(Object object) throws Exception {
        if (object instanceof String) {
            String stringValue = (String) object;
            // 检查是否已经是JSON格式
            if (isJsonString(stringValue)) {
                return stringValue;
            } else {
                // 普通字符串，需要JSON化
                return jsonUtils.toJson(stringValue);
            }
        } else if (object instanceof Number || object instanceof Boolean) {
            // 基本类型，直接转换
            return object.toString();
        } else {
            // 复杂对象，使用JsonUtils序列化
            return jsonUtils.toJson(object);
        }
    }
    
    /**
     * 设置响应头
     */
    private void setupResponseHeaders(HttpServletResponse response, String mediaType) {
        if (mediaType != null && !mediaType.isEmpty()) {
            response.setContentType(mediaType + ";charset=UTF-8");
        } else {
            response.setContentType("application/json;charset=UTF-8");
        }
        response.setCharacterEncoding("UTF-8");
        
        // 添加缓存控制头
        response.setHeader("Cache-Control", "no-cache");
    }
    
    /**
     * 判断字符串是否为JSON格式
     * 使用JsonUtils.isValidJson进行更准确的验证
     */
    private boolean isJsonString(String str) {
        return jsonUtils.isValidJson(str);
    }
    
    /**
     * 标准化媒体类型
     */
    private String normalizeMediaType(String mediaType) {
        if (mediaType == null) return null;
        
        // 移除参数部分 (如 charset=UTF-8)
        int semicolonIndex = mediaType.indexOf(';');
        String type = semicolonIndex > 0 ? mediaType.substring(0, semicolonIndex) : mediaType;
        
        return type.trim().toLowerCase();
    }
    
    /**
     * 检查媒体类型是否匹配
     */
    private boolean isMediaTypeMatch(String actualType, String supportedType) {
        if (actualType == null || supportedType == null) {
            return false;
        }
        
        // 精确匹配
        if (actualType.equals(supportedType)) {
            return true;
        }
        
        // 通配符匹配
        if (supportedType.contains("*")) {
            String pattern = supportedType.replace("*", ".*");
            return actualType.matches(pattern);
        }
        
        return false;
    }
    
    /**
     * 创建错误JSON响应
     */
    private String createErrorJson(String error, String message) {
        try {
            // 使用JsonUtils创建标准错误响应
            ErrorResponse errorResponse = new ErrorResponse(
                false,
                500,
                error,
                message,
                System.currentTimeMillis()
            );
            return jsonUtils.toJson(errorResponse);
        } catch (Exception e) {
            // 如果JsonUtils也失败了，使用手动构建的JSON
            return String.format(
                "{\"success\":false,\"code\":500,\"message\":\"%s\",\"error\":\"%s\",\"timestamp\":%d}",
                escapeJson(error),
                escapeJson(message != null ? message : ""),
                System.currentTimeMillis()
            );
        }
    }
    
    /**
     * 转义JSON字符串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 错误响应数据结构
     */
    private static class ErrorResponse {
        private final boolean success;
        private final int code;
        private final String message;
        private final String error;
        private final long timestamp;
        
        public ErrorResponse(boolean success, int code, String message, String error, long timestamp) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.error = error;
            this.timestamp = timestamp;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public int getCode() { return code; }
        public String getMessage() { return message; }
        public String getError() { return error; }
        public long getTimestamp() { return timestamp; }
    }
} 