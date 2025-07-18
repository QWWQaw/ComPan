package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.component.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * 分享管理控制器 - 处理文件/文件夹分享相关操作
 */
@Controller
@RequestMapping(path = "/api/v1/shares")
public class ShareController extends BaseController {

    /**
     * 创建分享链接
     * POST /api/v1/shares
     */
    @PostMapping(path = "/")
    public void createShare(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 验证token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            Integer fileId = getIntParameter(request, "file_id");
            Integer folderId = getIntParameter(request, "folder_id");
            String password = getParameter(request, "password");
            String expireAt = getParameter(request, "expire_at");
            String allowDownloadStr = getParameter(request, "allow_download", "true");
            String allowPreviewStr = getParameter(request, "allow_preview", "true");

            // 验证必须有文件ID或文件夹ID
            if (fileId == null && folderId == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "必须指定文件ID或文件夹ID");
                return;
            }

            boolean allowDownload = Boolean.parseBoolean(allowDownloadStr);
            boolean allowPreview = Boolean.parseBoolean(allowPreviewStr);

            // 生成分享链接
            String shareLink = "abc" + System.currentTimeMillis();
            String shareUrl = "https://kepan.com/s/" + shareLink;

            // 模拟分享数据
            Map<String, Object> shareData = new HashMap<>();
            shareData.put("share_id", System.currentTimeMillis());
            shareData.put("share_link", shareLink);
            shareData.put("share_url", shareUrl);
            shareData.put("item_type", fileId != null ? "file" : "folder");

            // 项目信息
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("id", fileId != null ? fileId : folderId);
            itemInfo.put("name", fileId != null ? "重要文档.pdf" : "我的文件夹");
            if (fileId != null) {
                itemInfo.put("size", 2048000L);
                itemInfo.put("mime_type", "application/pdf");
            }
            itemInfo.put("folder_path", "/");
            shareData.put("item_info", itemInfo);

            // 分享设置
            Map<String, Object> settings = new HashMap<>();
            settings.put("password_protected", password != null && !password.trim().isEmpty());
            settings.put("expire_at", expireAt);
            settings.put("allow_download", allowDownload);
            settings.put("allow_preview", allowPreview);
            shareData.put("settings", settings);

            shareData.put("created_at", java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_INSTANT));

            sendCreatedResponse(response, shareData, "分享链接创建成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "创建分享链接失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的分享列表
     * GET /api/v1/shares
     */
    @GetMapping(path = "/")
    public void getMyShares(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 验证token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            int page = getIntParameter(request, "page", 1);
            int perPage = getIntParameter(request, "per_page", 20);

            // 模拟分享列表
            List<Map<String, Object>> shares = new ArrayList<>();

            Map<String, Object> share1 = new HashMap<>();
            share1.put("share_id", 1);
            share1.put("share_link", "abc123def456ghi789");
            share1.put("share_url", "https://kepan.com/s/abc123def456ghi789");
            share1.put("item_type", "file");

            Map<String, Object> itemInfo1 = new HashMap<>();
            itemInfo1.put("id", 456);
            itemInfo1.put("name", "重要文档.pdf");
            itemInfo1.put("size", 2048000L);
            itemInfo1.put("mime_type", "application/pdf");
            itemInfo1.put("folder_path", "/我的文档/工作文件");
            share1.put("item_info", itemInfo1);

            Map<String, Object> settings1 = new HashMap<>();
            settings1.put("password_protected", true);
            settings1.put("expire_at", "2025-08-17T10:30:00Z");
            settings1.put("allow_download", true);
            settings1.put("allow_preview", true);
            share1.put("settings", settings1);

            share1.put("created_at", "2025-07-17T10:30:00Z");
            shares.add(share1);

            // 创建分页响应
            PaginationInfo pagination = new PaginationInfo(page, perPage, 15);
            PaginationResponse<Map<String, Object>> paginatedResponse = new PaginationResponse<>(shares, pagination);

            sendSuccessResponse(response, paginatedResponse, "获取分享列表成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取分享列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取分享详情
     * GET /api/v1/shares/{share_link}
     */
    @GetMapping(path = "/{shareLink}")
    public void getShareDetails(@PathVariable("shareLink") String shareLink,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        try {
            if (shareLink == null || shareLink.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "分享链接不能为空");
                return;
            }

            // 模拟检查分享链接是否有效
            if ("invalid_link".equals(shareLink)) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("error_type", "share_not_found");
                errorData.put("share_link", shareLink);
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "分享链接不存在或已过期", errorData);
                return;
            }

            // 模拟分享详情
            Map<String, Object> shareDetails = new HashMap<>();
            shareDetails.put("share_link", shareLink);
            shareDetails.put("item_type", "file");

            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("id", 123);
            itemInfo.put("name", "重要文档.pdf");
            itemInfo.put("size", 2048000L);
            itemInfo.put("mime_type", "application/pdf");
            shareDetails.put("item_info", itemInfo);

            Map<String, Object> sharedBy = new HashMap<>();
            sharedBy.put("username", "john_doe");
            shareDetails.put("shared_by", sharedBy);

            Map<String, Object> settings = new HashMap<>();
            settings.put("password_protected", true);
            settings.put("expire_at", "2025-08-17T10:30:00Z");
            settings.put("allow_download", true);
            settings.put("allow_preview", true);
            shareDetails.put("settings", settings);

            shareDetails.put("created_at", "2025-07-17T10:30:00Z");

            sendSuccessResponse(response, shareDetails, "获取分享信息成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取分享信息失败: " + e.getMessage());
        }
    }

    /**
     * 访问分享内容
     * POST /api/v1/shares/{share_link}/access
     */
    @PostMapping(path = "/{shareLink}/access")
    public void accessShare(@PathVariable("shareLink") String shareLink,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
        try {
            if (shareLink == null || shareLink.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "分享链接不能为空");
                return;
            }

            String password = getParameter(request, "password");

            // 模拟密码验证
            if ("abc123def456ghi789".equals(shareLink) && !"1234".equals(password)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "密码错误");
                return;
            }

            // 生成临时访问token
            String accessToken = "temp_access_token_" + System.currentTimeMillis();

            Map<String, Object> accessData = new HashMap<>();
            accessData.put("access_token", accessToken);
            accessData.put("expires_in", 3600);
            accessData.put("item_type", "file");

            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("id", 456);
            itemInfo.put("name", "重要文档.pdf");
            itemInfo.put("size", 2048000L);
            itemInfo.put("mime_type", "application/pdf");
            accessData.put("item_info", itemInfo);

            Map<String, Object> accessUrls = new HashMap<>();
            accessUrls.put("download", "/api/v1/shares/" + shareLink + "/download?token=" + accessToken);
            accessUrls.put("preview", "/api/v1/shares/" + shareLink + "/preview?token=" + accessToken);
            accessData.put("access_urls", accessUrls);

            sendSuccessResponse(response, accessData, "访问验证成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "访问验证失败: " + e.getMessage());
        }
    }

    /**
     * 删除分享
     * DELETE /api/v1/shares/{share_id}
     */
    @DeleteMapping(path = "/{shareId}")
    public void deleteShare(@PathVariable("shareId") Integer shareId,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
        try {
            // 验证token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            if (shareId == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "分享ID不能为空");
                return;
            }

            // 模拟删除分享
            Map<String, Object> result = new HashMap<>();
            result.put("share_id", shareId);
            result.put("deleted_at", java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_INSTANT));

            sendSuccessResponse(response, result, "分享已删除");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "删除分享失败: " + e.getMessage());
        }
    }
}
