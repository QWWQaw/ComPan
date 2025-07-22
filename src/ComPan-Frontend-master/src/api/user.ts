import http from '../utils/http';
import type { 
    RegisterRequest, 
    LoginRequest, 
    LoginResponse, 
    UserProfile, 
    UpdateProfileRequest, 
    ChangePasswordRequest, 
    StorageStats, 
    ActivityLog,
    GetActivityLogRequest,
    PaginatedData,
    User
} from '../types/user';

/**
 * 用户注册 // 交给AuthController处理
 * @param data 注册信息
 */
export const register = (data: RegisterRequest) => {
  return http.post<UserProfile>('/api/v1/auth/register', data);
};

/**
 * 用户登录 // 交给AuthController处理
 * @param data 登录凭据
 * @returns 登录响应数据
 */
export const login = (data: LoginRequest) => {
  return http.post<LoginResponse>('/api/v1/auth/login', data);
};

/**
 * 用户登出 // 交给AuthController处理
 * @returns 登出响应数据
 */
export const logout = () => {
  return http.post<void>('/api/v1/auth/logout');
};

/**
 * 获取当前用户的完整个人信息 // 交给UserController处理
 * @returns 用户个人信息
 */
export const getProfile = () => {
  return http.get<UserProfile>('/api/v1/users/me/profile');
};

/**
 * 更新当前用户的个人信息 // 交给UserController处理
 * @param data 要更新的信息
 * @returns 更新后的个人信息
 */
export const updateProfile = (data: UpdateProfileRequest) => {
  return http.put<UserProfile>('/api/v1/users/me/update-profile', data);
};

/**
 * 修改当前用户的密码 // 交给AuthController处理
 * @param data 新旧密码
 * @returns 更新后的密码
 */
export const changePassword = (data: ChangePasswordRequest) => {
  return http.put<void>('/api/v1/users/me/password', data);
};

/**
 * 获取用户的存储空间统计信息 // 交给userController处理
 * @returns 存储空间统计信息
 */
export const getStorageStats = () => {
  return http.get<StorageStats>('/api/v1/users/me/storage-stats');
};

/**
 * 获取用户的活动日志 // 交给UserController处理
 * @param params 查询参数 (分页、操作类型等)
 * @returns 活动日志
 */
export const getActivityLog = (params: GetActivityLogRequest) => {
  return http.get<ActivityLog>('/api/v1/users/me/activity-log', params);
};

/**
 * 获取用户列表 (可用于搜索) // 交给UserController处理
 * @param params 查询参数 (搜索关键词、分页等)
 * @returns 用户列表
 */
export const getUsers = (params: { search?: string; page?: number; perPage?: number }) => {
  return http.get<PaginatedData<User>>('/api/v1/users', params);
}; 