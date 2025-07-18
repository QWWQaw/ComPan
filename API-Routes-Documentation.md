# ComPan 云盘系统 - API 路由文档

## 📋 概述

本文档详细列出了 ComPan 云盘系统的所有 API 路由，包括已实现的可用路由和计划中的不可用路由。

**项目架构**：三层架构 (Handler → Service → Repository)  
**基础路径**：`/api/v1/`  
**响应格式**：JSON  
**认证方式**：Session 认证

---

## ✅ 可用路由 (已实现)

### 🔐 用户认证模块 (`/api/v1/auth/`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `POST` | `/api/v1/auth/register` | 用户注册 | ❌ 无 | ✅ 可用 |
| `POST` | `/api/v1/auth/login` | 用户登录 | ❌ 无 | ✅ 可用 |
| `POST` | `/api/v1/auth/logout` | 用户登出 | ❌ 无 | ✅ 可用 |

**请求示例**：
```bash
# 注册
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=testuser&email=test@example.com&password=123456"

# 登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=testuser&password=123456"
```

---

### 👤 用户管理模块 (`/api/v1/users/`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `GET` | `/api/v1/users/profile` | 获取用户资料 | ✅ 需要登录 | ✅ 可用 |
| `PUT` | `/api/v1/users/profile` | 更新用户资料 | ✅ 需要登录 | ✅ 可用 |
| `GET` | `/api/v1/users/storage-stats` | 获取存储统计 | ✅ 需要登录 | ✅ 可用 |
| `POST` | `/api/v1/users/change-password` | 修改密码 | ✅ 需要登录 | ✅ 可用 |

**请求示例**：
```bash
# 获取用户资料
curl -X GET http://localhost:8080/api/v1/users/profile \
  -H "Cookie: JSESSIONID=your_session_id"

# 获取存储统计
curl -X GET http://localhost:8080/api/v1/users/storage-stats \
  -H "Cookie: JSESSIONID=your_session_id"
```

---

### 📁 文件夹管理模块 (`/api/v1/folders/`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `POST` | `/api/v1/folders/create` | 创建文件夹 | ✅ 需要登录 | ✅ 可用 |
| `GET` | `/api/v1/folders/list` | 获取文件夹列表 | ✅ 需要登录 | ✅ 可用 |
| `PUT` | `/api/v1/folders/rename` | 重命名文件夹 | ✅ 需要登录 | ✅ 可用 |
| `DELETE` | `/api/v1/folders/delete` | 删除文件夹 | ✅ 需要登录 | ✅ 可用 |

**请求示例**：
```bash
# 创建文件夹
curl -X POST http://localhost:8080/api/v1/folders/create \
  -H "Cookie: JSESSIONID=your_session_id" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "folder_name=NewFolder&parent_folder_id=1"

# 获取文件夹列表
curl -X GET "http://localhost:8080/api/v1/folders/list?parent_folder_id=1" \
  -H "Cookie: JSESSIONID=your_session_id"
```

---

### 📄 文件管理模块 (`/api/v1/files/`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `POST` | `/api/v1/files/upload` | 文件上传 | ✅ 需要登录 | ✅ 可用 |
| `GET` | `/api/v1/files/list` | 获取文件列表 | ✅ 需要登录 | ✅ 可用 |
| `GET` | `/api/v1/files/download` | 文件下载 | ✅ 需要登录 | ✅ 可用 |
| `DELETE` | `/api/v1/files/delete` | 删除文件 | ✅ 需要登录 | ✅ 可用 |
| `PUT` | `/api/v1/files/rename` | 重命名文件 | ✅ 需要登录 | ✅ 可用 |
| `POST` | `/api/v1/files/move` | 移动文件 | ✅ 需要登录 | ✅ 可用 |

**请求示例**：
```bash
# 文件上传
curl -X POST http://localhost:8080/api/v1/files/upload \
  -H "Cookie: JSESSIONID=your_session_id" \
  -F "file=@/path/to/file.txt" \
  -F "folder_id=1"

# 获取文件列表
curl -X GET "http://localhost:8080/api/v1/files/list?folder_id=1&page=1&per_page=20" \
  -H "Cookie: JSESSIONID=your_session_id"

# 文件下载
curl -X GET "http://localhost:8080/api/v1/files/download?file_id=123" \
  -H "Cookie: JSESSIONID=your_session_id" \
  -o downloaded_file.txt
```

---

### 🔗 分享管理模块 (`/api/v1/shares/`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `POST` | `/api/v1/shares/create` | 创建分享链接 | ✅ 需要登录 | ✅ 可用 |
| `GET` | `/api/v1/shares/list` | 获取分享列表 | ✅ 需要登录 | ✅ 可用 |
| `DELETE` | `/api/v1/shares/delete` | 删除分享 | ✅ 需要登录 | ✅ 可用 |
| `GET` | `/api/v1/shares/access` | 访问分享 | ❌ 无 | ✅ 可用 |

**请求示例**：
```bash
# 创建分享链接
curl -X POST http://localhost:8080/api/v1/shares/create \
  -H "Cookie: JSESSIONID=your_session_id" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "file_id=123&password=1234&expire_hours=24"

# 访问分享
curl -X GET "http://localhost:8080/api/v1/shares/access?share_link=abc123&password=1234"
```

---

### 🔐 权限管理模块 (`/api/v1/permissions/`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `POST` | `/api/v1/permissions/grant` | 授予权限 | ✅ 需要登录 | ✅ 可用 |
| `DELETE` | `/api/v1/permissions/revoke` | 撤销权限 | ✅ 需要登录 | ✅ 可用 |
| `GET` | `/api/v1/permissions/list` | 获取权限列表 | ✅ 需要登录 | ✅ 可用 |

---

### 🗑️ 回收站模块 (`/api/v1/recycle-bin`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `GET` | `/api/v1/recycle-bin` | 获取回收站内容 | ✅ 需要登录 | ✅ 可用 |
| `POST` | `/api/v1/recycle-bin/restore` | 恢复文件/文件夹 | ✅ 需要登录 | ✅ 可用 |
| `DELETE` | `/api/v1/recycle-bin/permanent-delete` | 永久删除 | ✅ 需要登录 | ✅ 可用 |

---

### 🔔 通知模块 (`/api/v1/notifications`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `GET` | `/api/v1/notifications` | 获取通知列表 | ✅ 需要登录 | ✅ 可用 |
| `PUT` | `/api/v1/notifications/read` | 标记通知已读 | ✅ 需要登录 | ✅ 可用 |

---

## ❌ 不可用路由 (计划中/未实现)

### 🔧 管理后台模块 (`/api/v1/admin/`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `GET` | `/api/v1/admin/users` | 获取所有用户 | 🔒 管理员 | ❌ 未实现 |
| `DELETE` | `/api/v1/admin/users/{id}` | 删除用户 | 🔒 管理员 | ❌ 未实现 |
| `GET` | `/api/v1/admin/system-stats` | 系统统计信息 | 🔒 管理员 | ❌ 未实现 |
| `POST` | `/api/v1/admin/maintenance` | 系统维护 | 🔒 管理员 | ❌ 未实现 |

### 📊 高级统计模块 (`/api/v1/analytics/`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `GET` | `/api/v1/analytics/usage` | 使用情况分析 | ✅ 需要登录 | ❌ 未实现 |
| `GET` | `/api/v1/analytics/files-trend` | 文件趋势分析 | ✅ 需要登录 | ❌ 未实现 |
| `GET` | `/api/v1/analytics/storage-growth` | 存储增长分析 | ✅ 需要登录 | ❌ 未实现 |

### 🔄 同步模块 (`/api/v1/sync/`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `POST` | `/api/v1/sync/start` | 开始同步 | ✅ 需要登录 | ❌ 未实现 |
| `GET` | `/api/v1/sync/status` | 同步状态 | ✅ 需要登录 | ❌ 未实现 |
| `POST` | `/api/v1/sync/stop` | 停止同步 | ✅ 需要登录 | ❌ 未实现 |

### 📱 移动端专用 (`/api/v1/mobile/`)

| 方法 | 路由 | 功能描述 | 认证要求 | 状态 |
|------|------|----------|----------|------|
| `POST` | `/api/v1/mobile/device-register` | 设备注册 | ✅ 需要登录 | ❌ 未实现 |
| `GET` | `/api/v1/mobile/offline-files` | 离线文件列表 | ✅ 需要登录 | ❌ 未实现 |
| `POST` | `/api/v1/mobile/background-upload` | 后台上传 | ✅ 需要登录 | ❌ 未实现 |

---

## 🛠️ 技术信息

### 响应格式
所有API都返回统一的JSON格式：

```json
{
    "success": true,
    "message": "操作成功",
    "data": {...},
    "status_code": 200,
    "timestamp": "2025-07-19T05:30:00Z",
    "metadata": {
        "operation": "create_folder",
        "version": "v1"
    }
}
```

### 错误响应示例
```json
{
    "success": false,
    "message": "用户未登录",
    "data": null,
    "status_code": 401,
    "error_code": "NOT_LOGGED_IN",
    "timestamp": "2025-07-19T05:30:00Z"
}
```

### HTTP状态码
- `200` - 成功
- `400` - 请求参数错误
- `401` - 未授权 (未登录)
- `403` - 禁止访问 (无权限)
- `404` - 资源不存在
- `405` - 方法不允许
- `500` - 服务器内部错误

---

## 🚀 测试建议

### 1. 启动项目
```bash
cd "C:\Users\chen\TraeProjects\ComPan-1"
mvn clean package
mvn tomcat7:run
```

### 2. 访问测试
- **项目首页**: http://localhost:8080/
- **API基础路径**: http://localhost:8080/api/v1/

### 3. 认证流程测试
1. 先注册用户：`POST /api/v1/auth/register`
2. 登录获取session：`POST /api/v1/auth/login`
3. 使用session访问其他需要认证的接口

### 4. 功能模块测试顺序
1. 用户认证 → 用户管理 → 文件夹管理 → 文件管理 → 分享功能

---

## 📝 更新日志

- **2025-07-19**: 初始版本，实现基础的用户、文件、文件夹、分享功能
- **计划**: 后续版本将实现管理后台、高级统计、同步等功能

---

**注意事项**：
- 所有需要认证的接口都需要先登录获取session
- 文件上传使用multipart/form-data格式
- 分页查询默认每页20条记录
- 分享链接支持密码保护和过期时间设置
