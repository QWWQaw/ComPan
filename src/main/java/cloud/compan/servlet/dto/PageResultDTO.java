package cloud.compan.servlet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.List;
/**
 * 分页结果数据传输对象
 * 封装分页查询的结果数据，用于前端展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResultDTO<T> {
    @Valid
    private List<T> content;
    
    @PositiveOrZero(message = "总记录数不能为负数")
    private long total;
    
    @Min(value = 1, message = "页码必须大于等于1")
    private int page;
    
    @Min(value = 1, message = "每页大小必须大于等于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private int size;
    
    @PositiveOrZero(message = "总页数不能为负数")
    private int totalPages;
    
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isEmpty;

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
