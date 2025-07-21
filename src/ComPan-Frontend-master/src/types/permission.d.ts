/**
 * 创建权限请求
 * @property {number} fileId 文件ID
 * @property {number} folderId 文件夹ID
 * @property {number} userId 用户ID
 * @property {number} groupId 组ID
 * @property {string} permission 权限类型
 */
export interface CreatePermissionRequest {
  fileId?: number;
  folderId?: number;
  userId?: number;
  groupId?: number;
  permission: 'read' | 'write' | 'admin';
}

/**
 * 权限项
 * @property {number} permissionId 权限ID
 * @property {string} itemType 权限项类型
 * @property {number} itemId 权限项ID
 * @property {object} grantedTo 授予权限的用户或组
 * @property {string} permission 权限类型
 * @property {string} createdAt 创建时间
 */
export interface PermissionItem {
  permissionId: number;
  itemType: 'file' | 'folder';
  itemId: number;
  grantedTo: {
    type: 'user' | 'group';
    id: number;
    name: string;
  };
  permission: 'read' | 'write' | 'admin';
  createdAt: string;
}

/**
 * 更新权限请求
 * @property {string} permission 权限类型
 */
export interface UpdatePermissionRequest {
  permission: 'read' | 'write' | 'admin';
}

export type PermissionsList = PaginatedData<PermissionItem>;

/**
 * 获取权限请求
 * @property {number} fileId 文件ID
 * @property {number} folderId 文件夹ID
 * @property {number} page 页码
 * @property {number} perPage 每页数量
 */
export interface GetPermissionsRequest {
  fileId?: number;
  folderId?: number;
  page?: number;
  perPage?: number;
} 