package cloud.compan.servlet.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.when;

/**
 * HTTP请求构建器
 * 用于创建模拟的HTTP请求，支持各种HTTP方法和参数
 */
public class MockHttpRequestBuilder {
    
    private String method = "GET";
    private String path = "/";
    private String queryString = "";
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();
    private String body = "";
    private String contentType = "application/json";
    private String characterEncoding = "UTF-8";
    
    public MockHttpRequestBuilder method(String method) {
        this.method = method.toUpperCase();
        return this;
    }
    
    public MockHttpRequestBuilder path(String path) {
        this.path = path;
        return this;
    }
    
    public MockHttpRequestBuilder queryString(String queryString) {
        this.queryString = queryString;
        return this;
    }
    
    public MockHttpRequestBuilder header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }
    
    public MockHttpRequestBuilder parameter(String name, String value) {
        this.parameters.put(name, value);
        return this;
    }
    
    public MockHttpRequestBuilder body(String body) {
        this.body = body;
        return this;
    }
    
    public MockHttpRequestBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
    
    public MockHttpRequestBuilder characterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        return this;
    }
    
    public MockHttpRequest build(HttpServletRequest request, 
                                HttpServletResponse response, 
                                RequestDispatcher requestDispatcher,
                                StringWriter responseWriter) {
        // 设置请求方法和路径
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(path);
        when(request.getQueryString()).thenReturn(queryString.isEmpty() ? null : queryString);
        
        // 设置请求头
        headers.forEach((name, value) -> when(request.getHeader(name)).thenReturn(value));
        
        // 设置请求参数
        parameters.forEach((name, value) -> when(request.getParameter(name)).thenReturn(value));
        
        // 设置Content-Type
        when(request.getContentType()).thenReturn(contentType);
        when(request.getCharacterEncoding()).thenReturn(characterEncoding);
        
        return new MockHttpRequest(request, response, requestDispatcher, responseWriter);
    }
    
    // 便捷方法
    public static MockHttpRequestBuilder get(String path) {
        return new MockHttpRequestBuilder().method("GET").path(path);
    }
    
    public static MockHttpRequestBuilder post(String path) {
        return new MockHttpRequestBuilder().method("POST").path(path);
    }
    
    public static MockHttpRequestBuilder put(String path) {
        return new MockHttpRequestBuilder().method("PUT").path(path);
    }
    
    public static MockHttpRequestBuilder delete(String path) {
        return new MockHttpRequestBuilder().method("DELETE").path(path);
    }
    
    public static MockHttpRequestBuilder patch(String path) {
        return new MockHttpRequestBuilder().method("PATCH").path(path);
    }
} 