package cloud.compan.servlet.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路径变量注解 - 将URL路径中的变量绑定到方法参数
 * 
 * 使用示例：
 * @GetMapping("/users/{id}")
 * public User getUser(@PathVariable("id") Long userId) {
 *     return userService.findById(userId);
 * }
 * 
 * @GetMapping("/users/{userId}/posts/{postId}")
 * public Post getUserPost(@PathVariable Long userId, @PathVariable Long postId) {
 *     return postService.findByUserAndId(userId, postId);
 * }
 * 
 * 支持的参数类型：
 * - String
 * - Long, Integer, Short, Byte
 * - Boolean
 * - Double, Float
 * - UUID (未来扩展)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathVariable {
    
    /**
     * 路径变量的名称
     * 如果不指定，将使用参数名（需要编译时保留参数名）
     * @return 路径变量名
     */
    String value() default "";
    
    /**
     * 路径变量的名称（与value()相同，提供更清晰的语义）
     * @return 路径变量名
     */
    String name() default "";
    
    /**
     * 是否必需此路径变量
     * @return true表示必需，false表示可选
     */
    boolean required() default true;
    
} 