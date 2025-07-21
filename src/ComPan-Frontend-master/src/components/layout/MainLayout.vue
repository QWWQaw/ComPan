<script setup lang="ts">
import { ref } from 'vue';
import AppHeader from './Header.vue';
import LeftSidebar from './LeftSidebar.vue';
import RightSidebar from './RightSidebar.vue';
import AppFooter from './Footer.vue';

const leftSidebarCollapsed = ref(false);
const rightSidebarVisible = ref(false);

const toggleLeftSidebar = () => {
  leftSidebarCollapsed.value = !leftSidebarCollapsed.value;
};

const toggleRightSidebar = () => {
  rightSidebarVisible.value = !rightSidebarVisible.value;
};
</script>

<template>
  <div class="main-layout">
    <AppHeader 
      :left-sidebar-collapsed="leftSidebarCollapsed"
      :right-sidebar-visible="rightSidebarVisible"
      @toggle-left-sidebar="toggleLeftSidebar"
      @toggle-right-sidebar="toggleRightSidebar"
    />
    <div class="layout-body">
      <LeftSidebar :collapsed="leftSidebarCollapsed" />
      <main class="main-content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <template v-if="Component">
              <Suspense>
                <component :is="Component"></component>
                <template #fallback>
                  <div>Loading...</div>
                </template>
              </Suspense>
            </template>
          </router-view>
        </div>
        <AppFooter />
      </main>
      <RightSidebar :visible="rightSidebarVisible" />
    </div>
  </div>
</template>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  background-color: var(--color-bg-base);
}

.layout-body {
  display: flex;
  flex-direction: row; /* Changed from flex to row for clarity */
  flex-grow: 1;
  overflow: hidden;
  position: relative;
}

.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  transition: width var(--transition-base);
}

.content-wrapper {
  padding: var(--spacing-xl);
  flex-grow: 1;
}
</style> 