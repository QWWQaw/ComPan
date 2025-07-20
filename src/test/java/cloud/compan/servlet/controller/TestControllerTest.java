package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.WebTestBase;
import cloud.compan.servlet.web.MockHttpResponse;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.HashMap;

/**
 * TestController测试类
 */
public class TestControllerTest extends WebTestBase {
    
    @Test
    void testHandleTest() throws Exception {
        // 准备测试数据
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("test", "data");
        
        // 发送PUT请求
        MockHttpResponse response = put("/api/test/test")
                .jsonBody(requestData)
                .execute();
        
        // 验证响应
        response.expectApiSuccess()
                .expectApiMessage("测试成功");
    }
    
    @Test
    void testHandleTestWithEmptyBody() throws Exception {
        // 发送PUT请求，无请求体
        MockHttpResponse response = put("/api/test/test")
                .contentType("application/json")
                .execute();
        
        // 验证响应
        response.expectApiSuccess()
                .expectApiMessage("测试成功");
    }
} 