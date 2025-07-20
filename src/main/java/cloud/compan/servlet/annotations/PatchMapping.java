package cloud.compan.servlet.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PATCH请求映射注解
 * 用于标记一个方法为PATCH请求处理方法
 * PATCH通常用于部分更新资源
 * 
 * 使用示例：
 * @PatchMapping("/users/{id}")
 * public ApiResponseWrapper updateUserPartial(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
 *     // 部分更新用户信息
 *     return userService.updatePartial(id, updates);
 * }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PatchMapping {
    
    /**
     * 请求路径
     * @return 路径字符串
     */
    String path() default "";
    
    /**
     * 请求参数限制
     * @return 参数限制数组
     */
    String[] params() default {};
    
    /**
     * 请求头限制
     * @return 请求头限制数组
     */
    String[] headers() default {};
    
    /**
     * 消费的内容类型
     * @return 消费的内容类型数组
     */
    String[] consumes() default {};
    
    /**
     * 生产的内容类型
     * @return 生产的内容类型数组
     */
    String[] produces() default {};
} 