import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getFolderContents, getFolderPath } from '../api/folder';
import type { ContentItem, PathItem } from '../types/file';

export const useFileStore = defineStore('file', () => {
  const items = ref<ContentItem[]>([]);
  const path = ref<PathItem[]>([]);
  const currentFolderId = ref<string | null>('root');
  const isLoading = ref(false);

  async function fetchFolderContents(folderId: string) {
    isLoading.value = true;
    try {
      // In a real app, these might be combined into one API call
      const [contentsResponse, pathResponse] = await Promise.all([
        getFolderContents({ folderId }),
        getFolderPath(folderId),
      ]);
      
      items.value = contentsResponse.items;
      path.value = pathResponse.pathItems;
      currentFolderId.value = folderId;

    } catch (error) {
      console.error(`Failed to fetch contents for folder ${folderId}:`, error);
      // Handle error, maybe show a notification to the user
    } finally {
      isLoading.value = false;
    }
  }

  function navigateToFolder(folderId: string) {
    fetchFolderContents(folderId);
  }

  return {
    items,
    path,
    currentFolderId,
    isLoading,
    fetchFolderContents,
    navigateToFolder,
  };
}); 