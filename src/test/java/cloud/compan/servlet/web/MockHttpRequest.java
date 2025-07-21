package cloud.compan.servlet.web;

import static org.mockito.Mockito.when;
import java.io.StringWriter;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 模拟HTTP请求，支持流式API测试
 */
public class MockHttpRequest {
    
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final RequestDispatcher requestDispatcher;
    private final StringWriter responseWriter;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public MockHttpRequest(HttpServletRequest request, 
                          HttpServletResponse response, 
                          RequestDispatcher requestDispatcher,
                          StringWriter responseWriter) {
        this.request = request;
        this.response = response;
        this.requestDispatcher = requestDispatcher;
        this.responseWriter = responseWriter;
    }
    
    /**
     * 设置请求参数
     */
    public MockHttpRequest param(String name, String value) {
        when(request.getParameter(name)).thenReturn(value);
        return this;
    }
    
    /**
     * 设置多个请求参数
     */
    public MockHttpRequest params(Map<String, String> params) {
        params.forEach(this::param);
        return this;
    }
    
    /**
     * 设置请求头
     */
    public MockHttpRequest header(String name, String value) {
        when(request.getHeader(name)).thenReturn(value);
        return this;
    }
    
    /**
     * 设置Authorization头（JWT token）
     */
    public MockHttpRequest bearerToken(String token) {
        return header("Authorization", "Bearer " + token);
    }
    
    /**
     * 设置Content-Type
     */
    public MockHttpRequest contentType(String contentType) {
        return header("Content-Type", contentType);
    }
    
    /**
     * 设置JSON请求体
     */
    public MockHttpRequest jsonBody(Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            
            // 模拟请求体读取
            java.io.StringReader stringReader = new java.io.StringReader(json);
            java.io.BufferedReader bufferedReader = new java.io.BufferedReader(stringReader);
            
            when(request.getReader()).thenReturn(bufferedReader);
            when(request.getContentType()).thenReturn("application/json");
            when(request.getContentLength()).thenReturn(json.length());
            
            return contentType("application/json");
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JSON body", e);
        }
    }
    
    /**
     * 执行请求并返回测试结果
     */
    public MockHttpResponse execute() {
        try {
            requestDispatcher.dispatch(request, response);
            return new MockHttpResponse(response, responseWriter);
        } catch (Exception e) {
            throw new RuntimeException("Request execution failed", e);
        }
    }
    
    /**
     * 执行请求并期望成功（状态码200）
     */
    public MockHttpResponse andExpectOk() {
        MockHttpResponse mockResponse = execute();
        mockResponse.expectStatus(200);
        return mockResponse;
    }
    
    /**
     * 执行请求并期望创建成功（状态码201）
     */
    public MockHttpResponse andExpectCreated() {
        MockHttpResponse mockResponse = execute();
        mockResponse.expectStatus(201);
        return mockResponse;
    }
    
    /**
     * 执行请求并期望客户端错误（状态码400）
     */
    public MockHttpResponse andExpectBadRequest() {
        MockHttpResponse mockResponse = execute();
        mockResponse.expectStatus(400);
        return mockResponse;
    }
    
    /**
     * 执行请求并期望未授权（状态码401）
     */
    public MockHttpResponse andExpectUnauthorized() {
        MockHttpResponse mockResponse = execute();
        mockResponse.expectStatus(401);
        return mockResponse;
    }
} 