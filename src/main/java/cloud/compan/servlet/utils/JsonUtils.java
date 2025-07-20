package cloud.compan.servlet.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类 - 基于Jackson封装
 * 提供统一的JSON序列化和反序列化操作
 * 
 * @author ComPan
 * @version 1.0
 */
@Singleton
public class JsonUtils {
    
    private final ObjectMapper objectMapper;
    
    @Inject
    public JsonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // 注册Java 8时间模块
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * 将对象序列化为JSON字符串
     * 
     * @param object 要序列化的对象
     * @return JSON字符串
     * @throws RuntimeException 序列化失败时抛出
     */
    public String toJson(Object object) {
        if (object == null) {
            return "null";
        }
        
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将JSON字符串反序列化为指定类型的对象
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * @throws RuntimeException 反序列化失败时抛出
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从Reader读取JSON并反序列化为指定类型的对象
     * 
     * @param reader JSON数据源
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * @throws RuntimeException 反序列化失败时抛出
     */
    public <T> T fromJson(Reader reader, Class<T> clazz) {
        try {
            return objectMapper.readValue(reader, clazz);
        } catch (IOException e) {
            throw new RuntimeException("从Reader读取JSON失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将JSON字符串反序列化为List
     * 
     * @param json JSON字符串
     * @param elementClass List元素类型
     * @param <T> 泛型类型
     * @return List对象
     * @throws RuntimeException 反序列化失败时抛出
     */
    public <T> List<T> fromJsonList(String json, Class<T> elementClass) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            return objectMapper.readValue(json, 
                typeFactory.constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON List反序列化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将JSON字符串反序列化为Map
     * 
     * @param json JSON字符串
     * @return Map对象
     * @throws RuntimeException 反序列化失败时抛出
     */
    public Map<String, Object> fromJsonMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            return objectMapper.readValue(json, 
                typeFactory.constructMapType(Map.class, String.class, Object.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON Map反序列化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从Reader读取JSON字符串
     * 
     * @param reader JSON数据源
     * @return JSON字符串
     * @throws RuntimeException 读取失败时抛出
     */
    public String readJsonFromReader(Reader reader) {
        try {
            StringBuilder json = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                json.append(buffer, 0, bytesRead);
            }
            return json.toString();
        } catch (IOException e) {
            throw new RuntimeException("从Reader读取JSON字符串失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查字符串是否为有效的JSON格式
     * 
     * @param json 待检查的字符串
     * @return 是否为有效JSON
     */
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * 格式化JSON字符串（美化输出）
     * 
     * @param json 原始JSON字符串
     * @return 格式化后的JSON字符串
     * @throws RuntimeException 格式化失败时抛出
     */
    public String prettyJson(String json) {
        try {
            Object jsonObject = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON格式化失败: " + e.getMessage(), e);
        }
    }
}
