package cloud.compan.servlet.repository;

import cloud.compan.servlet.model.UserGroup;
import cloud.compan.servlet.utils.JdbcExecutor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class UserGroupRepository extends BaseRepository<UserGroup, Long> {

    @Inject
    public UserGroupRepository(JdbcExecutor executor) {
        super(executor);
    }


    
} 