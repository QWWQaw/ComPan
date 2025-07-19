package cloud.compan.servlet.dto.common;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 分页信息DTO
 * 用于文件列表、用户日志等分页查询的响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDTO {
    @Min(value = 1, message = "当前页码必须大于0")
    private int currentPage;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private int perPage;

    @Min(value = 0, message = "总记录数不能为负数")
    private int total;

    @Min(value = 0, message = "总页数不能为负数")
    private int totalPages;

    @NotNull(message = "是否有下一页不能为空")
    private boolean hasNext;

    @NotNull(message = "是否有上一页不能为空")
    private boolean hasPrev;

}
