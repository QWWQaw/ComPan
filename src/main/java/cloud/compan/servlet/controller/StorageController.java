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
 * 存储统计控制器 - 处理存储空间统计相关操作
 */
@Controller
@RequestMapping(path = "/api/v1/storage")
public class StorageController extends BaseController {

    /**
     * 获取用户存储统计
     * GET /api/v1/storage/summary
     */
    @GetMapping(path = "/summary")
    public void getStorageSummary(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 验证token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            // 模拟存储统计数据
            Map<String, Object> storageData = new HashMap<>();
            storageData.put("storage_limit", 10737418240L); // 10GB
            storageData.put("storage_used", 1073741824L);   // 1GB
            storageData.put("usage_percentage", 10.0);
            storageData.put("file_count", 156);
            storageData.put("folder_count", 23);

            // 文件类型分布
            Map<String, Object> breakdown = new HashMap<>();

            Map<String, Object> images = new HashMap<>();
            images.put("size", 536870912L);
            images.put("count", 45);
            breakdown.put("images", images);

            Map<String, Object> videos = new HashMap<>();
            videos.put("size", 268435456L);
            videos.put("count", 12);
            breakdown.put("videos", videos);

            Map<String, Object> documents = new HashMap<>();
            documents.put("size", 134217728L);
            documents.put("count", 89);
            breakdown.put("documents", documents);

            Map<String, Object> others = new HashMap<>();
            others.put("size", 134217728L);
            others.put("count", 10);
            breakdown.put("others", others);

            storageData.put("breakdown", breakdown);

            sendSuccessResponse(response, storageData, "获取存储统计成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取存储统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取存储使用趋势
     * GET /api/v1/storage/usage-trend
     */
    @GetMapping(path = "/usage-trend")
    public void getUsageTrend(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 验证token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            int days = getIntParameter(request, "days", 30);

            // 模拟趋势数据
            List<Map<String, Object>> trendData = new ArrayList<>();

            for (int i = days; i >= 0; i--) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", java.time.LocalDate.now().minusDays(i).toString());
                dayData.put("storage_used", 1073741824L - (i * 10485760L)); // 递增趋势
                dayData.put("file_count", 156 - i);
                dayData.put("folder_count", 23);
                trendData.add(dayData);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("period_days", days);
            result.put("trend_data", trendData);
            result.put("growth_rate", "+5.2%");
            result.put("average_daily_usage", 10485760L);

            sendSuccessResponse(response, result, "获取存储趋势成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取存储趋势失败: " + e.getMessage());
        }
    }
}
