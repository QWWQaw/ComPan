package cloud.compan.servlet.annotations.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates that a method parameter should be bound to a URI template variable.
 *
 * @see RequestMapping
 * @see GetMapping
 * @see PostMapping
 * @see PutMapping
 * @see DeleteMapping
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {

    /**
     * The name of the path variable to bind to.
     */
    String value();

}