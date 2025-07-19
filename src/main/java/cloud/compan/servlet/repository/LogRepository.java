package cloud.compan.servlet.repository;

import cloud.compan.servlet.model.Log;
import cloud.compan.servlet.utils.JdbcExecutor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class LogRepository extends BaseRepository<Log, Integer> {

    @Inject
    public LogRepository(JdbcExecutor executor) {
        super(executor);
    }

    /**
     * 根据用户ID查找所有相关的日志记录。
     * @param userId 用户ID
     * @return 日志记录列表
     */
    public Optional<List<Log>> findByUserId(Long userId) {
        String sql = "SELECT * FROM `log` WHERE `user_id` = ? ORDER BY `performed_at` DESC";
        List<Log> logs = executor.queryForList(entityClass, sql, rowMapper, userId);
        return Optional.of(logs);
    }

    public Optional<List<Log>> filterByOperation(String operation) {
        String sql = "SELECT * FROM `log` WHERE `operation` LIKE ?";
        List<Log> logs = executor.queryForList(entityClass, sql, rowMapper, "%" + operation + "%");
        return Optional.of(logs);
    }
} 