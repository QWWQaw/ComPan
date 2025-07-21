import { ref, nextTick } from 'vue';
import type { Ref } from 'vue';
import type { ContentItem } from '../types/file';
import { useFileStore } from '../store/file';
import { renameFolder, deleteFolder, moveFolder } from '../api/folder';
import { renameFile, deleteFile, moveFile } from '../api/file';

export function useFileActions(
  itemToMove: Ref<ContentItem | null>, 
  isMoveDialogVisible: Ref<boolean>,
  itemToShare: Ref<ContentItem | null>,
  isShareDialogVisible: Ref<boolean>
) {
  const fileStore = useFileStore();
  const renamingItemId = ref<string | null>(null);
  const renameInputValue = ref('');
  const renameInput = ref<HTMLInputElement | null>(null);

  const startRename = async (item: ContentItem) => {
    renamingItemId.value = item.id;
    renameInputValue.value = item.name;
    await nextTick();
    renameInput.value?.focus();
  };

  const cancelRename = () => {
    renamingItemId.value = null;
    renameInputValue.value = '';
  };

  const finishRename = async () => {
    if (!renamingItemId.value) return;
    const item = fileStore.items.find(i => i.id === renamingItemId.value);
    if (!item || renameInputValue.value === item.name || renameInputValue.value.trim() === '') {
      cancelRename();
      return;
    }
    const newName = renameInputValue.value.trim();
    try {
      const updatedItem = item.itemType === 'folder'
        ? await renameFolder(item.id, { folderName: newName })
        : await renameFile(item.id, { fileName: newName });
      const index = fileStore.items.findIndex(i => i.id === item.id);
      if (index !== -1) fileStore.items[index].name = updatedItem.name;
    } catch (error) {
      console.error(`Failed to rename ${item.name}:`, error);
      alert('Rename failed!');
    } finally {
      cancelRename();
    }
  };
  
  const handleDelete = async (item: ContentItem) => {
    if (!confirm(`Are you sure you want to delete "${item.name}"?`)) return;
    try {
      if (item.itemType === 'folder') {
        await deleteFolder(item.id);
      } else {
        await deleteFile(item.id);
      }
      fileStore.items = fileStore.items.filter(i => i.id !== item.id);
    } catch (error) {
      console.error(`Failed to delete ${item.name}:`, error);
      alert('Delete failed!');
    }
  };

  const startMove = (item: ContentItem) => {
    itemToMove.value = item;
    isMoveDialogVisible.value = true;
  };

  const startShare = (item: ContentItem) => {
    itemToShare.value = item;
    isShareDialogVisible.value = true;
  };

  return {
    renamingItemId,
    renameInputValue,
    renameInput,
    startRename,
    cancelRename,
    finishRename,
    handleDelete,
    startMove,
    startShare,
  };
} 