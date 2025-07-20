package cloud.compan.servlet.utils;

import cloud.compan.servlet.dto.UserDTO;
import cloud.compan.servlet.dto.FileDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonUtils 测试")
public class JsonUtilsTest {

    private JsonUtils jsonUtils;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 创建ObjectMapper并配置
        objectMapper = new ObjectMapper();
        
        // 创建JsonUtils实例
        jsonUtils = new JsonUtils(objectMapper);
    }

    @Nested
    @DisplayName("对象序列化测试")
    class ObjectSerializationTest {

        @Test
        @DisplayName("对象转JSON字符串应该成功")
        void testToJson() {
            // Given - 使用构造函数创建UserDTO
            UserDTO user = new UserDTO();
            user.setUserId(1L);
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setStorageLimit(10737418240L);
            user.setStorageUsed(1073741824L);
            user.setCreatedAt(LocalDateTime.now());

            // When
            String jsonString = jsonUtils.toJson(user);

            // Then
            assertNotNull(jsonString);
            assertTrue(jsonString.contains("\"userId\":1"));
            assertTrue(jsonString.contains("\"username\":\"testuser\""));
            assertTrue(jsonString.contains("\"email\":\"test@example.com\""));
        }

        @Test
        @DisplayName("null对象转JSON应该返回null字符串")
        void testToJsonWithNull() {
            // When
            String jsonString = jsonUtils.toJson(null);

            // Then
            assertEquals("null", jsonString);
        }
    }

    @Nested
    @DisplayName("JSON反序列化测试")
    class JsonDeserializationTest {

        @Test
        @DisplayName("JSON字符串转对象应该成功")
        void testFromJson() {
            // Given
            String jsonString = """
                {
                    "userId": 1,
                    "username": "testuser",
                    "email": "test@example.com",
                    "storageLimit": 10737418240,
                    "storageUsed": 1073741824
                }
                """;

            // When
            UserDTO user = jsonUtils.fromJson(jsonString, UserDTO.class);

            // Then
            assertNotNull(user);
            assertEquals(1L, user.getUserId());
            assertEquals("testuser", user.getUsername());
            assertEquals("test@example.com", user.getEmail());
            assertEquals(10737418240L, user.getStorageLimit());
            assertEquals(1073741824L, user.getStorageUsed());
        }

        @Test
        @DisplayName("无效JSON字符串应该抛出异常")
        void testFromJsonWithInvalidJson() {
            // Given
            String invalidJson = "{ invalid json }";

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                jsonUtils.fromJson(invalidJson, UserDTO.class);
            });
        }

        @Test
        @DisplayName("空JSON字符串应该返回null")
        void testFromJsonWithEmpty() {
            // When & Then
            assertNull(jsonUtils.fromJson("", UserDTO.class));
            // assertNull(jsonUtils.fromJson(null, UserDTO.class));
            assertNull(jsonUtils.fromJson("   ", UserDTO.class));
        }
    }

    @Nested
    @DisplayName("Map转换测试")
    class MapConversionTest {

        @Test
        @DisplayName("JSON字符串转Map应该成功")
        void testFromJsonMap() {
            // Given
            String jsonString = """
                {
                    "userId": 1,
                    "username": "testuser",
                    "active": true,
                    "score": 95.5
                }
                """;

            // When
            Map<String, Object> map = jsonUtils.fromJsonMap(jsonString);

            // Then
            assertNotNull(map);
            assertEquals(1, map.get("userId"));
            assertEquals("testuser", map.get("username"));
            assertEquals(true, map.get("active"));
            assertEquals(95.5, map.get("score"));
        }

        @Test
        @DisplayName("对象转JSON再转Map应该成功")
        void testObjectToJsonToMap() {
            // Given
            FileDTO file = new FileDTO();
            file.setFileId(1L);
            file.setFileName("test.txt");
            file.setFileSize(1024L);
            file.setMimeType("text/plain");

            // When
            String jsonString = jsonUtils.toJson(file);
            Map<String, Object> map = jsonUtils.fromJsonMap(jsonString);

            // Then
            assertNotNull(map);
            assertEquals(1, map.get("fileId"));
            assertEquals("test.txt", map.get("fileName"));
            assertEquals(1024, map.get("fileSize"));
            assertEquals("text/plain", map.get("mimeType"));
        }
    }

    @Nested
    @DisplayName("列表处理测试")
    class ListProcessingTest {

        @Test
        @DisplayName("JSON数组转对象列表应该成功")
        void testFromJsonList() {
            // Given
            String jsonArray = """
                [
                    {"fileId": 1, "fileName": "file1.txt", "fileSize": 1024},
                    {"fileId": 2, "fileName": "file2.txt", "fileSize": 2048}
                ]
                """;

            // When
            List<FileDTO> files = jsonUtils.fromJsonList(jsonArray, FileDTO.class);

            // Then
            assertNotNull(files);
            assertEquals(2, files.size());
            assertEquals(1L, files.get(0).getFileId());
            assertEquals("file1.txt", files.get(0).getFileName());
            assertEquals(2L, files.get(1).getFileId());
            assertEquals("file2.txt", files.get(1).getFileName());
        }

        @Test
        @DisplayName("对象列表转JSON数组应该成功")
        void testListToJson() {
            // Given
            FileDTO file1 = new FileDTO();
            file1.setFileId(1L);
            file1.setFileName("file1.txt");

            FileDTO file2 = new FileDTO();
            file2.setFileId(2L);
            file2.setFileName("file2.txt");

            List<FileDTO> files = List.of(file1, file2);

            // When
            String jsonArray = jsonUtils.toJson(files);

            // Then
            assertNotNull(jsonArray);
            assertTrue(jsonArray.contains("\"fileId\":1"));
            assertTrue(jsonArray.contains("\"fileName\":\"file1.txt\""));
            assertTrue(jsonArray.contains("\"fileId\":2"));
            assertTrue(jsonArray.contains("\"fileName\":\"file2.txt\""));
        }
    }

    @Nested
    @DisplayName("JSON验证测试")
    class JsonValidationTest {

        @Test
        @DisplayName("有效JSON应该通过验证")
        void testValidJson() {
            // Given
            String validJson = """
                {
                    "userId": 1,
                    "username": "testuser"
                }
                """;

            // When
            boolean isValid = jsonUtils.isValidJson(validJson);

            // Then
            assertTrue(isValid);
        }

        @Test
        @DisplayName("无效JSON应该验证失败")
        void testInvalidJson() {
            // Given
            String invalidJson = "{ invalid json }";

            // When
            boolean isValid = jsonUtils.isValidJson(invalidJson);

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("空字符串应该验证失败")
        void testEmptyJson() {
            // When & Then
            assertFalse(jsonUtils.isValidJson(""));
            assertFalse(jsonUtils.isValidJson(null));
            assertFalse(jsonUtils.isValidJson("   "));
        }
    }

    @Nested
    @DisplayName("JSON格式化测试")
    class JsonFormattingTest {

        @Test
        @DisplayName("JSON美化格式应该成功")
        void testPrettyJson() {
            // Given
            String compactJson = "{\"userId\":1,\"username\":\"testuser\",\"email\":\"test@example.com\"}";

            // When
            String prettyJson = jsonUtils.prettyJson(compactJson);

            // Then
            assertNotNull(prettyJson);
            assertTrue(prettyJson.contains("\n")); // 应该包含换行符
            assertTrue(prettyJson.contains("  ")); // 应该包含缩进
        }
    }
}