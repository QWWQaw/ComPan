import { ref } from 'vue';
import type { Ref } from 'vue';
import { useFileStore } from '../store/file';
import { uploadFile, type UploadProgressData } from '../utils/uploader';

interface UploadTask {
  id: number;
  name: string;
  progress: UploadProgressData;
}

export function useUpload(currentFolderId: Ref<string | null>) {
  const fileStore = useFileStore();
  const uploadTasks = ref<UploadTask[]>([]);
  const isDragging = ref(false);
  let dragCounter = 0;

  const handleDragEnter = (e: DragEvent) => {
    e.preventDefault();
    dragCounter++;
    if (e.dataTransfer?.items && e.dataTransfer.items.length > 0) {
      isDragging.value = true;
    }
  };

  const handleDragLeave = (e: DragEvent) => {
    e.preventDefault();
    dragCounter--;
    if (dragCounter === 0) {
      isDragging.value = false;
    }
  };

  const handleDragOver = (e: DragEvent) => {
    e.preventDefault();
  };

  const doUpload = async (file: File) => {
    const taskId = Date.now() + Math.random();
    uploadTasks.value.push({
      id: taskId,
      name: file.name,
      progress: { percentage: 0, uploadedSize: 0, totalSize: file.size },
    });
    try {
      await uploadFile({
        file,
        parentId: currentFolderId.value || 'root',
        onUploadProgress: (progressData) => {
          const task = uploadTasks.value.find(t => t.id === taskId);
          if (task) task.progress = progressData;
        },
      });
      fileStore.fetchFolderContents(currentFolderId.value || 'root');
    } catch (error) {
      console.error('Upload failed:', error);
      alert(`Upload of ${file.name} failed!`);
    } finally {
      setTimeout(() => {
        uploadTasks.value = uploadTasks.value.filter(t => t.id !== taskId);
      }, 5000);
    }
  };
  
  const handleDrop = async (e: DragEvent) => {
    e.preventDefault();
    isDragging.value = false;
    dragCounter = 0;
    const files = e.dataTransfer?.files;
    if (!files || files.length === 0) return;
    for (const file of files) {
      doUpload(file);
    }
  };
  
  const handleFileSelect = async (event: Event) => {
    const target = event.target as HTMLInputElement;
    const files = target.files;
    if (!files || files.length === 0) return;
    for (const file of files) {
      doUpload(file);
    }
    if (target) target.value = '';
  };

  return {
    uploadTasks,
    isDragging,
    handleDragEnter,
    handleDragLeave,
    handleDragOver,
    handleDrop,
    handleFileSelect,
  };
} 