package cloud.compan.servlet.controller;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PostMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RestController;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.UserDTO;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.utils.ControllerUtils;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 测试控制器
 * 用于测试和验证API功能
 */
@RestController("/api/v1/test")
@Singleton
public class TestController {
    
    @Inject
    private AuthService authService;
    
    /**
     * 健康检查
     * GET /api/test/health
     */
    @GetMapping(path = "/health")
    public ApiResponseWrapper health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "OK");
        data.put("timestamp", System.currentTimeMillis());
        data.put("version", "1.0.0");
        return ControllerUtils.success("Service is healthy", data);
    }
    
    /**
     * 测试用户注册
     * POST /api/test/register
     */
    @PostMapping(path = "/register")
    public ApiResponseWrapper testRegister(@RequestBody Map<String, Object> requestData) {
        String username = (String) requestData.get("username");
        String email = (String) requestData.get("email");
        String password = (String) requestData.get("password");
        
        if (username == null || email == null || password == null) {
            return ControllerUtils.error(400, "Missing required fields: username, email, password");
        }
        
        ServiceResult<UserDTO> result = authService.register(username, email, password);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 测试用户登录
     * POST /api/test/login
     */
    @PostMapping(path = "/login")
    public ApiResponseWrapper testLogin(@RequestBody Map<String, Object> requestData) {
        String username = (String) requestData.get("username");
        String password = (String) requestData.get("password");
        
        if (username == null || password == null) {
            return ControllerUtils.error(400, "Missing required fields: username, password");
        }
        
        ServiceResult<cloud.compan.servlet.dto.LoginResultDTO> result = authService.login(username, password);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 测试Token验证
     * GET /api/test/validate-token
     */
    @GetMapping(path = "/validate-token")
    public ApiResponseWrapper testValidateToken(HttpServletRequest request) {
        String token = ControllerUtils.extractTokenFromRequest(request);
        if (token == null) {
            return ControllerUtils.error(401, "No token provided");
        }
        
        ServiceResult<UserDTO> result = authService.validateToken(token);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取系统信息
     * GET /api/test/system-info
     */
    @GetMapping(path = "/system-info")
    public ApiResponseWrapper getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("java.version", System.getProperty("java.version"));
        info.put("java.vendor", System.getProperty("java.vendor"));
        info.put("os.name", System.getProperty("os.name"));
        info.put("os.version", System.getProperty("os.version"));
        info.put("user.timezone", System.getProperty("user.timezone"));
        info.put("memory.total", Runtime.getRuntime().totalMemory());
        info.put("memory.free", Runtime.getRuntime().freeMemory());
        info.put("memory.used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        
        return ControllerUtils.success("System information retrieved", info);
    }
    

}