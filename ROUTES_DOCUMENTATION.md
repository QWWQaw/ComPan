# ComPan 路由系统文档

## 📊 路由统计总览

### 总体统计
- **控制器数量**: 2个
- **总路由数量**: 23个
- **支持HTTP方法**: GET, POST, PUT, DELETE

### 按HTTP方法分类
- **GET请求**: 17个
- **POST请求**: 4个  
- **PUT请求**: 2个
- **DELETE请求**: 1个
- **多方法支持**: 1个 (GET+POST)

### 按控制器分类
- **TestController** (`/api/*`): 9个路由
- **EnhancedTestController** (`/api/v2/*`): 14个路由

---

## 🎯 详细路由清单

### 1. TestController (`/api/*`) - 基础功能演示

#### 1.1 基础路由

| 序号 | HTTP方法 | 路径 | 功能描述 | 状态码 |
|------|----------|------|----------|--------|
| 1 | GET | `/api` | API首页，显示可用路由列表 | 200 |
| 2 | GET | `/api/hello` | Hello World示例 | 200 |
| 3 | GET | `/api/status` | 系统状态检查 | 200 |
| 4 | GET | `/api/user-demo` | 示例用户数据 | 200 |
| 5 | GET | `/api/test-injection` | 依赖注入测试 | 200 |

#### 1.2 CRUD操作

| 序号 | HTTP方法 | 路径 | 功能描述 | 状态码 |
|------|----------|------|----------|--------|
| 6 | POST | `/api/users` | 创建用户 | 201 |
| 7 | PUT | `/api/users` | 更新用户 | 200 |
| 8 | DELETE | `/api/users` | 删除用户 | 200 |

#### 1.3 多方法支持

| 序号 | HTTP方法 | 路径 | 功能描述 | 状态码 |
|------|----------|------|----------|--------|
| 9 | GET/POST | `/api/multi` | 多HTTP方法演示 | 200 |

---

### 2. EnhancedTestController (`/api/v2/*`) - 增强功能演示

#### 2.1 路径参数 (@PathVariable)

| 序号 | HTTP方法 | 路径 | 功能描述 | 参数类型 | 状态码 |
|------|----------|------|----------|----------|--------|
| 10 | GET | `/api/v2/users/{id}` | 根据ID获取用户 | Long id | 200 |
| 11 | GET | `/api/v2/users/{userId}/posts/{postId}` | 获取用户文章 | Long userId, Long postId | 200 |
| 12 | GET | `/api/v2/users/{id}/profile` | 获取用户资料 | Long id + 查询参数 | 200 |
| 13 | PUT | `/api/v2/users/{id}` | 更新指定用户 | Long id + RequestBody | 200 |
| 14 | GET | `/api/v2/validate/users/{id}` | 用户ID验证 | Long id | 200 |

#### 2.2 查询参数 (@RequestParam)

| 序号 | HTTP方法 | 路径 | 功能描述 | 参数说明 | 状态码 |
|------|----------|------|----------|----------|--------|
| 15 | GET | `/api/v2/users` | 分页用户列表 | page, size, search | 200 |
| 16 | GET | `/api/v2/search` | 搜索功能 | q(必需), category, sort, limit | 200 |
| 17 | GET | `/api/v2/validate/search` | 搜索参数验证 | q(必需) | 200 |

#### 2.3 请求体 (@RequestBody)

| 序号 | HTTP方法 | 路径 | 功能描述 | 请求体类型 | 状态码 |
|------|----------|------|----------|------------|--------|
| 18 | POST | `/api/v2/users` | 创建用户(JSON) | User对象 | 201 |
| 19 | POST | `/api/v2/users/{userId}/posts` | 创建用户文章 | PostRequest对象 + 路径参数 + 查询参数 | 201 |

#### 2.4 系统功能

| 序号 | HTTP方法 | 路径 | 功能描述 | 状态码 |
|------|----------|------|----------|--------|
| 20 | GET | `/api/v2` | V2 API信息页 | 200 |
| 21 | GET | `/api/v2/legacy/info` | 兼容性测试 | 200 |
| 22 | GET | `/api/v2/json/demo` | JsonUtils序列化演示 | 200 |
| 23 | GET | `/api/v2/json/utils` | JsonUtils工具方法演示 | 200 |

---

## 🔧 技术特性

### 支持的注解类型
1. **@Controller** - 标记控制器类
2. **@RequestMapping** - 类级别路径前缀 + 方法级别路由
3. **@GetMapping** - GET请求映射
4. **@PostMapping** - POST请求映射  
5. **@PutMapping** - PUT请求映射
6. **@DeleteMapping** - DELETE请求映射
7. **@PathVariable** - 路径参数绑定
8. **@RequestParam** - 查询参数绑定
9. **@RequestBody** - JSON请求体绑定

### 参数解析功能
- ✅ 路径参数自动提取和类型转换
- ✅ 查询参数默认值和必需性验证
- ✅ JSON请求体自动反序列化
- ✅ HttpServletRequest/Response原生支持
- ✅ 基本类型自动转换 (String, int, long, boolean等)

### 错误处理
- **404错误**: 路由不存在时返回JSON格式错误信息
- **500错误**: 参数解析失败或业务逻辑异常时返回错误详情
- **参数验证**: 必需参数缺失或类型转换失败自动处理

---

## 📝 使用示例

### 基础GET请求
```bash
# 获取API信息
curl -X GET http://localhost:8080/api

# 系统状态检查
curl -X GET http://localhost:8080/api/status
```

### 路径参数示例
```bash
# 获取用户信息
curl -X GET http://localhost:8080/api/v2/users/123

# 获取用户文章
curl -X GET http://localhost:8080/api/v2/users/123/posts/456
```

### 查询参数示例
```bash
# 分页查询用户
curl -X GET "http://localhost:8080/api/v2/users?page=1&size=10&search=john"

# 搜索功能
curl -X GET "http://localhost:8080/api/v2/search?q=java&category=tech&sort=date"
```

### JSON请求体示例
```bash
# 创建用户
curl -X POST http://localhost:8080/api/v2/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","age":25}'

# 复合参数示例
curl -X POST "http://localhost:8080/api/v2/users/123/posts?category=tech&published=true" \
  -H "Content-Type: application/json" \
  -d '{"title":"Spring Boot教程","content":"这是一篇教程..."}'
```

---

## 🚀 框架特点

### 路由注册机制
- **自动扫描**: 通过包扫描自动发现@Controller类
- **依赖注入**: 使用Google Guice进行依赖管理
- **路径组合**: 支持类级别和方法级别路径组合
- **参数化路由**: 支持`{参数名}`格式的动态路径

### 性能优化
- **线程安全**: 使用ConcurrentHashMap存储路由信息
- **精确匹配**: 优先进行O(1)的精确路径匹配
- **模式匹配**: 仅在需要时进行正则表达式匹配
- **分组存储**: 按HTTP方法分组存储参数化路由

### 扩展性
- **模块化设计**: 控制器扫描、路由注册、请求分发分离
- **注解驱动**: 易于添加新的参数解析注解
- **JSON支持**: 集成JsonUtils提供完整的JSON处理能力

---

## 📊 开发统计

### 代码分布
- **控制器代码**: ~400行 (2个控制器类)
- **路由系统**: ~800行 (扫描器 + 注册器 + 分发器)
- **注解定义**: ~150行 (9个注解类)
- **工具类**: ~200行 (JsonUtils + 其他工具)

### 测试覆盖
- **单元测试**: 6个测试类
- **集成测试**: 完整的路由注册和分发测试
- **功能测试**: 覆盖所有注解和参数类型

---

*本文档生成于 2024年，对应ComPan项目的当前路由配置* 