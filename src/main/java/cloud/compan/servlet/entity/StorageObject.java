package cloud.compan.servlet.entity;

import java.sql.Timestamp;

/**
 * 存储对象实体类
 * 对应数据库表：storage_object
 * 用于文件去重和秒传功能
 */
public class StorageObject {
    private String hash;              // hash VARCHAR(255) NOT NULL PRIMARY KEY
    private Long size;                // size BIGINT NOT NULL
    private String storagePath;       // storage_path VARCHAR(1024) NOT NULL
    private Integer refCount;         // ref_count INT UNSIGNED NOT NULL DEFAULT 1
    private Timestamp createdAt;      // created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

    // 构造函数
    public StorageObject() {}

    public StorageObject(String hash, Long size, String storagePath) {
        this.hash = hash;
        this.size = size;
        this.storagePath = storagePath;
        this.refCount = 1;
    }

    // Getters and Setters
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public Integer getRefCount() { return refCount; }
    public void setRefCount(Integer refCount) { this.refCount = refCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "StorageObject{" +
                "hash='" + hash + '\'' +
                ", size=" + size +
                ", storagePath='" + storagePath + '\'' +
                ", refCount=" + refCount +
                '}';
    }
}
