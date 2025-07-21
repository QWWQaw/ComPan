import { ref, computed } from 'vue';

export function useFileSelection() {
  const selectedItems = ref<Set<string>>(new Set());

  const isSelected = (itemId: string) => selectedItems.value.has(itemId);

  const toggleSelection = (itemId: string) => {
    if (isSelected(itemId)) {
      selectedItems.value.delete(itemId);
    } else {
      selectedItems.value.add(itemId);
    }
  };

  const selectedCount = computed(() => selectedItems.value.size);

  const clearSelection = () => {
    selectedItems.value.clear();
  };

  return {
    selectedItems,
    isSelected,
    toggleSelection,
    selectedCount,
    clearSelection,
  };
} 