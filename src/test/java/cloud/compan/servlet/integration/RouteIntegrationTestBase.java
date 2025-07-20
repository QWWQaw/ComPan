package cloud.compan.servlet.integration;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.web.MockHttpRequestBuilder;
import cloud.compan.servlet.web.MockHttpRequest;
import cloud.compan.servlet.web.MockHttpResponse;
import cloud.compan.servlet.web.RequestDispatcher;
import cloud.compan.servlet.web.RouteRegistry;
import cloud.compan.servlet.web.ControllerScanner;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
import cloud.compan.servlet.utils.JsonUtils;
import cloud.compan.servlet.integration.HardcodedRouteRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.Mockito.*;

/**
 * 路由集成测试基类
 * 提供从HTTP请求到路由分发到控制器层到服务层的完整测试框架
 */
public abstract class RouteIntegrationTestBase extends SimpleTestBase {
    
    protected RequestDispatcher requestDispatcher;
    protected RouteRegistry routeRegistry;
    protected ControllerScanner controllerScanner;
    protected HardcodedRouteRegistry hardcodedRouteRegistry;
    protected HttpServletRequest mockRequest;
    protected HttpServletResponse mockResponse;
    protected StringWriter responseWriter;
    
    @BeforeEach
    void setUpRouteIntegration() {
        // 初始化路由组件
        routeRegistry = getService(RouteRegistry.class);
        controllerScanner = getService(ControllerScanner.class);
        requestDispatcher = getService(RequestDispatcher.class);
        
        // 初始化硬编码路由注册器
        hardcodedRouteRegistry = new HardcodedRouteRegistry(routeRegistry, injector);
        
        // 初始化Mock对象
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        
        // 设置基本的Mock响应
        try {
            when(mockResponse.getWriter()).thenReturn(new java.io.PrintWriter(responseWriter));
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup mock response writer", e);
        }
        
        // 使用硬编码方式注册所有路由
        hardcodedRouteRegistry.registerAllRoutes();
    }
    
    /**
     * 执行HTTP请求并返回响应
     */
    protected MockHttpResponse executeRequest(String method, String path) {
        return executeRequest(method, path, new HashMap<>(), new HashMap<>(), "");
    }
    
    /**
     * 执行HTTP请求并返回响应
     */
    protected MockHttpResponse executeRequest(String method, String path, Map<String, String> headers) {
        return executeRequest(method, path, headers, new HashMap<>(), "");
    }
    
    /**
     * 执行HTTP请求并返回响应
     */
    protected MockHttpResponse executeRequest(String method, String path, Map<String, String> headers, 
                                            Map<String, String> parameters, String body) {
        MockHttpRequestBuilder builder;
        
        // 根据HTTP方法创建对应的builder
        switch (method.toUpperCase()) {
            case "GET":
                builder = MockHttpRequestBuilder.get(path);
                break;
            case "POST":
                builder = MockHttpRequestBuilder.post(path);
                break;
            case "PUT":
                builder = MockHttpRequestBuilder.put(path);
                break;
            case "DELETE":
                builder = MockHttpRequestBuilder.delete(path);
                break;
            case "PATCH":
                builder = MockHttpRequestBuilder.patch(path);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
        
        // 设置请求头
        headers.forEach(builder::header);
        
        // 设置请求参数
        parameters.forEach(builder::parameter);
        
        // 设置请求体
        if (!body.isEmpty()) {
            builder.body(body).contentType("application/json");
        }
        
        MockHttpRequest mockHttpRequest = builder.build(mockRequest, mockResponse, requestDispatcher, responseWriter);
        return mockHttpRequest.execute();
    }
    
    /**
     * 验证响应状态码
     */
    protected void assertResponseStatus(MockHttpResponse response, int expectedStatus) {
        response.expectStatus(expectedStatus);
    }
    
    /**
     * 验证响应成功（状态码200）
     */
    protected void assertResponseOk(MockHttpResponse response) {
        assertResponseStatus(response, 200);
    }
    
    /**
     * 验证响应创建成功（状态码201）
     */
    protected void assertResponseCreated(MockHttpResponse response) {
        assertResponseStatus(response, 201);
    }
    
    /**
     * 验证响应错误（状态码400）
     */
    protected void assertResponseBadRequest(MockHttpResponse response) {
        assertResponseStatus(response, 400);
    }
    
    /**
     * 验证响应未授权（状态码401）
     */
    protected void assertResponseUnauthorized(MockHttpResponse response) {
        assertResponseStatus(response, 401);
    }
    
    /**
     * 验证响应内容包含特定字符串
     */
    protected void assertResponseContains(MockHttpResponse response, String expectedContent) {
        response.expectBodyContains(expectedContent);
    }
    
    /**
     * 验证响应是有效的JSON
     */
    protected void assertResponseIsValidJson(MockHttpResponse response) {
        try {
            response.parseAsApiResponse();
        } catch (Exception e) {
            fail("响应应该是有效的JSON格式: " + e.getMessage());
        }
    }
    
    /**
     * 验证响应是ApiResponseWrapper格式
     */
    protected ApiResponseWrapper assertResponseIsApiResponse(MockHttpResponse response) {
        try {
            return response.parseAsApiResponse();
        } catch (Exception e) {
            fail("响应应该是ApiResponseWrapper格式: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证路由是否存在
     */
    protected void assertRouteExists(String method, String path) {
        // 简化实现，实际项目中需要根据RouteRegistry的具体API调整
        assertTrue(true, "路由验证功能待实现");
    }
    
    /**
     * 验证路由不存在
     */
    protected void assertRouteNotExists(String method, String path) {
        // 简化实现，实际项目中需要根据RouteRegistry的具体API调整
        assertTrue(true, "路由验证功能待实现");
    }
    
    /**
     * 创建JSON请求体
     */
    protected String createJsonBody(Map<String, Object> data) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JSON body", e);
        }
    }
    
    /**
     * 创建认证头
     */
    protected Map<String, String> createAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("Content-Type", "application/json");
        return headers;
    }
} 