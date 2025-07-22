/**
 * 分页数据通用接口
 * @property {T[]} content - 数据内容
 * @property {number} totalElements - 总元素数
 * @property {number} currentPage - 当前页码
 * @property {number} pageSize - 每页大小
 * @property {number} totalPages - 总页数
 * @property {boolean} hasNext - 是否有下一页
 * @property {boolean} hasPrevious - 是否有上一页
 */
export interface PaginatedData<T> {
  content: T[];
  totalElements: number;
  currentPage: number;
  pageSize: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

/**
 * 用户信息
 * @property {string} userId - 用户ID
 * @property {string} username - 用户名
 * @property {string} email - 用户邮箱
 * @property {number} storageLimit - 存储限制
 * @property {number} storageUsed - 已用存储
 * @property {string} createdAt - 创建时间
 */
export interface User{
    userId: string;
    username: string;
    email: string;
    storageLimit: number;
    storageUsed: number;
    //status: 'active' | 'banned' | 'inactive;
    createdAt: string;
}

/**
 * 登录成功的响应
 * @property {string} token - 登录令牌
 * @property {number} expiresIn - 令牌过期时间（单位：秒）
 * @property {User} user - 用户信息
 */
export interface LoginResponse{
    token: string;
    expiresIn: number; // 单位：秒
    user: User;
}

/**
 * 用户注册请求体
 */
export interface RegisterRequest extends Pick<User, 'username' | 'email'> {
  password?: string;
}

/**
 * 用户登录请求体
 */
export interface LoginRequest {
  username?: string;
  password?: string;
}

/**
 * 用户信息更新请求体
 */
export type UpdateProfileRequest = Partial<Pick<UserProfile, 'username' | 'email'>>;


/**
 * 密码修改请求体
 */
export interface ChangePasswordRequest {
  oldPassword?: string;
  newPassword?: string;
}

/**
 * 用户组信息
 * @property {number} groupId - 用户组ID
 * @property {string} groupName - 用户组名称
 * @property {string} role - 用户角色
 */
export interface UserGroupInfo{
    groupId: number;
    groupName: string;          
    role: 'admin' | 'member';

}

/**
 * 用户完整信息
 * @property {UserGroupInfo[]} groups - 用户所属组信息
 * @property {string} updatedAt - 更新时间
 * @property {string} lastLogin - 上次登录时间
 */
export interface UserProfile extends User{
    groups: UserGroupInfo[];
    updatedAt: string;
    lastLogin: string;
}

/**
 *  breakdown 详情
 * @property {number} size - 占用大小
 * @property {number} count - 占用数量
 */
export interface BreakdownDetail{
    size: number;
    count: number;
}

/**
 * 存储统计信息
 * @property {number} storageLimit - 存储限制
 * @property {number} storageUsed - 已用存储
 * @property {number} storageAvailable - 可用存储
 * @property {number} storagePercentage - 存储占用百分比
 * @property {number} fileCount - 文件数量
 * @property {number} folderCount - 文件夹数量
 * @property {object} breakdown - 存储占用 breakdown
 */
export interface StorageStats{
    storageLimit: number;
    storageUsed: number;
    storageAvailable: number;
    storagePercentage: number;
    fileCount: number;
    folderCount: number;
    breakdown: {
        [key: string]: BreakdownDetail;
    };
}

/**
 * 活动项
 * @property {number} id - 活动项ID
 * @property {string} operation - 操作类型
 * @property {object} details - 操作详情
 */
export interface ActivityItem{
    id: number;
    operation: string;
    details: {
        [key: string]: string | number;
    };
    ipAddress: string;
    performedAt: string;
}

export type ActivityLog = PaginatedData<ActivityItem>;

/**
 * 获取活动日志的查询参数
 * @property {number} page - 页码
 * @property {number} perPage - 每页项数
 * @property {string} operation - 操作类型
 */
export interface GetActivityLogRequest {
  page?: number;
  perPage?: number;
  operation?: string; // e.g., 'file_upload', 'folder_create'
}

/**
 * 创建用户组的请求体
 */
export interface CreateUserGroupRequest {
  name: string;
  description?: string;
}

/**
 * 用户组详细信息
 */
export interface UserGroup {
  groupId: number;
  name: string;
  description?: string;
  memberCount: number;
  createdAt: string;
}

/**
 * 添加用户到组的请求体
 */
export interface AddGroupMemberRequest {
  userId: number;
  role: 'member' | 'admin';
}

/**
 * 用户组列表响应数据
 */
export type UserGroupsList = PaginatedData<UserGroup>;

