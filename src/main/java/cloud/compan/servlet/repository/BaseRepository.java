package cloud.compan.servlet.repository;

import cloud.compan.servlet.model.EntityMetadata;
import cloud.compan.servlet.utils.JdbcExecutor;
import com.google.inject.Inject;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 通用的数据访问仓库基类，提供了基本的 CRUD（创建、读取、更新、删除）功能。
 * 通过泛型和 {@link EntityMetadata}，它可以为任何被正确注解的实体提供服务。
 *
 * @param <T>  实体类型。
 * @param <ID> 实体主键的类型，必须是 {@link Serializable}。
 */
public abstract class BaseRepository<T, ID extends Serializable> {

    protected final JdbcExecutor executor;

    //类T的元数据
    protected final Class<T> entityClass; 

    //实体的元数据
    protected final EntityMetadata entityMetadata;
    protected final JdbcExecutor.RowMapper<T> rowMapper;

    @Inject
    @SuppressWarnings("unchecked")
    public BaseRepository(JdbcExecutor executor) {
        this.executor = executor;
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.entityMetadata = EntityMetadata.getMetadata(entityClass);
        this.rowMapper = JdbcExecutor.getEntityRowMapper(entityClass);
    }
    
    /**
     * 保存一个实体。如果实体ID为空，则执行插入；否则执行更新。
     *
     * @param entity 要保存的实体对象。
     * @return 已保存的实体，对于新插入的实体，会包含数据库生成的ID。
     */
    public T save(T entity) {
        Object id = entityMetadata.getId(entity);
        if (id == null) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }

    /**
     * 根据主键ID查找实体。
     *
     * @param id 要查找的实体的主键。
     * @return 一个包含实体的 {@link Optional}，如果未找到则为空。
     */
    public Optional<T> findById(ID id) {
        String sql = String.format("SELECT * FROM `%s` WHERE `%s` = ?",
                entityMetadata.getTableName(),
                entityMetadata.getIdColumnName());
        return executor.queryForObject(entityClass, sql, rowMapper, id);
    }

    /**
     * 查找所有实体。
     *
     * @return 包含所有实体的列表，如果表中没有数据则返回空列表。
     */
    public List<T> findAll() {
        String sql = String.format("SELECT * FROM `%s`", entityMetadata.getTableName());
        return executor.queryForList(entityClass, sql, rowMapper);
    }
    
    /**
     * 根据主键ID删除实体。
     *
     * @param id 要删除的实体的主键。
     */
    public void deleteById(ID id) {
        String sql = String.format("DELETE FROM `%s` WHERE `%s` = ?",
                entityMetadata.getTableName(),
                entityMetadata.getIdColumnName());
        executor.update(entityClass, sql, id);
    }

    /**
     * 插入一个实体。
     * @param entity 要插入的实体对象。
     * @return 已插入的实体，对于新插入的实体，会包含数据库生成的ID。
     */
    protected T insert(T entity) {
        List<String> columns = entityMetadata.getInsertableColumnNames();
        String sql = String.format("INSERT INTO `%s` (%s) VALUES (%s)",
                entityMetadata.getTableName(),
                columns.stream().map(c -> "`" + c + "`").collect(Collectors.joining(", ")),
                String.join(", ", Collections.nCopies(columns.size(), "?")));

        List<Object> values = entityMetadata.getValuesForInsert(entity);
        long newId = executor.updateAndGetKey(entityClass, sql, values.toArray());
        
        entityMetadata.setId(entity, newId);
        return entity;
    }
    
    /**
     * 更新一个实体。
     * @param entity 要更新的实体对象。
     * @return 已更新的实体。
     */
    protected T update(T entity) {
        List<String> columns = entityMetadata.getUpdatableColumnNames();
        String setClause = columns.stream()
                                  .map(c -> "`" + c + "` = ?")
                                  .collect(Collectors.joining(", "));
        String sql = String.format("UPDATE `%s` SET %s WHERE `%s` = ?",
                entityMetadata.getTableName(),
                setClause,
                entityMetadata.getIdColumnName());

        List<Object> values = entityMetadata.getValuesForUpdate(entity);
        executor.update(entityClass, sql, values.toArray());
        return entity;
    }
} 