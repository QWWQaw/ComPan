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
    private Long totalElements;  // 修正属性名

    @Min(value = 0, message = "页码必须大于等于0")
    private Integer currentPage; // 修正属性名，使用0基索引

    @Min(value = 1, message = "每页大小必须大于等于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize;    // 修正属性名

    @PositiveOrZero(message = "总页数不能为负数")
    private Integer totalPages;

    private Boolean hasNext;
    private Boolean hasPrevious;
    private Boolean isEmpty;

    // 计算派生属性的方法
    public Boolean getHasNext() {
        return currentPage < totalPages - 1;
    }

    public Boolean getHasPrevious() {
        return currentPage > 0;
    }

    public Boolean getIsEmpty() {
        return content == null || content.isEmpty();
    }
}
