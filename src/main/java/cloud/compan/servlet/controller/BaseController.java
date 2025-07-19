// package cloud.compan.servlet.controller;

// import cloud.compan.servlet.annotations.*;
// import cloud.compan.servlet.annotations.enums.RequestMethod;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// /**
//  * 基础控制器 - 提供基本的API接口
//  * 演示基础的HTTP请求处理和响应格式
//  */
// @Controller
// @RequestMapping(path = "/api")
// public class BaseController {
    
//     /**
//      * API根路径 - 显示API信息
//      * 访问: GET /api
//      */
//     @GetMapping(path = "")
//     public String apiInfo() {
//         return "{" +
//                "\"message\":\"ComPan API服务\"," +
//                "\"version\":\"1.0\"," +
//                "\"status\":\"running\"," +
//                "\"timestamp\":" + System.currentTimeMillis() + "," +
//                "\"endpoints\":[" +
//                    "\"/api/health\"," +
//                    "\"/api/hello\"," +
//                    "\"/api/status\"" +
//                "]" +
//                "}";
//     }
    
//     /**
//      * 健康检查接口
//      * 访问: GET /api/health
//      */
//     @GetMapping(path = "/health")
//     public String health() {
//         return "{" +
//                "\"status\":\"UP\"," +
//                "\"message\":\"服务正常运行\"," +
//                "\"timestamp\":" + System.currentTimeMillis() + "," +
//                "\"uptime\":\"" + getUptime() + "\"" +
//                "}";
//     }
    
//     /**
//      * Hello World接口
//      * 访问: GET /api/hello
//      */
//     @GetMapping(path = "/hello")
//     public String hello() {
//         return "{" +
//                "\"message\":\"Hello, World!\"," +
//                "\"greeting\":\"欢迎使用ComPan API\"," +
//                "\"timestamp\":" + System.currentTimeMillis() +
//                "}";
//     }
    
//     /**
//      * 系统状态接口
//      * 访问: GET /api/status
//      */
//     @GetMapping(path = "/status")
//     public String status(HttpServletRequest request) {
//         return "{" +
//                "\"status\":\"running\"," +
//                "\"message\":\"系统运行正常\"," +
//                "\"timestamp\":" + System.currentTimeMillis() + "," +
//                "\"requestInfo\":{" +
//                    "\"method\":\"" + request.getMethod() + "\"," +
//                    "\"uri\":\"" + request.getRequestURI() + "\"," +
//                    "\"userAgent\":\"" + request.getHeader("User-Agent") + "\"" +
//                "}" +
//                "}";
//     }
    
//     /**
//      * Echo接口 - 回显请求信息
//      * 访问: GET/POST /api/echo
//      */
//     @RequestMapping(path = "/echo", method = {RequestMethod.GET, RequestMethod.POST})
//     public String echo(HttpServletRequest request) {
//         String method = request.getMethod();
//         String queryString = request.getQueryString();
        
//         return "{" +
//                "\"message\":\"Echo服务\"," +
//                "\"method\":\"" + method + "\"," +
//                "\"path\":\"" + request.getRequestURI() + "\"," +
//                "\"queryString\":\"" + (queryString != null ? queryString : "") + "\"," +
//                "\"timestamp\":" + System.currentTimeMillis() +
//                "}";
//     }
    
//     /**
//      * 简单的POST接口示例
//      * 访问: POST /api/simple
//      */
//     @PostMapping(path = "/simple")
//     public String simplePost(HttpServletRequest request, HttpServletResponse response) {
//         response.setStatus(201); // Created
//         return "{" +
//                "\"message\":\"POST请求处理成功\"," +
//                "\"status\":\"created\"," +
//                "\"timestamp\":" + System.currentTimeMillis() +
//                "}";
//     }
    
//     /**
//      * 获取系统运行时间（简化版本）
//      */
//     private String getUptime() {
//         long uptimeMs = System.currentTimeMillis() % 86400000; // 简化计算
//         long hours = uptimeMs / 3600000;
//         long minutes = (uptimeMs % 3600000) / 60000;
//         return hours + "h " + minutes + "m";
//     }
// } 