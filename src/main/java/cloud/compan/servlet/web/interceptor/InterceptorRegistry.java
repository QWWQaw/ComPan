package cloud.compan.servlet.web.interceptor;

import com.google.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 拦截器注册器
 * 管理拦截器的注册和执行顺序
 */
@Singleton
public class InterceptorRegistry {
    
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();
    
    /**
     * 注册拦截器
     */
    public void addInterceptor(HandlerInterceptor interceptor) {
        interceptors.add(interceptor);
        System.out.println("注册拦截器: " + interceptor.getClass().getSimpleName());
    }
    
    /**
     * 执行所有拦截器的preHandle方法
     * @return 如果所有拦截器都返回true，则返回true；否则返回false
     */
    public boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        for (int i = 0; i < interceptors.size(); i++) {
            HandlerInterceptor interceptor = interceptors.get(i);
            try {
                if (!interceptor.preHandle(request, response, handler)) {
                    // 如果某个拦截器返回false，执行afterCompletion进行清理
                    triggerAfterCompletion(request, response, handler, null, i);
                    return false;
                }
            } catch (Exception e) {
                System.err.println("拦截器 " + interceptor.getClass().getSimpleName() + " 的preHandle方法执行异常: " + e.getMessage());
                // 发生异常时也需要执行afterCompletion进行清理
                triggerAfterCompletion(request, response, handler, e, i);
                return false;
            }
        }
        return true;
    }
    
    /**
     * 执行所有拦截器的postHandle方法
     */
    public void applyPostHandle(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception exception) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptors.get(i);
            try {
                interceptor.postHandle(request, response, handler, exception);
            } catch (Exception e) {
                System.err.println("拦截器 " + interceptor.getClass().getSimpleName() + " 的postHandle方法执行异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 执行所有拦截器的afterCompletion方法
     */
    public void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                      Object handler, Exception exception) {
        triggerAfterCompletion(request, response, handler, exception, interceptors.size());
    }
    
    /**
     * 执行指定数量拦截器的afterCompletion方法
     * @param interceptorIndex 要执行的拦截器数量（从0开始）
     */
    private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                       Object handler, Exception exception, int interceptorIndex) {
        for (int i = interceptorIndex - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptors.get(i);
            try {
                interceptor.afterCompletion(request, response, handler, exception);
            } catch (Exception e) {
                System.err.println("拦截器 " + interceptor.getClass().getSimpleName() + " 的afterCompletion方法执行异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 获取已注册的拦截器数量
     */
    public int getInterceptorCount() {
        return interceptors.size();
    }
    
    /**
     * 打印所有注册的拦截器
     */
    public void printInterceptors() {
        System.out.println("已注册的拦截器 (" + interceptors.size() + " 个):");
        for (int i = 0; i < interceptors.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + interceptors.get(i).getClass().getSimpleName());
        }
    }
} 