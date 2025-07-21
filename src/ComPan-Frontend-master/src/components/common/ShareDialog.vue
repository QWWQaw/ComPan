<script setup lang="ts">
import { ref, watch } from 'vue';
import type { ContentItem } from '../../types/file';
import type { Collaborator } from '../../types/share';
import { getUsers } from '../../api/user';
import { getUserGroups } from '../../api/usergroup';
import { useDebounceFn } from '@vueuse/core';
import type { User, UserGroup } from '../../types/user';

interface Props {
  isVisible: boolean;
  itemToShare: ContentItem | null;
}
const props = defineProps<Props>();
const emit = defineEmits(['close']);

// --- State for internal sharing (ACL) ---
const searchInput = ref('');
const searchResults = ref<Collaborator[]>([]);
const selectedCollaborators = ref<Collaborator[]>([]);
const isLoadingSearch = ref(false);

// --- State for public link sharing ---
const publicLinkEnabled = ref(false);
const publicLink = ref('https://kepan.com/s/abCdeFg123'); // Placeholder
const password = ref('');
const hasPassword = ref(false);
const expireDate = ref('');
const hasExpiry = ref(false);

const performSearch = useDebounceFn(async () => {
  if (searchInput.value.length < 2) {
    searchResults.value = [];
    return;
  }
  isLoadingSearch.value = true;
  try {
    const [usersRes, groupsRes] = await Promise.all([
      getUsers({ search: searchInput.value }),
      getUserGroups({ search: searchInput.value })
    ]);
    
    const users: Collaborator[] = usersRes.items.map((u: User) => ({ id: u.userId, name: u.username, type: 'user', email: u.email }));
    const groups: Collaborator[] = groupsRes.items.map((g: UserGroup) => ({ id: g.groupId, name: g.name, type: 'group' }));
    
    searchResults.value = [...users, ...groups];
  } catch (error) {
    console.error("Search failed:", error);
    searchResults.value = [];
  } finally {
    isLoadingSearch.value = false;
  }
}, 300);

const handleSearch = () => {
  performSearch();
};

const addCollaborator = (collaborator: Collaborator) => {
  if (!selectedCollaborators.value.find(c => c.id === collaborator.id && c.type === collaborator.type)) {
    selectedCollaborators.value.push(collaborator);
  }
  searchInput.value = '';
  searchResults.value = [];
};

</script>

<template>
  <transition name="modal-fade">
    <div v-if="isVisible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-dialog">
        <header class="modal-header">
          <h3 class="modal-title">Share "{{ itemToShare?.name }}"</h3>
          <button class="modal-close" @click="$emit('close')">&times;</button>
        </header>

        <div class="modal-body">
          <!-- Internal Sharing Section -->
          <div class="share-section">
            <h4>Share with people and groups</h4>
            <div class="search-and-add">
              <input 
                type="text" 
                v-model="searchInput"
                @input="handleSearch"
                placeholder="Search users or groups..." 
                class="search-input"
              />
              <div v-if="isLoadingSearch" class="search-results"><div class="search-result-item">Loading...</div></div>
              <div v-else-if="searchResults.length > 0" class="search-results">
                <div v-for="result in searchResults" :key="`${result.type}-${result.id}`" @click="addCollaborator(result)" class="search-result-item">
                  {{ result.name }} <span class="result-type">({{ result.type }})</span>
                </div>
              </div>
            </div>

            <div class="collaborators-list">
              <div v-for="collab in selectedCollaborators" :key="`${collab.type}-${collab.id}`" class="collaborator-item">
                <span class="collaborator-name">{{ collab.name }}</span>
                <select class="permission-select">
                  <option value="read">Can view</option>
                  <option value="write">Can edit</option>
                </select>
                <button class="remove-btn">&times;</button>
              </div>
            </div>
          </div>

          <!-- Divider -->
          <hr class="divider" />

          <!-- Public Link Sharing Section -->
          <div class="share-section">
            <h4>Get public link</h4>
            <div class="public-link-toggle">
              <span>Anyone with the link can view</span>
              <label class="switch">
                <input type="checkbox" v-model="publicLinkEnabled">
                <span class="slider round"></span>
              </label>
            </div>
            
            <div v-if="publicLinkEnabled" class="public-link-settings">
              <div class="link-display">
                <input type="text" :value="publicLink" readonly />
                <button>Copy</button>
              </div>
              <div class="settings-options">
                 <label><input type="checkbox" v-model="hasPassword" /> Password protect</label>
                 <input v-if="hasPassword" type="password" v-model="password" placeholder="Enter password" />
              </div>
               <div class="settings-options">
                 <label><input type="checkbox" v-model="hasExpiry" /> Set expiration date</label>
                 <input v-if="hasExpiry" type="date" v-model="expireDate"/>
              </div>
            </div>
          </div>
        </div>

        <footer class="modal-footer">
          <button class="btn-primary" @click="$emit('close')">Done</button>
        </footer>
      </div>
    </div>
  </transition>
</template>

<style scoped>
/* Using mostly the same modal styles as MoveItemDialog, with additions */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex; justify-content: center; align-items: center; z-index: 2000;
}
.modal-dialog {
  background-color: var(--color-bg-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-xl);
  min-width: 500px; max-width: 90vw;
  display: flex; flex-direction: column;
}
.modal-header, .modal-footer {
  padding: var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-header { border-bottom: 1px solid var(--color-border); }
.modal-footer { border-top: 1px solid var(--color-border); justify-content: flex-end; }
.modal-title { margin: 0; font-size: var(--font-size-lg); font-weight: var(--font-weight-bold); }
.modal-close { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: var(--color-text-secondary); }
.modal-body { padding: 0; max-height: 70vh; overflow-y: auto; }

.share-section {
  padding: var(--spacing-lg);
}
.divider {
  border: none;
  border-top: 1px solid var(--color-border);
  margin: 0;
}

.search-and-add {
  position: relative;
}
.search-input {
  width: 100%;
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-md);
  border: 1px solid var(--color-border);
}
.search-results {
  position: absolute;
  background: var(--color-bg-secondary);
  width: 100%;
  border: 1px solid var(--color-border);
  border-top: none;
  border-radius: 0 0 var(--border-radius-md) var(--border-radius-md);
  box-shadow: var(--shadow-md);
  z-index: 1;
}
.search-result-item {
  padding: var(--spacing-sm);
  cursor: pointer;
}
.search-result-item:hover {
  background: var(--color-bg-tertiary);
}
.result-type {
  color: var(--color-text-tertiary);
  font-size: 0.8em;
}

.collaborators-list {
  margin-top: var(--spacing-md);
  max-height: 150px;
  overflow-y: auto;
}
.collaborator-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) 0;
}
.collaborator-name {
  flex-grow: 1;
}
.permission-select {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-sm);
}
.remove-btn {
  background: none; border: none; color: var(--color-text-tertiary); cursor: pointer;
}

.public-link-toggle {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.public-link-settings {
  margin-top: var(--spacing-md);
  padding: var(--spacing-md);
  background-color: var(--color-bg-tertiary);
  border-radius: var(--border-radius-md);
}
.link-display {
  display: flex;
  gap: var(--spacing-sm);
}
.link-display input {
  flex-grow: 1;
  background-color: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-sm);
  padding: var(--spacing-xs);
}
.settings-options {
  margin-top: var(--spacing-md);
}

/* Basic Switch CSS */
.switch { position: relative; display: inline-block; width: 40px; height: 24px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #ccc; transition: .4s; }
.slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 4px; bottom: 4px; background-color: white; transition: .4s; }
input:checked + .slider { background-color: var(--color-primary); }
input:checked + .slider:before { transform: translateX(16px); }
.slider.round { border-radius: 24px; }
.slider.round:before { border-radius: 50%; }

/* Modal fade transition */
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.3s ease; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
</style> 