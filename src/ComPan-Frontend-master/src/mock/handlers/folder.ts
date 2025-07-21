import Mock from 'mockjs';
import { vfsApi } from '../vfs';

export const setupFolderMocks = () => {
  // Get Folder Contents
  Mock.mock(/\/api\/v1\/folders\/(.+)\/contents/, 'get', (options) => {
    const folderId = (options.url.match(/\/api\/v1\/folders\/(.+)\/contents/) || [])[1];
    const children = vfsApi.getChildren(folderId);

    return {
      success: true,
      code: 200,
      data: {
        items: children.map(c => ({...c, itemType: c.type, permission: c.permission || 'owner' })), // Ensure permission is passed
        pagination: { totalItems: children.length, totalPages: 1, perPage: children.length, currentPage: 1 },
      },
    };
  });

  // Get Folder Path
  Mock.mock(/\/api\/v1\/folders\/(.+)\/path/, 'get', (options) => {
    const folderId = (options.url.match(/\/api\/v1\/folders\/(.+)\/path/) || [])[1];
    const path = vfsApi.getPath(folderId);
    
    return {
      success: true,
      code: 200,
      data: {
        pathItems: path.map(p => ({ folderId: p.id, name: p.name })),
      },
    };
  });
}; 