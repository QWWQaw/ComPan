package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.enums.RequestMethod;
import com.google.inject.Singleton;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.*;

/**
 * 路由注册器 - 线程安全的路由管理中心
 * 负责存储、查找和管理所有应用程序的路由信息
 */
@Singleton
public class RouteRegistry {
    
    /**
     * 线程安全的路由存储
     * Key: "GET:/api/users" (HTTP方法:路径)
     * Value: RouteInfo对象
     */
    private final ConcurrentMap<String, RouteInfo> routes = new ConcurrentHashMap<>();
    
    /**
     * 参数化路由存储 - 用于模式匹配
     * 按HTTP方法分组存储，避免每次都遍历所有路由
     */
    private final Map<RequestMethod, List<ParameterizedRouteInfo>> parameterizedRoutes = new EnumMap<>(RequestMethod.class);
    
    /**
     * 注册单个路由
     * @param routeInfo 路由信息
     * @throws IllegalArgumentException 如果路由已存在
     */
    public void registerRoute(RouteInfo routeInfo) {
        String routeKey = routeInfo.getRouteKey();
        
        // 检查路由是否已存在
        RouteInfo existingRoute = routes.putIfAbsent(routeKey, routeInfo);
        if (existingRoute != null) {
            throw new IllegalArgumentException(
                String.format("路由冲突: %s 已经被 %s.%s 注册，无法再注册到 %s.%s",
                    routeKey,
                    existingRoute.getControllerClass().getSimpleName(),
                    existingRoute.getHandlerMethod().getName(),
                    routeInfo.getControllerClass().getSimpleName(),
                    routeInfo.getHandlerMethod().getName())
            );
        }
        
        // 如果是参数化路由，也添加到参数化路由集合中
        if (routeInfo instanceof ParameterizedRouteInfo) {
            ParameterizedRouteInfo paramRoute = (ParameterizedRouteInfo) routeInfo;
            if (paramRoute.hasPathVariables()) {
                RequestMethod method = routeInfo.getHttpMethod();
                parameterizedRoutes.computeIfAbsent(method, k -> new ArrayList<>()).add(paramRoute);
            }
        }
        
        System.out.println("✅ 路由注册成功: " + routeInfo);
    }
    
    /**
     * 批量注册路由
     * @param routeInfos 路由信息集合
     */
    public void registerRoutes(Collection<RouteInfo> routeInfos) {
        for (RouteInfo routeInfo : routeInfos) {
            registerRoute(routeInfo);
        }
    }
    
    /**
     * 根据请求路径和HTTP方法查找路由
     * @param requestPath 请求路径
     * @param requestMethod 请求方法
     * @return 匹配的路由信息，如果没找到返回Optional.empty()
     */
    public Optional<RouteInfo> findRoute(String requestPath, RequestMethod requestMethod) {
        String normalizedPath = normalizePath(requestPath);
        String routeKey = requestMethod.name() + ":" + normalizedPath;
        
        // 1. 先尝试精确匹配
        RouteInfo exactMatch = routes.get(routeKey);
        if (exactMatch != null) {
            return Optional.of(exactMatch);
        }
        
        // 2. 尝试参数化路由匹配
        List<ParameterizedRouteInfo> methodRoutes = parameterizedRoutes.get(requestMethod);
        if (methodRoutes != null) {
            for (ParameterizedRouteInfo paramRoute : methodRoutes) {
                if (paramRoute.matches(normalizedPath, requestMethod)) {
                    return Optional.of(paramRoute);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * 根据HTTP方法字符串查找路由
     * @param requestPath 请求路径
     * @param httpMethodStr HTTP方法字符串（GET、POST等）
     * @return 匹配的路由信息
     */
    public Optional<RouteInfo> findRoute(String requestPath, String httpMethodStr) {
        try {
            RequestMethod requestMethod = RequestMethod.valueOf(httpMethodStr.toUpperCase());
            return findRoute(requestPath, requestMethod);
        } catch (IllegalArgumentException e) {
            System.err.println("不支持的HTTP方法: " + httpMethodStr);
            return Optional.empty();
        }
    }
    
    /**
     * 获取所有已注册的路由
     * @return 只读的路由集合
     */
    public Collection<RouteInfo> getAllRoutes() {
        return routes.values();
    }
    
    /**
     * 获取已注册路由的数量
     */
    public int getRouteCount() {
        return routes.size();
    }
    
    /**
     * 检查是否包含指定路由
     */
    public boolean hasRoute(String requestPath, RequestMethod requestMethod) {
        return findRoute(requestPath, requestMethod).isPresent();
    }
    
    /**
     * 移除指定路由（主要用于测试）
     */
    public boolean removeRoute(String requestPath, RequestMethod requestMethod) {
        String routeKey = requestMethod.name() + ":" + normalizePath(requestPath);
        return routes.remove(routeKey) != null;
    }
    
    /**
     * 清空所有路由（主要用于测试）
     */
    public void clearRoutes() {
        routes.clear();
        System.out.println("🗑️ 所有路由已清空");
    }
    
    /**
     * 打印所有注册的路由（用于调试）
     */
    public void printAllRoutes() {
        System.out.println("\n📋 已注册的路由列表 (" + routes.size() + " 个):");
        routes.values().forEach(route -> 
            System.out.println("  " + route));
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
} 