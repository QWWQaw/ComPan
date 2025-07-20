package cloud.compan.servlet.dto;
import lombok.Data;

/**
 * 服务层统一返回结果包装器
 * @param <T> 数据类型
 */
@Data
public class ServiceResult<T> {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 返回数据
     */
    private T data;
    
    /**
     * 错误信息
     */
    private String message;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    public ServiceResult() {}
    
    public ServiceResult(boolean success, T data, String message, String errorCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * 创建成功结果
     */
    public static <T> ServiceResult<T> success(T data) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(true);
        result.setData(data);
        result.setMessage(null);
        result.setErrorCode(null);
        return result;
    }
    
    /**
     * 创建成功结果（无数据）
     */
    public static <T> ServiceResult<T> success() {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(true);
        result.setData(null);
        result.setMessage(null);
        result.setErrorCode(null);
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static <T> ServiceResult<T> failure(String message) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(false);
        result.setData(null);
        result.setMessage(message);
        result.setErrorCode(null);
        return result;
    }
    
    /**
     * 创建失败结果（带错误代码）
     */
    public static <T> ServiceResult<T> failure(String message, String errorCode) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(false);
        result.setData(null);
        result.setMessage(message);
        result.setErrorCode(errorCode);
        return result;
    }
} 