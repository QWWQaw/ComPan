import Mock from 'mockjs';

// --- Types ---
export interface VfsNode {
  id: string;
  name: string;
  type: 'folder' | 'file';
  parent: string | null;
  children?: string[];
  size?: number;
  createdAt: string;
  updatedAt: string;
  permission?: 'read' | 'write' | 'owner';
}

export interface Vfs {
  [key: string]: VfsNode;
}

// --- Constants ---
//load from .env file
const VFS_STORAGE_KEY = import.meta.env.VFS_STORAGE_KEY || 'compan-vfs';

// --- Initial Data ---
const initialVfs: Vfs = {
  'root': { id: 'root', name: 'My Files', type: 'folder', parent: null, children: ['folder1', 'file1'], createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), permission: 'owner' },
  'folder1': { id: 'folder1', name: 'Work Documents', type: 'folder', parent: 'root', children: ['file2'], createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), permission: 'owner' },
  'file1': { id: 'file1', name: 'notes.txt', type: 'file', parent: 'root', size: 1024, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), permission: 'owner' },
  'file2': { id: 'file2', name: 'project-brief.docx', type: 'file', parent: 'folder1', size: 20480, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), permission: 'owner' },
};

// --- VFS Singleton ---
let vfs: Vfs;

function saveVfs() {
  localStorage.setItem(VFS_STORAGE_KEY, JSON.stringify(vfs));
}

function loadVfs(): Vfs {
  const storedVfs = localStorage.getItem(VFS_STORAGE_KEY);
  if (storedVfs) {
    try {
      return JSON.parse(storedVfs);
    } catch (e) {
      console.error("Failed to parse VFS from localStorage, resetting.", e);
    }
  }
  return initialVfs;
}

// Initialize VFS
vfs = loadVfs();
saveVfs(); // Ensure it's saved on first load if it didn't exist

// --- VFS API ---
export const vfsApi = {
  get: (id: string): VfsNode | undefined => vfs[id],
  
  getChildren: (folderId: string): VfsNode[] => {
    const parent = vfs[folderId];
    if (parent && parent.type === 'folder' && parent.children) {
      return parent.children.map(id => vfs[id]).filter(Boolean);
    }
    return [];
  },
  
  getPath: (id: string): VfsNode[] => {
    const path: VfsNode[] = [];
    let current: VfsNode | undefined = vfs[id];
    while (current) {
      path.unshift(current);
      current = current.parent ? vfs[current.parent] : undefined;
    }
    return path;
  },

  createFile: (parentId: string, fileName: string, size: number): VfsNode => {
    const newId = Mock.Random.guid();
    const now = new Date().toISOString();
    const newFile: VfsNode = {
      id: newId,
      name: fileName,
      type: 'file',
      parent: parentId,
      size,
      createdAt: now,
      updatedAt: now,
      permission: 'owner', // New files created by the user are owned by them
    };
    vfs[newId] = newFile;
    vfs[parentId].children?.push(newId);
    saveVfs();
    return newFile;
  },
  
  rename: (id: string, newName: string): VfsNode => {
    vfs[id].name = newName;
    vfs[id].updatedAt = new Date().toISOString();
    saveVfs();
    return vfs[id];
  },
  
  delete: (id: string) => {
    const node = vfs[id];
    if (!node) return;
    
    // Recursively delete children if it's a folder
    if (node.type === 'folder' && node.children) {
      [...node.children].forEach(childId => vfsApi.delete(childId));
    }
    
    // Remove from parent's children list
    if (node.parent && vfs[node.parent]?.children) {
      const children = vfs[node.parent].children!;
      const index = children.indexOf(id);
      if (index > -1) {
        children.splice(index, 1);
      }
    }
    
    delete vfs[id];
    saveVfs();
  }
}; 