package cloud.compan.servlet.annotations.data.field;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Value {
    String value() default "";
}
