package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.enums.RequestMethod;

/**
 * 路由映射信息接口
 * 抽象所有HTTP映射注解的共同特征
 */
public interface RouteMapping {
    
    /**
     * 获取路径
     * @return 路径字符串
     */
    String getPath();
    
    /**
     * 获取HTTP方法
     * @return HTTP方法
     */
    RequestMethod getHttpMethod();
    
    /**
     * 获取请求参数限制
     * @return 参数限制数组
     */
    String[] getParams();
    
    /**
     * 获取请求头限制
     * @return 请求头限制数组
     */
    String[] getHeaders();
    
    /**
     * 获取消费的内容类型
     * @return 消费的内容类型数组
     */
    String[] getConsumes();
    
    /**
     * 获取生产的内容类型
     * @return 生产的内容类型数组
     */
    String[] getProduces();
} 