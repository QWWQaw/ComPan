package cloud.compan.servlet.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记一个方法为PUT请求处理方法。
 * 该注解用于指定处理PUT请求的HTTP请求路径、参数、请求头、请求体和响应体。
 * 通过该注解，可以方便地定义一个处理PUT请求的方法。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PutMapping {
    String path() default "";
    String[] params() default {};
    String[] headers() default {};
    String[] consumes() default {};
    String[] produces() default {};
}