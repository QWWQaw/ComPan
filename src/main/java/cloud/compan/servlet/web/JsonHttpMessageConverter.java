package cloud.compan.servlet.web;

import cloud.compan.servlet.utils.JsonUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * JSON HTTP消息转换器
 * 使用JsonUtils进行JSON序列化和反序列化
 */
@Singleton
public class JsonHttpMessageConverter implements HttpMessageConverter<Object> {
    
    private final JsonUtils jsonUtils;
    
    @Inject
    public JsonHttpMessageConverter(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }
    
    @Override
    public boolean canRead(Class<?> clazz, String mediaType) {
        return mediaType != null && mediaType.contains("application/json");
    }
    
    @Override
    public boolean canWrite(Class<?> clazz, String mediaType) {
        return mediaType == null || mediaType.contains("application/json");
    }
    
    @Override
    public String[] getSupportedMediaTypes() {
        return new String[]{"application/json", "application/*+json"};
    }
    
    @Override
    public Object read(Class<?> clazz, HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        if (!canRead(clazz, contentType)) {
            throw new IllegalArgumentException("不支持的内容类型: " + contentType);
        }
        
        return jsonUtils.fromJson(request.getReader(), clazz);
    }
    
    @Override
    public void write(Object object, String mediaType, HttpServletResponse response) throws IOException {
        if (object == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        
        // 设置响应头
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            if (object instanceof String) {
                String stringValue = (String) object;
                // 检查是否已经是JSON格式
                if (stringValue.startsWith("{") || stringValue.startsWith("[")) {
                    // 已经是JSON，直接写入
                    response.getWriter().write(stringValue);
                } else {
                    // 普通字符串，需要JSON化
                    response.getWriter().write(jsonUtils.toJson(stringValue));
                }
            } else if (object instanceof Number || object instanceof Boolean) {
                // 基本类型，直接转换
                response.getWriter().write(object.toString());
            } else {
                // 复杂对象，使用JsonUtils序列化
                String jsonResult = jsonUtils.toJson(object);
                response.getWriter().write(jsonResult);
                System.out.println("JSON转换成功: " + object.getClass().getSimpleName());
            }
        } catch (Exception e) {
            System.err.println("JSON转换失败: " + e.getMessage());
            // 使用fallback策略
            response.getWriter().write(createErrorJson("JSON转换失败", e.getMessage()));
        }
    }
    
    /**
     * 创建错误JSON
     */
    private String createErrorJson(String error, String message) {
        return String.format("{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}", 
                           error, message != null ? message.replace("\"", "\\\"") : "", 
                           System.currentTimeMillis());
    }
} 