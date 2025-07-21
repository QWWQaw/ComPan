package cloud.compan.servlet.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Repository;
import cloud.compan.servlet.model.UserGroup;
import cloud.compan.servlet.utils.JdbcExecutor;

@Singleton
@Repository
public class UserGroupRepository extends BaseRepository<UserGroup, Long> {

    @Inject
    public UserGroupRepository(JdbcExecutor executor) {
        super(executor);
    }


    
} 