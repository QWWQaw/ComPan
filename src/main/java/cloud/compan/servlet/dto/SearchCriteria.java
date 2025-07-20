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

    // 验证方法
    public boolean isValidPage() {
        return page != null && page >= 1;
    }

    public boolean isValidSize() {
        return size != null && size >= 1 && size <= 100;
    }

    public boolean isValidSortDirection() {
        return sortDirection != null && 
               (sortDirection.equalsIgnoreCase("ASC") || sortDirection.equalsIgnoreCase("DESC"));
    }

    // 计算偏移量
    public int getOffset() {
        if (page == null || size == null) {
            return 0;
        }
        return (page - 1) * size;
    }

    // 获取标准化的排序方向
    public String getNormalizedSortDirection() {
        if (sortDirection == null) {
            return "ASC";
        }
        return sortDirection.toUpperCase();
    }

    // 工具方法
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasSortBy() {
        return sortBy != null && !sortBy.trim().isEmpty();
    }

    public boolean hasFilters() {
        return filters != null;
    }

    // 清理空白字符
    public void trimValues() {
        if (keyword != null) {
            keyword = keyword.trim();
        }
        if (sortBy != null) {
            sortBy = sortBy.trim();
        }
        if (sortDirection != null) {
            sortDirection = sortDirection.trim().toUpperCase();
        }
    }

    // 设置默认值
    public void setDefaults() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 20;
        }
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "ASC";
        }
    }
    
    // Manual getter methods
    public Integer getPage() {
        return page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public Object getFilters() {
        return filters;
    }

}
