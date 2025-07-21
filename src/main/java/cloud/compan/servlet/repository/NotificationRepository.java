package cloud.compan.servlet.repository;

import java.util.List;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Repository;
import cloud.compan.servlet.model.Notification;
import cloud.compan.servlet.utils.JdbcExecutor;

@Singleton
@Repository
public class NotificationRepository extends BaseRepository<Notification, Integer> {

    @Inject
    public NotificationRepository(JdbcExecutor executor) {
        super(executor);
    }

    /**
     * 根据用户ID查找所有通知。
     * @param userId 用户ID
     * @return 通知列表
     */
    public List<Notification> findByUserId(Long userId) {
        String sql = "SELECT * FROM `notification` WHERE `user_id` = ? ORDER BY `created_at` DESC";
        return executor.queryForList(entityClass, sql, rowMapper, userId);
    }

    /**
     * 根据用户ID查找所有未读通知。
     * @param userId 用户ID
     * @return 未读通知列表
     */
    public List<Notification> findUnreadByUserId(Long userId) {
        String sql = "SELECT * FROM `notification` WHERE `user_id` = ? AND `is_read` = false ORDER BY `created_at` DESC";
        return executor.queryForList(entityClass, sql, rowMapper, userId);
    }
} 