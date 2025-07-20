package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.WebTestBase;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * DemoController Web测试示例
 * 展示类似于Spring Boot @WebMvcTest的测试风格，但适用于我们的自定义框架
 * 
 * 这个测试类演示了如何：
 * 1. 测试GET请求
 * 2. 测试POST请求
 * 3. 测试路径参数
 * 4. 测试请求参数
 * 5. 验证响应状态和内容
 */
class DemoControllerTest extends WebTestBase {
    
    @Test
    @DisplayName("测试GET请求 - 获取演示数据")
    void testGetExample() throws Exception {
        // When & Then: 执行GET请求并验证结果
        get("/api/demo/get-example")
            .andExpectOk()
            .expectApiSuccess()
            .expectBodyContains("GET请求处理成功")
            .expectBodyContains("method")
            .expectBodyContains("GET");
    }
    
    @Test
    @DisplayName("测试POST请求 - 创建数据")
    void testPostExample() throws Exception {
        // When & Then: 执行POST请求
        post("/api/demo/post-example")
            .contentType("application/json")
            .jsonBody("{\"test\": \"data\"}")
            .andExpectCreated()
            .expectApiSuccess()
            .expectBodyContains("POST请求处理成功");
    }
    
    @Test
    @DisplayName("测试PUT请求 - 带路径参数")
    void testPutExampleWithPathVariable() throws Exception {
        // When & Then: 测试带路径参数的PUT请求
        put("/api/demo/put-example/123")
            .contentType("application/json")
            .jsonBody("{\"name\": \"updated\"}")
            .andExpectOk()
            .expectApiSuccess()
            .expectBodyContains("PUT请求处理成功")
            .expectBodyContains("123");
    }
    
    @Test
    @DisplayName("测试DELETE请求")
    void testDeleteExample() throws Exception {
        // When & Then: 测试DELETE请求
        delete("/api/demo/delete-example/456")
            .andExpectOk()
            .expectApiSuccess()
            .expectBodyContains("DELETE请求处理成功")
            .expectBodyContains("456");
    }
    
    @Test
    @DisplayName("测试复杂参数处理")
    void testComplexParameters() throws Exception {
        // When & Then: 测试多种参数组合
        get("/api/demo/complex/electronics")
            .param("search", "laptop")
            .param("page", "2")
            .param("size", "15")
            .andExpectOk()
            .expectApiSuccess()
            .expectBodyContains("electronics")
            .expectBodyContains("laptop")
            .expectBodyContains("\"page\":2")
            .expectBodyContains("\"size\":15");
    }
    
    @Test
    @DisplayName("测试HTTP方法总览")
    void testMethodsOverview() throws Exception {
        // When & Then: 获取HTTP方法说明
        get("/api/demo/methods-overview")
            .andExpectOk()
            .expectApiSuccess()
            .expectBodyContains("GET")
            .expectBodyContains("POST")
            .expectBodyContains("PUT")
            .expectBodyContains("DELETE")
            .expectBodyContains("@GetMapping")
            .expectBodyContains("@PostMapping");
    }
    
    @Test
    @DisplayName("测试多HTTP方法支持")
    void testMultiMethodSupport_GET() throws Exception {
        // When & Then: 测试同一端点的GET方法
        get("/api/demo/multi-method")
            .andExpectOk()
            .expectApiSuccess()
            .expectBodyContains("GET方法处理");
    }
    
    @Test
    @DisplayName("测试多HTTP方法支持")
    void testMultiMethodSupport_POST() throws Exception {
        // When & Then: 测试同一端点的POST方法
        post("/api/demo/multi-method")
            .contentType("application/json")
            .jsonBody("{\"data\": \"test\"}")
            .andExpectOk()
            .expectApiSuccess()
            .expectBodyContains("POST方法处理");
    }
    
    @Test
    @DisplayName("测试不存在的路径 - 404错误")
    void testNotFoundPath() throws Exception {
        // When & Then: 测试404情况
        get("/api/demo/non-existent-path")
            .execute()
            .expectStatus(404);
    }
    
    @Test
    @DisplayName("测试参数验证")
    void testParameterValidation() throws Exception {
        // When & Then: 测试参数验证（页码超出范围）
        get("/api/demo/complex/test")
            .param("page", "0")  // 无效页码
            .execute()
            .expectStatus(400)
            .expectApiError()
            .expectBodyContains("页码必须大于0");
    }
} 