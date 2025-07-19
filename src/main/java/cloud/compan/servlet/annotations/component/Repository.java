package cloud.compan.servlet.annotations.component;

import java.lang.annotation.*;

/**
 * 标识数据访问层组件的注解
 *
 * <p>用于标记数据访问层的类，这些类负责与数据库交互、数据持久化操作。
 * Repository层应该封装数据访问逻辑，为上层提供干净的数据操作接口。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @Repository("userRepository")
 * public class UserRepository {
 *     // 数据访问实现
 * }
 * }
 * </pre>
 *
 * @author ComPan Team
 * @since 1.0
 * @see Service
 * @see Controller
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repository {

    /**
     * 指定Repository组件的名称
     *
     * @return Repository名称，默认为空字符串（使用类名的小驼峰形式）
     */
    String value() default "";

    /**
     * Repository描述信息
     *
     * @return Repository的功能描述
     */
    String description() default "";

    /**
     * 数据源名称
     *
     * @return 关联的数据源名称
     */
    String dataSource() default "default";

    /**
     * 是否启用事务管理
     *
     * @return true表示启用事务，false表示禁用
     */
    boolean transactional() default true;
}
