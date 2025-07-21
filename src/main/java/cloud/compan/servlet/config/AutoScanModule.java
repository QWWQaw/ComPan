package cloud.compan.servlet.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Controller;
import cloud.compan.servlet.annotations.Repository;
import cloud.compan.servlet.annotations.Service;

/**
 * 自动扫描模块
 * 自动发现和注册带有@Service和@Controller注解的类
 * 类似于Spring的组件扫描功能
 */
public class AutoScanModule extends AbstractModule {
    
    private static final Logger logger = Logger.getLogger(AutoScanModule.class.getName());
    
    // 要扫描的基础包名
    private final String[] basePackages;
    
    // 已注册的类，避免重复注册
    private final Set<Class<?>> registeredClasses = new HashSet<>();
    
    // Service接口到实现类的映射
    private final Map<Class<?>, Class<?>> serviceInterfaceToImplementation = new HashMap<>();
    
    public AutoScanModule(String... basePackages) {
        this.basePackages = basePackages.length > 0 ? basePackages : 
            new String[]{"cloud.compan.servlet"};
    }
    
    @Override
    protected void configure() {
        logger.info("Starting automatic component scanning...");
        
        // 扫描并注册Service组件
        scanAndRegisterServices();
        
        // 扫描并注册Controller组件
        scanAndRegisterControllers();
        
        // 扫描并注册Repository组件
        scanAndRegisterRepositories();
        
        // 自动绑定Service接口到实现类
        bindServiceInterfacesToImplementations();
        
        logger.info("Automatic scanning completed, registered " + registeredClasses.size() + " components");
        logger.info("Service interface bindings: " + serviceInterfaceToImplementation.size() + " items");
    }
    
    /**
     * 扫描并注册Service组件
     */
    private void scanAndRegisterServices() {
        logger.info("Scanning Service components...");
        
        Set<Class<?>> serviceClasses = ClassScanner.findClassesWithAnnotation(
            basePackages, Service.class);
        
        for (Class<?> serviceClass : serviceClasses) {
            if (!registeredClasses.contains(serviceClass)) {
                registerService(serviceClass);
                registeredClasses.add(serviceClass);
            }
        }
    }
    
    /**
     * 扫描并注册Controller组件
     */
    private void scanAndRegisterControllers() {
        logger.info("Scanning Controller components...");
        
        Set<Class<?>> controllerClasses = ClassScanner.findClassesWithAnnotation(
            basePackages, Controller.class);
        
        for (Class<?> controllerClass : controllerClasses) {
            if (!registeredClasses.contains(controllerClass)) {
                registerController(controllerClass);
                registeredClasses.add(controllerClass);
            }
        }
    }
    
    /**
     * 扫描并注册Repository组件
     */
    private void scanAndRegisterRepositories() {
        logger.info("Scanning Repository components...");
        
        Set<Class<?>> repositoryClasses = ClassScanner.findClassesWithAnnotation(
            basePackages, Repository.class);
        
        for (Class<?> repositoryClass : repositoryClasses) {
            if (!registeredClasses.contains(repositoryClass)) {
                registerRepository(repositoryClass);
                registeredClasses.add(repositoryClass);
            }
        }
        
        logger.info("Repository component scanning completed, found " + repositoryClasses.size() + " repositories");
    }

    /**
     * 注册Repository类
     */
    private void registerRepository(Class<?> repositoryClass) {
        try {
            // 检查是否有@Singleton注解
            boolean isSingleton = repositoryClass.isAnnotationPresent(Singleton.class);
            
            if (isSingleton) {
                bind(repositoryClass).asEagerSingleton();
                logger.info("Registered Repository as eager singleton: " + repositoryClass.getSimpleName());
            } else {
                bind(repositoryClass);
                logger.info("Registered Repository: " + repositoryClass.getSimpleName());
            }
            
        } catch (Exception e) {
            logger.warning("Failed to register Repository: " + repositoryClass.getName() + " - " + e.getMessage());
        }
    }
    
    /**
     * 自动绑定Service接口到实现类
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void bindServiceInterfacesToImplementations() {
        logger.info("Automatically binding Service interfaces to implementations...");
        
        for (Map.Entry<Class<?>, Class<?>> entry : serviceInterfaceToImplementation.entrySet()) {
            Class<?> interfaceClass = entry.getKey();
            Class<?> implementationClass = entry.getValue();
            
            try {
                // 检查实现类是否有@Singleton注解
                boolean isSingleton = implementationClass.isAnnotationPresent(Singleton.class);
                
                if (isSingleton) {
                    bind((Class) interfaceClass).to((Class) implementationClass).asEagerSingleton();
                    logger.info("Bound Service interface to eager singleton implementation: " + 
                        interfaceClass.getSimpleName() + " -> " + implementationClass.getSimpleName());
                } else {
                    bind((Class) interfaceClass).to((Class) implementationClass);
                    logger.info("Bound Service interface to implementation: " + 
                        interfaceClass.getSimpleName() + " -> " + implementationClass.getSimpleName());
                }
            } catch (Exception e) {
                logger.warning("Failed to bind Service interface: " + interfaceClass.getName() + 
                    " -> " + implementationClass.getName() + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * 注册Service组件
     */
    private void registerService(Class<?> serviceClass) {
        try {
            // 检查是否有@Singleton注解
            boolean isSingleton = serviceClass.isAnnotationPresent(Singleton.class);
            
            if (isSingleton) {
                bind(serviceClass).asEagerSingleton();
                logger.info("Registered Service as eager singleton: " + serviceClass.getSimpleName());
            } else {
                bind(serviceClass);
                logger.info("Registered Service: " + serviceClass.getSimpleName());
            }
            
            // Find and map service interfaces
            for (Class<?> interfaceClass : serviceClass.getInterfaces()) {
                if (isServiceInterface(interfaceClass)) {
                    serviceInterfaceToImplementation.put(interfaceClass, serviceClass);
                    logger.info("Found Service interface mapping: " + 
                        interfaceClass.getSimpleName() + " -> " + serviceClass.getSimpleName());
                }
            }
            
        } catch (Exception e) {
            logger.warning("Failed to register Service: " + serviceClass.getName() + " - " + e.getMessage());
        }
    }
    
    /**
     * 注册Controller组件
     */
    private void registerController(Class<?> controllerClass) {
        try {
            // 检查是否有@Singleton注解
            boolean isSingleton = controllerClass.isAnnotationPresent(Singleton.class);
            
            if (isSingleton) {
                bind(controllerClass).asEagerSingleton();
                logger.info("Registered Controller as eager singleton: " + controllerClass.getSimpleName());
            } else {
                bind(controllerClass);
                logger.info("Registered Controller: " + controllerClass.getSimpleName());
            }
        } catch (Exception e) {
            logger.warning("Failed to register Controller: " + controllerClass.getName() + " - " + e.getMessage());
        }
    }
    
    /**
     * 判断是否为Service接口
     * 检查接口是否在cloud.compan.servlet.service包中
     */
    private boolean isServiceInterface(Class<?> interfaceClass) {
        String packageName = interfaceClass.getPackage().getName();
        return packageName.startsWith("cloud.compan.servlet.service") && 
               !packageName.contains(".impl") &&
               interfaceClass.isInterface();
    }
} 