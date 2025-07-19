package cloud.compan.servlet.annotations.component;

import java.lang.annotation.*;

/**
 * 标识服务层组件的注解
 *
 * <p>用于标记业务逻辑层的类，这些类负责处理业务逻辑、事务管理和数据访问层的协调。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @Service("userService")
 * public class UserService {
 *     // 业务逻辑实现
 * }
 * }
 * </pre>
 *
 * @author ComPan Team
 * @since 1.0
 * @see Repository
 * @see Controller
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {

    /**
     * 指定服务组件的名称
     *
     * @return 服务名称，默认为空字符串（使用类名的小驼峰形式）
     */
    String value() default "";

    /**
     * 服务描述信息
     *
     * @return 服务的功能描述
     */
    String description() default "";

    /**
     * 是否为单例模式
     *
     * @return true表示单例，false表示每次创建新实例
     */
    boolean singleton() default true;
}
