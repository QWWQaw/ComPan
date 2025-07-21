package cloud.compan.servlet.repository;

import java.util.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Repository;
import cloud.compan.servlet.model.StorageObject;
import cloud.compan.servlet.utils.JdbcExecutor;

@Singleton
@Repository
public class StorageObjectRepository extends BaseRepository<StorageObject, String> {

    @Inject
    public StorageObjectRepository(JdbcExecutor executor) {
        super(executor);
    }

    /**
     * 保存一个实体。因为主键不是数据库生成的，
     * 我们通过先查找来决定是插入还是更新。
     * @param entity 要保存的实体。
     * @return 已保存的实体。
     */
    @Override
    public StorageObject save(StorageObject entity) {
        Optional<StorageObject> existing = findById(entity.getHash());
        if (existing.isPresent()) {
            return update(entity);
        } else {
            return insert(entity);
        }
    }

    /**
     * 插入一个新的 StorageObject。
     * 这个方法需要被重写，因为 BaseRepository 的 insert 假定主键是自动生成的。
     * @param entity 要插入的实体
     * @return 已插入的实体
     */
    @Override
    protected StorageObject insert(StorageObject entity) {
        String sql = "INSERT INTO `storage_object` (`hash`, `size`, `storage_path`, `ref_count`, `created_at`) VALUES (?, ?, ?, ?, ?)";
        executor.update(
                entityClass,
                sql,
                entity.getHash(),
                entity.getSize(),
                entity.getStoragePath(),
                entity.getRefCount(),
                entity.getCreatedAt()
        );
        return entity;
    }

    
} 