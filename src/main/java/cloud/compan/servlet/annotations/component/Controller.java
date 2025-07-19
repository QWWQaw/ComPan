package cloud.compan.servlet.annotations.component;

import java.lang.annotation.*;

/**
 * 标识控制器组件的注解
 *
 * <p>用于标记Web层的控制器类，这些类负责处理HTTP请求、参数验证和响应返回。
 * Controller层是用户请求的入口点，应该保持轻量级，主要负责请求分发。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @Controller("/api/v1/users")
 * public class UserController {
 *     // 控制器实现
 * }
 * }
 * </pre>
 *
 * @author ComPan Team
 * @since 1.0
 * @see Service
 * @see Repository
 * @see RequestMapping
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {

    /**
     * 指定控制器的基础路径
     *
     * @return 控制器的URL路径前缀，默认为空
     */
    String value() default "";

    /**
     * 控制器描述信息
     *
     * @return 控制器的功能描述
     */
    String description() default "";

    /**
     * API版本
     *
     * @return API版本号，用于版本控制
     */
    String version() default "v1";

    /**
     * 是否启用CORS跨域支持
     *
     * @return true表示允许跨域访问
     */
    boolean cors() default false;
}
