package cloud.compan.servlet.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Repository;
import cloud.compan.servlet.model.Log;
import cloud.compan.servlet.utils.JdbcExecutor;

@Singleton
@Repository
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

    /**
     * 根据操作类型查找日志记录
     * @param operation 操作类型
     * @return 操作类型匹配的日志记录
     */
    public Optional<List<Log>> findByOperation(String operation) {
        String sql = "SELECT * FROM `log` WHERE `operation` LIKE ?";
        List<Log> logs = executor.queryForList(entityClass, sql, rowMapper, "%" + operation + "%");
        return Optional.of(logs);
    }
    
    /**
     * 根据IP地址查找日志记录
     * @param ipAddress IP地址
     * @return 该IP地址的所有操作日志
     */
   public Optional<List<Log>> findByIpAddress(String ipAddress) {
       String sql = "SELECT * FROM `log` WHERE `ip_address` = ? ORDER BY `performed_at` DESC";
       List<Log> logs = executor.queryForList(entityClass, sql, rowMapper, ipAddress);
       return Optional.of(logs);
   }

   /**
    * 根据操作时间查找日志记录
    * @param performedAt 操作时间
    * @param intervalMinutes 时间区间（分钟）
    * @return 操作时间在给定时间前后范围内
    */
   public Optional<List<Log>> findByPerformedNear(LocalDateTime performedAt, Integer intervalMinutes) {
       String sql = "SELECT * FROM `log` WHERE `performed_at` BETWEEN ? AND ? ORDER BY `performed_at` DESC";
       List<Log> logs = executor.queryForList(entityClass, sql, rowMapper, 
                                                performedAt.minusMinutes(intervalMinutes), 
                                                performedAt.plusMinutes(intervalMinutes));
       return Optional.of(logs);
   }

   /**
    * 根据操作时间查找日志记录
    * @param start 开始时间
    * @param end 结束时间
    * @return 操作时间在给定时间范围内
    */
   public Optional<List<Log>> findByPerformedBetween(LocalDateTime start, LocalDateTime end) {
       String sql = "SELECT * FROM `log` WHERE `performed_at` BETWEEN ? AND ? ORDER BY `performed_at` DESC";
       List<Log> logs = executor.queryForList(entityClass, sql, rowMapper, start, end);
       return Optional.of(logs);
   }

   /**
    * 根据操作类型和操作时间查找日志记录
    * @param operation 操作类型
    * @param start 开始时间
    * @param end 结束时间
    * @return 操作类型和操作时间在给定时间范围内
    */
   public Optional<List<Log>> findByOperationAndPerformedBetween(String operation, LocalDateTime start, LocalDateTime end) {
       String sql = "SELECT * FROM `log` WHERE `operation` = ? AND `performed_at` BETWEEN ? AND ? ORDER BY `performed_at` DESC";
       List<Log> logs = executor.queryForList(entityClass, sql, rowMapper, operation, start, end);
       return Optional.of(logs);
   }

   /**
    * 根据操作类型和操作时间查找日志记录
    * @param operation 操作类型
    * @param performedAt 操作时间
    * @param intervalMinutes 时间区间（分钟）
    * @return 操作类型和操作时间在给定时间范围内
    */
   public Optional<List<Log>> findByOperationAndPerformedNear(String operation, LocalDateTime performedAt, Integer intervalMinutes) {
       String sql = "SELECT * FROM `log` WHERE `operation` = ? AND `performed_at` BETWEEN ? AND ? ORDER BY `performed_at` DESC";
       List<Log> logs = executor.queryForList(entityClass, sql, rowMapper, operation, performedAt.minusMinutes(intervalMinutes), performedAt.plusMinutes(intervalMinutes));
       return Optional.of(logs);
   }

   /**
    * 根据操作类型和IP地址查找日志记录
    * @param operation 操作类型
    * @param ipAddress IP地址
    * @return 操作类型和IP地址匹配的日志记录
    */
   public Optional<List<Log>> findByOperationAndIpAddress(String operation, String ipAddress) {
       String sql = "SELECT * FROM `log` WHERE `operation` = ? AND `ip_address` = ? ORDER BY `performed_at` DESC";
       List<Log> logs = executor.queryForList(entityClass, sql, rowMapper, operation, ipAddress);
       return Optional.of(logs);
   }
} 