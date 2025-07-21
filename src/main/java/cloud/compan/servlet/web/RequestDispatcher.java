package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.converter.JsonHttpMessageConverter;
import cloud.compan.servlet.web.exception.HttpExceptions;
import cloud.compan.servlet.web.exception.WebException;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * Enhanced HTTP request dispatcher
 * Integrates exception handling, response wrapping, and parameter validation
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
     * Dispatch HTTP request to the corresponding controller method
     */
    public void dispatch(HttpServletRequest request, HttpServletResponse response) {
        RouteInfo matchedRoute = null;
        try {
            // 1. Set CORS and security headers
            setupResponseHeaders(response);
            
            // 2. Handle preflight requests
            if ("OPTIONS".equals(request.getMethod())) {
                handlePreflightRequest(request, response);
                return;
            }
            
            // 3. Extract request information
            String requestPath = getRequestPath(request);
            String httpMethod = request.getMethod();

            System.out.println("Processing request: " + httpMethod + " " + requestPath);

            logRequest(requestPath, httpMethod, request);
            
            // 4. Find matching route
            Optional<RouteInfo> routeOpt = routeRegistry.findRoute(requestPath, httpMethod);
            
            if (routeOpt.isPresent()) {
                matchedRoute = routeOpt.get();
                // 5. Execute controller method
                handleRequest(matchedRoute, request, response);
            } else {
                // 6. Handle 404 error
                handleNotFound(requestPath, httpMethod, response);
            }
            
        } catch (WebException e) {
            // 7. Handle business exceptions
            handleWebException(e, response);
        } catch (Exception e) {
            // 8. Handle system exceptions
            handleSystemException(e, response, matchedRoute);
        }
    }
    
    /**
     * Execute specific controller method
     */
    private void handleRequest(RouteInfo routeInfo, HttpServletRequest request, 
                              HttpServletResponse response) throws Exception {
        
        try {
            System.out.println("START: Processing request...");
            System.out.println("ROUTE_INFO: " + routeInfo);
            
            // 1. Get controller method
            Method handlerMethod = routeInfo.getHandlerMethod();
            Object controllerInstance = routeInfo.getControllerInstance();
            
            System.out.println("CONTROLLER_METHOD: " + handlerMethod.getName());
            System.out.println("CONTROLLER_INSTANCE: " + controllerInstance.getClass().getSimpleName());
            
            // 2. Prepare method arguments
            System.out.println("START: Preparing method arguments...");
            Object[] methodArgs = prepareMethodArguments(handlerMethod, request, response, routeInfo);
            
            System.out.println("METHOD_ARGS_PREPARED: " + methodArgs.length + " arguments");
            for (int i = 0; i < methodArgs.length; i++) {
                System.out.println("ARG[" + i + "]: " + (methodArgs[i] != null ? methodArgs[i].getClass().getSimpleName() : "null"));
            }
            
            // 3. Call controller method
            System.out.println("START: Calling controller method...");
            Object result = handlerMethod.invoke(controllerInstance, methodArgs);
            
            System.out.println("CONTROLLER_METHOD_COMPLETED: " + (result != null ? result.getClass().getSimpleName() : "null"));
            
            // 4. Handle return result
            System.out.println("START: Processing return result...");
            handleMethodResult(result, request, response, routeInfo);
            
            System.out.println("REQUEST_PROCESSING_COMPLETED");
            
        } catch (Exception e) {
            System.err.println("ERROR: Request processing exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Enhanced parameter preparation method
     * Supports @RequestBody, @PathVariable, @RequestParam, and HttpServletRequest/Response
     */
    private Object[] prepareMethodArguments(Method method, HttpServletRequest request, 
                                          HttpServletResponse response, RouteInfo routeInfo) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        // If it's a parameterized route, extract path variables
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
                // 3. @RequestBody annotation
                else if (parameter.isAnnotationPresent(RequestBody.class)) {
                    args[i] = parseRequestBody(request, paramType);
                }
                // 4. @PathVariable annotation
                else if (parameter.isAnnotationPresent(PathVariable.class)) {
                    args[i] = parsePathVariable(parameter, pathVariables);
                }
                // 5. @RequestParam annotation
                else if (parameter.isAnnotationPresent(RequestParam.class)) {
                    args[i] = parseRequestParam(parameter, request);
                }
                // 6. Default attempt to be a request parameter
                else {
                    args[i] = parseDefaultParameter(parameter, request);
                }
                
            } catch (Exception e) {
                // Use a better parameter name for display
                String paramName = parameter.getName();
                if ("arg0".equals(paramName) || "arg1".equals(paramName)) {
                    // Try to get parameter name from annotation
                    if (parameter.isAnnotationPresent(RequestBody.class)) {
                        paramName = "requestBody";
                    } else if (parameter.isAnnotationPresent(PathVariable.class)) {
                        PathVariable pathVar = parameter.getAnnotation(PathVariable.class);
                        paramName = pathVar.value().isEmpty() ? parameter.getName() : pathVar.value();
                    } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                        RequestParam reqParam = parameter.getAnnotation(RequestParam.class);
                        paramName = reqParam.value().isEmpty() ? parameter.getName() : reqParam.value();
                    } else {
                        paramName = "parameter[" + i + "]";
                    }
                }
                throw HttpExceptions.parameterValidation(paramName, e.getMessage());
            }
        }
        
        return args;
    }
    
    /**
     * Extract path variables
     */
    private Map<String, String> extractPathVariables(RouteInfo routeInfo, HttpServletRequest request) {
        if (routeInfo instanceof ParameterizedRouteInfo) {
            ParameterizedRouteInfo paramRoute = (ParameterizedRouteInfo) routeInfo;
            return paramRoute.extractPathVariables(getRequestPath(request));
        }
        return new HashMap<>();
    }
    
    /**
     * Parse request body
     */
    private Object parseRequestBody(HttpServletRequest request, Class<?> targetType) {
        try {
            String contentType = request.getContentType();
            
            // If Content-Type is null, try to infer from request body
            if (contentType == null) {
                // Check if there is a request body content
                String requestBody = readRequestBody(request);
                if (requestBody != null && !requestBody.trim().isEmpty()) {
                    // If there is content and it looks like JSON, process as JSON
                    if (requestBody.trim().startsWith("{") || requestBody.trim().startsWith("[")) {
                        contentType = "application/json";
                    } else {
                        contentType = "application/x-www-form-urlencoded";
                    }
                } else {
                    // No content, return null or empty object
                    if (targetType == Map.class || targetType == java.util.HashMap.class) {
                        return new java.util.HashMap<>();
                    }
                    return null;
                }
            }
            
            if (contentType != null && contentType.contains("application/json")) {
                return jsonConverter.read(targetType, request);
            } else if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                // Process form data
                return parseFormData(request, targetType);
            } else {
                throw HttpExceptions.badRequest("Unsupported Content-Type: " + contentType);
            }
        } catch (IOException e) {
            throw HttpExceptions.requestBodyParseError("Failed to parse request body", e);
        }
    }
    
    /**
     * Read request body content
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
    
    /**
     * Parse form data
     */
    private Object parseFormData(HttpServletRequest request, Class<?> targetType) {
        // Simple implementation: only supports basic types, complex objects need further extension
        if (targetType == String.class) {
            // Return string representation of all parameters
            StringBuilder sb = new StringBuilder();
            request.getParameterMap().forEach((key, values) -> {
                if (sb.length() > 0) sb.append("&");
                sb.append(key).append("=").append(String.join(",", values));
            });
            return sb.toString();
        }
        
        throw HttpExceptions.badRequest("Form data only supports String type parameters");
    }
    
    /**
     * Parse path variable parameters
     */
    private Object parsePathVariable(Parameter parameter, Map<String, String> pathVariables) {
        PathVariable annotation = parameter.getAnnotation(PathVariable.class);
        
        // Get variable name
        String variableName = getVariableName(annotation.value(), annotation.name(), parameter.getName());
        String value = pathVariables.get(variableName);
        
        if (value == null && annotation.required()) {
            throw HttpExceptions.badRequest("Required path variable does not exist: " + variableName);
        }
        
        return convertValue(value, parameter.getType(), variableName);
    }
    
    /**
     * Parse request parameters
     */
    private Object parseRequestParam(Parameter parameter, HttpServletRequest request) {
        RequestParam annotation = parameter.getAnnotation(RequestParam.class);
        
        // Get parameter name
        String paramName = getVariableName(annotation.value(), annotation.name(), parameter.getName());
        String value = request.getParameter(paramName);
        
        if (value == null && annotation.required()) {
            throw HttpExceptions.badRequest("Required request parameter does not exist: " + paramName);
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
     * Parse default parameters (try to get from request parameters)
     */
    private Object parseDefaultParameter(Parameter parameter, HttpServletRequest request) {
        String paramName = parameter.getName();
        String value = request.getParameter(paramName);
        return convertValue(value, parameter.getType(), paramName);
    }
    
    /**
     * Get variable name
     */
    private String getVariableName(String value, String name, String parameterName) {
        if (!value.isEmpty()) return value;
        if (!name.isEmpty()) return name;
        return parameterName;
    }
    
    /**
     * Enhanced type conversion
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
            
            // Other types not supported
            throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getSimpleName());
            
        } catch (NumberFormatException e) {
            throw HttpExceptions.pathVariableError(paramName, value, targetType.getSimpleName());
        }
    }
    
    /**
     * Handle controller method return result
     */
    private void handleMethodResult(Object result, HttpServletRequest request, 
                                   HttpServletResponse response, RouteInfo routeInfo) throws IOException {
        
        // 1. Check if it should be wrapped as ApiResponse
        if (shouldWrapAsApiResponse(result, routeInfo)) {
            // Wrap in a unified response format
            ApiResponseWrapper wrappedResult;
            if (result == null) {
                wrappedResult = ApiResponseWrapper.success("Operation successful");
            } else {
                wrappedResult = ApiResponseWrapper.success(result);
            }
            jsonConverter.write(wrappedResult, "application/json", response);
        }
        // 2. Check if JSON serialization is needed
        else if (shouldUseJsonConverter(routeInfo)) {
            jsonConverter.write(result, "application/json", response);
        }
        // 3. Regular response handling
        else {
            handleRegularResponse(result, response);
        }
    }
    
    /**
     * Determine if it should be wrapped as ApiResponse
     */
    private boolean shouldWrapAsApiResponse(Object result, RouteInfo routeInfo) {
        // If the return result is already an ApiResponseWrapper, no need to wrap again
        if (result instanceof ApiResponseWrapper) {
            return false;
        }
        
        Method method = routeInfo.getHandlerMethod();
        Class<?> controllerClass = routeInfo.getControllerClass();
        
        // Check if the method or class has @ResponseBody annotation
        return method.isAnnotationPresent(ResponseBody.class) || 
               controllerClass.isAnnotationPresent(ResponseBody.class);
    }
    
    /**
     * Determine if JSON converter should be used
     */
    private boolean shouldUseJsonConverter(RouteInfo routeInfo) {
        Method method = routeInfo.getHandlerMethod();
        Class<?> controllerClass = routeInfo.getControllerClass();
        
        return method.isAnnotationPresent(ResponseBody.class) || 
               controllerClass.isAnnotationPresent(ResponseBody.class);
    }
    
    /**
     * Handle regular response
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
     * Set CORS and security response headers
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
     * Handle preflight requests
     */
    private void handlePreflightRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * Handle WebException
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
     * Handle system exceptions
     */
    private void handleSystemException(Exception e, HttpServletResponse response, RouteInfo route) {
        try {
            System.err.println("System exception: " + e.getMessage());
            e.printStackTrace();
            
            ApiResponseWrapper errorResponse = ApiResponseWrapper.error(500, "Server internal error");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            
            jsonConverter.write(errorResponse, "application/json", response);
        } catch (IOException ioException) {
            System.err.println("Exception writing error response: " + ioException.getMessage());
        }
    }
    
    /**
     * Handle 404 error
     */
    private void handleNotFound(String requestPath, String httpMethod, HttpServletResponse response) {
        try {
            ApiResponseWrapper notFoundResponse = ApiResponseWrapper.error(404, 
                String.format("Route not found: %s %s", httpMethod, requestPath));
            
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            jsonConverter.write(notFoundResponse, "application/json", response);
        } catch (IOException e) {
            System.err.println("Exception writing 404 error response: " + e.getMessage());
        }
    }
    
    /**
     * Log request, no actual logging, just for debugging
     */
    private void logRequest(String requestPath, String httpMethod, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        System.out.printf("%s %s (from %s)%n", httpMethod, requestPath, clientIp);
    }
    
    /**
     * Get client IP address
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
     * Extract request path
     */
    private String getRequestPath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // Remove context path
        if (contextPath != null && !contextPath.isEmpty() && requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
        }
        
        // Ensure path starts with /
        if (!requestURI.startsWith("/")) {
            requestURI = "/" + requestURI;
        }
        
        return requestURI;
    }

} 