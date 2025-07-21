package cloud.compan.servlet.utils;

import cloud.compan.servlet.config.GuiceDataSourceProvider;
import cloud.compan.servlet.repository.StorageObjectRepository;
import cloud.compan.servlet.service.impl.StorageServiceImpl;
import cloud.compan.servlet.service.transfer.ChunkService;
import cloud.compan.servlet.service.StorageService;
import cloud.compan.servlet.service.impl.ChunkServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.HashMap;
import java.util.Map;

public class ServiceLocate {
    private static final Map<Class<?>, Object> services = new HashMap<>();
    private static Injector injector;

    static {
        // 创建Guice注入器
        injector = Guice.createInjector(new AppModule());

        // 初始化GuiceDataSourceProvider
        GuiceDataSourceProvider dataSourceProvider = injector.getInstance(GuiceDataSourceProvider.class);

        // 初始化JdbcExecutor
        JdbcExecutor jdbcExecutor = injector.getInstance(JdbcExecutor.class);

        // 初始化仓库和服务
        StorageObjectRepository storageObjectRepository = new StorageObjectRepository(jdbcExecutor);
        StorageService storageService = new StorageServiceImpl();

        // 注册服务实例
        services.put(ChunkService.class, new ChunkServiceImpl((cloud.compan.servlet.service.transfer.StorageService) storageService, storageObjectRepository));
        services.put(StorageService.class, storageService);
        services.put(StorageObjectRepository.class, storageObjectRepository);
        services.put(JdbcExecutor.class, jdbcExecutor);
        services.put(JsonUtils.class, createJsonUtils());
    }

    private static JsonUtils createJsonUtils() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new JsonUtils(objectMapper);
    }

    /**
     * 获取服务实例
     * @param serviceClass 服务接口类
     * @return 服务实现实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> serviceClass) {
        Object service = services.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException("Service not found: " + serviceClass.getName());
        }
        return (T) service;
    }

    /**
     * 简单的Guice模块配置
     */
    private static class AppModule extends com.google.inject.AbstractModule {
        @Override
        protected void configure() {
            // 绑定必要的依赖
            bind(GuiceDataSourceProvider.class);
            bind(JdbcExecutor.class);
        }
    }
}