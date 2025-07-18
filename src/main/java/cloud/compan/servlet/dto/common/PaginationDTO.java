package cloud.compan.servlet.dto.common;

/**
 * 分页信息DTO
 * 用于文件列表、用户日志等分页查询的响应
 */
public class PaginationDTO {
    private int currentPage;
    private int perPage;
    private int total;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrev;

    // 构造函数
    public PaginationDTO() {}

    public PaginationDTO(int currentPage, int perPage, int total) {
        this.currentPage = currentPage;
        this.perPage = perPage;
        this.total = total;
        this.totalPages = (total + perPage - 1) / perPage;
        this.hasNext = currentPage < totalPages;
        this.hasPrev = currentPage > 1;
    }

    // Getters and Setters
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getPerPage() { return perPage; }
    public void setPerPage(int perPage) { this.perPage = perPage; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public boolean isHasPrev() { return hasPrev; }
    public void setHasPrev(boolean hasPrev) { this.hasPrev = hasPrev; }
}
