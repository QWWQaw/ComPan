package cloud.compan.servlet.annotations;

/**
 * 控制器注解，用于标记一个类为控制器。
 * 控制器是用于处理HTTP请求的类，它负责接收HTTP请求，处理请求，并返回HTTP响应。
 * 控制器通常与一个或多个路由注解一起使用，以定义请求的URL路径和处理方法。
 * 控制器可以包含多个处理方法，每个处理方法对应一个HTTP请求。
 * 控制器可以包含多个路由注解，每个路由注解对应一个HTTP请求。
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {
    String value() default "";
}
