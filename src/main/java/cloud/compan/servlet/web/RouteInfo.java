package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.enums.RequestMethod;
import java.lang.reflect.Method;

/**
 * 路由信息类 - 存储单个路由的完整元数据
 * 线程安全的不可变对象
 */
public class RouteInfo {
    
    private final String path;                // URL路径
    private final RequestMethod httpMethod;   // HTTP方法
    private final Class<?> controllerClass;  // 控制器类
    private final Method handlerMethod;      // 处理方法
    private final Object controllerInstance; // 控制器实例（由Guice管理）
    
    /**
     * 构造路由信息
     * @param path URL路径
     * @param httpMethod HTTP方法
     * @param controllerClass 控制器类
     * @param handlerMethod 处理方法
     * @param controllerInstance 控制器实例
     */
    public RouteInfo(String path, RequestMethod httpMethod, 
                     Class<?> controllerClass, Method handlerMethod, 
                     Object controllerInstance) {
        this.path = normalizePath(path);
        this.httpMethod = httpMethod;
        this.controllerClass = controllerClass;
        this.handlerMethod = handlerMethod;
        this.controllerInstance = controllerInstance;
    }
    
    /**
     * 标准化路径 - 确保路径格式一致
     */
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
    
    /**
     * 生成路由的唯一键
     * 格式: "GET:/api/users" 
     */
    public String getRouteKey() {
        return httpMethod.name() + ":" + path;
    }
    
    /**
     * 检查是否匹配给定的请求
     */
    public boolean matches(String requestPath, RequestMethod requestMethod) {
        return this.httpMethod.equals(requestMethod) && 
               this.path.equals(normalizePath(requestPath));
    }
    
    // Getters
    public String getPath() { return path; }
    public RequestMethod getHttpMethod() { return httpMethod; }
    public Class<?> getControllerClass() { return controllerClass; }
    public Method getHandlerMethod() { return handlerMethod; }
    public Object getControllerInstance() { return controllerInstance; }
    
    @Override
    public String toString() {
        return String.format("RouteInfo{%s %s -> %s.%s}", 
            httpMethod, path, 
            controllerClass.getSimpleName(), 
            handlerMethod.getName());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RouteInfo that = (RouteInfo) obj;
        return getRouteKey().equals(that.getRouteKey());
    }
    
    @Override
    public int hashCode() {
        return getRouteKey().hashCode();
    }
} 