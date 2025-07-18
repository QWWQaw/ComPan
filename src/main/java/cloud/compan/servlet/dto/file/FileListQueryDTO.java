package cloud.compan.servlet.dto.file;

import cloud.compan.servlet.dto.common.PaginationDTO;
import java.util.List;

/**
 * 文件列表查询请求DTO
 * 用于Handler层接收文件列表查询参数
 */
public class FileListQueryDTO {
    private Long folderId; // 文件夹ID，null表示根目录
    private Integer page = 1; // 页码，默认第1页
    private Integer perPage = 20; // 每页数量，默认20
    private String sortBy = "name"; // 排序字段：name, size, created_at, updated_at
    private String sortOrder = "asc"; // 排序顺序：asc, desc
    private String search; // 搜索关键字
    private String fileType; // 文件类型过滤：image, document, video, audio, other

    // 构造函数
    public FileListQueryDTO() {}

    // Getters and Setters
    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) {
        this.page = page != null && page > 0 ? page : 1;
    }

    public Integer getPerPage() { return perPage; }
    public void setPerPage(Integer perPage) {
        this.perPage = perPage != null && perPage > 0 && perPage <= 100 ? perPage : 20;
    }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) {
        if (sortBy != null && (sortBy.equals("name") || sortBy.equals("size") ||
            sortBy.equals("created_at") || sortBy.equals("updated_at"))) {
            this.sortBy = sortBy;
        }
    }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) {
        if ("desc".equalsIgnoreCase(sortOrder)) {
            this.sortOrder = "desc";
        } else {
            this.sortOrder = "asc";
        }
    }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

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
