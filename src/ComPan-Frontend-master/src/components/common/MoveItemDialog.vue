<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import type { ContentItem, FolderItem } from '../../types/file';
import { getFolderContents } from '../../api/folder';
import FolderTreeNode from './FolderTreeNode.vue';

interface Props {
  isVisible: boolean;
  itemToMove: ContentItem | null;
}
const props = defineProps<Props>();

const emit = defineEmits(['close', 'confirm']);

const rootFolders = ref<FolderItem[]>([]);
const isLoading = ref(false);
const selectedFolderId = ref<string | null>(null);

const fetchRootFolders = async () => {
  isLoading.value = true;
  try {
    const response = await getFolderContents({ folderId: 'root' });
    rootFolders.value = response.items.filter(item => item.itemType === 'folder') as FolderItem[];
  } catch (error) {
    console.error('Failed to load root folders:', error);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  if (props.isVisible) {
    fetchRootFolders();
  }
});

watch(() => props.isVisible, (newValue) => {
  if (newValue) {
    selectedFolderId.value = null; // Reset selection when dialog opens
    fetchRootFolders();
  }
});

const handleSelectFolder = (folderId: string) => {
  selectedFolderId.value = folderId;
};

const handleConfirm = () => {
  if (!selectedFolderId.value) {
    alert('Please select a destination folder.');
    return;
  }
  emit('confirm', selectedFolderId.value);
};

</script>

<template>
  <transition name="modal-fade">
    <div v-if="isVisible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-dialog">
        <header class="modal-header">
          <h3 class="modal-title">Move Item</h3>
          <button class="modal-close" @click="$emit('close')">&times;</button>
        </header>
        <div class="modal-body">
          <p v-if="itemToMove">
            Choose a new location for <strong>{{ itemToMove.name }}</strong>:
          </p>
          <div class="folder-tree-container">
            <div v-if="isLoading">Loading folders...</div>
            <FolderTreeNode
              v-for="folder in rootFolders"
              :key="folder.id"
              :node="folder"
              :selected-folder-id="selectedFolderId"
              @select="handleSelectFolder"
            />
          </div>
        </div>
        <footer class="modal-footer">
          <button class="btn-secondary" @click="$emit('close')">Cancel</button>
          <button class="btn-primary" @click="handleConfirm">Move Here</button>
        </footer>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-dialog {
  background-color: var(--color-bg-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-xl);
  min-width: 400px;
  max-width: 90vw;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--color-text-secondary);
}

.modal-body {
  padding: var(--spacing-lg);
  max-height: 60vh;
  overflow-y: auto;
}

.folder-tree-container {
  margin-top: var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-md);
  min-height: 200px;
  padding: var(--spacing-sm);
}

.modal-footer {
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
}

.btn-primary, .btn-secondary {
  padding: var(--spacing-sm) var(--spacing-lg);
  border-radius: var(--border-radius-md);
  border: none;
  cursor: pointer;
  font-weight: var(--font-weight-medium);
}

.btn-primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
}

.btn-secondary {
  background-color: var(--color-bg-secondary);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}

.modal-fade-enter-active, .modal-fade-leave-active {
  transition: opacity 0.3s ease;
}
.modal-fade-enter-from, .modal-fade-leave-to {
  opacity: 0;
}
.modal-fade-enter-active .modal-dialog,
.modal-fade-leave-active .modal-dialog {
  transition: transform 0.3s ease;
}
.modal-fade-enter-from .modal-dialog,
.modal-fade-leave-to .modal-dialog {
  transform: translateY(-20px);
}
</style> 