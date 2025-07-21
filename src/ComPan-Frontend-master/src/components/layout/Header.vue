<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useThemeStore } from '../../store/theme';
import { useUserStore } from '../../store/user';
import DropdownMenu from '../common/DropdownMenu.vue';

const themeStore = useThemeStore();
const userStore = useUserStore();
const router = useRouter();

const searchQuery = ref('');

defineProps<{
  leftSidebarCollapsed: boolean;
  rightSidebarVisible: boolean;
}>();

const emit = defineEmits([
  'toggle-left-sidebar',
  'toggle-right-sidebar',
]);

const handleLogout = () => {
  userStore.removeToken();
  router.push('/login');
};
</script>

<template>
  <header class="app-header">
    <div class="header-left">
      <button 
        class="sidebar-toggle-btn"
        @click="$emit('toggle-left-sidebar')"
        :aria-label="leftSidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'"
      >
        <!-- Icon placeholder -->
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="currentColor" d="M3 18v-2h18v2zm0-5v-2h18v2zm0-5V6h18v2z"/></svg>
      </button>
      <div class="logo">
        ComPan
      </div>
    </div>
    
    <div class="header-center">
      <div class="search-bar">
        <!-- Search icon -->
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"><path fill="currentColor" d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5A6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19zM9.5 14C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5S14 7.01 14 9.5S11.99 14 9.5 14"/></svg>
        <input type="text" v-model="searchQuery" placeholder="Search files...">
      </div>
    </div>

    <div class="header-right">
      <button class="icon-btn" @click="themeStore.toggleTheme" aria-label="Toggle theme">
        <!-- Sun/Moon icon placeholder -->
        <svg v-if="themeStore.theme === 'light'" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="currentColor" d="M12 7a5 5 0 1 0 5 5a5 5 0 0 0-5-5m0-2a7 7 0 1 1-7 7a7 7 0 0 1 7-7m-5 12h10v2H7zm8.6-8.6l1.4-1.4l1.4 1.4l-1.4 1.4zm-12 0l1.4 1.4l1.4-1.4l-1.4-1.4zM12 2v2h-2V2zm0 18v2h-2v-2zM4.9 6.3L3.5 4.9L2.1 6.3l1.4 1.4zm15.4 0l1.4-1.4l1.4 1.4l-1.4 1.4zM22 12v2h-2v-2zm-18 0v2H2v-2z"/></svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="currentColor" d="M12 21q-3.75 0-6.375-2.625T3 12q0-3.75 2.625-6.375T12 3q.35 0 .688.025t.662.075q-1.125.7-1.825 1.9T11 7.5q0 2.25 1.575 3.825T16.4 13q.7 0 1.288-.225t1.062-.625q.05.325.075.663T18.85 13.5q0 2.5-1.55 4.613T12 21"/></svg>
      </button>

      <button 
        class="sidebar-toggle-btn"
        @click="$emit('toggle-right-sidebar')"
        :aria-label="rightSidebarVisible ? 'Hide details' : 'Show details'"
      >
        <!-- Right sidebar icon placeholder -->
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="currentColor" d="M19 18h2v-2h-2zm0-5h2v-2h-2zm0-5h2V6h-2zM3 18v-2h14v2zm0-5v-2h14v2zm0-5V6h14v2z"/></svg>
      </button>

      <DropdownMenu>
        <template #trigger>
          <div class="user-profile">
            <img src="../../assets/generic/user.svg" alt="User avatar" class="avatar">
          </div>
        </template>
        <template #content>
          <div class="dropdown-content">
            <div class="user-info">
              <p class="username">{{ userStore.user?.username || 'User' }}</p>
              <p class="email">{{ userStore.user?.email || 'email@example.com' }}</p>
            </div>
            <hr class="divider">
            <router-link to="/profile" class="dropdown-item">Profile</router-link>
            <button @click="handleLogout" class="dropdown-item">Log Out</button>
          </div>
        </template>
      </DropdownMenu>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--layout-header-height);
  padding: 0 var(--spacing-lg);
  background-color: var(--color-bg-secondary);
  border-bottom: 1px solid var(--color-border);
  transition: background-color var(--transition-base), border-color var(--transition-base);
  flex-shrink: 0;
}

.header-left, .header-center, .header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.logo {
  font-size: 1.5rem;
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.search-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  background-color: var(--color-bg-tertiary);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-sm) var(--spacing-md);
  width: 400px;
}

.search-bar svg {
  color: var(--color-text-tertiary);
}

.search-bar input {
  border: none;
  outline: none;
  background: transparent;
  width: 100%;
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
}

.sidebar-toggle-btn, .icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background-color: transparent;
  color: var(--color-text-secondary);
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: var(--transition-base);
}

.sidebar-toggle-btn:hover, .icon-btn:hover {
  background-color: var(--color-bg-tertiary);
  color: var(--color-text-primary);
}

.user-profile {
  display: flex;
  align-items: center;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.dropdown-content {
  padding: var(--spacing-sm) 0;
}

.user-info {
  padding: var(--spacing-sm) var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

.user-info .username {
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
}

.user-info .email {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  margin: 0;
}

.divider {
  border: none;
  border-top: 1px solid var(--color-divider);
  margin: var(--spacing-sm) 0;
}

.dropdown-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: var(--spacing-sm) var(--spacing-md);
  color: var(--color-text-secondary);
  background: none;
  border: none;
  font-size: var(--font-size-base);
  cursor: pointer;
  text-decoration: none;
}

.dropdown-item:hover {
  background-color: var(--color-bg-tertiary);
  color: var(--color-text-primary);
}
</style> 