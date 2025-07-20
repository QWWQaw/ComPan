package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.annotations.enums.RequestMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 映射注解包装器
 * 将不同的HTTP映射注解统一包装为RouteMapping接口
 */
public class MappingWrapper implements RouteMapping {
    
    private final String path;
    private final RequestMethod httpMethod;
    private final String[] params;
    private final String[] headers;
    private final String[] consumes;
    private final String[] produces;
    
    private MappingWrapper(String path, RequestMethod httpMethod, String[] params, 
                          String[] headers, String[] consumes, String[] produces) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }
    
    /**
     * 从方法上的注解创建映射包装器
     */
    public static MappingWrapper fromMethod(Method method) {
        // 检查各种映射注解
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            return new MappingWrapper(
                mapping.path(), RequestMethod.GET, mapping.params(),
                mapping.headers(), mapping.consumes(), mapping.produces()
            );
        }
        
        if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            return new MappingWrapper(
                mapping.path(), RequestMethod.POST, mapping.params(),
                mapping.headers(), mapping.consumes(), mapping.produces()
            );
        }
        
        if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            return new MappingWrapper(
                mapping.path(), RequestMethod.PUT, mapping.params(),
                mapping.headers(), mapping.consumes(), mapping.produces()
            );
        }
        
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            return new MappingWrapper(
                mapping.path(), RequestMethod.DELETE, mapping.params(),
                mapping.headers(), mapping.consumes(), mapping.produces()
            );
        }
        
        if (method.isAnnotationPresent(PatchMapping.class)) {
            PatchMapping mapping = method.getAnnotation(PatchMapping.class);
            return new MappingWrapper(
                mapping.path(), RequestMethod.PATCH, mapping.params(),
                mapping.headers(), mapping.consumes(), mapping.produces()
            );
        }
        
        return null; // 没有找到映射注解
    }
    
    /**
     * 检查方法是否有HTTP映射注解
     */
    public static boolean hasHttpMapping(Method method) {
        return method.isAnnotationPresent(GetMapping.class) ||
               method.isAnnotationPresent(PostMapping.class) ||
               method.isAnnotationPresent(PutMapping.class) ||
               method.isAnnotationPresent(DeleteMapping.class) ||
               method.isAnnotationPresent(PatchMapping.class) ||
               method.isAnnotationPresent(RequestMapping.class);
    }
    
    /**
     * 从RequestMapping创建多个映射包装器
     */
    public static MappingWrapper[] fromRequestMapping(RequestMapping mapping) {
        RequestMethod[] methods = mapping.method();
        
        // 如果没有指定HTTP方法，默认支持GET
        if (methods.length == 0) {
            methods = new RequestMethod[]{RequestMethod.GET};
        }
        
        MappingWrapper[] wrappers = new MappingWrapper[methods.length];
        for (int i = 0; i < methods.length; i++) {
            wrappers[i] = new MappingWrapper(
                mapping.path(), methods[i], mapping.params(),
                mapping.headers(), mapping.consumes(), mapping.produces()
            );
        }
        
        return wrappers;
    }
    
    @Override
    public String getPath() {
        return path;
    }
    
    @Override
    public RequestMethod getHttpMethod() {
        return httpMethod;
    }
    
    @Override
    public String[] getParams() {
        return params;
    }
    
    @Override
    public String[] getHeaders() {
        return headers;
    }
    
    @Override
    public String[] getConsumes() {
        return consumes;
    }
    
    @Override
    public String[] getProduces() {
        return produces;
    }
    
    @Override
    public String toString() {
        return String.format("MappingWrapper{%s %s}", httpMethod, path);
    }
} 