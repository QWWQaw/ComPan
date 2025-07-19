package cloud.compan.servlet.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求体注解 - 将HTTP请求体绑定到方法参数
 * 
 * 使用示例：
 * @PostMapping("/users")
 * public User createUser(@RequestBody User user) {
 *     return userService.create(user);
 * }
 * 
 * 支持的内容类型：
 * - application/json
 * - application/x-www-form-urlencoded (未来扩展)
 * - multipart/form-data (未来扩展)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestBody {
    
    /**
     * 是否必需请求体
     * @return true表示必需，false表示可选
     */
    boolean required() default true;
    
} 