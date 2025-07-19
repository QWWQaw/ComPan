package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.UserService; // 暂时使用现有的UserService
import cloud.compan.servlet.annotations.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 通知管理处理器
 *
 * 根据RESTFUL API文档实现：
 * GET /api/v1/notifications - 获取通知列表
 * PUT /api/v1/notifications/{notification_id}/read - 标记通知为已读
 * PUT /api/v1/notifications/read-all - 标记所有通知为已读
 * DELETE /api/v1/notifications/{notification_id} - 删除通知
 */
@Service
public class NotificationHandler extends BaseHandler {

    private final UserService userService; // 暂时使用UserService

    public NotificationHandler() {
        this.userService = new UserService();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 暂时返回功能开发中的响应
        sendErrorResponse(response, 501, "通知功能正在开发中", Map.of("feature", "notification_management", "status", "under_development"));
    }

    @Override
    public String[] getSupportedMethods() {
        return new String[]{"GET", "PUT", "DELETE"};
    }

    @Override
    public String getPathPattern() {
        return "/api/v1/notifications*";
    }
}
