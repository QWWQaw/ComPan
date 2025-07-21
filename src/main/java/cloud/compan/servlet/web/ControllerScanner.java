package cloud.compan.servlet.web;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Controller;
import cloud.compan.servlet.annotations.RequestMapping;
import cloud.compan.servlet.annotations.RestController;

/**
 * 增强的控制器扫描器
 * 自动发现和注册控制器，支持所有HTTP映射注解
 */
@Singleton
public class ControllerScanner {
    
    private final Injector injector;
    private final RouteRegistry routeRegistry;
    private static final boolean DEBUG = Boolean.getBoolean("debug.components");
    
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
        if (DEBUG) System.out.println("开始扫描控制器，包名: " + packageName);
        
        try {
            // 1. 获取包下的所有类
            List<Class<?>> classes = getClassesInPackage(packageName);
            if (DEBUG) System.out.println("扫描到 " + classes.size() + " 个类");
            
            // 只保留主代码下的控制器，去重
            Set<Class<?>> controllerClasses = new HashSet<>();
            for (Class<?> clazz : classes) {
                if (isController(clazz) && isMainClass(clazz)) {
                    controllerClasses.add(clazz);
                    if (DEBUG) System.out.println("  控制器: " + clazz.getName());
                }
            }
            if (DEBUG) System.out.println("找到 " + controllerClasses.size() + " 个控制器类");
            
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
     * 判断是否为主代码下的类（排除测试类）
     */
    private boolean isMainClass(Class<?> clazz) {
        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        return path.contains("/main/") && !clazz.getName().endsWith("Test");
    }

    /**
     * 检查类是否为控制器
     */
    private boolean isController(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class) || 
               clazz.isAnnotationPresent(RestController.class);
    }
    
    /**
     * 获取类级别的路径前缀（合并处理Controller和RestController）
     */
    private String getClassLevelPath(Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            return controllerClass.getAnnotation(RequestMapping.class).path();
        }
        String value = null;
        if (controllerClass.isAnnotationPresent(Controller.class)) {
            value = controllerClass.getAnnotation(Controller.class).value();
        } else if (controllerClass.isAnnotationPresent(RestController.class)) {
            value = controllerClass.getAnnotation(RestController.class).value();
        }
        return (value != null && !value.isEmpty()) ? value : "";
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
     * 获取指定包下的所有类（去重，优先主类路径）
     */
    private List<Class<?>> getClassesInPackage(String packageName) {
        Set<String> classNames = new HashSet<>();
        List<Class<?>> classes = new ArrayList<>();
        try {
            String path = packageName.replace('.', '/');
            
            // 尝试多个ClassLoader来找到正确的类路径
            ClassLoader[] classLoaders = {
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader(),
                ClassLoader.getSystemClassLoader()
            };
            
            for (ClassLoader classLoader : classLoaders) {
                if (classLoader != null) {
                    Enumeration<URL> resources = classLoader.getResources(path);
                    while (resources.hasMoreElements()) {
                        URL resource = resources.nextElement();
                        String protocol = resource.getProtocol();
                        
                        if ("file".equals(protocol)) {
                            File directory = new File(resource.getFile());
                            if (directory.exists()) {
                                scanDirectory(directory, packageName, classes, classNames);
                            }
                        } else if ("jar".equals(protocol)) {
                            // 处理JAR包中的类
                            scanJarClasses(resource, path, packageName, classes, classNames);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("扫描包时发生错误: " + e.getMessage());
        }
        return classes;
    }
    
    /**
     * 递归扫描目录中的类文件（去重）
     */
    private void scanDirectory(File directory, String packageName, List<Class<?>> classes, Set<String> classNames) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, packageName + "." + file.getName(), classes, classNames);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    if (classNames.add(className)) {
                        try {
                            Class<?> clazz = Class.forName(className);
                            classes.add(clazz);
                        } catch (ClassNotFoundException e) {
                            if (DEBUG) System.err.println("无法加载类: " + className);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 扫描JAR包中的类文件（去重）
     */
    private void scanJarClasses(URL resource, String packagePath, String packageName, List<Class<?>> classes, Set<String> classNames) {
        try {
            String jarPath = resource.getPath();
            if (jarPath.startsWith("file:")) {
                jarPath = jarPath.substring(5);
            }
            if (jarPath.contains("!")) {
                jarPath = jarPath.substring(0, jarPath.indexOf("!"));
            }
            
            try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath)) {
                java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();
                
                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    
                    if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                        String className = entryName.substring(0, entryName.length() - 6).replace('/', '.');
                        if (classNames.add(className)) {
                            try {
                                Class<?> clazz = Class.forName(className);
                                classes.add(clazz);
                            } catch (ClassNotFoundException e) {
                                if (DEBUG) System.err.println("无法加载JAR中的类: " + className);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) System.err.println("扫描JAR包时发生错误: " + e.getMessage());
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