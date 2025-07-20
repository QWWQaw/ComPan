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
    private final JsonHttpMessageConverter jsonConverter;
    
    @Inject
    public RequestDispatcher(RouteRegistry routeRegistry, JsonHttpMessageConverter jsonConverter) {
        this.routeRegistry = routeRegistry;
        this.jsonConverter = jsonConverter;
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
            
            System.out.println("收到请求: " + httpMethod + " " + requestPath);
            
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
        
        System.out.println("匹配路由: " + routeInfo);
        
        try {
            // 1. 获取控制器方法
            Method handlerMethod = routeInfo.getHandlerMethod();
            Object controllerInstance = routeInfo.getControllerInstance();
            
            // 2. 准备方法参数
            Object[] methodArgs = prepareMethodArguments(handlerMethod, request, response, routeInfo);
            
            // 3. 调用控制器方法
            Object result = handlerMethod.invoke(controllerInstance, methodArgs);
            
            // 4. 处理返回结果
            handleMethodResult(result, request, response, routeInfo);
            
            System.out.println("请求处理成功");
            
        } catch (Exception e) {
            System.err.println("执行控制器方法时发生错误: " + e.getMessage());
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
                System.err.println("参数解析失败: " + parameter.getName() + " - " + e.getMessage());
                args[i] = null;
            }
        }
        
        return args;
    }
    
    /**
     * 解析请求体参数 - 使用JsonHttpMessageConverter
     */
    private Object parseRequestBody(HttpServletRequest request, Class<?> targetType) throws IOException {
        String contentType = request.getContentType();
        
        if (contentType != null && contentType.contains("application/json")) {
            return jsonConverter.read(targetType, request);
        } else {
            System.err.println("不支持的Content-Type: " + contentType);
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
        
        if (value == null) {
            if (annotation.required()) {
                throw new IllegalArgumentException("必需的请求参数不存在: " + paramName);
            } else {
                String defaultValue = annotation.defaultValue();
                value = defaultValue.isEmpty() ? null : defaultValue;
            }
        }
        
        return convertBasicType(value, parameter.getType());
    }
    
    /**
     * 判断是否为基本类型
     */
    private boolean isBasicType(Class<?> type) {
        return type == String.class || type == int.class || type == Integer.class ||
               type == long.class || type == Long.class || type == boolean.class ||
               type == Boolean.class || type == double.class || type == Double.class ||
               type == float.class || type == Float.class;
    }
    
    /**
     * 转换基本类型
     */
    private Object convertBasicType(String value, Class<?> targetType) {
        if (value == null) return null;
        
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
        if (targetType == float.class || targetType == Float.class) return Float.parseFloat(value);
        
        return value; // fallback
    }
    
    /**
     * 处理控制器方法的返回结果，使用HttpMessageConverter
     */
    private void handleMethodResult(Object result, HttpServletRequest request, 
                                   HttpServletResponse response, RouteInfo routeInfo) throws IOException {
        
        // 检查是否需要JSON序列化
        boolean useJsonConverter = shouldUseJsonConverter(routeInfo);
        
        if (useJsonConverter) {
            // 使用JSON转换器
            jsonConverter.write(result, "application/json", response);
        } else {
            // 普通响应处理
            handleRegularResponse(result, response);
        }
    }
    
    /**
     * 判断是否应该使用JSON转换器
     */
    private boolean shouldUseJsonConverter(RouteInfo routeInfo) {
        Method method = routeInfo.getHandlerMethod();
        Class<?> controllerClass = routeInfo.getControllerClass();
        
        // 检查方法级别的@ResponseBody
        if (method.isAnnotationPresent(ResponseBody.class)) {
            return true;
        }
        
        // 检查类级别的@ResponseBody
        if (controllerClass.isAnnotationPresent(ResponseBody.class)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 处理普通响应
     */
    private void handleRegularResponse(Object result, HttpServletResponse response) throws IOException {
        if (result == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        
        if (result instanceof String) {
            String stringResult = (String) result;
            if (stringResult.startsWith("{") || stringResult.startsWith("[")) {
                response.setContentType("application/json;charset=UTF-8");
            } else if (stringResult.startsWith("<")) {
                response.setContentType("text/html;charset=UTF-8");
            } else {
                response.setContentType("text/plain;charset=UTF-8");
            }
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(stringResult);
        } else {
            // 其他类型默认转为字符串
            response.setContentType("text/plain;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(result.toString());
        }
    }
    
    /**
     * 处理404错误
     */
    private void handle404Error(HttpServletRequest request, HttpServletResponse response) {
        try {
            String requestPath = getRequestPath(request);
            String httpMethod = request.getMethod();
            
            System.err.println("404 Not Found: " + httpMethod + " " + requestPath);
            
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            
            String errorJson = String.format(
                "{\"error\":\"Not Found\",\"message\":\"路由不存在: %s %s\",\"status\":404}",
                httpMethod, requestPath);
            
            response.getWriter().write(errorJson);
            
        } catch (IOException e) {
            System.err.println("写入404错误响应时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理500错误
     */
    private void handle500Error(HttpServletRequest request, HttpServletResponse response, Exception e) {
        try {
            String requestPath = getRequestPath(request);
            String httpMethod = request.getMethod();
            
            System.err.println("500 Internal Server Error: " + httpMethod + " " + requestPath);
            e.printStackTrace();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            
            String errorJson = String.format(
                "{\"error\":\"Internal Server Error\",\"message\":\"%s\",\"status\":500}",
                e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "服务器内部错误");
            
            response.getWriter().write(errorJson);
            
        } catch (IOException ioException) {
            System.err.println("写入500错误响应时发生异常: " + ioException.getMessage());
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
        System.out.println("\n支持的路由:");
        routeRegistry.getAllRoutes().forEach(route -> 
            System.out.println("  " + route.getHttpMethod() + " " + route.getPath() + 
                             " -> " + route.getControllerClass().getSimpleName() + 
                             "." + route.getHandlerMethod().getName()));
    }
} 