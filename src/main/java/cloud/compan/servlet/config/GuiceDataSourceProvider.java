
package cloud.compan.servlet.config;

import cloud.compan.servlet.model.EntityMetadata;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import javax.sql.DataSource;

/**
 * 一个由 Guice 管理的 DataSource 提供者。
 * 它根据实体类的元数据中定义的 dataSource 名称，
 * 从 Guice Injector 中检索出正确的 DataSource 实例。
 */
@Singleton // 推荐将其设为单例
public class GuiceDataSourceProvider {

    private final Injector injector;

    @Inject
    public GuiceDataSourceProvider(Injector injector) {
        this.injector = injector;
    }

    /**
     * 根据实体类获取其对应的数据源。
     * @param entityClass 实体类
     * @return 正确配置的 DataSource 实例
     */
    public DataSource get(Class<?> entityClass) {
        // 1. 获取元数据
        EntityMetadata metadata = EntityMetadata.of(entityClass);
        // 2. 从元数据中获取数据源的逻辑名称 (e.g., "default")
        String dataSourceName = metadata.getDataSourceName();
        
        // 3. 使用 Guice 的 Key 和 Names 来从 Injector 获取命名的实例
        Key<DataSource> dataSourceKey = Key.get(DataSource.class, Names.named(dataSourceName));
        
        try {
            return injector.getInstance(dataSourceKey);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to find a DataSource named '" + dataSourceName + "' for entity " + entityClass.getSimpleName() +
                ". Make sure it is bound in your Guice Module.", e);
        }
    }
}

