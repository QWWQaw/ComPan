package cloud.compan.servlet.web.exception;

/**
 * 常用HTTP异常的便捷工厂类
 * 提供标准HTTP状态码对应的异常创建方法
 */
public class HttpExceptions {
    
    /**
     * 400 Bad Request
     */
    public static WebException badRequest(String message) {
        return new WebException(400, message, "BAD_REQUEST");
    }
    
    public static WebException badRequest(String message, String errorCode) {
        return new WebException(400, message, errorCode);
    }
    
    /**
     * 401 Unauthorized
     */
    public static WebException unauthorized(String message) {
        return new WebException(401, message, "UNAUTHORIZED");
    }
    
    /**
     * 403 Forbidden
     */
    public static WebException forbidden(String message) {
        return new WebException(403, message, "FORBIDDEN");
    }
    
    /**
     * 404 Not Found
     */
    public static WebException notFound(String message) {
        return new WebException(404, message, "NOT_FOUND");
    }
    
    /**
     * 409 Conflict
     */
    public static WebException conflict(String message) {
        return new WebException(409, message, "CONFLICT");
    }
    
    /**
     * 422 Unprocessable Entity
     */
    public static WebException unprocessableEntity(String message) {
        return new WebException(422, message, "UNPROCESSABLE_ENTITY");
    }
    
    /**
     * 500 Internal Server Error
     */
    public static WebException internalServerError(String message) {
        return new WebException(500, message, "INTERNAL_SERVER_ERROR");
    }
    
    public static WebException internalServerError(String message, Throwable cause) {
        return new WebException(500, message, "INTERNAL_SERVER_ERROR", cause);
    }
    
    /**
     * 参数验证异常
     */
    public static WebException parameterValidation(String paramName, String message) {
        return new WebException(400, 
            String.format("参数 '%s' 验证失败: %s", paramName, message), 
            "PARAMETER_VALIDATION_ERROR");
    }
    
    /**
     * 路径变量解析异常
     */
    public static WebException pathVariableError(String variableName, String value, String expectedType) {
        return new WebException(400, 
            String.format("路径变量 '%s' 的值 '%s' 无法转换为 %s 类型", variableName, value, expectedType), 
            "PATH_VARIABLE_TYPE_ERROR");
    }
    
    /**
     * 请求体解析异常
     */
    public static WebException requestBodyParseError(String message, Throwable cause) {
        return new WebException(400, "请求体解析失败: " + message, "REQUEST_BODY_PARSE_ERROR", cause);
    }
} 