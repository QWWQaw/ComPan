package cloud.compan.servlet.service;

import java.util.List;
import java.util.Optional;

/**
 * 基础服务接口
 * 定义通用的CRUD操作，所有业务服务都应该实现或继承此接口
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public interface BaseService<T, ID> {
    
    // ============ 基础CRUD操作 ============
    
    /**
     * 根据ID查找实体
     * @param id 主键
     * @return Optional包装的实体
     */
    Optional<T> findById(ID id);
    
    /**
     * 查找所有实体
     * @return 实体列表
     */
    List<T> findAll();
    
    /**
     * 分页查找实体
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<T> findPage(int page, int size);
    
    /**
     * 保存实体（新增或更新）
     * @param entity 实体对象
     * @return 保存后的实体
     */
    T save(T entity);
    
    /**
     * 批量保存实体
     * @param entities 实体列表
     * @return 保存后的实体列表
     */
    List<T> saveAll(List<T> entities);
    
    /**
     * 根据ID删除实体
     * @param id 主键
     * @return 是否删除成功
     */
    boolean deleteById(ID id);
    
    /**
     * 删除实体
     * @param entity 实体对象
     * @return 是否删除成功
     */
    boolean delete(T entity);
    
    /**
     * 批量删除
     * @param ids 主键列表
     * @return 删除的数量
     */
    int deleteByIds(List<ID> ids);
    
    /**
     * 检查实体是否存在
     * @param id 主键
     * @return 是否存在
     */
    boolean existsById(ID id);
    
    /**
     * 统计实体总数
     * @return 总数
     */
    long count();
    
    // ============ 业务验证方法 ============
    
    /**
     * 保存前验证
     * @param entity 实体对象
     * @throws IllegalArgumentException 验证失败时抛出
     */
    default void validateBeforeSave(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("实体不能为空");
        }
    }
    
    /**
     * 删除前验证
     * @param id 主键
     * @throws IllegalArgumentException 验证失败时抛出
     */
    default void validateBeforeDelete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
    }
    
    // ============ 分页结果类 ============
    
    /**
     * 分页结果包装类
     */
    class PageResult<T> {
        private List<T> content;
        private long totalElements;
        private int page;
        private int size;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
        
        public PageResult(List<T> content, long totalElements, int page, int size) {
            this.content = content;
            this.totalElements = totalElements;
            this.page = page;
            this.size = size;
            this.totalPages = (int) Math.ceil((double) totalElements / size);
            this.hasNext = page < totalPages;
            this.hasPrevious = page > 1;
        }
        
        // Getters
        public List<T> getContent() { return content; }
        public long getTotalElements() { return totalElements; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public int getTotalPages() { return totalPages; }
        public boolean isHasNext() { return hasNext; }
        public boolean isHasPrevious() { return hasPrevious; }
        public boolean isEmpty() { return content == null || content.isEmpty(); }
    }
} 