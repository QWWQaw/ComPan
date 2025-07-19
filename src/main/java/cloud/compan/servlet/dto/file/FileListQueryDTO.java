package cloud.compan.servlet.dto.file;

import cloud.compan.servlet.dto.common.PaginationDTO;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件列表查询请求DTO
 * 用于Handler层接收文件列表查询参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileListQueryDTO {
    @Min(value = 1, message = "文件夹ID必须大于0")
    private Long folderId; // 文件夹ID，null表示根目录

    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1; // 页码，默认第1页

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer perPage = 20; // 每页数量，默认20

    @Pattern(regexp = "^(name|size|created_at|updated_at)$", message = "排序字段只能是name、size、created_at或updated_at")
    private String sortBy = "name"; // 排序字段：name, size, created_at, updated_at

    @Pattern(regexp = "^(asc|desc)$", message = "排序顺序只能是asc或desc")
    private String sortOrder = "asc"; // 排序顺序：asc, desc

    @Size(max = 100, message = "搜索关键字长度不能超过100个字符")
    private String search; // 搜索关键字

    @Pattern(regexp = "^(image|document|video|audio|other)$", message = "文件类型只能是image、document、video、audio或other")
    private String fileType; // 文件类型过滤：image, document, video, audio, other

    /**
     * 验证参数是否有效
     */
    public boolean isValid() {
        return page > 0 && perPage > 0 && perPage <= 100;
    }
}

/**
 * 文件列表响应DTO
 * 用于Service层返回分页的文件列表
 */
class FileListResponseDTO {
    private List<FileInfoDTO> files;
    private PaginationDTO pagination;
    private Long totalSize; // 当前文件夹总文件大小
    private Integer fileCount; // 文件数量
    private Integer folderCount; // 文件夹数量

    // 构造函数
    public FileListResponseDTO() {}

    public FileListResponseDTO(List<FileInfoDTO> files, PaginationDTO pagination) {
        this.files = files;
        this.pagination = pagination;
    }

    // Getters and Setters
    public List<FileInfoDTO> getFiles() { return files; }
    public void setFiles(List<FileInfoDTO> files) { this.files = files; }

    public PaginationDTO getPagination() { return pagination; }
    public void setPagination(PaginationDTO pagination) { this.pagination = pagination; }

    public Long getTotalSize() { return totalSize; }
    public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }

    public Integer getFileCount() { return fileCount; }
    public void setFileCount(Integer fileCount) { this.fileCount = fileCount; }

    public Integer getFolderCount() { return folderCount; }
    public void setFolderCount(Integer folderCount) { this.folderCount = folderCount; }
}
