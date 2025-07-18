package cloud.compan.servlet.annotations.component;

import org.junit.jupiter.api.Test;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.*;

public class AnnotationTests {

    @Test
    void testComponentAnnotations() {
        assertMetaAnnotations(Controller.class, "Controller", TYPE);
        assertMetaAnnotations(Service.class, "Service", TYPE);
        assertMetaAnnotations(Repository.class, "Repository", TYPE);
    }

    @Test
    void testRequestMappingAnnotations() {
        assertMetaAnnotations(RequestMapping.class, "RequestMapping", TYPE, METHOD);
        assertMetaAnnotations(GetMapping.class, "GetMapping", METHOD);
        assertMetaAnnotations(PostMapping.class, "PostMapping", METHOD);
        assertMetaAnnotations(PutMapping.class, "PutMapping", METHOD);
        assertMetaAnnotations(DeleteMapping.class, "DeleteMapping", METHOD);
    }

    @Test
    void testParameterAnnotations() {
        assertMetaAnnotations(PathVariable.class, "PathVariable", PARAMETER);
    }

    @Test
    void testRequestMappingDefaultValues() throws NoSuchMethodException {
        @RequestMapping
        class TestController {
            @RequestMapping
            public void testMethod() {}
        }

        RequestMapping methodAnnotation = TestController.class.getMethod("testMethod").getAnnotation(RequestMapping.class);
        assertArrayEquals(new RequestMethod[]{}, methodAnnotation.method(), "Default method should be an empty array");
    }


    private void assertMetaAnnotations(Class<? extends Annotation> annotationClass, String annotationName, ElementType... expectedTypes) {
        // Check for @Retention annotation
        assertTrue(annotationClass.isAnnotationPresent(Retention.class),
                () -> annotationName + " annotation should be annotated with @Retention");
        Retention retention = annotationClass.getAnnotation(Retention.class);
        assertEquals(RUNTIME, retention.value(),
                () -> annotationName + " annotation should have RUNTIME retention policy");

        // Check for @Target annotation
        assertTrue(annotationClass.isAnnotationPresent(Target.class),
                () -> annotationName + " annotation should be annotated with @Target");
        Target target = annotationClass.getAnnotation(Target.class);
        assertArrayEquals(expectedTypes, target.value(),
                () -> annotationName + " annotation should have correct target element types");
    }
}