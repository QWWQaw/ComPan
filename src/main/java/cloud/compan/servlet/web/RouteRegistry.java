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
     * Register a single route
     */
    public void registerRoute(RouteInfo routeInfo) {
        if (routeInfo == null) {
            throw new IllegalArgumentException("RouteInfo cannot be null");
        }
        
        String key = routeInfo.getRouteKey();
        
        synchronized (routes) {
            if (routes.containsKey(key)) {
                System.out.println("Route already exists, overwriting: " + routeInfo.getHttpMethod()+ " " + routeInfo.getPath() + " -> " +
                        routeInfo.getControllerClass().getSimpleName() + "." + routeInfo.getHandlerMethod().getName());
            }
            
            routes.put(key, routeInfo);
            System.out.println("Route registered successfully: " + routeInfo);
            
            // If it's a parameterized route, also add to parameterized routes collection
            if (routeInfo instanceof ParameterizedRouteInfo) {
                ParameterizedRouteInfo paramRoute = (ParameterizedRouteInfo) routeInfo;
                System.out.println("DEBUG: Found parameterized route: " + paramRoute.getPathPattern());
                if (paramRoute.hasPathVariables()) {
                    RequestMethod method = routeInfo.getHttpMethod();
                    parameterizedRoutes.computeIfAbsent(method, k -> new ArrayList<>()).add(paramRoute);
                    System.out.println("DEBUG: Added to parameterized routes for method " + method + 
                                     " (total: " + parameterizedRoutes.get(method).size() + ")");
                } else {
                    System.out.println("DEBUG: Parameterized route has no path variables: " + paramRoute.getPathPattern());
                }
            } else {
                System.out.println("DEBUG: Regular route (not parameterized): " + routeInfo.getPath());
            }
        }
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
        
        System.out.println("DEBUG: Looking for route: " + routeKey);
        
        // 1. 先尝试精确匹配
        RouteInfo exactMatch = routes.get(routeKey);
        if (exactMatch != null) {
            System.out.println("DEBUG: Found exact match: " + exactMatch);
            return Optional.of(exactMatch);
        }
        
        System.out.println("DEBUG: No exact match found, trying parameterized routes...");
        
        // 2. 尝试参数化路由匹配
        List<ParameterizedRouteInfo> methodRoutes = parameterizedRoutes.get(requestMethod);
        if (methodRoutes != null) {
            System.out.println("DEBUG: Found " + methodRoutes.size() + " parameterized routes for method " + requestMethod);
            for (ParameterizedRouteInfo paramRoute : methodRoutes) {
                System.out.println("DEBUG: Testing parameterized route: " + paramRoute.getPathPattern());
                if (paramRoute.matches(normalizedPath, requestMethod)) {
                    System.out.println("DEBUG: Found parameterized match: " + paramRoute);
                    return Optional.of(paramRoute);
                }
            }
        } else {
            System.out.println("DEBUG: No parameterized routes found for method " + requestMethod);
        }
        
        System.out.println("DEBUG: No route found for: " + routeKey);
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
        System.out.println( "所有路由已清空");
    }
    
    /**
     * 打印所有注册的路由（用于调试）
     */
    public void printAllRoutes() {
        System.out.println("\n=== REGISTERED ROUTES (" + routes.size() + " regular routes) ===");
        routes.values().forEach(route -> 
            System.out.println("  " + route));
        
        System.out.println("\n=== PARAMETERIZED ROUTES ===");
        parameterizedRoutes.forEach((method, routes) -> {
            System.out.println("  " + method + " (" + routes.size() + " routes):");
            routes.forEach(route -> System.out.println("    " + route));
        });
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