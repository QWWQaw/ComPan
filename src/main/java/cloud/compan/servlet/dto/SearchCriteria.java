package cloud.compan.servlet.dto;
import lombok.Data;

/**
 * 通用查询条件封装类
 * 用于封装各种查询参数，支持分页、排序、关键词搜索等
 */
@Data
public class SearchCriteria {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 排序字段
     */
    private String sortBy;
    
    /**
     * 排序方向 ASC/DESC
     */
    private String sortDirection = "ASC";
    
    /**
     * 页码（从1开始）
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 20;

    /**
     * 扩展过滤条件（JSON格式或Map）
     */
    private Object filters;

    public SearchCriteria() {}

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
