package cloud.compan.servlet.annotations;

import java.lang.annotation.*;

/**
 * 基础组件注解
 *
 * <p>这是所有组件注解的基础注解，定义了组件的通用属性。
 * 其他组件注解（如@Service、@Repository、@Controller）都可以组合使用此注解。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @Component("customComponent")
 * public class CustomComponent {
 *     // 自定义组件实现
 * }
 * }
 * </pre>
 *
 * @author ComPan Team
 * @since 1.0
 * @see Service
 * @see Repository
 * @see Controller
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {

    /**
     * 指定组件的名称
     *
     * @return 组件名称，默认为空字符串（使用类名的小驼峰形式）
     */
    String value() default "";

    /**
     * 组件描述信息
     *
     * @return 组件的功能描述
     */
    String description() default "";

    /**
     * 组件的优先级
     *
     * @return 优先级数值，数值越小优先级越高
     */
    int priority() default 0;

    /**
     * 是否懒加载
     *
     * @return true表示懒加载，false表示立即加载
     */
    boolean lazy() default false;

    /**
     * 组件的作用域
     *
     * @return 作用域类型
     */
    Scope scope() default Scope.SINGLETON;

    /**
     * 组件作用域枚举
     */
    enum Scope {
        /** 单例模式 - 全局唯一实例 */
        SINGLETON,
        /** 原型模式 - 每次请求创建新实例 */
        PROTOTYPE,
        /** 请求作用域 - 每个HTTP请求一个实例 */
        REQUEST,
        /** 会话作用域 - 每个HTTP会话一个实例 */
        SESSION
    }
}
