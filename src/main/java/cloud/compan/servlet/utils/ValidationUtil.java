package cloud.compan.servlet.utils;

import com.google.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class ValidationUtil {
    
    private final Validator validator;

    public ValidationUtil() {
        try(var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    /**
     * 验证一个对象
     * @param <T> 对象类型
     * @param object 要验证的对象
     * @return 验证结果
     */
    public <T> Set<ConstraintViolation<T>> validate(T object) {
        return validator.validate(object);
    }

    /**
     * 格式化验证结果
     * @param <T> 对象类型
     * @param violations 验证结果
     * @return 格式化后的验证结果
     */
    public static <T> String formatViolations(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining("\n"));
    }



}
