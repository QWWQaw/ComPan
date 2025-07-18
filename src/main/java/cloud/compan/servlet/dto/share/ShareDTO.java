package cloud.compan.servlet.dto.share;

/**
 * 分享创建请求DTO
 */
class ShareCreateDTO {
    private String resourceType; // "file" 或 "folder"
    private Long resourceId;
    private Integer expiryDays;
    private String password;
    private String description;

    // 构造函数
    public ShareCreateDTO() {}

    public ShareCreateDTO(String resourceType, Long resourceId, Integer expiryDays, String password, String description) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.expiryDays = expiryDays;
        this.password = password;
        this.description = description;
    }

    // Getters and Setters
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public Integer getExpiryDays() { return expiryDays; }
    public void setExpiryDays(Integer expiryDays) { this.expiryDays = expiryDays; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // 验证方法
    public boolean isValid() {
        return resourceType != null &&
               (resourceType.equals("file") || resourceType.equals("folder")) &&
               resourceId != null;
    }

    @Override
    public String toString() {
        return "ShareCreateDTO{" +
                "resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", expiryDays=" + expiryDays +
                ", hasPassword=" + (password != null && !password.isEmpty()) +
                ", description='" + description + '\'' +
                '}';
    }
}
