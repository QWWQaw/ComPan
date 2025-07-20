package cloud.compan.servlet.web;

import cloud.compan.servlet.web.response.ApiResponseWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 模拟HTTP响应，支持响应断言
 */
public class MockHttpResponse {
    
    private final HttpServletResponse response;
    private final StringWriter responseWriter;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public MockHttpResponse(HttpServletResponse response, StringWriter responseWriter) {
        this.response = response;
        this.responseWriter = responseWriter;
    }
    
    /**
     * 验证HTTP状态码
     */
    public MockHttpResponse expectStatus(int expectedStatus) {
        verify(response).setStatus(expectedStatus);
        return this;
    }
    
    /**
     * 验证响应内容类型
     */
    public MockHttpResponse expectContentType(String expectedContentType) {
        verify(response).setContentType(expectedContentType);
        return this;
    }
    
    /**
     * 获取响应体内容
     */
    public String getResponseBody() {
        return responseWriter.toString();
    }
    
    /**
     * 验证响应体包含指定文本
     */
    public MockHttpResponse expectBodyContains(String expectedText) {
        String responseBody = getResponseBody();
        assertTrue(responseBody.contains(expectedText), 
            "Response body should contain: " + expectedText + ", but was: " + responseBody);
        return this;
    }
    
    /**
     * 验证JSON响应结构
     */
    public MockHttpResponse expectJsonPath(String jsonPath, Object expectedValue) {
        // 简化实现，实际项目中可以使用JsonPath库
        String responseBody = getResponseBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertTrue(responseBody.contains("\"" + jsonPath + "\""), 
            "Response should contain JSON path: " + jsonPath);
        return this;
    }
    
    /**
     * 解析响应为ApiResponseWrapper
     */
    public ApiResponseWrapper parseAsApiResponse() {
        try {
            String responseBody = getResponseBody();
            return objectMapper.readValue(responseBody, ApiResponseWrapper.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse API response", e);
        }
    }
    
    /**
     * 验证API响应成功
     */
    public MockHttpResponse expectApiSuccess() {
        ApiResponseWrapper apiResponse = parseAsApiResponse();
        assertTrue(apiResponse.isSuccess(), "API response should be successful");
        return this;
    }
    
    /**
     * 验证API响应失败
     */
    public MockHttpResponse expectApiError() {
        ApiResponseWrapper apiResponse = parseAsApiResponse();
        assertFalse(apiResponse.isSuccess(), "API response should be error");
        return this;
    }
    
    /**
     * 验证API响应消息
     */
    public MockHttpResponse expectApiMessage(String expectedMessage) {
        ApiResponseWrapper apiResponse = parseAsApiResponse();
        assertEquals(expectedMessage, apiResponse.getMessage(), 
            "API response message should match");
        return this;
    }
    
    /**
     * 获取API响应数据
     */
    public <T> T getApiData(Class<T> dataType) {
        ApiResponseWrapper apiResponse = parseAsApiResponse();
        if (apiResponse.getData() == null) {
            return null;
        }
        return objectMapper.convertValue(apiResponse.getData(), dataType);
    }
    
    /**
     * 打印响应内容（用于调试）
     */
    public MockHttpResponse printResponse() {
        System.out.println("Response Body: " + getResponseBody());
        return this;
    }
} 