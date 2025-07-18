package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.UserService; // 暂时使用现有的UserService
import cloud.compan.servlet.annotations.component.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 回收站管理处理器
 *
 * 根据RESTFUL API文档实现：
 * GET /api/v1/recycle-bin - 获取回收站内容
 * POST /api/v1/recycle-bin/{item_id}/restore - 恢复文件/文件夹
 * DELETE /api/v1/recycle-bin/{item_id} - 彻底删除
 * DELETE /api/v1/recycle-bin - 清空回收站
 */
@Service
public class RecycleBinHandler extends BaseHandler {

    private final UserService userService; // 暂时使用UserService

    public RecycleBinHandler() {
        this.userService = new UserService();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 暂时返回功能开发中的响应
        sendErrorResponse(response, 501, "回收站功能正在开发中", Map.of("feature", "recycle_bin", "status", "under_development"));
    }

    @Override
    public String[] getSupportedMethods() {
        return new String[]{"GET", "POST", "DELETE"};
    }

    @Override
    public String getPathPattern() {
        return "/api/v1/recycle-bin*";
    }
}
