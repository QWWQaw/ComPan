package cloud.compan.servlet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

/**
 * 服务层统一返回结果包装器
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResult<T> {
    
    /**
     * 是否成功
     */
    @NotNull(message = "成功标识不能为空")
    private boolean success;
    
    /**
     * 返回数据
     */
    @Valid
    private T data;
    
    /**
     * 错误信息
     */
    @Size(max = 500, message = "错误信息长度不能超过500个字符")
    private String message;
    
    /**
     * 错误代码
     */
    @Size(max = 50, message = "错误代码长度不能超过50个字符")
    private String errorCode;
    

    // ============ Getter和Setter方法 ============
    
    public boolean isSuccess() {
        return this.success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public T getData() {
        return this.data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return this.errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * 判断操作是否失败
     * @return true表示失败，false表示成功
     */
    public boolean isFailure() {
        return !this.success;
    }

    // ============ 静态工厂方法 ============

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
     * 创建成功结果（带消息）
     */
    public static <T> ServiceResult<T> success(T data, String message) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(true);
        result.setData(data);
        result.setMessage(message);
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
    
    /**
     * 创建失败结果（带错误代码和数据）
     */
    public static <T> ServiceResult<T> failure(String message, String errorCode, T data) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(false);
        result.setData(data);
        result.setMessage(message);
        result.setErrorCode(errorCode);
        return result;
    }
    
    /**
     * 创建失败结果 - error方法别名
     */
    public static <T> ServiceResult<T> error(String message) {
        return failure(message);
    }

    /**
     * 创建失败结果（带错误代码） - error方法别名
     */
    public static <T> ServiceResult<T> error(String message, String errorCode) {
        return failure(message, errorCode);
    }

    /**
     * 创建失败结果（带错误代码和数据） - error方法别名
     */
    public static <T> ServiceResult<T> error(String message, String errorCode, T data) {
        return failure(message, errorCode, data);
    }

    @Override
    public String toString() {
        return "ServiceResult{" +
                "success=" + success +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}
