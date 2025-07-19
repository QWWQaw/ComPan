package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.enums.RequestMethod;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参数化路由信息类 - 支持路径参数的高级RouteInfo
 * 支持 /users/{id} 或 /users/{userId}/posts/{postId} 等路径模式
 */
public class ParameterizedRouteInfo extends RouteInfo {
    
    private final String pathPattern;              // 原始路径模式：/users/{id}
    private final Pattern compiledPattern;        // 编译后的正则：/users/(\d+)
    private final List<String> pathVariableNames; // 路径变量名：[id]
    private final boolean hasPathVariables;       // 是否包含路径变量
    
    /**
     * 构造参数化路由信息
     */
    public ParameterizedRouteInfo(String pathPattern, RequestMethod httpMethod, 
                                 Class<?> controllerClass, Method handlerMethod, 
                                 Object controllerInstance) {
        super(pathPattern, httpMethod, controllerClass, handlerMethod, controllerInstance);
        
        this.pathPattern = pathPattern;
        this.pathVariableNames = new ArrayList<>();
        this.hasPathVariables = pathPattern.contains("{");
        
        if (hasPathVariables) {
            this.compiledPattern = compilePathPattern(pathPattern);
        } else {
            this.compiledPattern = Pattern.compile("^" + Pattern.quote(pathPattern) + "$");
        }
    }
    
    /**
     * 将路径模式编译为正则表达式
     * /users/{id} -> /users/([^/]+)
     * /users/{id}/posts/{postId} -> /users/([^/]+)/posts/([^/]+)
     */
    private Pattern compilePathPattern(String pathPattern) {
        String regex = pathPattern;
        
        // 找到所有的路径变量 {variableName}
        Pattern variablePattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = variablePattern.matcher(pathPattern);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            pathVariableNames.add(variableName);
            
            // 替换 {variableName} 为捕获组
            // 使用 [^/]+ 匹配除了斜杠以外的任意字符
            regex = regex.replace("{" + variableName + "}", "([^/]+)");
        }
        
        // 编译为正则表达式，添加起始和结束锚点
        return Pattern.compile("^" + regex + "$");
    }
    
    /**
     * 检查请求路径是否匹配此路由
     * @param requestPath 请求路径
     * @param requestMethod 请求方法
     * @return 是否匹配
     */
    @Override
    public boolean matches(String requestPath, RequestMethod requestMethod) {
        if (!this.getHttpMethod().equals(requestMethod)) {
            return false;
        }
        
        String normalizedPath = normalizePath(requestPath);
        
        if (!hasPathVariables) {
            // 无参数的精确匹配
            return this.getPath().equals(normalizedPath);
        } else {
            // 有参数的模式匹配
            return compiledPattern.matcher(normalizedPath).matches();
        }
    }
    
    /**
     * 从请求路径中提取路径变量
     * @param requestPath 请求路径
     * @return 路径变量映射 Map<变量名, 值>
     */
    public Map<String, String> extractPathVariables(String requestPath) {
        Map<String, String> variables = new HashMap<>();
        
        if (!hasPathVariables) {
            return variables; // 空Map
        }
        
        String normalizedPath = normalizePath(requestPath);
        Matcher matcher = compiledPattern.matcher(normalizedPath);
        
        if (matcher.matches()) {
            for (int i = 0; i < pathVariableNames.size(); i++) {
                String variableName = pathVariableNames.get(i);
                String variableValue = matcher.group(i + 1); // 捕获组从1开始
                variables.put(variableName, variableValue);
            }
        }
        
        return variables;
    }
    
    /**
     * 生成路由的唯一键（考虑路径参数）
     * @return 路由键
     */
    @Override
    public String getRouteKey() {
        return getHttpMethod().name() + ":" + pathPattern;
    }
    
    /**
     * 标准化路径
     */
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
    
    // Getters
    public String getPathPattern() { return pathPattern; }
    public List<String> getPathVariableNames() { return Collections.unmodifiableList(pathVariableNames); }
    public boolean hasPathVariables() { return hasPathVariables; }
    
    @Override
    public String toString() {
        String baseInfo = String.format("ParameterizedRouteInfo{%s %s -> %s.%s}", 
            getHttpMethod(), pathPattern, 
            getControllerClass().getSimpleName(), 
            getHandlerMethod().getName());
            
        if (hasPathVariables) {
            return baseInfo + " [variables: " + pathVariableNames + "]";
        }
        return baseInfo;
    }
    
    /**
     * 验证路径模式的合法性
     * @param pathPattern 路径模式
     * @return 是否合法
     */
    public static boolean isValidPathPattern(String pathPattern) {
        if (pathPattern == null || pathPattern.isEmpty()) {
            return false;
        }
        
        // 检查花括号是否匹配
        int openBraces = 0;
        for (char c : pathPattern.toCharArray()) {
            if (c == '{') {
                openBraces++;
            } else if (c == '}') {
                openBraces--;
                if (openBraces < 0) {
                    return false; // 右括号多于左括号
                }
            }
        }
        
        return openBraces == 0; // 左右括号数量相等
    }
    
    /**
     * 提取路径模式中的所有变量名
     * @param pathPattern 路径模式
     * @return 变量名列表
     */
    public static List<String> extractVariableNames(String pathPattern) {
        List<String> variables = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(pathPattern);
        
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return variables;
    }
} 