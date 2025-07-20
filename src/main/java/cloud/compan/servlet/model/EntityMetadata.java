package cloud.compan.servlet.model;

import cloud.compan.servlet.annotations.Column;
import cloud.compan.servlet.annotations.Entity;
import cloud.compan.servlet.annotations.Table;
import cloud.compan.servlet.annotations.Id;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 实体类的元数据，通过解析注解在运行时缓存类的结构信息，
 * 例如表名、主键列、字段与列的映射关系等。
 * 这个类是实现通用数据访问操作的核心。
 */
public class EntityMetadata {

    private final String tableName;
    private final String idColumnName;
    private final Field idField;
    private final String dataSourceName;
    // 缓存列名 -> 字段的映射
    private final Map<String, Field> columnToFieldMappings;
    // 缓存字段名 -> 列名的映射
    private final Map<String, String> fieldToColumnMappings;



    /**
     * 为给定的实体类创建元数据。
     * 该构造函数是私有的，通过 {@link #getMetadata(Class)} 方法进行调用和缓存。
     * @param clazz 需要解析的实体类，必须被 @Entity 注解。
     * @throws IllegalArgumentException 如果该类没有被 {@link Entity} 或 {@link Table} 注解。
     * @throws IllegalStateException 如果在类中找不到被 {@link Id} 注解的字段。
     */
    private EntityMetadata(Class<?> clazz){
        if (!clazz.isAnnotationPresent(Entity.class)){
            throw new IllegalArgumentException(
                "Class " + clazz.getName() + " must be annotated with @Entity");
        }

        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " must be annotated with @Table");
        }
        this.tableName = tableAnnotation.name();
        this.dataSourceName = tableAnnotation.dataSource();
        
        this.columnToFieldMappings = new HashMap<>();
        this.fieldToColumnMappings = new HashMap<>();

        Field tempIdField = null;

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // 允许访问私有字段

            // 独立检查 @Id 注解
            if (field.isAnnotationPresent(Id.class)) {
                if (tempIdField != null) {
                    throw new IllegalStateException("Multiple @Id fields found in " + clazz.getName());
                }
                tempIdField = field;
            }

            // 独立检查 @Column 注解
            if (field.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = columnAnnotation.name();
                columnToFieldMappings.put(columnName, field);
                fieldToColumnMappings.put(field.getName(), columnName);
            }
        }

        if (tempIdField == null) {
            throw new IllegalStateException("No @Id field found in " + clazz.getName());
        }
        this.idField = tempIdField;

        // 确定主键的列名
        // 如果主键字段上已经有@Column，就用它；否则，约定列名和字段名一样
        this.idColumnName = this.fieldToColumnMappings.computeIfAbsent(
        idField.getName(), 
        fieldName -> {
            // 如果ID字段没有@Column, 它的映射尚不存在, 在这里添加它
            columnToFieldMappings.put(fieldName, idField);
            return fieldName; // 返回字段名作为列名
        }
    );
    }

    /**
     * 获取实体类对应的数据库表名。
     * @return 表名。
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * 获取数据源的逻辑名称。
     * @return 在 Guice 中绑定的数据源名称。
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * 获取主键列的名称。
     * @return 主键列名。
     */
    public String getIdColumnName() {
        return idColumnName;
    }

    /**
     * 通过字段名获取其对应的数据库列名。
     * @param fieldName 实体类的字段名。
     * @return 对应的数据库列名。
     */
    public String getColumnName(String fieldName) {
        return fieldToColumnMappings.get(fieldName);
    }

    /**
     * 获取所有列名，用于 SELECT 查询。
     * 返回的列名将被格式化为 SQL 查询中使用的形式（例如 "`column_name`"）。
     * @return 格式化后的所有列名字符串，例如 "`col1`, `col2`"。
     */
    public String getAllColumnsForSelect(){
        return String.join(", ", columnToFieldMappings.keySet()
                .stream()
                .map(column -> "`" + column + "`")
                .collect(Collectors.toList()));
    }

    private static final Map<Class<?>, EntityMetadata> metadataCache = new ConcurrentHashMap<>();

    /**
     * 获取指定类的元数据，如果缓存中不存在则创建新的元数据。
     * 此方法是线程安全的。
     * @param clazz 目标实体类。
     * @return 实体类的元数据实例。
     */
    public static EntityMetadata getMetadata(Class<?> clazz) {
        return metadataCache.computeIfAbsent(clazz, EntityMetadata::new);
    }
    
    /**
     * 将 ResultSet 的当前行映射到一个新的实体对象实例。
     * @param rs ResultSet，游标应指向需要映射的行。
     * @param clazz 目标实体类的 Class 对象。
     * @param <T> 实体类型。
     * @return 填充了数据的实体对象。
     * @throws Exception 如果在实例化或设置字段值时发生错误。
     */
    public <T> T mapRowToObject(ResultSet rs, Class<T> clazz) throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();
        for (Map.Entry<String, Field> entry : columnToFieldMappings.entrySet()) {
            String columnName = entry.getKey();
            Field field = entry.getValue();
            Object value = rs.getObject(columnName);

            // === 添加 LocalDateTime 类型的特殊处理 ===
            if (field.getType() == LocalDateTime.class && value instanceof java.sql.Timestamp) {
                value = ((java.sql.Timestamp) value).toLocalDateTime();
            }
            // === 结束 LocalDateTime 类型的特殊处理 ===

            // 如果值是null，直接设置null，避免对基本数据类型的空值进行自动装箱
            if (value == null && field.getType().isPrimitive()) {
                // 基本数据类型不能设置为null，这里需要根据实际情况处理，例如赋默认值0
                // 对于LocalDateTime，如果value为null，则直接设置null，不需要额外处理
                continue; // 对于基本数据类型，如果SQL返回NULL，而字段不是包装类型，则跳过设置
            }

            field.set(instance, value);
        }
        return instance;
    }
    
    /**
     * 静态工厂方法，是 {@link #getMetadata(Class)} 的简写。
     * @param clazz 目标实体类。
     * @return 实体类的元数据实例。
     */
    public static EntityMetadata of(Class<?> clazz) {
        return getMetadata(clazz);
    }

    /**
     * 从实体对象中获取主键字段的值。
     * @param entity 实体对象实例。
     * @return 主键的值。
     * @throws RuntimeException 如果无法访问ID字段。
     */
    public Object getId(Object entity) {
        try {
            return this.idField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not access ID field in entity: " + entity.getClass().getName(), e);
        }
    }

    /**
     * 为实体对象设置主键字段的值。
     * @param entity 实体对象实例。
     * @param value 要设置的主键值。
     * @throws RuntimeException 如果无法访问ID字段。
     */
    public void setId(Object entity, Object value) {
        try {
            this.idField.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not access ID field in entity: " + entity.getClass().getName(), e);
        }
    }

    /**
     * 获取除主键外的所有列名，用于 INSERT 语句。
     * @return 非主键列的列表。
     */
    public List<String> getInsertableColumnNames() {
        return columnToFieldMappings.keySet().stream()
                .filter(columnName -> !columnName.equals(this.idColumnName))
                .collect(Collectors.toList());
    }

    /**
     * 从实体对象中获取除主键外的所有字段的值，用于 INSERT 语句。
     * @param entity 实体对象。
     * @return 字段值的列表，顺序与 {@link #getInsertableColumnNames()} 返回的列名一致。
     */
    public List<Object> getValuesForInsert(Object entity) {
        List<Object> values = new ArrayList<>();
        getInsertableColumnNames().forEach(columnName -> {
            try {
                values.add(columnToFieldMappings.get(columnName).get(entity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access field for column: " + columnName, e);
            }
        });
        return values;
    }

    /**
     * 获取除主键外的所有列名，用于 UPDATE 语句。
     * @return 非主键列的列表。
     */
    public List<String> getUpdatableColumnNames() {
        // 目前，更新操作和插入操作涉及的列是相同的（不包括ID）
        // 未来可以扩展，例如通过 @Updatable(false) 注解来排除某些字段
        return getInsertableColumnNames();
    }

    /**
     * 从实体对象中获取用于 UPDATE 语句的字段值。
     * @param entity 实体对象。
     * @return 字段值的列表，顺序与 {@link #getUpdatableColumnNames()} 返回的列名一致。
     */
    public List<Object> getValuesForUpdate(Object entity) {
        List<Object> values = new ArrayList<>();
        getUpdatableColumnNames().forEach(columnName -> {
            try {
                values.add(columnToFieldMappings.get(columnName).get(entity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access field for column: " + columnName, e);
            }
        });
        // 对于UPDATE，最后需要ID的值用于WHERE子句
        values.add(getId(entity));
        return values;
    }

    /**
     * 主函数，用于测试元数据解析功能。
     */
    public static void main(String[] args) {
        EntityMetadata metadata = EntityMetadata.of(User.class);
        System.out.println("Table Name: " + metadata.getTableName());
        System.out.println("ID Column Name: " + metadata.getIdColumnName());
        System.out.println("All Columns for Select: " + metadata.getAllColumnsForSelect());
    }

}
