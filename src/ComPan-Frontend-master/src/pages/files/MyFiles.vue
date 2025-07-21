<script setup lang="ts">
import { onMounted, ref, computed, nextTick } from 'vue';
import { storeToRefs } from 'pinia';
import { useFileStore } from '../../store/file';
import { useFileSelection } from '../../composables/useFileSelection';
import { useFileActions } from '../../composables/useFileActions';
import { useBatchActions } from '../../composables/useBatchActions';
import { useUpload } from '../../composables/useUpload';
import Breadcrumb from '../../components/common/Breadcrumb.vue';
import DropdownMenu from '../../components/common/DropdownMenu.vue';
import MoveItemDialog from '../../components/common/MoveItemDialog.vue';
import ShareDialog from '../../components/common/ShareDialog.vue';
import type { FolderItem, FileItem, ContentItem } from '../../types/file';
import { downloadFile, moveFile } from '../../api/file';
import { moveFolder } from '../../api/folder';

const fileStore = useFileStore();
const { items, path, isLoading, currentFolderId } = storeToRefs(fileStore);
const fileInput = ref<HTMLInputElement | null>(null);

// --- Composables ---
const { selectedItems, isSelected, toggleSelection, selectedCount, clearSelection } = useFileSelection();

const itemToMove = ref<ContentItem | null>(null);
const isMoveDialogVisible = ref(false);
const itemToShare = ref<ContentItem | null>(null);
const isShareDialogVisible = ref(false);

const { 
  renamingItemId, renameInputValue, renameInput,
  startRename, cancelRename, finishRename, handleDelete, startMove, startShare
} = useFileActions(itemToMove, isMoveDialogVisible, itemToShare, isShareDialogVisible);

const { handleBatchDownload, handleBatchDelete } = useBatchActions(selectedItems, clearSelection);

const {
  uploadTasks, isDragging, handleDragEnter, handleDragLeave,
  handleDragOver, handleDrop, handleFileSelect
} = useUpload(currentFolderId);

// --- Component Logic ---
onMounted(() => {
  fileStore.fetchFolderContents('root');
});

const triggerFileInput = () => fileInput.value?.click();

const handleDownload = async (file: FileItem) => {
  try {
    const blob = await downloadFile(file.id);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = file.name;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error(`Failed to download file ${file.name}:`, error);
    alert('Download failed!');
  }
};

const handleRename = (item: ContentItem) => {
  startRename(item);
};

// This logic is partially in useFileActions.ts
const closeMoveDialog = () => {
  isMoveDialogVisible.value = false;
  itemToMove.value = null;
};

const handleMoveConfirm = async (targetFolderId: string) => {
  if (!itemToMove.value) return;
  try {
    if (itemToMove.value.itemType === 'folder') {
      await moveFolder(itemToMove.value.id, { targetParentId: targetFolderId });
    } else {
      await moveFile(itemToMove.value.id, { targetFolderId: targetFolderId });
    }
    fileStore.fetchFolderContents(currentFolderId.value || 'root');
    alert('Item moved successfully!');
  } catch (error) {
    console.error(`Failed to move ${itemToMove.value.name}:`, error);
    alert('Move failed!');
  } finally {
    closeMoveDialog();
  }
};

const handleItemClick = (item: ContentItem) => {
  if (renamingItemId.value === item.id) return;
  if (item.itemType === 'folder') {
    fileStore.navigateToFolder(item.id);
  } else {
    toggleSelection(item.id);
  }
};

type ViewMode = 'grid' | 'list';
const viewMode = ref<ViewMode>('grid');

type SortKey = 'name' | 'size' | 'updatedAt';
type SortDirection = 'asc' | 'desc';
const sortKey = ref<SortKey>('name');
const sortDirection = ref<SortDirection>('asc');

const sortedItems = computed(() => {
  const sorted = [...items.value].sort((a, b) => {
    if (a.itemType === 'folder' && b.itemType !== 'folder') return -1;
    if (a.itemType !== 'folder' && b.itemType === 'folder') return 1;

    let compare = 0;
    if (sortKey.value === 'name') {
      compare = a.name.localeCompare(b.name);
    } else if (sortKey.value === 'size') {
      compare = (a.size || 0) - (b.size || 0);
    } else if (sortKey.value === 'updatedAt') {
      compare = new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    }
    
    return sortDirection.value === 'asc' ? compare : -compare;
  });
  return sorted;
});

const setSort = (key: SortKey) => {
  if (sortKey.value === key) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortKey.value = key;
    sortDirection.value = 'asc';
  }
};
</script>

<template>
  <div class="my-files-page">
    <input type="file" ref="fileInput" @change="handleFileSelect" style="display: none" multiple />
    <header class="page-header">
      <Breadcrumb :path="path" @navigate="fileStore.navigateToFolder" />
      <div class="header-actions">
        <div v-if="selectedCount > 0" class="batch-actions">
          <span>{{ selectedCount }} selected</span>
          <button @click="handleBatchDownload" class="batch-action-btn">Download</button>
          <button @click="handleBatchDelete" class="batch-action-btn danger">Delete</button>
        </div>
        <div class="view-switcher">
          <button @click="viewMode = 'grid'" :class="{ active: viewMode === 'grid' }">Grid</button>
          <button @click="viewMode = 'list'" :class="{ active: viewMode === 'list' }">List</button>
        </div>
        <button class="upload-btn" @click="triggerFileInput">
          <span>⬆️ Upload</span>
        </button>
      </div>
    </header>

    <MoveItemDialog
      :is-visible="isMoveDialogVisible"
      :item-to-move="itemToMove"
      @close="closeMoveDialog"
      @confirm="handleMoveConfirm"
    />

    <ShareDialog
      :is-visible="isShareDialogVisible"
      :item-to-share="itemToShare"
      @close="isShareDialogVisible = false"
    />

    <!-- Upload Progress Section -->
    <div v-if="uploadTasks.length > 0" class="upload-progress-area">
      <h4>Uploading...</h4>
      <div v-for="task in uploadTasks" :key="task.id" class="upload-task">
        <span class="task-name">{{ task.name }}</span>
        <div class="progress-bar-container">
          <div 
            class="progress-bar" 
            :style="{ width: task.progress.percentage + '%' }"
          ></div>
        </div>
        <span class="task-percentage">{{ task.progress.percentage }}%</span>
      </div>
    </div>
    
    <div 
      class="file-display-area"
      @dragenter="handleDragEnter"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
    >
      <!-- Drag and Drop Overlay -->
      <div v-if="isDragging" class="drag-overlay">
        <div class="drag-overlay-content">
          <span>⬆️</span>
          <p>Drop files here to upload</p>
        </div>
      </div>

      <!-- File Grid -->
      <div v-if="isLoading" class="loading-indicator">
        Loading...
      </div>
      <!-- List View -->
      <div v-if="viewMode === 'list' && !isLoading" class="file-list">
       <!-- List Header -->
      <div class="list-header">
        <div class="list-cell select-all"></div>
        <button class="list-cell name" @click="setSort('name')">Name</button>
        <button class="list-cell size" @click="setSort('size')">Size</button>
        <button class="list-cell modified" @click="setSort('updatedAt')">Last Modified</button>
        <div class="list-cell actions"></div>
      </div>
      <!-- List Body -->
      <div 
        v-for="item in sortedItems" 
        :key="item.id"
        class="list-item"
        :class="{ selected: isSelected(item.id) }"
        @click="handleItemClick(item)"
      >
        <div class="list-cell select-cell">
          <input 
            type="checkbox" 
            class="item-checkbox" 
            :checked="isSelected(item.id)"
            @click.stop="toggleSelection(item.id)"
          />
        </div>
        <div class="list-cell name-cell">
          <img v-if="item.itemType === 'folder'" src="../../assets/generic/folder.svg" alt="" class="item-icon-small" />
          <img v-else src="../../assets/generic/file.svg" alt="" class="item-icon-small" />
          <span v-if="renamingItemId !== item.id" class="item-name">{{ item.name }}</span>
          <input 
            v-else
            ref="renameInput"
            v-model="renameInputValue"
            class="rename-input"
            @blur="finishRename"
            @keydown.enter.prevent="finishRename"
            @keydown.esc.prevent="cancelRename"
          />
        </div>
        <div class="list-cell size-cell">{{ item.size ? (item.size / 1024).toFixed(1) + ' KB' : '--' }}</div>
        <div class="list-cell modified-cell">{{ new Date(item.updatedAt).toLocaleDateString() }}</div>
        <div class="list-cell actions-cell" @click.stop>
          <DropdownMenu>
            <template #trigger>
              <button class="kebab-btn"><span>⋮</span></button>
            </template>
            <template #content>
               <div class="dropdown-content">
                <button v-if="item.itemType === 'file'" @click="handleDownload(item as FileItem)" class="dropdown-item">
                  <span class="dropdown-item-icon">⬇️</span>
                  <span>Download</span>
                </button>
                <template v-if="item.permission === 'owner' || item.permission === 'write'">
                  <button @click="handleRename(item)" class="dropdown-item">
                    <span class="dropdown-item-icon">✏️</span>
                    <span>Rename</span>
                  </button>
                  <button @click="startMove(item)" class="dropdown-item">
                    <span class="dropdown-item-icon">➡️</span>
                    <span>Move</span>
                  </button>
                </template>
                <button @click="startShare(item)" class="dropdown-item">
                  <span class="dropdown-item-icon">🤝</span>
                  <span>Share</span>
                </button>
                <button v-if="item.permission === 'owner' || item.permission === 'write'" @click="handleDelete(item)" class="dropdown-item danger">
                  <span class="dropdown-item-icon">🗑️</span>
                  <span>Delete</span>
                </button>
              </div>
            </template>
          </DropdownMenu>
        </div>
      </div>
    </div>
    
    <!-- Grid View -->
    <div v-if="viewMode === 'grid' && !isLoading" class="file-grid">
      <div 
        v-for="item in sortedItems" 
        :key="item.id" 
        class="grid-item"
        :class="{ selected: isSelected(item.id) }"
        @click="handleItemClick(item)"
      >
        <input 
          type="checkbox" 
          class="item-checkbox" 
          :checked="isSelected(item.id)"
          @click.stop="toggleSelection(item.id)"
        />
        <img v-if="item.itemType === 'folder'" src="../../assets/generic/folder.svg" alt="Folder" class="item-icon" />
        <img v-else src="../../assets/generic/file.svg" alt="File" class="item-icon" />
        <div class="item-name-wrapper">
          <span v-if="renamingItemId !== item.id" class="item-name">{{ item.name }}</span>
          <input
            v-else
            ref="renameInput"
            v-model="renameInputValue"
            class="rename-input"
            @blur="finishRename"
            @keydown.enter.prevent="finishRename"
            @keydown.esc.prevent="cancelRename"
          />
        </div>
        
        <div class="item-actions" @click.stop>
          <DropdownMenu>
            <template #trigger>
              <button class="kebab-btn">
                <span>⋮</span>
              </button>
            </template>
            <template #content>
              <div class="dropdown-content">
                 <button v-if="item.itemType === 'file'" @click="handleDownload(item as FileItem)" class="dropdown-item">
                  <span class="dropdown-item-icon">⬇️</span>
                  <span>Download</span>
                </button>
                <template v-if="item.permission === 'owner' || item.permission === 'write'">
                  <button @click="handleRename(item)" class="dropdown-item">
                    <span class="dropdown-item-icon">✏️</span>
                    <span>Rename</span>
                  </button>
                  <button @click="startMove(item)" class="dropdown-item">
                    <span class="dropdown-item-icon">➡️</span>
                    <span>Move</span>
                  </button>
                </template>
                 <button @click="startShare(item)" class="dropdown-item">
                  <span class="dropdown-item-icon">🤝</span>
                  <span>Share</span>
                </button>
                <button v-if="item.permission === 'owner' || item.permission === 'write'" @click="handleDelete(item)" class="dropdown-item danger">
                  <span class="dropdown-item-icon">🗑️</span>
                  <span>Delete</span>
                </button>
              </div>
            </template>
          </DropdownMenu>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<style scoped>
/* Styles are mostly the same, just adjusted the header */
.my-files-page {
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  height: 100%;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  flex-shrink: 0;
  gap: var(--spacing-md);
}
.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}
.batch-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-md);
  background-color: var(--color-bg-secondary);
}
.batch-actions span {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}
.batch-action-btn {
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  border: 1px solid transparent;
  background-color: transparent;
  cursor: pointer;
  transition: all var(--transition-base);
}
.batch-action-btn:hover {
  background-color: var(--color-bg-tertiary);
  border-color: var(--color-border-hover);
}
.batch-action-btn.danger {
  color: var(--color-danger);
}
.batch-action-btn.danger:hover {
  background-color: var(--color-danger-light);
  color: var(--color-danger-dark);
  border-color: var(--color-danger);
}

.file-display-area {
  position: relative;
  flex-grow: 1;
  display: flex; /* To make the children (grid/list) take up space */
  flex-direction: column;
}

.drag-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(var(--color-primary-rgb), 0.1);
  border: 2px dashed var(--color-primary);
  border-radius: var(--border-radius-lg);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
  pointer-events: none; /* Allow drop events to pass through */
}

.drag-overlay-content {
  text-align: center;
  color: var(--color-primary);
}

.drag-overlay-content span {
  font-size: 3rem;
}

.drag-overlay-content p {
  font-size: 1.2rem;
  font-weight: var(--font-weight-bold);
  margin-top: var(--spacing-md);
}

.file-grid {
  flex-grow: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: var(--spacing-lg);
  overflow-y: auto;
}
.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-md);
  border-radius: var(--border-radius-md);
  transition: background-color var(--transition-base);
  cursor: pointer;
  height: fit-content;
  position: relative;
  border: 1px solid transparent; /* Add border for smooth transition */
}
.grid-item:hover {
  background-color: var(--color-bg-tertiary);
}
.grid-item.selected {
  background-color: var(--color-primary-light);
  border: 1px solid var(--color-primary);
}
.upload-btn {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
  border: none;
  border-radius: var(--border-radius-md);
  padding: var(--spacing-sm) var(--spacing-md);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: background-color var(--transition-base);
}
.upload-btn:hover {
  background-color: var(--color-primary-hover);
}
.item-icon {
  width: 64px;
  height: 64px;
  margin-bottom: var(--spacing-sm);
}
.item-name {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  text-align: center;
  word-break: break-all;
}
.item-name-wrapper {
  width: 100%;
  text-align: center;
}
.rename-input {
  width: 100%;
  padding: var(--spacing-xs);
  font-size: 0.875rem;
  border-radius: var(--border-radius-sm);
  border: 1px solid var(--color-primary);
  background-color: var(--color-bg-primary);
  color: var(--color-text-primary);
  text-align: center;
  box-sizing: border-box;
}
.upload-progress-area {
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-md);
  background-color: var(--color-bg-secondary);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
}
.upload-task {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-top: var(--spacing-sm);
}
.task-name {
  flex-grow: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.progress-bar-container {
  width: 200px;
  height: 8px;
  background-color: var(--color-bg-tertiary);
  border-radius: 4px;
  overflow: hidden;
}
.progress-bar {
  height: 100%;
  background-color: var(--color-primary);
  transition: width 0.3s ease;
}
.task-percentage {
  font-size: 0.875rem;
  font-weight: var(--font-weight-medium);
  width: 40px;
  text-align: right;
}
.item-checkbox {
  position: absolute;
  top: var(--spacing-sm);
  left: var(--spacing-sm);
  width: 18px;
  height: 18px;
  opacity: 0;
  transition: opacity var(--transition-base);
}
.grid-item:hover .item-checkbox,
.grid-item.selected .item-checkbox {
  opacity: 1;
}

.item-actions {
  position: absolute;
  bottom: var(--spacing-sm);
  right: var(--spacing-sm);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.grid-item:hover .item-actions,
.grid-item.selected .item-actions {
  opacity: 1;
}

.kebab-btn {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--color-border);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1.2rem;
  line-height: 1;
}

.dark-theme .kebab-btn {
  background: rgba(var(--color-bg-tertiary), 0.7);
}

.dropdown-content {
  padding: var(--spacing-xs);
  min-width: 160px; /* Give the dropdown a bit more width */
  background-color: var(--color-bg-primary);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}
/* dropdown item */
.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  width: 100%;
  text-align: left;
  padding: var(--spacing-sm) var(--spacing-md);
  color: var(--color-text-secondary);
  background: none;
  border: none;
  font-size: var(--font-size-base);
  cursor: pointer;
  white-space: nowrap; /* Prevent text from wrapping */
  border-radius: var(--border-radius-sm);
  transition: background-color 0.2s, color 0.2s;
}
.dropdown-item:hover {
  background-color: var(--color-bg-tertiary);
  color: var(--color-text-primary);
}
.dropdown-item.danger:hover {
  background-color: #fee2e2;
  color: #b91c1c;
}
.dark-theme .dropdown-item.danger:hover {
    background-color: #3f1a1a;
    color: #fca5a5;
}
.dropdown-item-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.2em;
}
.dropdown-item.danger {
  color: #ef4444;
}

/* List View Styles */
.file-list {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.list-header, .list-item {
  display: grid;
  grid-template-columns: 40px 1fr 120px 200px 50px;
  gap: var(--spacing-md);
  align-items: center;
  padding: 0 var(--spacing-md);
}
.list-header {
  border-bottom: 1px solid var(--color-border);
  padding-bottom: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}
.list-header button {
    background: none;
    border: none;
    cursor: pointer;
    font-weight: inherit;
    color: inherit;
    text-align: left;
}

.view-switcher {
  display: flex;
  background-color: var(--color-bg-tertiary);
  border-radius: var(--border-radius-md);
  padding: 2px;
}
.view-switcher button {
  padding: var(--spacing-xs) var(--spacing-sm);
  border: none;
  background-color: transparent;
  border-radius: var(--border-radius-sm);
  cursor: pointer;
}
.view-switcher button.active {
  background-color: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
}

.list-item {
  border-radius: var(--border-radius-md);
  transition: background-color var(--transition-base);
  padding: var(--spacing-sm) var(--spacing-md);
}
.list-item:hover {
  background-color: var(--color-bg-tertiary);
}
.list-item.selected {
  background-color: var(--color-primary-light);
}

.name-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
.item-icon-small {
  width: 24px;
  height: 24px;
}
.actions-cell {
  display: flex;
  justify-content: flex-end;
}
</style> 