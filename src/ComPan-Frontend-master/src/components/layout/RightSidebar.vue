<script setup lang="ts">
defineProps<{
  visible: boolean;
}>();
</script>

<template>
  <aside :class="['right-sidebar', { visible }]">
    <div class="sidebar-header">
      <h3>File Details</h3>
      <!-- Close button could be added here -->
    </div>
    <div class="sidebar-content">
      <p>This is where file preview or an AI assistant could live.</p>
      <p>For now, it's a placeholder.</p>
    </div>
  </aside>
</template>

<style scoped>
.right-sidebar {
  width: var(--sidebar-right-width);
  background-color: var(--color-bg-secondary);
  border-left: 1px solid var(--color-border);
  flex-shrink: 0;
  margin-right: calc(-1 * var(--sidebar-right-width));
  transition: margin-right var(--transition-base), background-color var(--transition-base), border-color var(--transition-base);
  z-index: 900;
  display: flex;
  flex-direction: column;
}

.right-sidebar.visible {
  margin-right: 0;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.05);
}

.sidebar-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-divider);
  flex-shrink: 0;
}

.sidebar-header h3 {
  font-size: 1.1rem;
}

.sidebar-content {
  padding: var(--spacing-lg);
  flex-grow: 1;
  overflow-y: auto;
}
</style> 