package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.UserService; // 暂时使用现有的UserService
import cloud.compan.servlet.annotations.component.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 权限管理处理器
 *
 * 根据RESTFUL API文档实现：
 * POST /api/v1/permissions - 设置文件/文件夹权限
 * GET /api/v1/permissions - 获取权限列表
 * PUT /api/v1/permissions/{permission_id} - 更新权限
 * DELETE /api/v1/permissions/{permission_id} - 删除权限
 */
@Service
public class PermissionHandler extends BaseHandler {

    private final UserService userService; // 暂时使用UserService

    public PermissionHandler() {
        this.userService = new UserService();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 暂时返回功能开发中的响应
        sendErrorResponse(response, 501, "权限管理功能正在开发中", Map.of("feature", "permission_management", "status", "under_development"));
    }

    @Override
    public String[] getSupportedMethods() {
        return new String[]{"GET", "POST", "PUT", "DELETE"};
    }

    @Override
    public String getPathPattern() {
        return "/api/v1/permissions*";
    }
}
