# 云盘系统 RESTful API 设计文档

## 目录
1. [API 设计原则](#api-设计原则)
2. [认证与授权](#认证与授权)
3. [通用响应格式](#通用响应格式)
4. [用户管理 API](#用户管理-api)
5. [文件管理 API](#文件管理-api)
6. [文件夹管理 API](#文件夹管理-api)
7. [分享管理 API](#分享管理-api)
8. [权限管理 API](#权限管理-api)
9. [回收站 API](#回收站-api)
10. [存储统计 API](#存储统计-api)
11. [日志与通知 API](#日志与通知-api)

## API 设计原则

### 基本原则
- 使用 HTTP 动词表示操作：GET(查询)、POST(创建)、PUT(更新)、DELETE(删除)
- URL 使用名词复数形式，表示资源集合
- 使用 HTTP 状态码表示响应状态

### 版本控制
- API 版本通过 URL 路径控制：`/api/v1/`

### 基础 URL
```
https://api.kepan.com/api/v1
```

## 认证与授权

### JWT Token 认证
- 登录成功后返回 JWT token
- 所有需要认证的 API 都需要在 Header 中携带：`Authorization: Bearer <token>`
- Token 过期时间：7天（可配置）

### 权限级别
- `owner`: 文件/文件夹所有者
- `read`: 只读权限
- `write`: 读写权限
- `admin`: 管理员权限

## 通用响应格式

```http状态码
200 OK           - 请求成功
201 Created      - 创建成功
400 Bad Request  - 请求错误
401 Unauthorized - 未认证
403 Forbidden    - 权限不足
404 Not Found    - 资源不存在
500 Server Error - 服务器错误
```
### 成功响应
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2025-07-17T10:30:00Z"
}
```

### 成功响应
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2025-07-17T10:30:00Z"
}
```

### 错误响应
```json
{
  "success": false,
  "code": 400,
  "message": "请求参数错误",
  "data": {
    "error_code": "INVALID_PARAMETERS",
    "errors": [
      {
        "field": "username",
        "message": "用户名已存在"
      }
    ]
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [],
    "pagination": {
      "current_page": 1,
      "per_page": 20,
      "total_items": 100,
      "total_pages": 5,
      "has_next": true,
      "has_prev": false,
      "next_page": 2,
      "prev_page": null
    }
  }
}
```

## 用户管理 API

### 1. 用户注册
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**响应示例：**
```json
// 成功响应
{
  "success": true,
  "code": 201,
  "message": "注册成功",
  "data": {
    "user_id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "storage_limit": 10737418240,
    "storage_used": 0,
    "status": "active",
    "created_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-05-12T10:30:00Z"
}
```
```json
// 失败响应, 举例用户名已存在
{
  "success": false,
  "code": 409,
  "message": "注册失败",
  "data": {
    "errors": [
      {
        "field": "username",
        "message": "用户名已存在"
      }
    ]
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```

### 2. 用户登录
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

**响应示例：**
```json
// 成功响应
{
  "success": true,
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 604800,  // 秒是单位
    "user": {
      "user_id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "storage_limit": 10737418240,
      "storage_used": 1024000,
      "status": "active",
      "created_at": "2025-07-10T10:30:00Z",
    }
  },
  "timestamp": "2025-07-17T10:30:00Z"
}

// 失败响应 - 凭据错误
{
  "success": false,
  "code": 401,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": "2025-07-17T10:30:00Z"

}
// 失败响应 - 账户被禁用
{
  "success": false,
  "code": 403,
  "message": "账户已被禁用，请联系管理员",
  "data": {
    "status": "banned",
    "contact": "support@example.com"
  },
  "timestamp": "2025-07-17T10:30:00Z"

}
```

### 3. 用户登出
```http
POST /api/v1/auth/logout
Authorization: Bearer <token>
```
响应示例：
```json
{
  "success": true,
  "code": 200,
  "message": "成功退出账号",
  "data": null,
  "timestamp": "2025-07-17T10:30:00Z"
}
```

### 4. 获取用户信息
```http
GET /api/v1/users/profile
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "获取用户信息成功",
  "data": {
    "user_id": 123,
    "username": "john_doe",
    "email": "john@example.com",
    "storage_limit": 10737418240,
    "storage_used": 1073741824,
    "status": "active",
    "created_at": "2025-07-10T10:30:00Z",
    "updated_at": "2025-07-17T09:00:00Z",
    "last_login": "2025-07-17T10:30:00Z",
    "groups": [
      {
        "group_id": 1,
        "group_name": "开发团队",
        "role": "member"
      }
    ]
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 5. 更新用户信息
```http
PUT /api/v1/users/update-profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "john",
  "email": "newemail@example.com"
}
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "用户信息更新成功",
  "data": {
    "user_id": 123,
    "username": "john_doe_new",
    "email": "newemail@example.com",
    "storage_limit": 10737418240,
    "storage_used": 1073741824,
    "status": "active",
    "updated_at": "2025-07-17T10:30:00Z" // 用户信息更改，更新updated_at
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 6. 修改密码
```http
PUT /api/v1/users/me/password
Authorization: Bearer <token>
Content-Type: application/json

{
  "old_password": "string",
  "new_password": "string"
}
```
响应格式：
```json
// 成功响应
{
  "success": true,
  "code": 200,
  "message": "密码修改成功",
  "data": null,
  "timestamp": "2025-07-17T10:30:00Z"
}
// 失败响应 - 当前密码错误
{
  "success": false,
  "code": 400,
  "message": "当前密码错误",
  "data": null,
  "timestamp": "2025-07-17T10:30:00Z"
}
```

### 7. 获取用户存储统计
```http
GET /api/v1/users/storage-stats
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "获取存储统计成功",
  "data": {
    "storage_limit": 10737418240,
    "storage_used": 1073741824,
    "storage_available": 9663676416,
    "storage_percentage": 10.0,
    "file_count": 156,
    "folder_count": 23,
    "breakdown": {
      "documents": {
        "count": 45,
        "size": 104857600
      },
      "images": {
        "count": 67,
        "size": 536870912
      },
      "videos": {
        "count": 15,
        "size": 402653184
      },
      "others": {
        "count": 29,
        "size": 29360128
      }
    }
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```

### 8. 获取用户活动日志
```http
GET /api/v1/users/activity-log?page=1&per_page=20&operation=upload
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "获取活动日志成功",
  "data": {
    "items": [
      {
        "id": 1001,
        "operation": "file_upload",
        "details": {
          "file_name": "document.pdf",
          "file_size": 1024000,
          "folder_path": "/工作文档"
        },
        "ip_address": "192.168.1.100",
        "performed_at": "2025-07-17T10:30:00Z"
      },
      {
        "id": 1000,
        "operation": "folder_create",
        "details": {
          "folder_name": "新建文件夹",
          "parent_path": "/我的文档"
        },
        "ip_address": "192.168.1.100",
        "performed_at": "2025-07-17T10:25:00Z"
      }
    ],
    "pagination": {
      "current_page": 1,
      "per_page": 20,
      "total": 156,
      "total_pages": 8,
      "has_next": true,
      "has_prev": false,
      "next_page": "1",
      "pre_page": "2"
    }
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
## 文件管理 API

### 1. 文件上传
```http
POST /api/v1/files/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data
{
  "folder_id": number (可选，默认根目录)
  "file": <binary_data>,
  "file_size": 341
  "file_name": "document.pdf" // 默认是原文件名
}
```

**支持秒传的上传：**
```http
POST /api/v1/files/quick-upload
Authorization: Bearer <token>
Content-Type: application/json

{
  "folder_id": 1,
  "file_name": "example.txt",
  "file_hash": "sha256_hash",
  "file_size": 1024,
  "mime_type": "text/plain"
}
```
响应格式：
```json
{
  "success": true,
  "code": 201,
  "message": "文件上传成功",
  "data": {
    "file_id": 456,
    "file_name": "document.pdf",
    "file_size": 2048000,
    "mime_type": "application/pdf",
    "folder_id": 123,
    "object_hash": "sha256:abc123def456...",
    "upload_type": "normal",  // normal | fast (秒传)
    "created_at": "2025-07-17T10:30:00Z",
    "download_url": "/api/v1/files/456/download"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 2. 分片上传初始化(大文件上传)
```http
POST /api/v1/files/multipart-upload/init
Authorization: Bearer <token>
Content-Type: application/json

{
  "folder_id": 1,
  "file_name": "large_file.zip",
  "file_size": 104857600,
  "mime_type": "application/zip",
  "chunk_size": 1048576，
  "mime_type": "video/mp4"
}
```

**响应：**
```json
{
  "success": true,
  "data": 
  {
    "upload_id": "uuid-string",
    "file_name": "caife,mp4",
    "file_size": 111111,
    "total_chunks": 100,
    "chunk_size": 1048576
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```

### 3. 分片上传 // 这部分先搁置一下，后面再看
```http
PUT /api/v1/files/multipart-upload/{upload_id}/chunks/{chunk_number}
Authorization: Bearer <token>
Content-Type: application/octet-stream

[chunk data]
```

### 4. 完成分片上传 // 再议
```http
POST /api/v1/files/multipart-upload/{upload_id}/complete
Authorization: Bearer <token>
Content-Type: application/json

{
  "chunks": [
    {"chunk_number": 1, "etag": "hash1"},
    {"chunk_number": 2, "etag": "hash2"}
  ]
}
```

### 5. 获取文件列表
```http
GET /api/v1/files?folder_id=1&page=1&per_page=20&sort=name&order=asc&search=keyword
Authorization: Bearer <token>
```

**查询参数：**
- `folder_id`: 文件夹ID（可选，默认根目录）
- `page`: 页码（默认1）
- `per_page`: 每页数量（默认20，最大100）
- `sort`: 排序字段（name, size, created_at, updated_at）
- `order`: 排序方向（asc, desc）
- `search`: 搜索关键词
- `mime_type`: 文件类型过滤

### 6. 获取文件详情
```http
GET /api/v1/files/{file_id}
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "获取文件详情成功",
  "data": {
    "file_id": 123,
    "file_name": "name",
    "mime_type": "type",
    "status": true,
    "created": "2025-07-17T10:30:00Z",
    "updated_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 7. 文件下载
```http
GET /api/v1/files/{file_id}/download
Authorization: Bearer <token>
```

**支持断点续传的下载：**
```http
GET /api/v1/files/{file_id}/download
Authorization: Bearer <token>
Range: bytes=1024-2048
```
响应格式：
```http
HTTP/1.1 206 Partial Content
Content-Type: application/pdf
Content-Length: 1024000
Content-Range: bytes 1024000-2047999/2048000
Accept-Ranges: bytes

<partial_binary_data>
```

### 8. 文件流式预览
```http
GET /api/v1/files/{file_id}/preview
Authorization: Bearer <token>
```

### 9. 获取文件缩略图
```http
GET /api/v1/files/{file_id}/thumbnail?size=small
Authorization: Bearer <token>
```

### 10. 重命名文件
```http
PUT /api/v1/files/{file_id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "file_name": "new_name.txt"
}
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "文件重命名成功",
  "data": {
    "file_id": 132,
    "file_name": "string",
    "updated_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 11. 移动文件
```http
PUT /api/v1/files/{file_id}/move
Authorization: Bearer <token>
Content-Type: application/json

{
  "target_folder_id": 2
}
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "文件移动成功",
  "data": {
    "file_id": 123,
    "target_folder_id": 2,
    "moved_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 12. 复制文件
```http
POST /api/v1/files/{file_id}/copy
Authorization: Bearer <token>
Content-Type: application/json

{
  "target_folder_id": 2,
  "new_name": "copy_of_file.txt"
}
```
响应格式：
```json
{
  "success": true,
  "code": 201,
  "message": "文件复制成功",
  "data": {
    "file_id": 234,
    "original_file_id": 123,
    "target_folder_id": 2,
    "new_name": "copy_of_file.txt",
    "copied_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 13. 删除文件（移入回收站）

```http
DELETE /api/v1/files/{file_id}
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "文件已移入回收站",
  "data": {
    "file_id": 456,
    "file_name": "document.pdf",
    "deleted_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```

### 14. 批量操作文件
```http
POST /api/v1/files/batch
Authorization: Bearer <token>
Content-Type: application/json

{
  "action": "delete|move|copy",
  "file_ids": [1, 2, 3],
  "target_folder_id": 2
}
```

## 文件夹管理 API

### 1. 创建文件夹
```http
POST /api/v1/folders
Authorization: Bearer <token>
Content-Type: application/json

{
  "folder_name": "新建文件夹",
  "parent_folder_id": 1
}
```

### 2. 获取文件夹列表
```http
GET /api/v1/folders?parent_id=1&page=1&per_page=20
Authorization: Bearer <token>
```

### 3. 获取文件夹详情
```http
GET /api/v1/folders/{folder_id}
Authorization: Bearer <token>
```

### 4. 获取文件夹内容（文件+子文件夹）
```http
GET /api/v1/folders/{folder_id}/contents?page=1&per_page=20&sort=name&order=asc
Authorization: Bearer <token>
```

### 5. 获取文件夹路径
```http
GET /api/v1/folders/{folder_id}/path
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "success": true,
  "code": 201,
  "data": {
    "full_path": "/根目录/文档/工作文件",
    "path_items": [
      {"folder_id": null, "name": "根目录"},
      {"folder_id": 1, "name": "文档"},
      {"folder_id": 2, "name": "工作文件"}
    ]
  }
}
```

### 6. 重命名文件夹
```http
PUT /api/v1/folders/{folder_id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "folder_name": "新名称"
}
```

### 7. 移动文件夹
```http
PUT /api/v1/folders/{folder_id}/move
Authorization: Bearer <token>
Content-Type: application/json

{
  "target_parent_id": 3
}
```

### 8. 删除文件夹
```http
DELETE /api/v1/folders/{folder_id}
Authorization: Bearer <token>
```

### 9. 获取文件夹大小统计
```http
GET /api/v1/folders/{folder_id}/size
Authorization: Bearer <token>
```

## 分享管理 API

### 1. 创建分享链接
```http
POST /api/v1/shares
Authorization: Bearer <token>
Content-Type: application/json

{
  "file_id": 1,          // 二选一
  "folder_id": 1,        // 二选一
  "password": "1234",    // 可选
  "expire_at": "2025-08-17T10:30:00Z"  // 可选
  "allow_download": true,
  "allow_preview": true
}
```
响应格式：
```json
{
  "success": true,
  "code": 201,
  "message": "分享链接创建成功",
  "data": {
  "shard_id": 1,
  "share_link": "abds",
  "share_url": "htt-----",
  "item_type": "file/floder",
  "item_info": {
     "id": 3242,
     "name": "name",
     "size": 141324,
     "mime_type": "pdf",
     "folder_path": "/"
  },
  "settings": {
      "password_protected": true,
      "expire_at": "2025-08-17T10:30:00Z",
      "allow_download": true,
      "allow_preview": true,
    },
  "created_at": "2025-07-17T10:30:00Z",
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 2. 获取我的分享列表
```http
GET /api/v1/shares?page=1&per_page=20
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "获取分享列表成功",
  "data": {
    "items": [
      {
        "share_id": 1,
        "share_link": "abc123def456ghi789",
        "share_url": "https://kepan.com/s/abc123def456ghi789",
        "item_type": "file",
        "item_info": {
          "id": 456,
          "name": "重要文档.pdf",
          "size": 2048000,
          "mime_type": "application/pdf",
          "folder_path": "/我的文档/工作文件"
        },
        "settings": {
          "password_protected": true,
          "expire_at": "2025-08-17T10:30:00Z",
          "allow_download": true,
          "allow_preview": true
        },
        "created_at": "2025-07-17T10:30:00Z"
      },
    ],
    "pagination": {
      "current_page": 1,
      "per_page": 20,
      "total": 15,
      "total_pages": 1,
      "has_next": false,
      "has_prev": false
    },
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 3. 获取分享详情
```http
GET /api/v1/shares/{share_link}
```
响应格式：
```json
// 成功获取
{
  "success": true,
  "code": 200,
  "message": "获取分享信息成功",
  "data": {
    "share_link": "abc123def456ghi789",
    "item_type": "file",
    "item_info": {
    "id": 123,
      "name": "重要文档.pdf",
      "size": 2048000,
      "mime_type": "application/pdf",
    },
    "shared_by": {
      "username": "john_doe",
    },
    "settings": {
      "password_protected": true,
      "expire_at": "2025-08-17T10:30:00Z",
      "allow_download": true,
      "allow_preview": true
    },
    "created_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
```json
// 失败获取
{
  "success": false,
  "code": 404,
  "message": "分享链接不存在或已过期",
  "data": {
    "error_type": "share_not_found",
    "share_link": "invalid_link"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 4. 访问分享内容
```http
POST /api/v1/shares/{share_link}/access
Content-Type: application/json

{
  "password": "1234"  // 如果设置了密码
}
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "访问验证成功",
  "data": {
    "access_token": "temp_access_token_xyz789",
    "expires_in": 3600,
    "item_type": "file",
    "item_info": {
      "id": 456,
      "name": "重要文档.pdf",
      "size": 2048000,
      "mime_type": "application/pdf"
    },
    "access_urls": {
      "download": "/api/v1/shares/abc123def456ghi789/download?token=temp_access_token_xyz789",
      "preview": "/api/v1/shares/abc123def456ghi789/preview?token=temp_access_token_xyz789"
    }
  },
  "timestamp": "2025-07-17T10:30:00Z"
}

```
### 5. 删除分享
```http
DELETE /api/v1/shares/{share_id}
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "权限已删除",
  "data": {
    "permission_id": 101,
    "revoked_permission": "read",
    "revoked_from": {
      "type": "user",
      "username": "jane_smith"
    },
    "item_info": {
      "type": "file",
      "name": "重要文档.pdf"
    },
    "deleted_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
## 权限管理 API

### 1. 设置文件/文件夹权限
```http
POST /api/v1/permissions
Authorization: Bearer <token>
Content-Type: application/json

{
  "file_id": 1,          // 二选一
  "folder_id": 1,        // 二选一
  "user_id": 2,          // 和 group_id 二选一
  "group_id": 1,         // 和 user_id 二选一
  "permission": "read"   // read, write, admin
}
```
响应格式：
```json
{
  "success": true,
  "code": 201,
  "message": "权限设置成功",
  "data": {
    "permission_id": 101,
    "item_type": "file",
    "item_info": {
      "id": 456,
      "name": "重要文档.pdf",
      "owner": "john_doe"
    },
    "created_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 2. 获取权限列表
```http
GET /api/v1/permissions?file_id=1&page=1&per_page=20
Authorization: Bearer <token>
```

### 3. 更新权限
```http
PUT /api/v1/permissions/{permission_id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "permission": "write"
}
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "权限更新成功",
  "data": {
    "permission_id": 101,
    "old_permission": "read",
    "new_permission": "write",
    "granted_to": {
      "username": "jane_smith"
    },
    "item_info": {
      "type": "file",
      "name": "重要文档.pdf"
    },
    "updated_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 4. 删除权限
```http
DELETE /api/v1/permissions/{permission_id}
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "权限已删除",
  "data": {
    "permission_id": 101,
    "revoked_permission": "read",
    "revoked_from": {
      "type": "user",
      "username": "jane_smith"
    },
    "item_info": {
      "type": "file",
      "name": "重要文档.pdf"
    },
    "deleted_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```

## 回收站 API

### 1. 获取回收站内容
```http
GET /api/v1/recycle-bin?page=1&per_page=20&item_type=file
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "获取回收站内容成功",
  "data": {
    "items": [
      {
        "item_type": "file",
        "id": 456,
        "name": "已删除文档.pdf",
        "original_path": "/我的文档/工作文件/已删除文档.pdf",
        "size": 2048000,
        "mime_type": "application/pdf",
        "folder_id": 123,
        "folder_name": "工作文件",
        "deleted_at": "2025-07-17T10:30:00Z",
        "auto_delete_at": "2025-08-17T10:30:00Z",  // 30天后自动删除
        "days_until_permanent_delete": 30,
        "can_restore": true,
        "restore_conflicts": false
      },
    ],
    "pagination": {
      "current_page": 1,
      "per_page": 20,
      "total": 45,
      "total_pages": 3,
      "has_next": true,
      "has_prev": false
    },
    "cleanup_policy": {
      "retention_days": 30,
      "auto_cleanup_enabled": true,
      "next_cleanup_at": "2025-07-18T00:00:00Z"
    }
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 2. 恢复文件/文件夹
```http
POST /api/v1/recycle-bin/{item_id}/restore
Authorization: Bearer <token>
Content-Type: application/json

{
  "item_type": "file",  // file 或 folder
  "target_folder_id": 1  // 恢复到指定文件夹，可选
}
```
```json
{
  "success": true,
  "code": 200,
  "message": "文件恢复成功",
  "data": {
    "item_type": "file",
    "id": 456,
    "name": "已删除文档.pdf",
    "restored_to": {
      "folder_id": 123,
      "folder_name": "工作文件",
      "folder_path": "/我的文档/工作文件"
    },
    "original_location": true,  // 是否恢复到原位置
    "final_name": "已删除文档.pdf",  // 最终文件名
    "restored_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 3. 彻底删除
```http
DELETE /api/v1/recycle-bin/{item_id}
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "文件已彻底删除",
  "data": {
    "item_type": "file",
    "id": 456,
    "name": "已删除文档.pdf",
    "size": 2048000,
    "permanently_deleted_at": "2025-07-17T10:30:00Z",
    "physical_file_deleted": true  // 是否删除了物理文件
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 4. 清空回收站
```http
DELETE /api/v1/recycle-bin
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "回收站已清空",
  "data": {
    "files_deleted": 32,
    "folders_deleted": 13,
    "total_storage_freed": 1073741824,
    "cleanup_completed_at": "2025-07-17T10:30:00Z",
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
## 存储统计 API

### 1. 获取用户存储统计
```http
GET /api/v1/storage/summary
Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "success": true,
  "data": {
    "storage_limit": 10737418240,
    "storage_used": 1073741824,
    "usage_percentage": 10.0,
    "file_count": 156,
    "folder_count": 23,
    "breakdown": {
      "images": {
        "size": 536870912,
        "count": 45
      },
      "videos": {
        "size": 268435456,
        "count": 12
      },
      "documents": {
        "size": 134217728,
        "count": 89
      },
      "others": {
        "size": 134217728,
        "count": 10
      }
    }
  },
  "timestamp": ""2025-07-17T10:30:00Z""
}
```

### 2. 获取存储使用趋势
```http
GET /api/v1/storage/usage-trend?days=30
Authorization: Bearer <token>
```

## 日志与通知 API

### 1. 获取操作日志
```http
GET /api/v1/logs?page=1&per_page=20&operation=upload&start_date=2025-07-01&end_date=2025-07-17
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "获取操作日志成功",
  "data": {
    "logs": [
      {
        "id": 1001,
        "operation": "file_upload",
        "operation_name": "文件上传",
        "details": {
          "file_id": 456,
          "file_name": "重要文档.pdf",
          "file_size": 2048000,
          "folder_name": "工作文档"
        },
        "ip_address": "192.168.1.100",
        "performed_at": "2025-07-17T10:30:00Z"
      }
    ],
    "total_count": 156,
    "returned_count": 50,
    "has_more": true,
    "filter_summary": {
      "operation": "upload",
      "date_range": "2025-07-01 至 2025-07-17",
      "matched_records": 45
    }
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 2. 获取通知列表
```http
GET /api/v1/notifications?page=1&per_page=20&is_read=false
Authorization: Bearer <token>
```
响应格式
```json
{
  "success": true,
  "code": 200,
  "message": "获取通知列表成功",
  "data": {
    "notifications": [
      {
        "id": 501,
        "message": "john_doe 与您分享了文件「重要项目文档.pdf」",
        "is_read": false,
        "created_at": "2025-07-17T10:30:00Z"
      },
      {
        "id": 500,
        "message": "您的存储空间已使用90%，建议清理不必要的文件",
        "is_read": false,
        "created_at": "2025-07-17T08:30:00Z"
      }
    ],
    "unread_count": 8,
    "total_count": 25
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 3. 标记通知为已读
```http
PUT /api/v1/notifications/{notification_id}/read
Authorization: Bearer <token>
```
响应格式： ```
```json
{
  "success": true,
  "code": 200,
  "message": "通知已标记为已读",
  "data": {
    "notification_id": 501,
    "updated_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 4. 标记所有通知为已读
```http
PUT /api/v1/notifications/read-all
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "所有通知已标记为已读",
  "data": {
    "updated_count": 8
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 5. 删除通知
```http
DELETE /api/v1/notifications/{notification_id}
Authorization: Bearer <token>
```
响应格式
```json
{
  "success": true,
  "code": 200,
  "message": "通知已删除",
  "data": {
    "notification_id": 501
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
## 用户组管理 API

### 1. 创建用户组
```http
POST /api/v1/user-groups
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "开发团队",
  "description": "软件开发团队"
}
```
响应格式：
```json
{
  "success": true,
  "code": 201,
  "message": "用户组创建成功",
  "data": {
    "group_id": 5,
    "name": "开发团队",
    "description": "软件开发团队",
    "member_count": 0,
    "created_at": "2025-07-17T10:30:00Z"
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 2. 获取用户组列表
```http
GET /api/v1/user-groups?page=1&per_page=20
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "获取用户组列表成功",
  "data": {
    "groups": [
      {
        "group_id": 5,
        "name": "开发团队",
        "description": "软件开发团队",
        "member_count": 12,
      },
      {
        "group_id": 6,
        "name": "设计团队",
        "description": "UI/UX设计师团队",
        "member_count": 8,
      }
    ],
    "total_count": 15
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 3. 添加用户到组
```http
POST /api/v1/user-groups/{group_id}/members
Authorization: Bearer <token>
Content-Type: application/json

{
  "user_id": 2,
  "role": "member"  // member, admin
}
```
响应格式：
```json
{
  "success": true,
  "code": 201,
  "message": "用户添加到组成功",
  "data": {
    "group_id": 5,
    "group_name": "开发团队",
    "added_user": {
      "user_id": 789,
      "username": "new_member",
      "role": "member"
    },
    "total_members": 13
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
### 4. 移除用户组成员
```http
DELETE /api/v1/user-groups/{group_id}/members/{user_id}
Authorization: Bearer <token>
```
响应格式：
```json
{
  "success": true,
  "code": 200,
  "message": "用户已从组中移除",
  "data": {
    "group_id": 5,
    "group_name": "开发团队",
    "removed_user": {
      "user_id": 789,
      "username": "removed_member"
    },
    "remaining_members": 11
  },
  "timestamp": "2025-07-17T10:30:00Z"
}
```
## HTTP 状态码说明

- `200 OK`: 请求成功
- `201 Created`: 创建成功
- `204 No Content`: 删除成功
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 无权限
- `404 Not Found`: 资源不存在
- `409 Conflict`: 资源冲突（如文件名重复）
- `413 Payload Too Large`: 文件过大
- `422 Unprocessable Entity`: 参数验证失败
- `429 Too Many Requests`: 请求频率限制
- `500 Internal Server Error`: 服务器内部错误

## 错误代码说明

```json
{
  "success": false,
  "code": 40001,
  "message": "文件名已存在",
  "error": "FILE_NAME_ALREADY_EXISTS"
}
```

### 根据情景可以设计错误代码：
例如：
- 40001 文件名已存在
- 40002 文件名不合法
- 
这个 API 设计涵盖了云盘系统的核心功能。

1. **第一阶段**：用户认证、基础文件上传下载、文件夹管理
2. **第二阶段**：权限管理、分享功能、回收站
3. **第三阶段**：分片上传、断点续传、在线预览
4. **第四阶段**：高级功能如 P2P 下载、多线程下载等
