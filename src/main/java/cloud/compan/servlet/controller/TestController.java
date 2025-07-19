package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.annotations.enums.RequestMethod;
import cloud.compan.servlet.model.User;
import cloud.compan.servlet.repository.UserRepository;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 测试控制器 - 演示路由分发系统的使用
 * 展示各种注解的用法、三层架构的实现以及错误处理
 * 模拟可用和不可用的各种情况
 */
@Controller
@RequestMapping(path = "/api")
public class TestController {
    
    @Inject
    private UserRepository userRepository;  // 注入数据层
    
    // ============ 基础功能路由 ============
    
    /**
     * API根路径 - 显示所有可用路由
     * 访问: GET /api 或 GET /api/
     */
    @GetMapping(path = "")
    public String apiRoot() {
        return "{" +
               "\"message\":\"欢迎使用ComPan API\"," +
               "\"version\":\"1.0\"," +
               "\"status\":\"running\"," +
               "\"timestamp\":" + System.currentTimeMillis() + "," +
               "\"availableRoutes\":[" +
                   "\"/api/hello\"," +
                   "\"/api/status\"," +
                   "\"/api/health\"," +
                   "\"/api/users\"," +
                   "\"/api/multi\"," +
                   "\"/api/user-demo\"," +
                   "\"/api/test-injection\"," +
                   "\"/api/error/*\"," +
                   "\"/api/validation/*\"," +
                   "\"/api/simulation/*\"" +
               "]" +
               "}";
    }
    
    /**
     * Hello World接口
     * 访问: GET /api/hello
     */
    @GetMapping(path = "/hello")
    public String hello() {
        return "{" +
               "\"message\":\"Hello, World!\"," +
               "\"greeting\":\"路由系统正常工作！\"," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    /**
     * 系统状态检查
     * 访问: GET /api/status
     */
    @GetMapping(path = "/status")
    public String getStatus(HttpServletRequest request) {
        return "{" +
               "\"status\":\"running\"," +
               "\"message\":\"应用运行正常\"," +
               "\"timestamp\":" + System.currentTimeMillis() + "," +
               "\"requestInfo\":{" +
                   "\"method\":\"" + request.getMethod() + "\"," +
                   "\"uri\":\"" + request.getRequestURI() + "\"," +
                   "\"remoteAddr\":\"" + request.getRemoteAddr() + "\"" +
               "}" +
               "}";
    }
    
    /**
     * 健康检查接口
     * 访问: GET /api/health
     */
    @GetMapping(path = "/health")
    public String health() {
        boolean isHealthy = userRepository != null; // 简单的健康检查
        String status = isHealthy ? "UP" : "DOWN";
        int statusCode = isHealthy ? 200 : 503;
        
        return "{" +
               "\"status\":\"" + status + "\"," +
               "\"message\":\"" + (isHealthy ? "服务正常运行" : "服务异常") + "\"," +
               "\"timestamp\":" + System.currentTimeMillis() + "," +
               "\"dependencies\":{" +
                   "\"userRepository\":\"" + (userRepository != null ? "OK" : "FAIL") + "\"" +
               "}" +
               "}";
    }
    
    // ============ CRUD操作示例 ============
    
    /**
     * 创建用户 - POST请求示例
     * 访问: POST /api/users
     */
    @PostMapping(path = "/users")
    public String createUser(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 模拟用户创建逻辑
            String userInfo = "{" +
                   "\"message\":\"用户创建成功\"," +
                   "\"status\":\"created\"," +
                   "\"id\":" + (System.currentTimeMillis() % 10000) + "," +
                   "\"timestamp\":" + System.currentTimeMillis() +
                   "}";
            response.setStatus(201); // Created
            return userInfo;
        } catch (Exception e) {
            response.setStatus(500);
            return "{\"error\":\"用户创建失败\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }
    
    /**
     * 更新用户 - PUT请求示例
     * 访问: PUT /api/users
     */
    @PutMapping(path = "/users")
    public String updateUser(HttpServletRequest request) {
        return "{" +
               "\"message\":\"用户更新功能\"," +
               "\"status\":\"ready\"," +
               "\"method\":\"" + request.getMethod() + "\"," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    /**
     * 删除用户 - DELETE请求示例
     * 访问: DELETE /api/users
     */
    @DeleteMapping(path = "/users")
    public String deleteUser() {
        return "{" +
               "\"message\":\"用户删除功能\"," +
               "\"status\":\"ready\"," +
               "\"warning\":\"这是一个模拟接口\"," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    /**
     * 获取用户列表 - GET请求示例
     * 访问: GET /api/users
     */
    @GetMapping(path = "/users")
    public String getUsers(HttpServletRequest request) {
        String page = request.getParameter("page");
        String size = request.getParameter("size");
        
        return "{" +
               "\"message\":\"用户列表\"," +
               "\"page\":" + (page != null ? page : "1") + "," +
               "\"size\":" + (size != null ? size : "10") + "," +
               "\"total\":100," +
               "\"data\":[" +
                   "{\"id\":1,\"username\":\"user1\",\"email\":\"user1@example.com\"}," +
                   "{\"id\":2,\"username\":\"user2\",\"email\":\"user2@example.com\"}" +
               "]," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    // ============ 多方法支持 ============
    
    /**
     * 多HTTP方法支持示例
     * 访问: GET/POST /api/multi
     */
    @RequestMapping(path = "/multi", method = {RequestMethod.GET, RequestMethod.POST})
    public String multiMethod(HttpServletRequest request) {
        String method = request.getMethod();
        return "{" +
               "\"message\":\"支持多种HTTP方法\"," +
               "\"current_method\":\"" + method + "\"," +
               "\"supported_methods\":[\"GET\",\"POST\"]," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    // ============ 数据演示接口 ============
    
    /**
     * 用户数据演示
     * 访问: GET /api/user-demo
     */
    @GetMapping(path = "/user-demo")
    public String getUserDemo() {
        return "{" +
               "\"userId\":1," +
               "\"username\":\"demo_user\"," +
               "\"email\":\"demo@example.com\"," +
               "\"storageLimit\":10737418240," +
               "\"storageUsed\":1073741824," +
               "\"profile\":{" +
                   "\"displayName\":\"演示用户\"," +
                   "\"avatar\":\"https://example.com/avatar.jpg\"," +
                   "\"createdAt\":\"2024-01-01T00:00:00Z\"" +
               "}," +
               "\"message\":\"示例用户数据\"," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    /**
     * 依赖注入测试
     * 访问: GET /api/test-injection
     */
    @GetMapping(path = "/test-injection")
    public String testInjection() {
        if (userRepository != null) {
            return "{" +
                   "\"message\":\"依赖注入测试成功\"," +
                   "\"repository\":\"已注入\"," +
                   "\"repositoryClass\":\"" + userRepository.getClass().getSimpleName() + "\"," +
                   "\"timestamp\":" + System.currentTimeMillis() +
                   "}";
        } else {
            return "{" +
                   "\"message\":\"依赖注入测试失败\"," +
                   "\"repository\":\"未注入\"," +
                   "\"error\":\"UserRepository为null\"," +
                   "\"timestamp\":" + System.currentTimeMillis() +
                   "}";
        }
    }
    
    // ============ 错误处理演示 ============
    
    /**
     * 模拟500错误
     * 访问: GET /api/error/500
     */
    @GetMapping(path = "/error/500")
    public String simulateError500() {
        throw new RuntimeException("这是一个模拟的500错误");
    }
    
    /**
     * 模拟空指针异常
     * 访问: GET /api/error/null
     */
    @GetMapping(path = "/error/null")
    public String simulateNullPointer() {
        String nullString = null;
        return nullString.toString(); // 这会抛出NullPointerException
    }
    
    /**
     * 模拟数据验证错误
     * 访问: GET /api/error/validation
     */
    @GetMapping(path = "/error/validation")
    public String simulateValidationError(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(400); // Bad Request
        return "{" +
               "\"error\":\"Validation Error\"," +
               "\"message\":\"模拟的数据验证错误\"," +
               "\"details\":[" +
                   "\"用户名不能为空\"," +
                   "\"邮箱格式不正确\"" +
               "]," +
               "\"status\":400," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    /**
     * 模拟权限错误
     * 访问: GET /api/error/forbidden
     */
    @GetMapping(path = "/error/forbidden")
    public String simulateForbidden(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(403); // Forbidden
        return "{" +
               "\"error\":\"Forbidden\"," +
               "\"message\":\"没有权限访问此资源\"," +
               "\"status\":403," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    // ============ 参数验证演示 ============
    
    /**
     * 参数验证 - 必需参数
     * 访问: GET /api/validation/required?name=test
     */
    @GetMapping(path = "/validation/required")
    public String validateRequired(HttpServletRequest request, HttpServletResponse response) {
        String name = request.getParameter("name");
        
        if (name == null || name.trim().isEmpty()) {
            response.setStatus(400);
            return "{" +
                   "\"error\":\"Bad Request\"," +
                   "\"message\":\"参数'name'是必需的\"," +
                   "\"status\":400," +
                   "\"timestamp\":" + System.currentTimeMillis() +
                   "}";
        }
        
        return "{" +
               "\"message\":\"参数验证通过\"," +
               "\"name\":\"" + name + "\"," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    /**
     * 数字参数验证
     * 访问: GET /api/validation/number?id=123
     */
    @GetMapping(path = "/validation/number")
    public String validateNumber(HttpServletRequest request, HttpServletResponse response) {
        String idStr = request.getParameter("id");
        
        if (idStr == null) {
            response.setStatus(400);
            return "{\"error\":\"参数'id'是必需的\",\"status\":400}";
        }
        
        try {
            int id = Integer.parseInt(idStr);
            if (id <= 0) {
                response.setStatus(400);
                return "{\"error\":\"参数'id'必须是正整数\",\"status\":400}";
            }
            
            return "{" +
                   "\"message\":\"数字参数验证通过\"," +
                   "\"id\":" + id + "," +
                   "\"timestamp\":" + System.currentTimeMillis() +
                   "}";
        } catch (NumberFormatException e) {
            response.setStatus(400);
            return "{" +
                   "\"error\":\"参数'id'必须是有效的数字\"," +
                   "\"provided\":\"" + idStr + "\"," +
                   "\"status\":400," +
                   "\"timestamp\":" + System.currentTimeMillis() +
                   "}";
        }
    }
    
    // ============ 响应时间模拟 ============
    
    /**
     * 快速响应模拟
     * 访问: GET /api/simulation/fast
     */
    @GetMapping(path = "/simulation/fast")
    public String simulateFast() {
        return "{" +
               "\"message\":\"快速响应\"," +
               "\"responseTime\":\"< 10ms\"," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    /**
     * 慢速响应模拟
     * 访问: GET /api/simulation/slow
     */
    @GetMapping(path = "/simulation/slow")
    public String simulateSlow() {
        try {
            Thread.sleep(2000); // 模拟2秒延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "{" +
               "\"message\":\"慢速响应\"," +
               "\"responseTime\":\"~2000ms\"," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
    
    /**
     * 随机响应模拟
     * 访问: GET /api/simulation/random
     */
    @GetMapping(path = "/simulation/random")
    public String simulateRandom(HttpServletResponse response) {
        int random = (int) (Math.random() * 100);
        
        if (random < 10) {
            // 10% 概率返回错误
            response.setStatus(500);
            return "{\"error\":\"随机错误\",\"probability\":\"10%\",\"status\":500}";
        } else if (random < 30) {
            // 20% 概率返回客户端错误
            response.setStatus(400);
            return "{\"error\":\"随机客户端错误\",\"probability\":\"20%\",\"status\":400}";
        } else {
            // 70% 概率正常返回
            return "{" +
                   "\"message\":\"随机响应成功\"," +
                   "\"probability\":\"70%\"," +
                   "\"randomValue\":" + random + "," +
                   "\"timestamp\":" + System.currentTimeMillis() +
                   "}";
        }
    }
    
    // ============ 内容类型测试 ============
    
    /**
     * 纯文本响应
     * 访问: GET /api/content/text
     */
    @GetMapping(path = "/content/text")
    public String responseText(HttpServletResponse response) {
        response.setContentType("text/plain;charset=UTF-8");
        return "这是一个纯文本响应，时间戳: " + System.currentTimeMillis();
    }
    
    /**
     * HTML响应
     * 访问: GET /api/content/html
     */
    @GetMapping(path = "/content/html")
    public String responseHtml(HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
        return "<html><body>" +
               "<h1>ComPan API 测试页面</h1>" +
               "<p>当前时间: " + new java.util.Date() + "</p>" +
               "<p>这是一个HTML响应示例</p>" +
               "</body></html>";
    }
    
    // ============ Echo和调试功能 ============
    
    /**
     * Echo服务 - 回显请求信息
     * 访问: GET/POST /api/echo
     */
    @RequestMapping(path = "/echo", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
    public String echo(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.append("\"").append(headerName).append("\":\"").append(request.getHeader(headerName)).append("\",");
        }
        
        if (headers.length() > 0) {
            headers.setLength(headers.length() - 1); // 移除最后的逗号
        }
        
        return "{" +
               "\"message\":\"Echo服务\"," +
               "\"method\":\"" + request.getMethod() + "\"," +
               "\"path\":\"" + request.getRequestURI() + "\"," +
               "\"queryString\":\"" + (request.getQueryString() != null ? request.getQueryString() : "") + "\"," +
               "\"headers\":{" + headers.toString() + "}," +
               "\"remoteAddr\":\"" + request.getRemoteAddr() + "\"," +
               "\"timestamp\":" + System.currentTimeMillis() +
               "}";
    }
} 