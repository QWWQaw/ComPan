package cloud.compan.servlet.dto.folder;

/**
 * 文件夹创建请求DTO
 */
class FolderCreateDTO {
    private String folderName;
    private Long parentFolderId;
    private String description;

    // 构造函数
    public FolderCreateDTO() {}

    public FolderCreateDTO(String folderName, Long parentFolderId, String description) {
        this.folderName = folderName;
        this.parentFolderId = parentFolderId;
        this.description = description;
    }

    // Getters and Setters
    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public Long getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(Long parentFolderId) { this.parentFolderId = parentFolderId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // 验证方法
    public boolean isValid() {
        return folderName != null && !folderName.trim().isEmpty() && folderName.length() <= 255;
    }

    @Override
    public String toString() {
        return "FolderCreateDTO{" +
                "folderName='" + folderName + '\'' +
                ", parentFolderId=" + parentFolderId +
                ", description='" + description + '\'' +
                '}';
    }
}

/**
 * 文件夹重命名请求DTO
 */
class FolderRenameDTO {
    private Long folderId;
    private String newFolderName;
    private String description;

    // 构造函数
    public FolderRenameDTO() {}

    public FolderRenameDTO(Long folderId, String newFolderName, String description) {
        this.folderId = folderId;
        this.newFolderName = newFolderName;
        this.description = description;
    }

    // Getters and Setters
    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public String getNewFolderName() { return newFolderName; }
    public void setNewFolderName(String newFolderName) { this.newFolderName = newFolderName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // 验证方法
    public boolean isValid() {
        return folderId != null && newFolderName != null && !newFolderName.trim().isEmpty();
    }
}

/**
 * 文件夹移动请求DTO
 */
class FolderMoveDTO {
    private Long folderId;
    private Long targetParentId;

    // 构造函数
    public FolderMoveDTO() {}

    public FolderMoveDTO(Long folderId, Long targetParentId) {
        this.folderId = folderId;
        this.targetParentId = targetParentId;
    }

    // Getters and Setters
    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public Long getTargetParentId() { return targetParentId; }
    public void setTargetParentId(Long targetParentId) { this.targetParentId = targetParentId; }

    // 验证方法
    public boolean isValid() {
        return folderId != null;
    }
}
