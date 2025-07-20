package cloud.compan.servlet.dto;
import lombok.Data;

import java.util.List;

/**
 * 分页结果数据传输对象
 * 封装分页查询的结果数据，用于前端展示
 */
@Data
public class PageResultDTO<T> {
    private List<T> content;
    private long total;
    private int page;
    private int size;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isEmpty;
    
    public PageResultDTO() {}

    public PageResultDTO(List<T> content, long total, int page, int size) {
        this.content = content;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
        this.hasNext = page < totalPages;
        this.hasPrevious = page > 1;
        this.isEmpty = content == null || content.isEmpty();
    }

    // getters and setters

    public void setContent(List<T> content) {
        this.content = content;
        this.isEmpty = content == null || content.isEmpty();
    }

    public void setTotal(long total) {
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
        this.hasNext = page < totalPages;
    }
    

    public void setPage(int page) {
        this.page = page;
        this.hasNext = page < totalPages;
        this.hasPrevious = page > 1;
    }

    public void setSize(int size) {
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
        this.hasNext = page < totalPages;
    }



}
