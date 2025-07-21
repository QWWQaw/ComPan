package cloud.compan.servlet.controller;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.DeleteMapping;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PatchMapping;
import cloud.compan.servlet.annotations.PathVariable;
import cloud.compan.servlet.annotations.PostMapping;
import cloud.compan.servlet.annotations.PutMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RequestMapping;
import cloud.compan.servlet.annotations.RequestParam;
import cloud.compan.servlet.annotations.RestController;
import cloud.compan.servlet.annotations.enums.RequestMethod;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 演示控制器
 * 展示所有HTTP映射注解的使用方法
 */
@RestController("/api/demo")
@Singleton
public class DemoController extends BaseController {
    
    /**
     * GET请求演示
     * GET /api/demo/get-example
     */
    @GetMapping(path = "/get-example")
    public ApiResponseWrapper getExample() {
        return success("GET请求处理成功", Map.of(
            "method", "GET",
            "description", "用于获取资源",
            "example", "获取用户列表、获取文件信息等"
        ));
    }
    
    /**
     * POST请求演示
     * POST /api/demo/post-example
     */
    @PostMapping(path = "/post-example")
    public ApiResponseWrapper postExample(@RequestBody Map<String, Object> data) {
        return created("POST请求处理成功", Map.of(
            "method", "POST",
            "description", "用于创建新资源",
            "receivedData", data,
            "example", "创建用户、上传文件等"
        ));
    }
    
    /**
     * PUT请求演示
     * PUT /api/demo/put-example/{id}
     */
    @PutMapping(path = "/put-example/{id}")
    public ApiResponseWrapper putExample(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        return success("PUT请求处理成功", Map.of(
            "method", "PUT",
            "description", "用于完整更新资源",
            "resourceId", id,
            "updateData", data,
            "example", "更新用户完整信息、替换文件等"
        ));
    }
    
    /**
     * DELETE请求演示
     * DELETE /api/demo/delete-example/{id}
     */
    @DeleteMapping(path = "/delete-example/{id}")
    public ApiResponseWrapper deleteExample(@PathVariable Long id) {
        return success("DELETE请求处理成功", Map.of(
            "method", "DELETE",
            "description", "用于删除资源",
            "deletedResourceId", id,
            "example", "删除用户、删除文件等"
        ));
    }
    
    /**
     * PATCH请求演示
     * PATCH /api/demo/patch-example/{id}
     */
    @PatchMapping(path = "/patch-example/{id}")
    public ApiResponseWrapper patchExample(@PathVariable Long id, @RequestBody Map<String, Object> partialData) {
        return success("PATCH请求处理成功", Map.of(
            "method", "PATCH",
            "description", "用于部分更新资源",
            "resourceId", id,
            "partialUpdateData", partialData,
            "example", "更新用户密码、修改文件名等"
        ));
    }
    
    /**
     * 通用RequestMapping演示 - 支持多种HTTP方法
     * GET/POST /api/demo/multi-method
     */
    @RequestMapping(path = "/multi-method", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResponseWrapper multiMethodExample(HttpServletRequest request, 
                                                 @RequestBody(required = false) Map<String, Object> data) {
        String httpMethod = request.getMethod();
        
        if ("GET".equals(httpMethod)) {
            return success("GET方法处理", Map.of(
                "method", "GET",
                "description", "通过RequestMapping处理的GET请求"
            ));
        } else if ("POST".equals(httpMethod)) {
            return success("POST方法处理", Map.of(
                "method", "POST",
                "description", "通过RequestMapping处理的POST请求",
                "data", data
            ));
        } else {
            return error("不支持的HTTP方法: " + httpMethod);
        }
    }
    
    /**
     * 路径变量和请求参数组合演示
     * GET /api/demo/complex/{category}?search=keyword&page=1
     */
    @GetMapping(path = "/complex/{category}")
    public ApiResponseWrapper complexExample(@PathVariable String category,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return success("复杂参数处理成功", Map.of(
            "category", category,
            "search", search != null ? search : "无搜索条件",
            "page", page,
            "size", size,
            "description", "展示路径变量和请求参数的组合使用"
        ));
    }
    
    /**
     * HTTP方法总览
     * GET /api/demo/methods-overview
     */
    @GetMapping(path = "/methods-overview")
    public ApiResponseWrapper methodsOverview() {
        return success("HTTP方法总览", Map.of(
            "GET", "获取资源 - 幂等、安全",
            "POST", "创建资源 - 非幂等",
            "PUT", "完整更新资源 - 幂等",
            "DELETE", "删除资源 - 幂等",
            "PATCH", "部分更新资源 - 非幂等",
            "OPTIONS", "获取资源支持的方法 - 安全",
            "HEAD", "获取资源头信息 - 幂等、安全",
            "supportedMappings", Map.of(
                "@GetMapping", "GET请求映射",
                "@PostMapping", "POST请求映射",
                "@PutMapping", "PUT请求映射",
                "@DeleteMapping", "DELETE请求映射",
                "@PatchMapping", "PATCH请求映射",
                "@RequestMapping", "通用请求映射（支持多种HTTP方法）"
            )
        ));
    }
} 