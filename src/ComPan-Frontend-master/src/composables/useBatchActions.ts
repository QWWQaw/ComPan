import type { Ref } from 'vue';
import { batchFiles, batchDownloadFiles } from '../api/file';
import { useFileStore } from '../store/file';

export function useBatchActions(
  selectedItems: Ref<Set<string>>,
  clearSelection: () => void
) {
  const fileStore = useFileStore();

  const handleBatchDownload = async () => {
    if (selectedItems.value.size === 0) return;
    const idsToDownload = Array.from(selectedItems.value);
    try {
      const blob = await batchDownloadFiles(idsToDownload);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `ComPan-Download-${new Date().toISOString().slice(0,10)}.zip`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      clearSelection();
    } catch (error) {
      console.error('Batch download failed:', error);
      alert('Failed to download selected files.');
    }
  };

  const handleBatchDelete = async () => {
    if (selectedItems.value.size === 0) return;
    if (!confirm(`Are you sure you want to delete ${selectedItems.value.size} items?`)) return;
    const idsToDelete = Array.from(selectedItems.value);
    try {
      await batchFiles({ action: 'delete', fileIds: idsToDelete });
      fileStore.items = fileStore.items.filter(item => !idsToDelete.includes(item.id));
      clearSelection();
    } catch (error) {
      console.error('Batch delete failed:', error);
      alert('Failed to delete selected items.');
    }
  };

  return {
    handleBatchDownload,
    handleBatchDelete,
  };
} 