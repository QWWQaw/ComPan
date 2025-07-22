package cloud.compan.servlet.repository;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.google.inject.Inject;
import cloud.compan.servlet.model.EntityMetadata;
import cloud.compan.servlet.utils.JdbcExecutor;

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
        String sql = String.format("SELECT %s FROM `%s` WHERE `%s` = ?",
                entityMetadata.getAllColumnsForSelect(),
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
        String sql = String.format("SELECT %s FROM `%s`",
                entityMetadata.getAllColumnsForSelect(),
                entityMetadata.getTableName());
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

    /**
     * 根据字段名查找单个实体（期望最多一个结果）。
     * 适用于唯一字段查询，如用户名、邮箱等。
     * @param fieldName 字段名（Java 字段名）
     * @param value 字段值
     * @return 一个包含实体的 {@link Optional}，如果未找到则为空。
     */
    public Optional<T> findOneByField(String fieldName, Object value) {
        String columnName = entityMetadata.getColumnName(fieldName);
        String sql = String.format("SELECT %s FROM `%s` WHERE `%s` = ? LIMIT 1",
                entityMetadata.getAllColumnsForSelect(),
                entityMetadata.getTableName(),
                columnName);
        return executor.queryForObject(entityClass, sql, rowMapper, value);
    }

    /**
     * 根据字段名查找所有匹配的实体。
     * 适用于非唯一字段查询，如角色、状态等。
     * @param fieldName 字段名（Java 字段名）
     * @param value 字段值
     * @return 匹配的实体列表
     */
    public List<T> findAllByField(String fieldName, Object value) {
        String columnName = entityMetadata.getColumnName(fieldName);
        String sql = String.format("SELECT %s FROM `%s` WHERE `%s` = ?",
                entityMetadata.getAllColumnsForSelect(),
                entityMetadata.getTableName(),
                columnName);
        return executor.queryForList(entityClass, sql, rowMapper, value);
    }

    /**
     * 检查指定字段值是否存在。
     * 高效的存在性检查，只查询主键列。
     * @param fieldName 字段名（Java 字段名）
     * @param value 字段值
     * @return 如果存在则为 true
     */
    public boolean existsByField(String fieldName, Object value) {
        String columnName = entityMetadata.getColumnName(fieldName);
        String sql = String.format("SELECT 1 FROM `%s` WHERE `%s` = ? LIMIT 1",
                entityMetadata.getTableName(),
                columnName);
        return executor.queryForObject(entityClass, sql, rs -> rs.getInt(1), value).isPresent();
    }

    /**
     * 根据多个字段条件查找实体。
     * 支持 AND 条件组合查询。
     * @param conditions 字段名和值的映射
     * @return 匹配的实体列表
     */
    public List<T> findByFields(Map<String, Object> conditions) {
        if (conditions.isEmpty()) {
            return findAll();
        }
        
        List<String> whereClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String columnName = entityMetadata.getColumnName(entry.getKey());
            whereClauses.add(String.format("`%s` = ?", columnName));
            values.add(entry.getValue());
        }
        
        String whereClause = String.join(" AND ", whereClauses);
        String sql = String.format("SELECT %s FROM `%s` WHERE %s",
                entityMetadata.getAllColumnsForSelect(),
                entityMetadata.getTableName(),
                whereClause);
        
        return executor.queryForList(entityClass, sql, rowMapper, values.toArray());
    }

    /**
     * 分页查询所有实体。
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页结果
     */
    public List<T> findAll(int page, int size) {
        String sql = String.format("SELECT %s FROM `%s` LIMIT ? OFFSET ?",
                entityMetadata.getAllColumnsForSelect(),
                entityMetadata.getTableName());
        return executor.queryForList(entityClass, sql, rowMapper, size, page * size);
    }

    /**
     * 根据字段值进行模糊查询。
     * @param fieldName 字段名（Java 字段名）
     * @param pattern 模糊匹配模式（支持 % 通配符）
     * @return 匹配的实体列表
     */
    public List<T> findByFieldLike(String fieldName, String pattern) {
        String columnName = entityMetadata.getColumnName(fieldName);
        String sql = String.format("SELECT %s FROM `%s` WHERE `%s` LIKE ?",
                entityMetadata.getAllColumnsForSelect(),
                entityMetadata.getTableName(),
                columnName);
        return executor.queryForList(entityClass, sql, rowMapper, pattern);
    }

    /**
     * 根据字段值进行范围查询。
     * @param fieldName 字段名（Java 字段名）
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 匹配的实体列表
     */
    public List<T> findByFieldBetween(String fieldName, Object minValue, Object maxValue) {
        String columnName = entityMetadata.getColumnName(fieldName);
        String sql = String.format("SELECT %s FROM `%s` WHERE `%s` BETWEEN ? AND ?",
                entityMetadata.getAllColumnsForSelect(),
                entityMetadata.getTableName(),
                columnName);
        return executor.queryForList(entityClass, sql, rowMapper, minValue, maxValue);
    }

    /**
     * 根据字段值进行 IN 查询。
     * @param fieldName 字段名（Java 字段名）
     * @param values 值列表
     * @return 匹配的实体列表
     */
    public List<T> findByFieldIn(String fieldName, List<Object> values) {
        if (values.isEmpty()) {
            return new ArrayList<>();
        }
        
        String columnName = entityMetadata.getColumnName(fieldName);
        String placeholders = String.join(", ", Collections.nCopies(values.size(), "?"));
        String sql = String.format("SELECT %s FROM `%s` WHERE `%s` IN (%s)",
                entityMetadata.getAllColumnsForSelect(),
                entityMetadata.getTableName(),
                columnName,
                placeholders);
        return executor.queryForList(entityClass, sql, rowMapper, values.toArray());
    }

    /**
     * 根据字段值进行排序查询。
     * @param fieldName 排序字段名（Java 字段名）
     * @param ascending 是否升序
     * @return 排序后的实体列表
     */
    public List<T> findAllOrderBy(String fieldName, boolean ascending) {
        String columnName = entityMetadata.getColumnName(fieldName);
        String orderDirection = ascending ? "ASC" : "DESC";
        String sql = String.format("SELECT %s FROM `%s` ORDER BY `%s` %s",
                entityMetadata.getAllColumnsForSelect(),
                entityMetadata.getTableName(),
                columnName,
                orderDirection);
        return executor.queryForList(entityClass, sql, rowMapper);
    }

    /**
     * 统计指定字段值的数量。
     * @param fieldName 字段名（Java 字段名）
     * @param value 字段值
     * @return 匹配的记录数量
     */
    public long countByField(String fieldName, Object value) {
        String columnName = entityMetadata.getColumnName(fieldName);
        String sql = String.format("SELECT COUNT(*) FROM `%s` WHERE `%s` = ?",
                entityMetadata.getTableName(),
                columnName);
        return executor.queryForObject(entityClass, sql, rs -> rs.getLong(1), value).orElse(0L);
    }

    /**
     * 统计所有记录数量。
     * @return 总记录数
     */
    public long count() {
        String sql = String.format("SELECT COUNT(*) FROM `%s`", entityMetadata.getTableName());
        return executor.queryForObject(entityClass, sql, rs -> rs.getLong(1)).orElse(0L);
    }

    /**
     * 检查实体是否存在（通过主键）。
     * @param id 主键值
     * @return 如果存在则为 true
     */
    public boolean existsById(ID id) {
        String sql = String.format("SELECT 1 FROM `%s` WHERE `%s` = ? LIMIT 1",
                entityMetadata.getTableName(),
                entityMetadata.getIdColumnName());
        return executor.queryForObject(entityClass, sql, rs -> rs.getInt(1), id).isPresent();
    }
} 