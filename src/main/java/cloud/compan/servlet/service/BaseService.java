package cloud.compan.servlet.service;

import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.SearchCriteria;

import java.util.List;

/**
 * 基础服务接口
 * 定义所有服务层的通用操作
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public interface BaseService<T, ID> {
    
    // ============ 基础CRUD操作 ============
    
    /**
     * 根据ID查找实体
     */
    ServiceResult<T> findById(ID id);
    
    /**
     * 查找所有实体
     */
    ServiceResult<List<T>> findAll();
    
    /**
     * 保存实体
     */
    ServiceResult<T> save(T entity);
    
    /**
     * 根据ID删除实体
     */
    ServiceResult<Boolean> deleteById(ID id);
    
    /**
     * 删除实体
     */
    ServiceResult<Boolean> delete(T entity);
    
    /**
     * 根据ID更新实体
     */
    ServiceResult<T> updateById(ID id, T entity);
    
    // ============ 扩展查询操作 ============
    
    /**
     * 分页查询
     */
    ServiceResult<PageResultDTO<T>> findPage(int page, int size);
    
    /**
     * 根据条件查询
     */
    ServiceResult<List<T>> findByCriteria(SearchCriteria criteria);
    
    /**
     * 根据条件分页查询
     */
    ServiceResult<PageResultDTO<T>> findPageByCriteria(SearchCriteria criteria);
    
    /**
     * 批量保存
     */
    ServiceResult<List<T>> saveAll(List<T> entities);
    
    /**
     * 批量删除
     */
    ServiceResult<Integer> deleteByIds(List<ID> ids);
    
    /**
     * 检查实体是否存在
     */
    ServiceResult<Boolean> existsById(ID id);
    
    /**
     * 统计实体数量
     */
    ServiceResult<Long> count();
    
    /**
     * 根据条件统计数量
     */
    ServiceResult<Long> countByCriteria(SearchCriteria criteria);
} 