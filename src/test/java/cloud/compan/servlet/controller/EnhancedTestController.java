package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.annotations.enums.RequestMethod;
import cloud.compan.servlet.model.User;
import cloud.compan.servlet.repository.UserRepository;
import cloud.compan.servlet.utils.JsonUtils;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.ArrayList;

/**
 * 增强版测试控制器 - 演示新的参数解析功能
 * 展示@RequestBody、@PathVariable、@RequestParam等注解的用法
 */
@Controller
@RequestMapping(path = "/api/v2")
public class EnhancedTestController {
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private JsonUtils jsonUtils;
    
    // ============ @PathVariable 演示 ============
    
    /**
     * 单个路径参数示例
     * 访问: GET /api/v2/users/123
     */
    @GetMapping(path = "/users/{id}")
    public String getUserById(@PathVariable("id") Long userId) {
        return "{\"message\":\"获取用户\",\"userId\":" + userId + ",\"type\":\"PathVariable示例\"}";
    }
    
    /**
     * 多个路径参数示例
     * 访问: GET /api/v2/users/123/posts/456
     */
    @GetMapping(path = "/users/{userId}/posts/{postId}")
    public String getUserPost(@PathVariable("userId") Long userId, 
                             @PathVariable("postId") Long postId) {
        return String.format("{\"message\":\"获取用户文章\",\"userId\":%d,\"postId\":%d,\"type\":\"多路径参数\"}", 
                           userId, postId);
    }
    
    /**
     * 路径参数 + 查询参数组合
     * 访问: GET /api/v2/users/123/profile?includeStats=true
     */
    @GetMapping(path = "/users/{id}/profile")
    public String getUserProfile(@PathVariable Long id, 
                                @RequestParam(value = "includeStats", defaultValue = "false") boolean includeStats) {
        String stats = includeStats ? ",\"stats\":{\"loginCount\":42,\"lastLogin\":\"2024-01-01\"}" : "";
        return "{\"userId\":" + id + ",\"profile\":{\"name\":\"测试用户\",\"email\":\"test@example.com\"}" + stats + "}";
    }
    
    // ============ @RequestParam 演示 ============
    
    /**
     * 查询参数示例 - 分页查询
     * 访问: GET /api/v2/users?page=1&size=10&search=john
     */
    @GetMapping(path = "/users")
    public String getUsers(@RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "size", defaultValue = "10") int size,
                          @RequestParam(required = false) String search) {
        String searchPart = search != null ? ",\"search\":\"" + search + "\"" : "";
        return String.format("{\"message\":\"用户列表\",\"page\":%d,\"size\":%d%s,\"total\":100}", 
                           page, size, searchPart);
    }
    
    /**
     * 必需和可选参数混合
     * 访问: GET /api/v2/search?q=java&category=tech&sort=date
     */
    @GetMapping(path = "/search")
    public String search(@RequestParam("q") String query,
                        @RequestParam(value = "category", defaultValue = "all") String category,
                        @RequestParam(value = "sort", defaultValue = "relevance") String sort,
                        @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return String.format("{\"query\":\"%s\",\"category\":\"%s\",\"sort\":\"%s\",\"limit\":%d,\"results\":[]}", 
                           query, category, sort, limit);
    }
    
    // ============ @RequestBody 演示 ============
    
    /**
     * JSON请求体示例 - 创建用户
     * 访问: POST /api/v2/users
     * Content-Type: application/json
     * Body: {"username":"john","email":"john@example.com","age":25}
     */
    @PostMapping(path = "/users")
    public String createUser(@RequestBody User user, HttpServletResponse response) {
        response.setStatus(201); // Created
        
        // 模拟保存用户并返回结果
        Long newUserId = System.currentTimeMillis() % 10000; // 简单的ID生成
        
        // 使用JsonUtils序列化用户对象，然后手动构建响应
        String userJson = jsonUtils.toJson(user);
        return String.format("{\"message\":\"用户创建成功\",\"userId\":%d,\"userData\":%s}", 
                           newUserId, userJson);
    }
    
    /**
     * 更新用户信息
     * 访问: PUT /api/v2/users/123
     * Content-Type: application/json  
     * Body: {"username":"john_updated","email":"john.new@example.com"}
     */
    @PutMapping(path = "/users/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody User user) {
        // 使用JsonUtils序列化用户对象
        String userJson = jsonUtils.toJson(user);
        return String.format("{\"message\":\"用户更新成功\",\"userId\":%d,\"userData\":%s}", 
                           id, userJson);
    }
    
    /**
     * 复杂的请求处理 - 组合所有参数类型
     * 访问: POST /api/v2/users/123/posts?category=tech&published=true
     * Content-Type: application/json
     * Body: {"title":"Spring Boot教程","content":"这是一篇关于Spring Boot的教程..."}
     */
    @PostMapping(path = "/users/{userId}/posts")
    public String createUserPost(@PathVariable("userId") Long userId,
                                @RequestParam("category") String category,
                                @RequestParam(value = "published", defaultValue = "false") boolean published,
                                @RequestBody PostRequest postRequest,
                                HttpServletResponse response) {
        response.setStatus(201);
        
        Long postId = System.currentTimeMillis() % 10000;
        
        return String.format("{\"message\":\"文章创建成功\",\"postId\":%d,\"userId\":%d," +
                           "\"title\":\"%s\",\"category\":\"%s\",\"published\":%b}", 
                           postId, userId, postRequest.getTitle(), category, published);
    }
    
    // ============ 数据传输对象 ============
    
    /**
     * 简单的文章请求DTO
     */
    public static class PostRequest {
        private String title;
        private String content;
        private String tags;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }
    
    // ============ 错误处理演示 ============
    
    /**
     * 参数验证失败示例
     * 访问: GET /api/v2/validate/users/abc (abc不是有效的数字)
     */
    @GetMapping(path = "/validate/users/{id}")
    public String validateUser(@PathVariable("id") Long userId) {
        return "{\"message\":\"用户ID验证通过\",\"userId\":" + userId + "}";
    }
    
    /**
     * 必需参数缺失示例  
     * 访问: GET /api/v2/validate/search (缺少必需的q参数)
     */
    @GetMapping(path = "/validate/search")
    public String validateSearch(@RequestParam("q") String query) {
        return "{\"message\":\"搜索参数验证通过\",\"query\":\"" + query + "\"}";
    }
    
    // ============ 兼容性测试 ============
    
    /**
     * 传统方式仍然支持
     * 访问: GET /api/v2/legacy/info
     */
    @GetMapping(path = "/legacy/info")
    public String legacyInfo(HttpServletRequest request, HttpServletResponse response) {
        String userAgent = request.getHeader("User-Agent");
        return "{\"message\":\"传统方式仍然支持\",\"userAgent\":\"" + 
               (userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "unknown") + "\"}";
    }
    
    /**
     * API信息和使用说明
     * 访问: GET /api/v2 或 GET /api/v2/
     */
    @GetMapping(path = "")
    public String apiInfo() {
        return "{" +
               "\"message\":\"增强版API接口\"," +
               "\"version\":\"2.0\"," +
               "\"features\":[\"@PathVariable\",\"@RequestParam\",\"@RequestBody\"]," +
               "\"examples\":{" +
                   "\"pathVariable\":\"/api/v2/users/123\"," +
                   "\"requestParam\":\"/api/v2/users?page=1&size=10\"," +
                   "\"requestBody\":\"POST /api/v2/users + JSON body\"," +
                   "\"combined\":\"POST /api/v2/users/123/posts?category=tech + JSON body\"" +
               "}" +
               "}";
    }
    
    // ============ JsonUtils 功能演示 ============
    
    /**
     * 演示JsonUtils的序列化功能
     * 访问: GET /api/v2/json/demo
     */
    @GetMapping(path = "/json/demo")
    public SimpleUser jsonDemo() {
        // 创建一个示例对象
        SimpleUser user = new SimpleUser(999L, "jsonDemo", "demo@example.com");
        
        // 直接返回对象，RequestDispatcher会自动使用JsonUtils序列化
        return user;
    }
    
    /**
     * 演示JsonUtils工具方法
     * 访问: GET /api/v2/json/utils
     */
    @GetMapping(path = "/json/utils")
    public String jsonUtilsDemo() {
        // 创建测试数据
        SimpleUser user = new SimpleUser(888L, "utilsDemo", "utils@example.com");
        
        // 使用JsonUtils手动序列化
        String userJson = jsonUtils.toJson(user);
        
        // 演示JsonUtils的其他功能
        boolean isValid = jsonUtils.isValidJson(userJson);
        String prettyJson = jsonUtils.prettyJson(userJson);
        
        // 返回结果
        return jsonUtils.toJson(new JsonDemoResult(userJson, isValid, prettyJson));
    }
    
    /**
     * 简单用户类 - 用于JsonUtils演示
     */
    public static class SimpleUser {
        private Long id;
        private String username;
        private String email;
        
        public SimpleUser(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        
        // Setters
        public void setId(Long id) { this.id = id; }
        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email) { this.email = email; }
    }
    
    /**
     * 内部类：演示结果
     */
    public static class JsonDemoResult {
        private String originalJson;
        private boolean isValidJson;
        private String prettyJson;
        
        public JsonDemoResult(String originalJson, boolean isValidJson, String prettyJson) {
            this.originalJson = originalJson;
            this.isValidJson = isValidJson;
            this.prettyJson = prettyJson;
        }
        
        // Getters
        public String getOriginalJson() { return originalJson; }
        public boolean isValidJson() { return isValidJson; }
        public String getPrettyJson() { return prettyJson; }
    }
} 