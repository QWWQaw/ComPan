<script setup lang="ts">
import type { PathItem } from '../../types/file';

defineProps<{
  path: PathItem[];
}>();

const emit = defineEmits(['navigate']);

const onNavigate = (folderId: string | null) => {
  if (folderId) {
    emit('navigate', folderId);
  }
};
</script>

<template>
  <nav aria-label="Breadcrumb" class="breadcrumb">
    <ol>
      <li v-for="(item, index) in path" :key="item.folderId || 'root'">
        <button 
          v-if="index < path.length - 1"
          @click="onNavigate(item.folderId)" 
          class="breadcrumb-link"
        >
          {{ item.name }}
        </button>
        <span v-else class="breadcrumb-current" aria-current="page">
          {{ item.name }}
        </span>
        <span v-if="index < path.length - 1" class="separator" aria-hidden="true">/</span>
      </li>
    </ol>
  </nav>
</template>

<style scoped>
.breadcrumb {
  font-size: 1.1rem;
  color: var(--color-text-secondary);
}

.breadcrumb ol {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  list-style: none;
  margin: 0;
  padding: 0;
}

.breadcrumb-link {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: inherit;
  padding: 0;
  transition: color var(--transition-base);
}

.breadcrumb-link:hover {
  color: var(--color-primary);
}

.breadcrumb-current {
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

.separator {
  margin: 0 var(--spacing-sm);
  color: var(--color-text-tertiary);
}
</style> 