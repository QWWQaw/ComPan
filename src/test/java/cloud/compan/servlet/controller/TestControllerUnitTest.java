package cloud.compan.servlet.controller;

import cloud.compan.servlet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TestController单元测试
 */
@DisplayName("TestController 单元测试")
class TestControllerUnitTest {
    
    @Mock
    private UserRepository userRepository;
    
    private TestController testController;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testController = new TestController();
        // 手动注入mock对象（模拟Guice注入）
        // 由于没有setter，我们直接测试不依赖于repository的方法
    }
    
    @Test
    @DisplayName("测试hello方法")
    void testHello() {
        // When
        String result = testController.hello();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("Hello, World!"));
        assertTrue(result.contains("路由系统正常工作"));
    }
    
    @Test
    @DisplayName("测试getStatus方法")
    void testGetStatus() {
        // When
        String result = testController.getStatus(null);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("\"status\":\"running\""));
        assertTrue(result.contains("\"message\":\"应用运行正常\""));
        assertTrue(result.contains("\"timestamp\""));
    }
    
    @Test
    @DisplayName("测试getUserDemo方法")
    void testGetUserDemo() {
        // When
        String result = testController.getUserDemo();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("\"userId\":1"));
        assertTrue(result.contains("\"username\":\"demo_user\""));
        assertTrue(result.contains("\"email\":\"demo@example.com\""));
        assertTrue(result.contains("\"message\":\"示例用户数据\""));
    }
    
    @Test
    @DisplayName("测试apiRoot方法")
    void testApiRoot() {
        // When
        String result = testController.apiRoot();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("\"message\":\"欢迎使用ComPan API\""));
        assertTrue(result.contains("\"version\":\"1.0\""));
        assertTrue(result.contains("\"/api/hello\""));
        assertTrue(result.contains("\"/api/status\""));
        assertTrue(result.contains("\"/api/users\""));
    }
    
    @Test
    @DisplayName("测试updateUser方法")
    void testUpdateUser() {
        // When
        String result = testController.updateUser();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("\"message\":\"用户更新功能\""));
        assertTrue(result.contains("\"status\":\"ready\""));
    }
    
    @Test
    @DisplayName("测试deleteUser方法")
    void testDeleteUser() {
        // When
        String result = testController.deleteUser();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("\"message\":\"用户删除功能\""));
        assertTrue(result.contains("\"status\":\"ready\""));
    }
} 