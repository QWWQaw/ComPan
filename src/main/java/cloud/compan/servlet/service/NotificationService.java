package cloud.compan.servlet.service;

import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.model.Notification;
import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 * 继承BaseService获得基础CRUD能力，同时提供通知管理相关的专门业务逻辑
 */
public interface NotificationService  {
    
    // ============ 通知创建相关 ============
    
    /**
     * 创建通知
     * @param userId 用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @param priority 优先级
     * @return 创建结果
     */
    ServiceResult<Notification> createNotification(Long userId, String title, String content, 
                                                  String type, String priority);
    
    /**
     * 批量创建通知
     * @param userIds 用户ID列表
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return 批量创建结果
     */
    ServiceResult<List<Notification>> createBatchNotifications(List<Long> userIds, String title, 
                                                              String content, String type);
    
    // ============ 通知查询相关 ============
    
    /**
     * 获取用户通知列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param type 通知类型过滤
     * @param status 状态过滤
     * @return 通知列表
     */
    ServiceResult<PageResultDTO<Notification>> getUserNotifications(Long userId, int page, int size, 
                                                                   String type, String status);
    
    /**
     * 获取未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    ServiceResult<Long> getUnreadNotificationCount(Long userId);
    
    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 操作结果
     */
    ServiceResult<Boolean> markAsRead(Long notificationId, Long userId);
    
    /**
     * 批量标记为已读
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @return 操作结果
     */
    ServiceResult<Map<String, Object>> markBatchAsRead(List<Long> notificationIds, Long userId);
    
    /**
     * 标记所有通知为已读
     * @param userId 用户ID
     * @return 操作结果
     */
    ServiceResult<Boolean> markAllAsRead(Long userId);
    
    /**
     * 删除通知
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 删除结果
     */
    ServiceResult<Boolean> deleteNotification(Long notificationId, Long userId);
} 