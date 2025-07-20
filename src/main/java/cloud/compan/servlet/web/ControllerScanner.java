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
 * 控制器扫描器 - 自动发现和注册控制器
 * 扫描指定包下的@Controller类，提取@RequestMapping信息并注册路由
 */
@Singleton
public class ControllerScanner {
    
    private final Injector injector;
    private final RouteRegistry routeRegistry;
    
    // 注解，容器选择如何找到参数对象并且注入
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
            
            // 2. 获取类级别的@RequestMapping
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
     * 从方法提取路由信息
     */
    private List<RouteInfo> extractMethodRoutes(Class<?> controllerClass, Method method, 
                                               Object controllerInstance, String classPath) {
        List<RouteInfo> routes = new ArrayList<>();
        
        // 1. 检查@RequestMapping
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            routes.addAll(createRoutesFromRequestMapping(
                controllerClass, method, controllerInstance, classPath, mapping));
        }
        
        // 2. 检查@GetMapping
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            String fullPath = combinePaths(classPath, mapping.path());
            routes.add(new ParameterizedRouteInfo(fullPath, RequestMethod.GET, 
                                                controllerClass, method, controllerInstance));
        }
        
        // 3. 检查@PostMapping
        if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            String fullPath = combinePaths(classPath, mapping.path());
            routes.add(new ParameterizedRouteInfo(fullPath, RequestMethod.POST, 
                                                controllerClass, method, controllerInstance));
        }
        
        // 4. 检查@PutMapping
        if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            String fullPath = combinePaths(classPath, mapping.path());
            routes.add(new ParameterizedRouteInfo(fullPath, RequestMethod.PUT, 
                                                controllerClass, method, controllerInstance));
        }
        
        // 5. 检查@DeleteMapping
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            String fullPath = combinePaths(classPath, mapping.path());
            routes.add(new ParameterizedRouteInfo(fullPath, RequestMethod.DELETE, 
                                                controllerClass, method, controllerInstance));
        }
        
        return routes;
    }
    
    /**
     * 从@RequestMapping创建路由信息
     */
    private List<RouteInfo> createRoutesFromRequestMapping(Class<?> controllerClass, Method method,
                                                          Object controllerInstance, String classPath,
                                                          RequestMapping mapping) {
        List<RouteInfo> routes = new ArrayList<>();
        String fullPath = combinePaths(classPath, mapping.path());
        
        // 如果没有指定HTTP方法，默认支持所有方法
        RequestMethod[] methods = mapping.method();
        if (methods.length == 0) {
            methods = new RequestMethod[]{RequestMethod.GET, RequestMethod.POST, 
                                        RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH};
        }
        
        // 为每个HTTP方法创建路由
        for (RequestMethod httpMethod : methods) {
            routes.add(new ParameterizedRouteInfo(fullPath, httpMethod, 
                                                controllerClass, method, controllerInstance));
        }
        
        return routes;
    }
    
    /**
     * 获取类级别的路径
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
     * 检查类是否为控制器, 含有注解@Controller就是控制器
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
} 