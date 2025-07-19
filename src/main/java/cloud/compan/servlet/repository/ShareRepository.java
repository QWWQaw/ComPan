package cloud.compan.servlet.repository;

import cloud.compan.servlet.model.Share;
import cloud.compan.servlet.utils.JdbcExecutor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Singleton
public class ShareRepository extends BaseRepository<Share, Integer> {

    @Inject
    public ShareRepository(JdbcExecutor executor) {
        super(executor);
    }

    /**
     * 根据分享链接查找分享记录。
     * @param shareLink 分享链接
     * @return 分享记录 Optional
     */
    public Optional<Share> findByShareLink(String shareLink) {
        String sql = "SELECT * FROM `share` WHERE `share_link` = ?";
        Optional<Share> share = executor.queryForObject(entityClass, sql, rowMapper, shareLink);
        return share;
    }

    /**
     * 根据文件ID查找分享记录。
     * @param fileId 文件ID
     * @return 分享记录列表
     */
    public Optional<List<Share>> findByFileId(Long fileId) {
        String sql = "SELECT * FROM `share` WHERE `file_id` = ?";
        List<Share> shares = executor.queryForList(entityClass, sql, rowMapper, fileId);
        return Optional.of(shares);
    }

    /**
     * 根据文件夹ID查找分享记录。
     * @param folderId 文件夹ID
     * @return 分享记录列表
     */
    public Optional<List<Share>> findByFolderId(Long folderId) {
        String sql = "SELECT * FROM `share` WHERE `folder_id` = ?";
        List<Share> shares = executor.queryForList(entityClass, sql, rowMapper, folderId);
        return Optional.of(shares);
    }

    /**
     * 根据用户ID查找分享记录。
     * @param userId 用户ID
     * @return 分享记录列表
     */
    public Optional<List<Share>> findByUserId(Long userId) {
        String sql = "SELECT * FROM `share` WHERE `user_id` = ?";
        List<Share> shares = executor.queryForList(entityClass, sql, rowMapper, userId);
        return Optional.of(shares);
    }

    /**
     * 根据过期时间查找分享记录，过期时间晚于给定时间。
     * @param expiredTime 过期时间
     * @return 分享记录列表
     */
    public Optional<List<Share>> findByExpiredTimeA(LocalDateTime expiredTime) {
        String sql = "SELECT * FROM `share` WHERE `expired_time` > ?";
        List<Share> shares = executor.queryForList(entityClass, sql, rowMapper, expiredTime);
        return Optional.of(shares);
    }

    /**
     * 根据过期时间查找分享记录。
     * @param expiredTime 过期时间
     * @return 分享记录列表
     */
    public Optional<List<Share>> findByExpiredTime(LocalDateTime expiredTime) {
        String sql = "SELECT * FROM `share` WHERE `expired_time` = ?";
        List<Share> shares = executor.queryForList(entityClass, sql, rowMapper, expiredTime);
        return Optional.of(shares);
    }

    /**
     * 根据过期时间查找分享记录，过期时间早于给定时间。
     * @param expiredTime 过期时间
     * @return 分享记录列表
     */
    public Optional<List<Share>> findByExpiredTimeBefore(LocalDateTime expiredTime) {
        String sql = "SELECT * FROM `share` WHERE `expired_time` < ?";
        List<Share> shares = executor.queryForList(entityClass, sql, rowMapper, expiredTime);
        return Optional.of(shares);
    }

} 