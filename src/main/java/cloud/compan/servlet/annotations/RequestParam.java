package cloud.compan.servlet.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求参数注解 - 将HTTP请求参数绑定到方法参数
 * 
 * 使用示例：
 * @GetMapping("/users")
 * public List<User> getUsers(@RequestParam("page") int page, 
 *                           @RequestParam(value = "size", defaultValue = "10") int size,
 *                           @RequestParam(required = false) String search) {
 *     return userService.findUsers(page, size, search);
 * }
 * 
 * 支持的参数来源：
 * - URL查询参数：/users?page=1&size=10&search=john
 * - 表单参数：Content-Type: application/x-www-form-urlencoded
 * 
 * 支持的参数类型：
 * - String
 * - Long, Integer, Short, Byte
 * - Boolean
 * - Double, Float
 * - 数组类型：String[], int[]等
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {
    
    /**
     * 请求参数的名称
     * @return 参数名
     */
    String value() default "";
    
    /**
     * 请求参数的名称（与value()相同，提供更清晰的语义）
     * @return 参数名
     */
    String name() default "";
    
    /**
     * 是否必需此参数
     * @return true表示必需，false表示可选
     */
    boolean required() default true;
    
    /**
     * 参数的默认值
     * 当参数不存在且required=false时使用
     * @return 默认值
     */
    String defaultValue() default "";
    
} 