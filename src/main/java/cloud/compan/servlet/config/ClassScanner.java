package cloud.compan.servlet.config;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * 类扫描器
 * 用于扫描指定包下的类，支持文件系统和JAR包
 */
public class ClassScanner {
    
    private static final Logger logger = Logger.getLogger(ClassScanner.class.getName());
    
    /**
     * 扫描指定包下带有指定注解的类
     * 
     * @param basePackages 要扫描的基础包名数组
     * @param annotationClass 要查找的注解类型
     * @return 带有指定注解的类集合
     */
    public static Set<Class<?>> findClassesWithAnnotation(String[] basePackages, Class<? extends Annotation> annotationClass) {
        Set<Class<?>> annotatedClasses = new HashSet<>();
        
        for (String basePackage : basePackages) {
            try {
                Set<Class<?>> classes = findClassesInPackage(basePackage);
                for (Class<?> clazz : classes) {
                    if (clazz.isAnnotationPresent(annotationClass)) {
                        annotatedClasses.add(clazz);
                        logger.info("Found class with annotation: " + clazz.getName() + " [" + annotationClass.getSimpleName() + "]");
                    }
                }
            } catch (Exception e) {
                logger.warning("Error scanning package " + basePackage + ": " + e.getMessage());
            }
        }
        
        return annotatedClasses;
    }
    
    /**
     * 扫描指定包下的所有类
     * 
     * @param packageName 包名
     * @return 包下的所有类
     */
    public static Set<Class<?>> findClassesInPackage(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        
        try {
            String packagePath = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();
                
                if ("file".equals(protocol)) {
                    // 文件系统
                    findClassesInDirectory(new File(resource.getFile()), packageName, classes);
                } else if ("jar".equals(protocol)) {
                    // JAR包
                    findClassesInJar(resource, packagePath, packageName, classes);
                }
            }
        } catch (IOException e) {
            logger.warning("Error scanning package " + packageName + ": " + e.getMessage());
        }
        
        return classes;
    }
    
    /**
     * 在目录中查找类
     */
    private static void findClassesInDirectory(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                
                if (file.isDirectory()) {
                    // 递归扫描子目录
                    findClassesInDirectory(file, packageName + "." + fileName, classes);
                } else if (fileName.endsWith(".class")) {
                    String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        logger.warning("Could not load class " + className + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Find classes in JAR file
     * @param resource JAR resource URL
     * @param packagePath Package path
     * @param packageName Package name
     * @param classes Set to add found classes
     */
    private static void findClassesInJar(URL resource, String packagePath, String packageName, Set<Class<?>> classes) {
        String jarPath = resource.getPath();
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(5);
        }
        if (jarPath.contains("!")) {
            jarPath = jarPath.substring(0, jarPath.indexOf("!"));
        }
        
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    String className = entryName.substring(0, entryName.length() - 6).replace('/', '.');
                    try {
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        logger.warning("Could not load class " + className + " from JAR: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logger.warning("Error reading JAR file " + jarPath + ": " + e.getMessage());
        }
    }
} 