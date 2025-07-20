package cloud.compan.servlet.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 验证错误信息
 * 用于表示单个字段的验证错误
 */
public class ValidationError {
    
    @JsonProperty("field")
    private String field;
    
    @JsonProperty("message")
    private String message;
    
    // 默认构造函数，用于JSON反序列化
    public ValidationError() {}
    
    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return String.format("ValidationError{field='%s', message='%s'}", field, message);
    }
} 