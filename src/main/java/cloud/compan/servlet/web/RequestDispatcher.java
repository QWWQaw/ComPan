package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.annotations.enums.RequestMethod;
import cloud.compan.servlet.utils.JsonUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.io.IOException;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * HTTP请求分发器 - 处理所有HTTP请求的核心组件
 * 负责路由匹配、方法调用、响应处理等
 */
@Singleton
public class RequestDispatcher {
    
    private final RouteRegistry routeRegistry;
    private final JsonUtils jsonUtils;
    
    @Inject
    public RequestDispatcher(RouteRegistry routeRegistry, JsonUtils jsonUtils) {
        this.routeRegistry = routeRegistry;
        this.jsonUtils = jsonUtils;
    }
    
    /**
     * 分发HTTP请求到相应的控制器方法
     * @param request HTTP请求
     * @param response HTTP响应
     */
    public void dispatch(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. 提取请求信息
            String requestPath = getRequestPath(request);
            String httpMethod = request.getMethod();
            
            System.out.println("📥 收到请求: " + httpMethod + " " + requestPath);
            
            // 2. 查找匹配的路由
            Optional<RouteInfo> routeOpt = routeRegistry.findRoute(requestPath, httpMethod);
            
            if (routeOpt.isPresent()) {
                // 3. 执行控制器方法
                handleRequest(routeOpt.get(), request, response);
            } else {
                // 4. 处理404错误
                handle404Error(request, response);
            }
            
        } catch (Exception e) {
            // 5. 处理500错误
            handle500Error(request, response, e);
        }
    }
    
    /**
     * 执行具体的控制器方法
     */
    private void handleRequest(RouteInfo routeInfo, HttpServletRequest request, 
                              HttpServletResponse response) throws Exception {
        
        System.out.println("🎯 匹配路由: " + routeInfo);
        
        try {
            // 1. 获取控制器方法
            Method handlerMethod = routeInfo.getHandlerMethod();
            Object controllerInstance = routeInfo.getControllerInstance();
            
            // 2. 准备方法参数
            Object[] methodArgs = prepareMethodArguments(handlerMethod, request, response, routeInfo);
            
            // 3. 调用控制器方法
            Object result = handlerMethod.invoke(controllerInstance, methodArgs);
            
            // 4. 处理返回结果
            handleMethodResult(result, request, response);
            
            System.out.println("✅ 请求处理成功");
            
        } catch (Exception e) {
            System.err.println("❌ 执行控制器方法时发生错误: " + e.getMessage());
            throw new RuntimeException("控制器方法执行失败", e);
        }
    }
    
    /**
     * 准备方法参数 - 支持多种参数注解
     * 支持 @RequestBody, @PathVariable, @RequestParam 以及 HttpServletRequest/Response
     */
    private Object[] prepareMethodArguments(Method method, HttpServletRequest request, 
                                          HttpServletResponse response, RouteInfo routeInfo) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        // 如果是参数化路由，提取路径变量
        Map<String, String> pathVariables = null;
        if (routeInfo instanceof ParameterizedRouteInfo) {
            ParameterizedRouteInfo paramRoute = (ParameterizedRouteInfo) routeInfo;
            pathVariables = paramRoute.extractPathVariables(getRequestPath(request));
        }
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();
            
            try {
                // 1. 检查 HttpServletRequest/Response
                if (HttpServletRequest.class.isAssignableFrom(paramType)) {
                    args[i] = request;
                } else if (HttpServletResponse.class.isAssignableFrom(paramType)) {
                    args[i] = response;
                }
                // 2. 检查 @RequestBody
                else if (parameter.isAnnotationPresent(RequestBody.class)) {
                    args[i] = parseRequestBody(request, paramType);
                }
                // 3. 检查 @PathVariable
                else if (parameter.isAnnotationPresent(PathVariable.class)) {
                    args[i] = parsePathVariable(parameter, pathVariables);
                }
                // 4. 检查 @RequestParam
                else if (parameter.isAnnotationPresent(RequestParam.class)) {
                    args[i] = parseRequestParam(parameter, request);
                }
                // 5. 默认处理：尝试从查询参数获取（如果是基本类型）
                else if (isBasicType(paramType)) {
                    String paramName = parameter.getName();
                    String value = request.getParameter(paramName);
                    args[i] = convertBasicType(value, paramType);
                }
                // 6. 其他情况：null
                else {
                    args[i] = null;
                }
                
            } catch (Exception e) {
                System.err.println("⚠️ 参数解析失败: " + parameter.getName() + " - " + e.getMessage());
                args[i] = null;
            }
        }
        
        return args;
    }
    
    /**
     * 解析请求体参数 - 使用封装的JsonUtils
     */
    private Object parseRequestBody(HttpServletRequest request, Class<?> targetType) throws IOException {
        String contentType = request.getContentType();
        
        if (contentType != null && contentType.contains("application/json")) {
            if (targetType == String.class) {
                // 如果目标类型是String，直接返回原始JSON字符串
                return jsonUtils.readJsonFromReader(request.getReader());
            } else {
                // 使用JsonUtils反序列化为目标对象
                Object result = jsonUtils.fromJson(request.getReader(), targetType);
                System.out.println("📥 JsonUtils解析成功: " + targetType.getSimpleName());
                return result;
            }
        } else {
            // 其他内容类型处理（未来扩展）
            System.err.println("⚠️ 不支持的Content-Type: " + contentType);
            return null;
        }
    }
    
    /**
     * 解析路径变量参数
     */
    private Object parsePathVariable(Parameter parameter, Map<String, String> pathVariables) {
        PathVariable annotation = parameter.getAnnotation(PathVariable.class);
        
        // 获取变量名
        String variableName = annotation.value();
        if (variableName.isEmpty()) {
            variableName = annotation.name();
        }
        if (variableName.isEmpty()) {
            variableName = parameter.getName(); // 使用参数名
        }
        
        String value = pathVariables != null ? pathVariables.get(variableName) : null;
        
        if (value == null && annotation.required()) {
            throw new IllegalArgumentException("必需的路径变量不存在: " + variableName);
        }
        
        return convertBasicType(value, parameter.getType());
    }
    
    /**
     * 解析请求参数
     */
    private Object parseRequestParam(Parameter parameter, HttpServletRequest request) {
        RequestParam annotation = parameter.getAnnotation(RequestParam.class);
        
        // 获取参数名
        String paramName = annotation.value();
        if (paramName.isEmpty()) {
            paramName = annotation.name();
        }
        if (paramName.isEmpty()) {
            paramName = parameter.getName(); // 使用参数名
        }
        
        String value = request.getParameter(paramName);
        
        if (value == null && annotation.required()) {
            if (!annotation.defaultValue().isEmpty()) {
                value = annotation.defaultValue();
            } else {
                throw new IllegalArgumentException("必需的请求参数不存在: " + paramName);
            }
        }
        
        return convertBasicType(value, parameter.getType());
    }
    
    /**
     * 检查是否为基本类型
     */
    private boolean isBasicType(Class<?> type) {
        return type == String.class ||
               type == int.class || type == Integer.class ||
               type == long.class || type == Long.class ||
               type == double.class || type == Double.class ||
               type == float.class || type == Float.class ||
               type == boolean.class || type == Boolean.class ||
               type == short.class || type == Short.class ||
               type == byte.class || type == Byte.class;
    }
    
    /**
     * 转换基本类型
     */
    private Object convertBasicType(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        try {
            if (targetType == String.class) {
                return value;
            } else if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(value);
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(value);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(value);
            } else if (targetType == float.class || targetType == Float.class) {
                return Float.parseFloat(value);
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(value);
            } else if (targetType == short.class || targetType == Short.class) {
                return Short.parseShort(value);
            } else if (targetType == byte.class || targetType == Byte.class) {
                return Byte.parseByte(value);
            } else {
                return value;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("参数类型转换失败: " + value + " -> " + targetType.getSimpleName());
        }
    }
    
    /**
     * 处理控制器方法的返回结果
     */
    private void handleMethodResult(Object result, HttpServletRequest request, 
                                   HttpServletResponse response) throws IOException {
        
        // 设置响应头
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        if (result == null) {
            // 空结果
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        
        if (result instanceof String) {
            // 字符串结果
            String stringResult = (String) result;
            if (stringResult.startsWith("{") || stringResult.startsWith("[")) {
                // 看起来像JSON
                response.setContentType("application/json;charset=UTF-8");
            } else {
                // 普通文本
                response.setContentType("text/plain;charset=UTF-8");
            }
            response.getWriter().write(stringResult);
            
        } else if (result instanceof Number || result instanceof Boolean) {
            // 基本类型
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(result.toString());
            
        } else {
            // 复杂对象 - 使用JsonUtils序列化为JSON
            try {
                response.setContentType("application/json;charset=UTF-8");
                String jsonResult = jsonUtils.toJson(result);
                response.getWriter().write(jsonResult);
                System.out.println("📤 JsonUtils序列化成功: " + result.getClass().getSimpleName());
            } catch (Exception e) {
                System.err.println("❌ JsonUtils序列化失败: " + e.getMessage());
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write(result.toString());
            }
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * 处理404错误
     */
    private void handle404Error(HttpServletRequest request, HttpServletResponse response) {
        try {
            String requestPath = getRequestPath(request);
            String httpMethod = request.getMethod();
            
            System.err.println("❌ 404 Not Found: " + httpMethod + " " + requestPath);
            
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            
            String errorJson = String.format(
                "{\"error\":\"Not Found\",\"message\":\"路由不存在: %s %s\",\"status\":404}",
                httpMethod, requestPath);
            
            response.getWriter().write(errorJson);
            
        } catch (IOException e) {
            System.err.println("❌ 写入404错误响应时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理500错误
     */
    private void handle500Error(HttpServletRequest request, HttpServletResponse response, Exception e) {
        try {
            String requestPath = getRequestPath(request);
            String httpMethod = request.getMethod();
            
            System.err.println("❌ 500 Internal Server Error: " + httpMethod + " " + requestPath);
            e.printStackTrace();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            
            String errorJson = String.format(
                "{\"error\":\"Internal Server Error\",\"message\":\"%s\",\"status\":500}",
                e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "服务器内部错误");
            
            response.getWriter().write(errorJson);
            
        } catch (IOException ioException) {
            System.err.println("❌ 写入500错误响应时发生异常: " + ioException.getMessage());
        }
    }
    
    /**
     * 提取请求路径
     */
    private String getRequestPath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // 移除上下文路径
        if (contextPath != null && !contextPath.isEmpty() && requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
        }
        
        // 确保路径以/开头
        if (!requestURI.startsWith("/")) {
            requestURI = "/" + requestURI;
        }
        
        return requestURI;
    }
    
    /**
     * 获取支持的HTTP方法统计（调试用）
     */
    public void printSupportedRoutes() {
        System.out.println("\n🚀 支持的路由:");
        routeRegistry.getAllRoutes().forEach(route -> 
            System.out.println("  " + route.getHttpMethod() + " " + route.getPath() + 
                             " -> " + route.getControllerClass().getSimpleName() + 
                             "." + route.getHandlerMethod().getName()));
    }
} 