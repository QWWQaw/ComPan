package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.component.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

/**
 * 请求分发器 - 负责将HTTP请求分发到对应的Controller方法
 *
 * 工作流程：
 * 1. 启动时扫描所有Controller类，建立路由映射
 * 2. 接收请求时，根据URL和HTTP方法匹配对应的处理方法
 * 3. 解析路径参数和方法参数，调用Controller方法
 */
public class Dispatcher {

    // ==================== 核心数据结构 ====================

    /**
     * 路由映射表：简单的字符串键 -> RouteInfo
     * Key: "HTTP方法:路径"，例如 "POST:/v1/auth/login"
     */
    private final Map<String, RouteInfo> routeMap = new HashMap<>();

    /**
     * 带参数的路由映射表：包含路径变量的路由
     * Key: "HTTP方法:路径模板"，例如 "GET:/v1/files/{id}"
     */
    private final Map<String, RouteInfo> paramRouteMap = new HashMap<>();

    /**
     * 控制器实例缓存：类名 -> 实例对象
     */
    private final Map<String, Object> controllerInstanceMap = new HashMap<>();

    // ==================== 构造函数 ====================

    public Dispatcher() {
        System.out.println("=== Dispatcher 初始化开始 ===");
        scanAndRegisterControllers("cloud.compan.servlet");
        System.out.println("=== Dispatcher 初始化完成，共注册 " + (routeMap.size() + paramRouteMap.size()) + " 个路由 ===");
        printRegisteredRoutes();
    }

    // ==================== 主要分发方法 ====================

    /**
     * 分发HTTP请求到对应的Controller方法
     */
    public void dispatch(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. 解析请求路径
            RequestInfo requestInfo = parseRequest(request);
            System.out.println("处理请求: " + requestInfo);

            // 2. 查找匹配的路由
            RouteMatchResult matchResult = findMatchingRoute(requestInfo);

            if (matchResult == null) {
                // 没有找到匹配的路由
                handleNotFound(response, requestInfo);
                return;
            }

            // 3. 调用Controller方法
            invokeControllerMethod(matchResult, request, response);

        } catch (Exception e) {
            handleError(response, e);
        }
    }

    // ==================== 请求解析 ====================

    /**
     * 解析HTTP请求信息
     */
    private RequestInfo parseRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestURI.substring(contextPath.length());
        String method = request.getMethod().toUpperCase();

        return new RequestInfo(method, path, requestURI, contextPath);
    }

    /**
     * 查找匹配的路由 - 简化版本
     */
    private RouteMatchResult findMatchingRoute(RequestInfo requestInfo) {
        String routeKey = requestInfo.method + ":" + requestInfo.path;

        // 1. 首先尝试精确匹配（无参数路由）
        RouteInfo exactMatch = routeMap.get(routeKey);
        if (exactMatch != null) {
            return new RouteMatchResult(exactMatch, null, new HashMap<>());
        }

        // 2. 尝试参数化路由匹配
        for (Map.Entry<String, RouteInfo> entry : paramRouteMap.entrySet()) {
            String routeTemplate = entry.getKey();
            RouteInfo routeInfo = entry.getValue();

            // 解析路由模板，例如 "GET:/v1/files/{id}"
            String[] templateParts = routeTemplate.split(":");
            if (templateParts.length != 2) continue;

            String templateMethod = templateParts[0];
            String templatePath = templateParts[1];

            // 检查HTTP方法是否匹配
            if (!templateMethod.equals(requestInfo.method)) continue;

            // 检查路径是否匹配
            Map<String, String> pathParams = matchPath(templatePath, requestInfo.path);
            if (pathParams != null) {
                return new RouteMatchResult(routeInfo, templatePath, pathParams);
            }
        }

        return null;
    }

    /**
     * 简单的路径匹配算法
     * 例如：模板 "/v1/files/{id}" 匹配路径 "/v1/files/123"
     */
    private Map<String, String> matchPath(String template, String actualPath) {
        String[] templateSegments = template.split("/");
        String[] actualSegments = actualPath.split("/");

        // 路径段数量必须相同
        if (templateSegments.length != actualSegments.length) {
            return null;
        }

        Map<String, String> pathParams = new HashMap<>();

        for (int i = 0; i < templateSegments.length; i++) {
            String templateSegment = templateSegments[i];
            String actualSegment = actualSegments[i];

            if (templateSegment.startsWith("{") && templateSegment.endsWith("}")) {
                // 这是一个路径参数
                String paramName = templateSegment.substring(1, templateSegment.length() - 1);
                pathParams.put(paramName, actualSegment);
            } else if (!templateSegment.equals(actualSegment)) {
                // 非参数段必须完全匹配
                return null;
            }
        }

        return pathParams;
    }

    // ==================== Controller方法调用 ====================

    /**
     * 调用Controller方法
     */
    private void invokeControllerMethod(RouteMatchResult matchResult,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        RouteInfo routeInfo = matchResult.routeInfo;
        Method method = routeInfo.method;
        Object controller = routeInfo.controllerInstance;

        // 解析方法参数
        Object[] args = resolveMethodArguments(method, matchResult.pathParams, request, response);

        // 调用方法
        System.out.println("调用方法: " + method.getDeclaringClass().getSimpleName() + "." + method.getName());
        method.invoke(controller, args);
    }

    /**
     * 解析方法参数
     */
    private Object[] resolveMethodArguments(Method method,
                                          Map<String, String> pathParams,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {

        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            if (param.isAnnotationPresent(PathVariable.class)) {
                // 路径变量参数
                args[i] = resolvePathVariable(param, pathParams);

            } else if (param.getType() == HttpServletRequest.class) {
                // HttpServletRequest参数
                args[i] = request;

            } else if (param.getType() == HttpServletResponse.class) {
                // HttpServletResponse参数
                args[i] = response;

            } else {
                // 其他类型参数，暂时设为null
                args[i] = null;
                System.out.println("警告: 未处理的参数类型 " + param.getType().getName());
            }
        }

        return args;
    }

    /**
     * 解析路径变量参数
     */
    private Object resolvePathVariable(Parameter param, Map<String, String> pathParams) {
        PathVariable pathVar = param.getAnnotation(PathVariable.class);
        String varName = pathVar.value();

        try {
            String value = pathParams.get(varName);
            if (value == null) {
                System.err.println("路径变量 " + varName + " 未找到");
                return null;
            }
            return convertToTargetType(value, param.getType());
        } catch (Exception e) {
            System.err.println("解析路径变量失败: " + varName + ", 参数类型: " + param.getType());
            return null;
        }
    }

    // ==================== 控制器扫描注册 ====================

    /**
     * 扫描并注册所有Controller
     */
    private void scanAndRegisterControllers(String basePackage) {
        System.out.println("扫描包: " + basePackage);

        try {
            Set<Class<?>> controllerClasses = findControllerClasses(basePackage);

            for (Class<?> controllerClass : controllerClasses) {
                registerController(controllerClass);
            }

        } catch (Exception e) {
            System.err.println("扫描Controller失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 查找所有Controller类
     */
    private Set<Class<?>> findControllerClasses(String basePackage) throws Exception {
        Set<Class<?>> classes = new HashSet<>();

        URL resource = getClass().getClassLoader().getResource(basePackage.replace('.', '/'));
        if (resource == null) {
            System.out.println("未找到包: " + basePackage);
            return classes;
        }

        File directory = new File(resource.toURI());
        if (directory.exists()) {
            findClassesInDirectory(directory, basePackage, classes);
        }

        return classes;
    }

    /**
     * 递归查找目录中的类文件
     */
    private void findClassesInDirectory(File directory, String packageName, Set<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归扫描子目录
                findClassesInDirectory(file, packageName + "." + file.getName(), classes);

            } else if (file.getName().endsWith(".class")) {
                // 加载类
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        classes.add(clazz);
                        System.out.println("发现Controller: " + className);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("加载类失败: " + className);
                }
            }
        }
    }

    /**
     * 注册单个Controller
     */
    private void registerController(Class<?> controllerClass) {
        try {
            // 1. 创建Controller实例
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
            controllerInstanceMap.put(controllerClass.getName(), controllerInstance);

            // 2. 获取类级别的路径前缀
            String classPathPrefix = getClassPathPrefix(controllerClass);

            // 3. 注册所有处理方法
            Method[] methods = controllerClass.getDeclaredMethods();
            for (Method method : methods) {
                registerControllerMethod(method, controllerInstance, classPathPrefix);
            }

            System.out.println("注册Controller成功: " + controllerClass.getSimpleName());

        } catch (Exception e) {
            System.err.println("注册Controller失败: " + controllerClass.getName() + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 获取类级别的路径前缀
     */
    private String getClassPathPrefix(Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
            return normalizedPath(classMapping.path());
        }
        return "";
    }

    /**
     * 注册Controller方法 - 简化版本
     */
    private void registerControllerMethod(Method method, Object controllerInstance, String classPathPrefix) {
        // 检查方法上的映射注解
        List<MappingInfo> mappings = extractMethodMappings(method, classPathPrefix);

        for (MappingInfo mapping : mappings) {
            // 生成路由键
            String routeKey = mapping.httpMethod + ":" + mapping.path;

            // 创建路由信息
            RouteInfo routeInfo = new RouteInfo(method, controllerInstance, mapping);

            // 判断是否包含路径参数
            if (mapping.path.contains("{") && mapping.path.contains("}")) {
                // 包含路径参数的路由
                paramRouteMap.put(routeKey, routeInfo);
                System.out.println("注册参数路由: " + routeKey + " -> " +
                                 method.getDeclaringClass().getSimpleName() + "." + method.getName());
            } else {
                // 精确匹配的路由
                routeMap.put(routeKey, routeInfo);
                System.out.println("注册精确路由: " + routeKey + " -> " +
                                 method.getDeclaringClass().getSimpleName() + "." + method.getName());
            }
        }
    }

    /**
     * 提取方法的映射信息
     */
    private List<MappingInfo> extractMethodMappings(Method method, String classPathPrefix) {
        List<MappingInfo> mappings = new ArrayList<>();

        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            String path = classPathPrefix + normalizedPath(mapping.path());

            RequestMethod[] methods = mapping.method();
            if (methods.length == 0) {
                // 默认支持所有HTTP方法
                methods = RequestMethod.values();
            }

            for (RequestMethod httpMethod : methods) {
                mappings.add(new MappingInfo(httpMethod.name(), path));
            }

        } else if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            String path = classPathPrefix + normalizedPath(mapping.path());
            mappings.add(new MappingInfo("GET", path));

        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            String path = classPathPrefix + normalizedPath(mapping.path());
            mappings.add(new MappingInfo("POST", path));

        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            String path = classPathPrefix + normalizedPath(mapping.path());
            mappings.add(new MappingInfo("PUT", path));

        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            String path = classPathPrefix + normalizedPath(mapping.path());
            mappings.add(new MappingInfo("DELETE", path));
        }

        return mappings;
    }

    // ==================== 工具方法 ====================

    /**
     * 标准化路径
     */
    private String normalizedPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        return path.replaceAll("/+", "/");
    }

    /**
     * 类型转换
     */
    private Object convertToTargetType(String value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        return value;
    }

    // ==================== 错误处理 ====================

    /**
     * 处理404错误
     */
    private void handleNotFound(HttpServletResponse response, RequestInfo requestInfo) {
        try {
            System.out.println("404: 未找到匹配的路由 " + requestInfo.method + " " + requestInfo.path);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            System.err.println("发送404错误失败: " + e.getMessage());
        }
    }

    /**
     * 处理服务器错误
     */
    private void handleError(HttpServletResponse response, Exception e) {
        System.err.println("处理请求时发生错误: " + e.getMessage());
        e.printStackTrace();

        try {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (IOException ioException) {
            System.err.println("发送错误响应失败: " + ioException.getMessage());
        }
    }

    /**
     * 打印已注册的路由
     */
    private void printRegisteredRoutes() {
        System.out.println("\n=== 已注册的路由列表 ===");

        // 打印精确路由
        System.out.println("精确匹配路由:");
        routeMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    RouteInfo routeInfo = entry.getValue();
                    System.out.println("  " + entry.getKey() + " -> " +
                                     routeInfo.method.getDeclaringClass().getSimpleName() + "." +
                                     routeInfo.method.getName());
                });

        // 打印参数路由
        System.out.println("参数匹配路由:");
        paramRouteMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    RouteInfo routeInfo = entry.getValue();
                    System.out.println("  " + entry.getKey() + " -> " +
                                     routeInfo.method.getDeclaringClass().getSimpleName() + "." +
                                     routeInfo.method.getName());
                });

        System.out.println("========================\n");
    }

    // ==================== 内部数据类 ====================

    /**
     * 请求信息
     */
    private static class RequestInfo {
        final String method;
        final String path;
        final String requestURI;
        final String contextPath;

        RequestInfo(String method, String path, String requestURI, String contextPath) {
            this.method = method;
            this.path = path;
            this.requestURI = requestURI;
            this.contextPath = contextPath;
        }

        @Override
        public String toString() {
            return method + " " + path;
        }
    }

    /**
     * 路由信息
     */
    private static class RouteInfo {
        final Method method;
        final Object controllerInstance;
        final MappingInfo mappingInfo;

        RouteInfo(Method method, Object controllerInstance, MappingInfo mappingInfo) {
            this.method = method;
            this.controllerInstance = controllerInstance;
            this.mappingInfo = mappingInfo;
        }
    }

    /**
     * 映射信息
     */
    private static class MappingInfo {
        final String httpMethod;
        final String path;

        MappingInfo(String httpMethod, String path) {
            this.httpMethod = httpMethod;
            this.path = path;
        }
    }

    /**
     * 路由匹配结果
     */
    private static class RouteMatchResult {
        final RouteInfo routeInfo;
        final String routeTemplate;
        final Map<String, String> pathParams;

        RouteMatchResult(RouteInfo routeInfo, String routeTemplate, Map<String, String> pathParams) {
            this.routeInfo = routeInfo;
            this.routeTemplate = routeTemplate;
            this.pathParams = pathParams;
        }
    }
}
