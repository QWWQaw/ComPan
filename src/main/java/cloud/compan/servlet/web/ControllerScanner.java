package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.annotations.enums.RequestMethod;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 增强的控制器扫描器
 * 自动发现和注册控制器，支持所有HTTP映射注解
 */
@Singleton
public class ControllerScanner {
    
    private final Injector injector;
    private final RouteRegistry routeRegistry;
    
    @Inject
    public ControllerScanner(Injector injector, RouteRegistry routeRegistry) {
        this.injector = injector;
        this.routeRegistry = routeRegistry;
    }
    
    /**
     * 扫描指定包下的所有控制器并注册路由
     * @param packageName 要扫描的包名，例如 "cloud.compan.servlet.controller"
     */
    public void scanAndRegister(String packageName) {
        System.out.println("开始扫描控制器，包名: " + packageName);
        
        try {
            // 1. 获取包下的所有类
            List<Class<?>> classes = getClassesInPackage(packageName);
            
            // 2. 过滤出控制器类
            List<Class<?>> controllerClasses = classes.stream()
                .filter(this::isController)
                .toList();
            
            System.out.println("找到 " + controllerClasses.size() + " 个控制器类");
            
            // 3. 为每个控制器注册路由
            for (Class<?> controllerClass : controllerClasses) {
                registerControllerRoutes(controllerClass);
            }
            
            // 4. 打印注册结果
            routeRegistry.printAllRoutes();
            
        } catch (Exception e) {
            System.err.println("扫描控制器时发生错误: " + e.getMessage());
            throw new RuntimeException("控制器扫描失败", e);
        }
    }
    
    /**
     * 注册单个控制器的所有路由
     */
    private void registerControllerRoutes(Class<?> controllerClass) {
        try {
            // 1. 从Guice获取控制器实例
            Object controllerInstance = injector.getInstance(controllerClass);
            
            // 2. 获取类级别的路径前缀
            String classPath = getClassLevelPath(controllerClass);
            
            // 3. 扫描所有处理方法
            Method[] methods = controllerClass.getDeclaredMethods();
            for (Method method : methods) {
                List<RouteInfo> routes = extractMethodRoutes(
                    controllerClass, method, controllerInstance, classPath);
                
                // 4. 注册路由
                for (RouteInfo route : routes) {
                    routeRegistry.registerRoute(route);
                }
            }
            
        } catch (Exception e) {
            System.err.println("注册控制器 " + controllerClass.getSimpleName() + " 时发生错误: " + e.getMessage());
            throw new RuntimeException("控制器注册失败: " + controllerClass.getName(), e);
        }
    }
    
    /**
     * 从方法提取路由信息 - 使用统一的映射处理
     */
    private List<RouteInfo> extractMethodRoutes(Class<?> controllerClass, Method method, 
                                               Object controllerInstance, String classPath) {
        List<RouteInfo> routes = new ArrayList<>();
        
        // 1. 处理@RequestMapping（支持多个HTTP方法）
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            MappingWrapper[] wrappers = MappingWrapper.fromRequestMapping(mapping);
            
            for (MappingWrapper wrapper : wrappers) {
                String fullPath = combinePaths(classPath, wrapper.getPath());
                routes.add(new ParameterizedRouteInfo(fullPath, wrapper.getHttpMethod(), 
                                                    controllerClass, method, controllerInstance));
            }
        }
        
        // 2. 处理特定HTTP方法的映射注解
        if (MappingWrapper.hasHttpMapping(method)) {
            MappingWrapper wrapper = MappingWrapper.fromMethod(method);
            if (wrapper != null) {
                String fullPath = combinePaths(classPath, wrapper.getPath());
                routes.add(new ParameterizedRouteInfo(fullPath, wrapper.getHttpMethod(), 
                                                    controllerClass, method, controllerInstance));
            }
        }
        
        return routes;
    }
    
    /**
     * 获取类级别的路径前缀
     */
    private String getClassLevelPath(Class<?> controllerClass) {
        // 检查@RequestMapping
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            return controllerClass.getAnnotation(RequestMapping.class).path();
        }
        // 检查@Controller
        if (controllerClass.isAnnotationPresent(Controller.class)) {
            String value = controllerClass.getAnnotation(Controller.class).value();
            return value.isEmpty() ? "" : value;
        }
        return "";
    }
    
    /**
     * 组合类路径和方法路径
     */
    private String combinePaths(String classPath, String methodPath) {
        if (classPath == null) classPath = "";
        if (methodPath == null) methodPath = "";
        
        if (classPath.isEmpty()) return methodPath;
        if (methodPath.isEmpty()) return classPath;
        
        return classPath + (methodPath.startsWith("/") ? methodPath : "/" + methodPath);
    }
    
    /**
     * 检查类是否为控制器
     */
    private boolean isController(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class);
    }
    
    /**
     * 获取指定包下的所有类
     */
    private List<Class<?>> getClassesInPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            String path = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);
            
            if (resource != null) {
                File directory = new File(resource.getFile());
                if (directory.exists()) {
                    scanDirectory(directory, packageName, classes);
                }
            }
        } catch (Exception e) {
            System.err.println("扫描包时发生错误: " + e.getMessage());
        }
        return classes;
    }
    
    /**
     * 递归扫描目录中的类文件
     */
    private void scanDirectory(File directory, String packageName, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, packageName + "." + file.getName(), classes);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    try {
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        System.err.println("无法加载类: " + className);
                    }
                }
            }
        }
    }
    
    /**
     * 获取所有支持的HTTP映射注解信息
     */
    public void printSupportedMappings() {
        System.out.println("\n支持的HTTP映射注解:");
        System.out.println("  @GetMapping - GET请求");
        System.out.println("  @PostMapping - POST请求");
        System.out.println("  @PutMapping - PUT请求");
        System.out.println("  @DeleteMapping - DELETE请求");
        System.out.println("  @PatchMapping - PATCH请求");
        System.out.println("  @RequestMapping - 通用映射（支持多种HTTP方法）");
    }
} 