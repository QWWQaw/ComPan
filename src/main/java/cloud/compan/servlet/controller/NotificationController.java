package cloud.compan.servlet.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.DeleteMapping;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PatchMapping;
import cloud.compan.servlet.annotations.PathVariable;
import cloud.compan.servlet.annotations.PutMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RestController;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.model.Notification;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.NotificationService;
import cloud.compan.servlet.utils.ControllerUtils;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 通知控制器
 * 处理通知管理相关的HTTP请求
 * 
 * 职责：
 * - 处理HTTP请求参数
 * - 调用Service层接口
 * - 统一响应格式
 * 
 * 实现通知管理路由：
 * - GET /api/notifications - 获取通知列表
 * - GET /api/notifications/unread-count - 获取未读数量
 * - PATCH /api/notifications/{id}/read - 标记为已读
 * - PATCH /api/notifications/read-all - 全部标记为已读
 * - DELETE /api/notifications/{id} - 删除通知
 */
@RestController("/api/v1/notifications")
@Singleton
public class NotificationController {
    
    @Inject
    private NotificationService notificationService;
    
    @Inject
    private AuthService authService;
    
    /**
     * 获取用户通知列表
     * GET /api/notifications
     */
    @GetMapping
    public ApiResponseWrapper getUserNotifications(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String type = request.getParameter("type");
        String status = request.getParameter("status");
        
        int page = ControllerUtils.parseIntParam(request, "page", 1);
        int size = ControllerUtils.parseIntParam(request, "size", 20);
        
        ServiceResult<PageResultDTO<Notification>> result = notificationService.getUserNotifications(
            userId, page, size, type, status);
            
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取未读通知数量
     * GET /api/notifications/unread-count
     */
    @GetMapping(path = "/unread-count")
    public ApiResponseWrapper getUnreadNotificationCount(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Long> result = notificationService.getUnreadNotificationCount(userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 标记通知为已读
     * PUT /api/notifications/{id}/read
     */
    @PutMapping(path = "/{id}/read")
    public ApiResponseWrapper markAsRead(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Boolean> result = notificationService.markAsRead(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 批量标记通知为已读
     * PATCH /api/notifications/batch-read
     */
    @PatchMapping(path = "/batch-read")
    public ApiResponseWrapper markBatchAsRead(@RequestBody Map<String, Object> requestData,
                                             HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<Long> notificationIds = (List<Long>) requestData.get("notification_ids");
        
        if (notificationIds == null || notificationIds.isEmpty()) {
            return ControllerUtils.error(400, "通知ID列表不能为空");
        }
        
        ServiceResult<Map<String, Object>> result = notificationService.markBatchAsRead(notificationIds, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 标记所有通知为已读
     * PATCH /api/notifications/read-all
     */
    @PatchMapping(path = "/read-all")
    public ApiResponseWrapper markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Boolean> result = notificationService.markAllAsRead(userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 删除通知
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping(path = "/{id}")
    public ApiResponseWrapper deleteNotification(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Boolean> result = notificationService.deleteNotification(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 从token中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = ControllerUtils.extractTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        
        ServiceResult<Long> result = authService.extractUserIdFromToken(token);
        if (!result.isSuccess()) {
            return null;
        }
        
        return result.getData();
    }
} 