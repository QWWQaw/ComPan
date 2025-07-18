package cloud.compan.servlet.web;

import cloud.compan.servlet.handler.BaseHandler;
import cloud.compan.servlet.handler.AuthHandler;
import cloud.compan.servlet.handler.FileHandler;
import cloud.compan.servlet.handler.FolderHandler;
import cloud.compan.servlet.handler.UserHandler;
import cloud.compan.servlet.handler.ShareHandler;
import cloud.compan.servlet.handler.PermissionHandler;
import cloud.compan.servlet.handler.RecycleBinHandler;
import cloud.compan.servlet.handler.NotificationHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 简化版请求分发器 - 基于Handler模式
 *
 * 工作流程：
 * 1. 启动时注册所有Handler
 * 2. 接收请求时，根据路径模式匹配对应的Handler
 * 3. 调用Handler处理请求
 */
public class Dispatcher {

    // Handler注册表：路径模式 -> Handler实例
    private final List<HandlerMapping> handlerMappings = new ArrayList<>();

    // ==================== 构造函数 ====================

    public Dispatcher() {
        System.out.println("=== Dispatcher 初始化开始 ===");
        registerHandlers();
        System.out.println("=== Dispatcher 初始化完成，共注册 " + handlerMappings.size() + " 个Handler ===");
        printRegisteredHandlers();
    }

    // ==================== 主要分发方法 ====================

    /**
     * 分发HTTP请求到对应的Handler
     */
    public void dispatch(HttpServletRequest request, HttpServletResponse response) {
        try {
            String requestURI = request.getRequestURI();
            String contextPath = request.getContextPath();
            String path = requestURI.substring(contextPath.length());
            String method = request.getMethod().toUpperCase();

            System.out.println("处理请求: " + method + " " + path);

            // 查找匹配的Handler
            BaseHandler handler = findMatchingHandler(path);

            if (handler == null) {
                handleNotFound(response, path);
                return;
            }

            // 检查HTTP方法是否支持
            if (!isMethodSupported(handler, method)) {
                handleMethodNotAllowed(response, method);
                return;
            }

            // 调用Handler处理请求
            handler.handle(request, response);

        } catch (Exception e) {
            handleError(response, e);
        }
    }

    // ==================== Handler注册 ====================

    /**
     * 注册所有Handler
     */
    private void registerHandlers() {
        // 注册认证Handler
        registerHandler(new AuthHandler());

        // 注册用户管理Handler
        registerHandler(new UserHandler());

        // 注册文件夹Handler
        registerHandler(new FolderHandler());

        // 注册文件Handler
        registerHandler(new FileHandler());

        // 注册分享Handler
        registerHandler(new ShareHandler());

        // 注册权限Handler
        registerHandler(new PermissionHandler());

        // 注册回收站Handler
        registerHandler(new RecycleBinHandler());

        // 注册通知Handler
        registerHandler(new NotificationHandler());
    }

    /**
     * 注册单个Handler
     */
    private void registerHandler(BaseHandler handler) {
        String pathPattern = handler.getPathPattern();
        String[] supportedMethods = handler.getSupportedMethods();

        HandlerMapping mapping = new HandlerMapping(pathPattern, handler, supportedMethods);
        handlerMappings.add(mapping);

        System.out.println("注册Handler: " + handler.getClass().getSimpleName() +
                          " -> " + pathPattern + " [" + String.join(", ", supportedMethods) + "]");
    }

    // ==================== 路径匹配 ====================

    /**
     * 查找匹配的Handler
     */
    private BaseHandler findMatchingHandler(String requestPath) {
        for (HandlerMapping mapping : handlerMappings) {
            if (pathMatches(requestPath, mapping.getPathPattern())) {
                return mapping.getHandler();
            }
        }
        return null;
    }

    /**
     * 简化的路径匹配算法
     */
    private boolean pathMatches(String requestPath, String pattern) {
        // 精确匹配
        if (requestPath.equals(pattern)) {
            return true;
        }

        // 通配符匹配（以*结尾）
        if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return requestPath.startsWith(prefix);
        }

        return false;
    }

    /**
     * 检查Handler是否支持指定的HTTP方法
     */
    private boolean isMethodSupported(BaseHandler handler, String method) {
        String[] supportedMethods = handler.getSupportedMethods();
        for (String supportedMethod : supportedMethods) {
            if (supportedMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 错误处理 ====================

    /**
     * 处理404错误
     */
    private void handleNotFound(HttpServletResponse response, String path) {
        try {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"请求的资源[" + path + "]不可用\",\"data\":null}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理405错误（方法不允许）
     */
    private void handleMethodNotAllowed(HttpServletResponse response, String method) {
        try {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"不支持的HTTP方法: " + method + "\",\"data\":null}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理500错误
     */
    private void handleError(HttpServletResponse response, Exception e) {
        try {
            System.err.println("请求处理异常: " + e.getMessage());
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"服务器内部错误\",\"data\":null}");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * 打印已注册的Handler信息
     */
    private void printRegisteredHandlers() {
        System.out.println("=== 已注册的Handler列表 ===");
        for (HandlerMapping mapping : handlerMappings) {
            System.out.println(mapping.getHandler().getClass().getSimpleName() +
                              " -> " + mapping.getPathPattern() +
                              " [" + String.join(", ", mapping.getSupportedMethods()) + "]");
        }
        System.out.println("========================");
    }

    // ==================== 内部类 ====================

    /**
     * Handler映射信息
     */
    private static class HandlerMapping {
        private final String pathPattern;
        private final BaseHandler handler;
        private final String[] supportedMethods;

        public HandlerMapping(String pathPattern, BaseHandler handler, String[] supportedMethods) {
            this.pathPattern = pathPattern;
            this.handler = handler;
            this.supportedMethods = supportedMethods;
        }

        public String getPathPattern() { return pathPattern; }
        public BaseHandler getHandler() { return handler; }
        public String[] getSupportedMethods() { return supportedMethods; }
    }
}
