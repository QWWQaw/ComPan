package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.annotations.enums.RequestMethod;
import cloud.compan.servlet.converter.JsonHttpMessageConverter;
import cloud.compan.servlet.utils.JsonUtils;
import cloud.compan.servlet.web.exception.HttpExceptions;
import cloud.compan.servlet.web.exception.WebException;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
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
 * 增强的HTTP请求分发器
 * 集成异常处理、响应包装、参数验证等功能
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
     */
    public void dispatch(HttpServletRequest request, HttpServletResponse response) {
        RouteInfo matchedRoute = null;
        try {
            // 1. 设置CORS和安全头
            setupResponseHeaders(response);
            
            // 2. 处理预检请求
            if ("OPTIONS".equals(request.getMethod())) {
                handlePreflightRequest(request, response);
                return;
            }
            
            // 3. 提取请求信息
            String requestPath = getRequestPath(request);
            String httpMethod = request.getMethod();

            System.out.println("📨 处理请求: " + httpMethod + " " + requestPath);

            logRequest(requestPath, httpMethod, request);
            
            // 4. 查找匹配的路由
            Optional<RouteInfo> routeOpt = routeRegistry.findRoute(requestPath, httpMethod);
            
            if (routeOpt.isPresent()) {
                matchedRoute = routeOpt.get();
                // 5. 执行控制器方法
                handleRequest(matchedRoute, request, response);
            } else {
                // 6. 处理404错误
                handleNotFound(requestPath, httpMethod, response);
            }
            
        } catch (WebException e) {
            // 7. 处理业务异常
            handleWebException(e, response);
        } catch (Exception e) {
            // 8. 处理系统异常
            handleSystemException(e, response, matchedRoute);
        }
    }
    
    /**
     * 执行具体的控制器方法
     */
    private void handleRequest(RouteInfo routeInfo, HttpServletRequest request, 
                              HttpServletResponse response) throws Exception {
        
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
            
        } catch (Exception e) {
            // 如果是反射调用异常，提取真实异常
            Throwable cause = e.getCause();
            if (cause instanceof WebException) {
                throw (WebException) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new RuntimeException("控制器方法执行失败", e);
            }
        }
    }
    
    /**
     * 增强的参数准备方法
     * 支持 @RequestBody, @PathVariable, @RequestParam 以及 HttpServletRequest/Response
     */
    private Object[] prepareMethodArguments(Method method, HttpServletRequest request, 
                                          HttpServletResponse response, RouteInfo routeInfo) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        // 如果是参数化路由，提取路径变量
        Map<String, String> pathVariables = extractPathVariables(routeInfo, request);
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();
            
            try {
                // 1. HttpServletRequest
                if (paramType == HttpServletRequest.class) {
                    args[i] = request;
                }
                // 2. HttpServletResponse
                else if (paramType == HttpServletResponse.class) {
                    args[i] = response;
                }
                // 3. @RequestBody 注解
                else if (parameter.isAnnotationPresent(RequestBody.class)) {
                    args[i] = parseRequestBody(request, paramType);
                }
                // 4. @PathVariable 注解
                else if (parameter.isAnnotationPresent(PathVariable.class)) {
                    args[i] = parsePathVariable(parameter, pathVariables);
                }
                // 5. @RequestParam 注解
                else if (parameter.isAnnotationPresent(RequestParam.class)) {
                    args[i] = parseRequestParam(parameter, request);
                }
                // 6. 默认尝试作为请求参数
                else {
                    args[i] = parseDefaultParameter(parameter, request);
                }
                
            } catch (Exception e) {
                throw HttpExceptions.parameterValidation(parameter.getName(), e.getMessage());
            }
        }
        
        return args;
    }
    
    /**
     * 提取路径变量
     */
    private Map<String, String> extractPathVariables(RouteInfo routeInfo, HttpServletRequest request) {
        if (routeInfo instanceof ParameterizedRouteInfo) {
            ParameterizedRouteInfo paramRoute = (ParameterizedRouteInfo) routeInfo;
            return paramRoute.extractPathVariables(getRequestPath(request));
        }
        return new HashMap<>();
    }
    
    /**
     * 解析请求体
     */
    private Object parseRequestBody(HttpServletRequest request, Class<?> targetType) {
        try {
            String contentType = request.getContentType();
            
            if (contentType != null && contentType.contains("application/json")) {
                return jsonConverter.read(targetType, request);
            } else if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                // 处理表单数据
                return parseFormData(request, targetType);
            } else {
                throw HttpExceptions.badRequest("不支持的Content-Type: " + contentType);
            }
        } catch (IOException e) {
            throw HttpExceptions.requestBodyParseError("请求体解析失败", e);
        }
    }
    
    /**
     * 解析表单数据
     */
    private Object parseFormData(HttpServletRequest request, Class<?> targetType) {
        // 简单实现：只支持基本类型，复杂对象需要进一步扩展
        if (targetType == String.class) {
            // 返回所有参数的字符串表示
            StringBuilder sb = new StringBuilder();
            request.getParameterMap().forEach((key, values) -> {
                if (sb.length() > 0) sb.append("&");
                sb.append(key).append("=").append(String.join(",", values));
            });
            return sb.toString();
        }
        
        throw HttpExceptions.badRequest("表单数据只支持String类型参数");
    }
    
    /**
     * 解析路径变量参数
     */
    private Object parsePathVariable(Parameter parameter, Map<String, String> pathVariables) {
        PathVariable annotation = parameter.getAnnotation(PathVariable.class);
        
        // 获取变量名
        String variableName = getVariableName(annotation.value(), annotation.name(), parameter.getName());
        String value = pathVariables.get(variableName);
        
        if (value == null && annotation.required()) {
            throw HttpExceptions.badRequest("必需的路径变量不存在: " + variableName);
        }
        
        return convertValue(value, parameter.getType(), variableName);
    }
    
    /**
     * 解析请求参数
     */
    private Object parseRequestParam(Parameter parameter, HttpServletRequest request) {
        RequestParam annotation = parameter.getAnnotation(RequestParam.class);
        
        // 获取参数名
        String paramName = getVariableName(annotation.value(), annotation.name(), parameter.getName());
        String value = request.getParameter(paramName);
        
        if (value == null && annotation.required()) {
            throw HttpExceptions.badRequest("必需的请求参数不存在: " + paramName);
        }
        
        if (value == null) {
            value = annotation.defaultValue();
            if (value.isEmpty()) {
                value = null;
            }
        }
        
        return convertValue(value, parameter.getType(), paramName);
    }
    
    /**
     * 解析默认参数（尝试从请求参数中获取）
     */
    private Object parseDefaultParameter(Parameter parameter, HttpServletRequest request) {
        String paramName = parameter.getName();
        String value = request.getParameter(paramName);
        return convertValue(value, parameter.getType(), paramName);
    }
    
    /**
     * 获取变量名
     */
    private String getVariableName(String value, String name, String parameterName) {
        if (!value.isEmpty()) return value;
        if (!name.isEmpty()) return name;
        return parameterName;
    }
    
    /**
     * 增强的类型转换
     */
    private Object convertValue(String value, Class<?> targetType, String paramName) {
        if (value == null) return null;
        
        try {
            if (targetType == String.class) return value;
            if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
            if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
            if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
            if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
            if (targetType == float.class || targetType == Float.class) return Float.parseFloat(value);
            
            // 其他类型暂不支持
            throw new IllegalArgumentException("不支持的参数类型: " + targetType.getSimpleName());
            
        } catch (NumberFormatException e) {
            throw HttpExceptions.pathVariableError(paramName, value, targetType.getSimpleName());
        }
    }
    
    /**
     * 处理控制器方法的返回结果
     */
    private void handleMethodResult(Object result, HttpServletRequest request, 
                                   HttpServletResponse response, RouteInfo routeInfo) throws IOException {
        
        // 1. 检查是否应该包装为ApiResponse
        if (shouldWrapAsApiResponse(result, routeInfo)) {
            // 包装为统一响应格式
            ApiResponseWrapper wrappedResult;
            if (result == null) {
                wrappedResult = ApiResponseWrapper.success("操作成功");
            } else {
                wrappedResult = ApiResponseWrapper.success(result);
            }
            jsonConverter.write(wrappedResult, "application/json", response);
        }
        // 2. 检查是否需要JSON序列化
        else if (shouldUseJsonConverter(routeInfo)) {
            jsonConverter.write(result, "application/json", response);
        }
        // 3. 普通响应处理
        else {
            handleRegularResponse(result, response);
        }
    }
    
    /**
     * 判断是否应该包装为ApiResponse
     */
    private boolean shouldWrapAsApiResponse(Object result, RouteInfo routeInfo) {
        // 如果返回结果已经是ApiResponseWrapper，不需要再包装
        if (result instanceof ApiResponseWrapper) {
            return false;
        }
        
        Method method = routeInfo.getHandlerMethod();
        Class<?> controllerClass = routeInfo.getControllerClass();
        
        // 检查方法或类是否有@ResponseBody注解
        return method.isAnnotationPresent(ResponseBody.class) || 
               controllerClass.isAnnotationPresent(ResponseBody.class);
    }
    
    /**
     * 判断是否应该使用JSON转换器
     */
    private boolean shouldUseJsonConverter(RouteInfo routeInfo) {
        Method method = routeInfo.getHandlerMethod();
        Class<?> controllerClass = routeInfo.getControllerClass();
        
        return method.isAnnotationPresent(ResponseBody.class) || 
               controllerClass.isAnnotationPresent(ResponseBody.class);
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
            response.setContentType("text/plain;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(result.toString());
        }
    }
    
    /**
     * 设置CORS和安全响应头
     */
    private void setupResponseHeaders(HttpServletResponse response) {
        // CORS headers
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Max-Age", "3600");
        
        // Security headers
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Cache control
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }
    
    /**
     * 处理预检请求
     */
    private void handlePreflightRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * 处理WebException
     */
    private void handleWebException(WebException e, HttpServletResponse response) {
        try {
            ApiResponseWrapper errorResponse = ApiResponseWrapper.fromException(e);
            response.setStatus(e.getStatusCode());
            jsonConverter.write(errorResponse, "application/json", response);
        } catch (IOException ioException) {
            handleSystemException(ioException, response, null);
        }
    }
    
    /**
     * 处理系统异常
     */
    private void handleSystemException(Exception e, HttpServletResponse response, RouteInfo route) {
        try {
            System.err.println("系统异常: " + e.getMessage());
            e.printStackTrace();
            
            ApiResponseWrapper errorResponse = ApiResponseWrapper.error(500, "服务器内部错误");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            
            jsonConverter.write(errorResponse, "application/json", response);
        } catch (IOException ioException) {
            System.err.println("写入错误响应时发生异常: " + ioException.getMessage());
        }
    }
    
    /**
     * 处理404错误
     */
    private void handleNotFound(String requestPath, String httpMethod, HttpServletResponse response) {
        try {
            ApiResponseWrapper notFoundResponse = ApiResponseWrapper.error(404, 
                String.format("路由不存在: %s %s", httpMethod, requestPath));
            
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonConverter.write(notFoundResponse, "application/json", response);
        } catch (IOException e) {
            System.err.println("写入404错误响应时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 记录请求日志，没有真正记录，只是为了方便调试
     */
    private void logRequest(String requestPath, String httpMethod, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        System.out.printf("%s %s (来自 %s)%n", httpMethod, requestPath, clientIp);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", 
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0];
                }
                return ip.trim();
            }
        }
        return request.getRemoteAddr();
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
} 