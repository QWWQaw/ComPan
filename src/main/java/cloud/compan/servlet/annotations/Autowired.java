package cloud.compan.servlet.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义依赖注入注解
 * 功能等同于 Spring 的 @Autowired
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
    /**
     * 是否必须注入
     * @return 默认 true，表示必须注入
     */
    boolean required() default true;
}