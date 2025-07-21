package cloud.compan.servlet.controller;

import cloud.compan.servlet.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TestController {

    private UserRepository userRepository;

    public TestController() {
        // 默认构造函数
    }

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String hello() {
        return "{\"message\":\"Hello, World!\"," +
                "\"greeting\":\"路由系统正常工作！\"," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
    }

    public String getStatus(HttpServletRequest request) {
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("method", request.getMethod());
        requestInfo.put("uri", request.getRequestURI());
        requestInfo.put("remoteAddr", request.getRemoteAddr());
        requestInfo.put("parameters", request.getParameterMap());

        return "{\"status\":\"running\"," +
                "\"message\":\"应用运行正常\"," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"," +
                "\"requestInfo\":" + toJson(requestInfo) + "}";
    }

    public String health() {
        Map<String, Object> dependencies = new HashMap<>();
        dependencies.put("database", "connected");
        dependencies.put("cache", "available");
        dependencies.put("storage", "online");

        return "{\"status\":\"healthy\"," +
                "\"message\":\"所有系统运行正常\"," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"," +
                "\"dependencies\":" + toJson(dependencies) + "}";
    }

    public String getUserDemo() {
        return "{\"userId\":1," +
                "\"username\":\"demo_user\"," +
                "\"email\":\"demo@example.com\"," +
                "\"profile\":{\"age\":30,\"location\":\"New York\"}," +
                "\"message\":\"示例用户数据\"," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
    }

    public String apiRoot() {
        Map<String, String> routes = new HashMap<>();
        routes.put("/api/hello", "GET - 欢迎接口");
        routes.put("/api/status", "GET - 应用状态");
        routes.put("/api/users", "GET - 用户列表");
        routes.put("/api/users/create", "POST - 创建用户");
        routes.put("/api/users/update", "PUT - 更新用户");
        routes.put("/api/users/delete", "DELETE - 删除用户");

        return "{\"message\":\"欢迎使用ComPan API\"," +
                "\"version\":\"1.0\"," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"," +
                "\"availableRoutes\":" + toJson(routes) + "}";
    }

    public String createUser(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_CREATED);
        return "{\"message\":\"用户创建成功\"," +
                "\"status\":\"created\"," +
                "\"id\":1001," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
    }

    public String updateUser(HttpServletRequest request) {
        return "{\"message\":\"用户更新功能\"," +
                "\"status\":\"ready\"," +
                "\"method\":\"" + request.getMethod() + "\"," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
    }

    public String deleteUser() {
        return "{\"message\":\"用户删除功能\"," +
                "\"status\":\"ready\"," +
                "\"warning\":\"这是一个模拟接口\"," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
    }

    public String getUsers(HttpServletRequest request) {
        int page = Integer.parseInt(request.getParameter("page"));
        int size = Integer.parseInt(request.getParameter("size"));

        Map<String, Object> data = new HashMap<>();
        data.put("message", "用户列表");
        data.put("page", page);
        data.put("size", size);
        data.put("total", 100);
        data.put("data", Collections.singletonList(
                Map.of("id", 1, "name", "User1", "email", "user1@example.com")
        ));

        return toJson(data);
    }

    public String multiMethod(HttpServletRequest request) {
        Map<String, String> supportedMethods = new HashMap<>();
        supportedMethods.put("GET", "获取资源");
        supportedMethods.put("POST", "创建资源");
        supportedMethods.put("PUT", "更新资源");
        supportedMethods.put("DELETE", "删除资源");

        return "{\"message\":\"支持多种HTTP方法\"," +
                "\"current_method\":\"" + request.getMethod() + "\"," +
                "\"supported_methods\":" + toJson(supportedMethods) + "," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
    }

    public String testInjection() {
        String repoStatus = (userRepository != null) ? "已注入" : "未注入";
        return "{\"message\":\"依赖注入测试\"," +
                "\"repository\":\"" + repoStatus + "\"," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
    }

    public void simulateError500() {
        throw new RuntimeException("模拟500服务器错误");
    }

    public void simulateNullPointer() {
        throw new NullPointerException("模拟空指针异常");
    }

    public String simulateValidationError(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return "{\"error\":\"Validation Error\"," +
                "\"message\":\"请求参数验证失败\"," +
                "\"status\":400," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
    }

    public String validateRequired(HttpServletRequest request, HttpServletResponse response) {
        String name = request.getParameter("name");
        if (name == null || name.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "{\"error\":\"Bad Request\"," +
                    "\"message\":\"缺少必填参数: name\"," +
                    "\"status\":400," +
                    "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
        }

        return "{\"message\":\"参数验证通过\"," +
                "\"name\":\"" + name + "\"," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
    }

    public String simulateFast() {
        return "{\"message\":\"快速响应\"," +
                "\"responseTime\":\"< 10ms\"," +
                "\"timestamp\":\"" + getCurrentTimestamp() + "\"}";
    }

    public String echo(HttpServletRequest request) {
        Map<String, Object> echoData = new HashMap<>();
        echoData.put("message", "Echo服务");
        echoData.put("method", request.getMethod());
        echoData.put("uri", request.getRequestURI());
        echoData.put("queryString", request.getQueryString());

        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        echoData.put("headers", headers);

        return toJson(echoData);
    }

    // 辅助方法：获取当前时间戳
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    // 简单的JSON序列化方法（实际项目中应使用JSON库）
    private String toJson(Object data) {
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            return "{" + map.entrySet().stream()
                    .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
                    .collect(Collectors.joining(",")) + "}";
        } else if (data instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) data;
            return "[" + iterable.iterator().next().toString() + "]";
        }
        return "\"" + data.toString() + "\"";
    }
}
