package cloud.compan.servlet.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理器拦截器接口
 * 提供请求处理前后的拦截点，可用于日志记录、权限验证、性能监控等
 */
public interface HandlerInterceptor {
    
    /**
     * 在控制器方法执行前调用
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器信息
     * @return true继续执行，false中断执行
     */
    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return true;
    }
    
    /**
     * 在控制器方法执行后、视图渲染前调用
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器信息
     * @param exception 如果处理过程中发生异常，则为异常对象，否则为null
     */
    default void postHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler, Exception exception) {
        // 默认实现为空
    }
    
    /**
     * 在整个请求完成后调用（包括视图渲染完成）
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器信息
     * @param exception 如果处理过程中发生异常，则为异常对象，否则为null
     */
    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception exception) {
        // 默认实现为空
    }
} 