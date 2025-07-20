package cloud.compan.servlet.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * HTTP消息转换器接口
 * 负责将对象转换为HTTP响应格式（如JSON、XML等）
 */
public interface HttpMessageConverter<T> {
    
    /**
     * 判断是否可以读取指定类型
     * @param clazz 类型
     * @param mediaType 媒体类型
     * @return 是否支持读取
     */
    boolean canRead(Class<?> clazz, String mediaType);
    
    /**
     * 判断是否可以写入指定类型
     * @param clazz 类型
     * @param mediaType 媒体类型
     * @return 是否支持写入
     */
    boolean canWrite(Class<?> clazz, String mediaType);
    
    /**
     * 获取支持的媒体类型
     * @return 支持的媒体类型列表
     */
    String[] getSupportedMediaTypes();
    
    /**
     * 从HTTP请求中读取对象
     * @param clazz 目标类型
     * @param request HTTP请求
     * @return 读取的对象
     * @throws IOException IO异常
     */
    T read(Class<? extends T> clazz, HttpServletRequest request) throws IOException;
    
    /**
     * 将对象写入HTTP响应
     * @param object 要写入的对象
     * @param mediaType 媒体类型如application/json
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void write(T object, String mediaType, HttpServletResponse response) throws IOException;
} 