package cloud.compan.servlet.dto.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应DTO
 * 用于Handler与Service层之间以及API响应的标准格式
 */
public class ResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
    private Integer statusCode;
    private Map<String, Object> metadata;

    // 构造函数
    public ResponseDTO() {
        this.metadata = new HashMap<>();
    }

    public ResponseDTO(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }

    public ResponseDTO(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }

    public ResponseDTO(boolean success, String message, T data, Integer statusCode) {
        this(success, message, data);
        this.statusCode = statusCode;
    }

    // 静态工厂方法
    public static <T> ResponseDTO<T> success(String message, T data) {
        return new ResponseDTO<>(true, message, data, 200);
    }

    public static <T> ResponseDTO<T> success(String message, T data, Integer statusCode) {
        return new ResponseDTO<>(true, message, data, statusCode);
    }

    public static <T> ResponseDTO<T> success(String message) {
        return new ResponseDTO<>(true, message, null, 200);
    }

    public static <T> ResponseDTO<T> error(String message) {
        return new ResponseDTO<>(false, message, null, 400);
    }

    public static <T> ResponseDTO<T> error(String message, Integer statusCode) {
        return new ResponseDTO<>(false, message, null, statusCode);
    }

    public static <T> ResponseDTO<T> error(String message, T data, Integer statusCode) {
        return new ResponseDTO<>(false, message, data, statusCode);
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // 元数据操作方法
    public ResponseDTO<T> addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    public ResponseDTO<T> addTimestamp() {
        this.metadata.put("timestamp", System.currentTimeMillis());
        return this;
    }

    public ResponseDTO<T> addRequestId(String requestId) {
        this.metadata.put("request_id", requestId);
        return this;
    }
}
