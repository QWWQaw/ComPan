package cloud.compan.servlet.service.impl;

import cloud.compan.servlet.service.NotificationService;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.model.Notification;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 通知服务实现类
 * 简化实现，主要用于测试
 */
@Singleton
public class NotificationServiceImpl implements NotificationService {
    
    @Override
    public ServiceResult<Notification> createNotification(Long userId, String title, String content, 
                                                         String type, String priority) {
        return ServiceResult.success(null, "创建通知成功");
    }
    
    @Override
    public ServiceResult<List<Notification>> createBatchNotifications(List<Long> userIds, String title, 
                                                                     String content, String type) {
        return ServiceResult.success(List.of(), "批量创建通知成功");
    }
    
    @Override
    public ServiceResult<PageResultDTO<Notification>> getUserNotifications(Long userId, int page, int size, 
                                                                          String type, String status) {
        PageResultDTO<Notification> pageResult = PageResultDTO.<Notification>builder()
            .content(List.of())
            .totalElements(0L)
            .currentPage(page)
            .pageSize(size)
            .totalPages(0)
            .build();
        return ServiceResult.success(pageResult, "获取用户通知列表成功");
    }
    
    @Override
    public ServiceResult<Long> getUnreadNotificationCount(Long userId) {
        return ServiceResult.success(5L, "获取未读通知数量成功");
    }
    
    @Override
    public ServiceResult<Boolean> markAsRead(Long notificationId, Long userId) {
        return ServiceResult.success(true, "标记为已读成功");
    }
    
    @Override
    public ServiceResult<Map<String, Object>> markBatchAsRead(List<Long> notificationIds, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("marked", notificationIds.size());
        return ServiceResult.success(result, "批量标记为已读成功");
    }
    
    @Override
    public ServiceResult<Boolean> markAllAsRead(Long userId) {
        return ServiceResult.success(true, "标记所有通知为已读成功");
    }
    
    @Override
    public ServiceResult<Boolean> deleteNotification(Long notificationId, Long userId) {
        return ServiceResult.success(true, "删除通知成功");
    }
} 