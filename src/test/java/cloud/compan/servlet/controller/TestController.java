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
 * 展示各种注解的用法和三层架构的实现
 */
@Controller
@RequestMapping(path = "/api")
public class TestController {
    
    @Inject
    private UserRepository userRepository;  // 注入数据层
    
    /**
     * 简单的GET请求示例
     * 访问: GET /api/hello
     */
    @GetMapping(path = "/hello")
    public String hello() {
        return "Hello, World! 路由系统正常工作！";
    }
    
    /**
     * 带参数的GET请求示例  
     * 访问: GET /api/status
     */
    @GetMapping(path = "/status")
    public String getStatus(HttpServletRequest request) {
        return "{\"status\":\"running\",\"message\":\"应用运行正常\",\"timestamp\":\"" + 
               System.currentTimeMillis() + "\"}";
    }
    
    /**
     * POST请求示例
     * 访问: POST /api/users
     */
    @PostMapping(path = "/users")
    public String createUser(HttpServletRequest request, HttpServletResponse response) {
        // 这里可以从request中解析用户数据
        String userInfo = "{\"message\":\"用户创建功能\",\"status\":\"ready\",\"id\":123}";
        response.setStatus(201); // Created
        return userInfo;
    }
    
    /**
     * PUT请求示例
     * 访问: PUT /api/users
     */
    @PutMapping(path = "/users")
    public String updateUser() {
        return "{\"message\":\"用户更新功能\",\"status\":\"ready\"}";
    }
    
    /**
     * DELETE请求示例
     * 访问: DELETE /api/users
     */
    @DeleteMapping(path = "/users")
    public String deleteUser() {
        return "{\"message\":\"用户删除功能\",\"status\":\"ready\"}";
    }
    
    /**
     * 通用RequestMapping示例，支持多种HTTP方法
     * 访问: GET/POST /api/multi
     */
    @RequestMapping(path = "/multi", method = {RequestMethod.GET, RequestMethod.POST})
    public String multiMethod(HttpServletRequest request) {
        String method = request.getMethod();
        return "{\"message\":\"支持多种HTTP方法\",\"current_method\":\"" + method + "\"}";
    }
    
    /**
     * 返回用户示例（JSON格式）
     * 访问: GET /api/user-demo
     */
    @GetMapping(path = "/user-demo")
    public String getUserDemo() {
        // 返回示例用户JSON（避免Lombok setter问题）
        return "{\"userId\":1,\"username\":\"demo_user\",\"email\":\"demo@example.com\"," +
               "\"storageLimit\":10737418240,\"storageUsed\":1073741824,\"message\":\"示例用户数据\"}";
    }
    
    /**
     * 测试服务层注入
     * 访问: GET /api/test-injection
     */
    @GetMapping(path = "/test-injection")
    public String testInjection() {
        // 测试UserRepository是否正确注入
        if (userRepository != null) {
            return "{\"message\":\"依赖注入测试成功\",\"repository\":\"已注入\"}";
        } else {
            return "{\"message\":\"依赖注入测试失败\",\"repository\":\"未注入\"}";
        }
    }
    
    /**
     * 根路径处理
     * 访问: GET /api 或 GET /api/
     */
    @GetMapping(path = "")
    public String apiRoot() {
        return "{\"message\":\"欢迎使用ComPan API\",\"version\":\"1.0\",\"routes\":[" +
               "\"/api/hello\",\"/api/status\",\"/api/users\",\"/api/multi\"," +
               "\"/api/user-demo\",\"/api/test-injection\"]}";
    }
} 