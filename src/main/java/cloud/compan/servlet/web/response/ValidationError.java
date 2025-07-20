package cloud.compan.servlet.web.response;

/**
 * 验证错误信息
 * 用于表示单个字段的验证错误
 */
public class ValidationError {
    
    private final String field;
    private final String message;
    
    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }
    
    public String getField() {
        return field;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return String.format("ValidationError{field='%s', message='%s'}", field, message);
    }
} 