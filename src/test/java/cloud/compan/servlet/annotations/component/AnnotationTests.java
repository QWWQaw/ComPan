package cloud.compan.servlet.annotations.component;

import org.junit.jupiter.api.Test;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.*;

public class AnnotationTests {

    @Test
    void testControllerAnnotation() {
        assertTrue(Controller.class.isAnnotationPresent(Retention.class));
        assertEquals(RUNTIME, Controller.class.getAnnotation(Retention.class).value());
        assertTrue(Controller.class.isAnnotationPresent(Target.class));
        assertArrayEquals(new java.lang.annotation.ElementType[]{TYPE}, Controller.class.getAnnotation(Target.class).value());
    }

    @Test
    void testServiceAnnotation() {
        assertTrue(Service.class.isAnnotationPresent(Retention.class));
        assertEquals(RUNTIME, Service.class.getAnnotation(Retention.class).value());
        assertTrue(Service.class.isAnnotationPresent(Target.class));
        assertArrayEquals(new java.lang.annotation.ElementType[]{TYPE}, Service.class.getAnnotation(Target.class).value());
    }

    @Test
    void testRepositoryAnnotation() {
        assertTrue(Repository.class.isAnnotationPresent(Retention.class));
        assertEquals(RUNTIME, Repository.class.getAnnotation(Retention.class).value());
        assertTrue(Repository.class.isAnnotationPresent(Target.class));
        assertArrayEquals(new java.lang.annotation.ElementType[]{TYPE}, Repository.class.getAnnotation(Target.class).value());
    }

    @Test
    void testRequestMappingAnnotation() {
        assertTrue(RequestMapping.class.isAnnotationPresent(Retention.class));
        assertEquals(RUNTIME, RequestMapping.class.getAnnotation(Retention.class).value());
        assertTrue(RequestMapping.class.isAnnotationPresent(Target.class));
        assertArrayEquals(new java.lang.annotation.ElementType[]{TYPE, METHOD}, RequestMapping.class.getAnnotation(Target.class).value());
    }

    @Test
    void testGetMappingAnnotation() {
        assertTrue(GetMapping.class.isAnnotationPresent(Retention.class));
        assertEquals(RUNTIME, GetMapping.class.getAnnotation(Retention.class).value());
        assertTrue(GetMapping.class.isAnnotationPresent(Target.class));
        assertArrayEquals(new java.lang.annotation.ElementType[]{METHOD}, GetMapping.class.getAnnotation(Target.class).value());
    }

    @Test
    void testPostMappingAnnotation() {
        assertTrue(PostMapping.class.isAnnotationPresent(Retention.class));
        assertEquals(RUNTIME, PostMapping.class.getAnnotation(Retention.class).value());
        assertTrue(PostMapping.class.isAnnotationPresent(Target.class));
        assertArrayEquals(new java.lang.annotation.ElementType[]{METHOD}, PostMapping.class.getAnnotation(Target.class).value());
    }

    @Test
    void testPutMappingAnnotation() {
        assertTrue(PutMapping.class.isAnnotationPresent(Retention.class));
        assertEquals(RUNTIME, PutMapping.class.getAnnotation(Retention.class).value());
        assertTrue(PutMapping.class.isAnnotationPresent(Target.class));
        assertArrayEquals(new java.lang.annotation.ElementType[]{METHOD}, PutMapping.class.getAnnotation(Target.class).value());
    }

    @Test
    void testDeleteMappingAnnotation() {
        assertTrue(DeleteMapping.class.isAnnotationPresent(Retention.class));
        assertEquals(RUNTIME, DeleteMapping.class.getAnnotation(Retention.class).value());
        assertTrue(DeleteMapping.class.isAnnotationPresent(Target.class));
        assertArrayEquals(new java.lang.annotation.ElementType[]{METHOD}, DeleteMapping.class.getAnnotation(Target.class).value());
    }
}