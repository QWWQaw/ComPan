package cloud.compan.servlet.annotations.component;

import java.lang.annotation.*;

/**
 * 标识数据访问层组件的注解
 *
 * <p>用于标记数据访问层的Repository类，这些类负责封装数据库访问逻辑，执行CRUD操作。
 * Repository层是数据持久化的核心，提供统一的数据访问接口。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @Repository("userRepository")
 * public class UserRepository {
 *     public User findById(Long id) {
 *         // 数据库查询逻辑
 *         return user;
 *     }
 * }
 * }
 * </pre>
 *
 * @author ComPan Team
 * @since 1.0
 * @see Service
 * @see Controller
 * @see Transactional
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repository {

    /**
     * 指定Repository的名称
     *
     * @return Repository的唯一标识名称，默认为空
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
     * @return 使用的数据源名称，默认为主数据源
     */
    String dataSource() default "primary";

    /**
     * 是否支持事务
     *
     * @return true表示支持事务操作
     */
    boolean transactional() default true;

    /**
     * 缓存策略
     *
     * @return 缓存策略类型，none表示不使用缓存
     */
    String cacheStrategy() default "none";

    /**
     * 是否启用读写分离
     *
     * @return true表示启用读写分离
     */
    boolean readWriteSplit() default false;
}