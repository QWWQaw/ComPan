package cloud.compan.servlet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;

/**
 * 通用查询条件封装类
 * 用于封装各种查询参数，支持分页、排序、关键词搜索等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCriteria {
    /**
     * 搜索关键词
     */
    @Size(max = 100, message = "搜索关键词长度不能超过100个字符")
    private String keyword;
    
    /**
     * 排序字段
     */
    @Size(max = 50, message = "排序字段长度不能超过50个字符")
    private String sortBy;
    
    /**
     * 排序方向 ASC/DESC
     */
    @Pattern(regexp = "^(ASC|DESC)$", message = "排序方向只能是ASC或DESC")
    @Builder.Default
    private String sortDirection = "ASC";
    
    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码必须大于等于1")
    @Max(value = 1000, message = "页码不能超过1000")
    @Builder.Default
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于等于1")
    @Max(value = 100, message = "每页大小不能超过100")
    @Builder.Default
    private Integer size = 20;

    /**
     * 扩展过滤条件（JSON格式或Map）
     */
    private Object filters;


    public SearchCriteria(String keyword) {
        this.keyword = keyword;
    }

    // Builder pattern methods
    public SearchCriteria keyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public SearchCriteria sortBy(String sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    public SearchCriteria sortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
        return this;
    }

    public SearchCriteria page(Integer page) {
        this.page = page;
        return this;
    }

    public SearchCriteria size(Integer size) {
        this.size = size;
        return this;
    }

    public SearchCriteria filters(Object filters) {
        this.filters = filters;
        return this;
    }

    // Getters and Setters
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Object getFilters() {
        return filters;
    }

    public void setFilters(Object filters) {
        this.filters = filters;
    }

    /**
     * 检查是否有关键词搜索
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * 检查是否有排序
     */
    public boolean hasSort() {
        return sortBy != null && !sortBy.trim().isEmpty();
    }
    
    /**
     * 检查是否有扩展过滤条件
     */
    public boolean hasFilters() {
        return filters != null;
    }
}
