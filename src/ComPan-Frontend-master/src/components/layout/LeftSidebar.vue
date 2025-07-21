<script setup lang="ts">
import { ref } from 'vue';
import { useRoute } from 'vue-router';

defineProps<{
  collapsed: boolean;
}>();

const route = useRoute();

const navItems = ref([
  { id: 'dashboard', name: 'Dashboard', icon: '📊', path: '/dashboard/stats' },
  { id: 'my-files', name: 'My Files', icon: '📁', path: '/dashboard/my-files' },
  { id: 'shared', name: 'Shared with me', icon: '🤝', path: '/shared' },
  { id: 'trash', name: 'Trash', icon: '🗑️', path: '/trash' },
  { id: 'settings', name: 'Settings', icon: '⚙️', path: '/settings' },
]);

</script>

<template>
  <aside :class="['left-sidebar', { collapsed }]">
    <nav class="main-nav">
      <ul>
        <li
          v-for="item in navItems"
          :key="item.id"
          :class="{ active: route.meta.navId === item.id }"
        >
          <router-link :to="item.path" class="nav-link">
            <span class="nav-icon">{{ item.icon }}</span>
            <span class="nav-text">{{ item.name }}</span>
          </router-link>
        </li>
      </ul>
    </nav>

    <div class="sidebar-footer">
      <!-- Storage status can be a component later -->
      <div class="storage-status">
        <span class="nav-icon">💾</span>
        <div class="nav-text">
          <p>Storage</p>
          <progress value="30" max="100"></progress>
          <span>3GB / 10GB Used</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.left-sidebar {
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-secondary);
  width: var(--sidebar-width-open);
  border-right: 1px solid var(--color-border);
  transition: width var(--transition-base), background-color var(--transition-base), border-color var(--transition-base);
  overflow: hidden;
  flex-shrink: 0;
}

.left-sidebar.collapsed {
  width: var(--sidebar-width-collapsed);
}

.main-nav {
  flex-grow: 1;
  padding: var(--spacing-md) 0;
}

.main-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
  text-decoration: none;
  white-space: nowrap;
  border-radius: var(--border-radius-md);
  margin: var(--spacing-xs) var(--spacing-md);
  transition: var(--transition-base);
}

.nav-link:hover {
  background-color: var(--color-bg-tertiary);
  color: var(--color-text-primary);
}

li.active .nav-link {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
}

.nav-icon {
  font-size: 1.5rem;
  width: 24px;
  text-align: center;
}

.nav-text {
  opacity: 1;
  transition: opacity 0.2s ease-in-out;
}

.left-sidebar.collapsed .nav-text {
  opacity: 0;
  pointer-events: none;
}

.sidebar-footer {
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-divider);
}

.storage-status {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.storage-status p {
  margin: 0 0 var(--spacing-xs);
  font-weight: var(--font-weight-medium);
}

.storage-status progress {
  width: 100%;
}
</style> 