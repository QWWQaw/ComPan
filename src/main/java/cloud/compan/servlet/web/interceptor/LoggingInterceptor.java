package cloud.compan.servlet.web.interceptor;

import cloud.compan.servlet.web.RouteInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志记录拦截器
 * 记录请求的详细信息、执行时间和响应状态
 */
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final String START_TIME_ATTRIBUTE = "request.start.time";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        
        // 获取请求信息
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        // 构建完整URL
        String fullUrl = uri;
        if (queryString != null && !queryString.isEmpty()) {
            fullUrl += "?" + queryString;
        }
        
        // 获取处理器信息
        String handlerInfo = "Unknown";
        if (handler instanceof RouteInfo) {
            RouteInfo routeInfo = (RouteInfo) handler;
            handlerInfo = String.format("%s.%s", 
                routeInfo.getControllerClass().getSimpleName(),
                routeInfo.getHandlerMethod().getName());
        }
        
        // 记录请求开始日志
        System.out.printf("┌─ [%s] Request started ─────────────────────────────────────%n", 
            LocalDateTime.now().format(TIME_FORMATTER));
        System.out.printf("│ %s %s%n", method, fullUrl);
        System.out.printf("│ Client IP: %s%n", remoteAddr);
        System.out.printf("│ Handler: %s%n", handlerInfo);
        if (userAgent != null) {
            System.out.printf("│ User-Agent: %s%n", userAgent.length() > 100 ? userAgent.substring(0, 100) + "..." : userAgent);
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, Exception exception) {
        // 记录响应状态
        int status = response.getStatus();
        String contentType = response.getContentType();
        
        System.out.printf("│ Response Status: %d%n", status);
        if (contentType != null) {
            System.out.printf("│ Content-Type: %s%n", contentType);
        }
        
        if (exception != null) {
            System.out.printf("│  Error: %s - %s%n", exception.getClass().getSimpleName(), exception.getMessage());
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception exception) {
        // 计算执行时间
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.printf("│   Execution time: %d ms%n", duration);
            
            // 根据执行时间添加性能提示
            if (duration > 1000) {
                System.out.printf("│ Slow request warning: execution time exceeds 1 second%n");
            } else if (duration > 500) {
                System.out.printf("│ Performance hint: execution time is longer%n");
            }
        }
        
        System.out.printf("└─ [%s] Request completed ─────────────────────────────────────%n%n", 
            LocalDateTime.now().format(TIME_FORMATTER));
    }
    
    /**
     * 获取客户端真实IP地址
     * 考虑代理服务器的情况
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // 尝试从各种Header中获取真实IP
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For可能包含多个IP，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0];
                }
                return ip.trim();
            }
        }
        
        // 如果都没有，返回远程地址
        return request.getRemoteAddr();
    }
} 