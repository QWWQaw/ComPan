package cloud.compan.servlet.repository;

import cloud.compan.servlet.model.UserGroup;
import cloud.compan.servlet.utils.JdbcExecutor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UserGroupRepository extends BaseRepository<UserGroup, Long> {

    @Inject
    public UserGroupRepository(JdbcExecutor executor) {
        super(executor);
    }

    // 可以根据需要添加自定义查询方法
} 