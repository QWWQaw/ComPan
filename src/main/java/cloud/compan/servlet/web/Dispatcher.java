package cloud.compan.servlet.web;


import cloud.compan.servlet.annotations.component.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Parameter;
/**
* @brief 分发器，用来将请求分发到对应的处理方法
* @details 分发器用来将请求分发到对应的处理方法，处理方法的参数来自于请求的参数，处理方法的返回值来自于响应的参数。
*/
public class Dispatcher {
    /**
    * @brief 处理方法映射
    * @details 处理方法映射，用来将请求的路径映射到对应的处理方法
    */
    private final Map<Pattern, Method> handlerMap = new HashMap<>();

    /**
     * @brief 控制器映射，存储控制器示例化对象与控制器类名的映射
     */
    private final Map<String, Object> controllerMap = new HashMap<>();

    public Dispatcher() {
        scanControllers("cloud.compan.servlet");
    }

    /**
    * @brief 分发请求
    * @details 分发请求，根据请求的路径找到对应的处理方法，调用处理方法
    * @param req 请求对象
    * @param resp 响应对象
    */
    public void dispatch(HttpServletRequest req, HttpServletResponse resp) {
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestURI.substring(contextPath.length()); // 去除部署在服务器时的上下文路径，得到实际请求路径
        System.out.println("Handling request: " + req.getMethod() + " " + path);
        for (Map.Entry<Pattern, Method> entry : handlerMap.entrySet()) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(req.getMethod().toUpperCase() + ":" + path);
            if (matcher.matches()) {
                Method method = entry.getValue();
            try {
                String className = method.getDeclaringClass().getName();
                Object controller = controllerMap.get(className);
                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].isAnnotationPresent(PathVariable.class)) {
                        PathVariable pathVariable = parameters[i].getAnnotation(PathVariable.class);
                        String value = matcher.group(pathVariable.value());
                        args[i] = convert(value, parameters[i].getType());
                    } else if (parameters[i].getType() == HttpServletRequest.class) {
                        args[i] = req;
                    } else if (parameters[i].getType() == HttpServletResponse.class) {
                        args[i] = resp;
                    }
                }
                method.invoke(controller, args);
            } catch (Exception e) {
                  e.printStackTrace();
                  try {
                      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                  } catch (IOException ioException) {
                      ioException.printStackTrace();
                  }
              }
                return;
            }
        }

        try {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object convert(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }

    /**
    * @brief 扫描控制器
    * @details 扫描控制器，将控制器的实例化对象放入controllerMap中
    * @param basePackage 基础包名
    */
    private void scanControllers(String basePackage) {
        try {
            URL resource = getClass().getClassLoader().getResource(basePackage.replace('.', '/'));
            if (resource == null) {
                return;
            }

            File dir = new File(resource.toURI());
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    scanControllers(basePackage + "." + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    String className = basePackage + "." + file.getName().replace(".class", "");
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                        controllerMap.put(className, controllerInstance);
                        String pathPrefix = "";
                        if (clazz.isAnnotationPresent(RequestMapping.class)) {
                            pathPrefix = clazz.getAnnotation(RequestMapping.class).path();
                        }

                        for (Method method : clazz.getMethods()) {
                            if (method.isAnnotationPresent(RequestMapping.class)) {
                                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                                String path = (pathPrefix + requestMapping.path()).replaceAll("/+", "/");
                                String pathRegex = path.replaceAll("\\{(\\w+)\\}", "(?<$1>[^/]+)");

                                RequestMethod[] methods = requestMapping.method(); // 多个请求的 http方法
                                if (methods.length == 0) { // 默认支持所有的http方法
                                    methods = RequestMethod.values();
                                }
                                for (RequestMethod requestMethod : methods) {
                                    handlerMap.put(Pattern.compile(requestMethod.name().toUpperCase() + ":" + pathRegex), method);
                                }
                            } else if (method.isAnnotationPresent(GetMapping.class)) {
                                GetMapping getMapping = method.getAnnotation(GetMapping.class);
                                String path = (pathPrefix + getMapping.path()).replaceAll("/+", "/");
                                String pathRegex = path.replaceAll("\\{(\\w+)\\}", "(?<$1>[^/]+)");
                                handlerMap.put(Pattern.compile("GET:" + pathRegex), method);
                            } else if (method.isAnnotationPresent(PostMapping.class)) {
                                PostMapping postMapping = method.getAnnotation(PostMapping.class);
                                String path = (pathPrefix + postMapping.path()).replaceAll("/+", "/");
                                String pathRegex = path.replaceAll("\\{(\\w+)\\}", "(?<$1>[^/]+)");
                                handlerMap.put(Pattern.compile("POST:" + pathRegex), method);
                            } else if (method.isAnnotationPresent(PutMapping.class)) {
                                PutMapping putMapping = method.getAnnotation(PutMapping.class);
                                String path = (pathPrefix + putMapping.path()).replaceAll("/+", "/");
                                String pathRegex = path.replaceAll("\\{(\\w+)\\}", "(?<$1>[^/]+)");
                                handlerMap.put(Pattern.compile("PUT:" + pathRegex), method);
                            } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                                DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
                                String path = (pathPrefix + deleteMapping.path()).replaceAll("/+", "/");
                                String pathRegex = path.replaceAll("\\{(\\w+)\\}", "(?<$1>[^/]+)");
                                handlerMap.put(Pattern.compile("DELETE:" + pathRegex), method);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
