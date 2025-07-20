package cloud.compan.servlet.web.exception;

/**
 * Web层基础异常类
 * 包含HTTP状态码和错误信息，用于统一异常处理
 */
public class WebException extends RuntimeException {
    
    private final int statusCode;
    private final String errorCode;
    
    public WebException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = null;
    }
    
    public WebException(int statusCode, String message, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
    
    public WebException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = null;
    }
    
    public WebException(int statusCode, String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
} 