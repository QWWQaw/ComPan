package cloud.compan.servlet.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonUtils工具类测试
 * 测试基于Jackson的JSON序列化和反序列化功能
 */
@DisplayName("JsonUtils 工具类测试")
class JsonUtilsTest {
    
    private JsonUtils jsonUtils;
    
    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        jsonUtils = new JsonUtils(objectMapper);
    }
    
    @Test
    @DisplayName("测试对象序列化为JSON")
    void testToJson() {
        // Given
        TestUser user = new TestUser(123L, "john", "john@example.com");
        
        // When
        String json = jsonUtils.toJson(user);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"id\":123"));
        assertTrue(json.contains("\"username\":\"john\""));
        assertTrue(json.contains("\"email\":\"john@example.com\""));
    }
    
    @Test
    @DisplayName("测试null对象序列化")
    void testToJsonWithNull() {
        // When
        String json = jsonUtils.toJson(null);
        
        // Then
        assertEquals("null", json);
    }
    
    @Test
    @DisplayName("测试JSON字符串反序列化为对象")
    void testFromJson() {
        // Given
        String json = "{\"id\":456,\"username\":\"jane\",\"email\":\"jane@example.com\"}";
        
        // When
        TestUser user = jsonUtils.fromJson(json, TestUser.class);
        
        // Then
        assertNotNull(user);
        assertEquals(456L, user.getId());
        assertEquals("jane", user.getUsername());
        assertEquals("jane@example.com", user.getEmail());
    }
    
    @Test
    @DisplayName("测试JSON格式验证")
    void testIsValidJson() {
        // Valid JSON
        assertTrue(jsonUtils.isValidJson("{\"name\":\"test\"}"));
        assertTrue(jsonUtils.isValidJson("[1,2,3]"));
        assertTrue(jsonUtils.isValidJson("\"string\""));
        
        // Invalid JSON
        assertFalse(jsonUtils.isValidJson("{name:test}"));  // 缺少引号
        assertFalse(jsonUtils.isValidJson(""));             // 空字符串
        assertFalse(jsonUtils.isValidJson(null));           // null
    }
    
    @Test
    @DisplayName("测试序列化反序列化往返")
    void testSerializationRoundTrip() {
        // Given
        TestUser originalUser = new TestUser(999L, "roundTrip", "roundtrip@test.com");
        
        // When
        String json = jsonUtils.toJson(originalUser);
        TestUser deserializedUser = jsonUtils.fromJson(json, TestUser.class);
        
        // Then
        assertNotNull(deserializedUser);
        assertEquals(originalUser.getId(), deserializedUser.getId());
        assertEquals(originalUser.getUsername(), deserializedUser.getUsername());
        assertEquals(originalUser.getEmail(), deserializedUser.getEmail());
    }
    
    /**
     * 测试用户类
     */
    public static class TestUser {
        private Long id;
        private String username;
        private String email;
        
        // 默认构造函数（Jackson需要）
        public TestUser() {}
        
        public TestUser(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
} 