package cloud.compan.servlet.dto.common;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应DTO
 * 用于Handler与Service层之间以及API响应的标准格式
 */
@Data
@NoArgsConstructor
public class ResponseDTO<T> {
    @NotNull(message = "成功标识不能为空")
    private Boolean success;

    @NotBlank(message = "响应消息不能为空")
    @Size(max = 500, message = "响应消息长度不能超过500个字符")
    private String message;

    private T data;

    @Min(value = 100, message = "状态码不能小于100")
    @Max(value = 599, message = "状态码不能大于599")
    private Integer statusCode;

    private Map<String, Object> metadata;

    // 构造函数
    public ResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.metadata = new HashMap<>();
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

    // 元数据操作方法
    public ResponseDTO<T> addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
}
