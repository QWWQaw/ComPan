<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../store/user';
import { login } from '../../api/user';

const router = useRouter();
const userStore = useUserStore();

const username = ref('admin');
const password = ref('password');
const isLoading = ref(false);
const errorMessage = ref('');

const handleLogin = async () => {
  if (isLoading.value) return;

  isLoading.value = true;
  errorMessage.value = '';
  
  try {
    const response = await login({
      username: username.value,
      password: password.value,
    });
    userStore.setToken(response.token);
    userStore.setUser(response.user);
    router.push('/');
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'An unknown error occurred.';
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1 class="title">Welcome to ComPan</h1>
        <p class="subtitle">Sign in to continue</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">Username</label>
          <input 
            id="username" 
            type="text" 
            v-model="username" 
            placeholder="e.g., admin"
            required 
          />
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input 
            id="password" 
            type="password" 
            v-model="password" 
            placeholder="e.g., password"
            required 
          />
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <button type="submit" class="login-button" :disabled="isLoading">
          <span v-if="isLoading">Signing In...</span>
          <span v-else>Sign In</span>
        </button>
      </form>

      <div class="login-footer">
        <p>
          Don't have an account? <a href="/register">Sign Up</a>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100vw;
  height: 100vh;
  background-color: var(--color-bg-base);
}

.login-container {
  width: 100%;
  max-width: 400px;
  padding: var(--spacing-xl);
  background-color: var(--color-bg-secondary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-md);
  text-align: center;
}

.login-header {
  margin-bottom: var(--spacing-xl);
}

.title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.subtitle {
  color: var(--color-text-secondary);
  margin-top: var(--spacing-xs);
}

.form-group {
  margin-bottom: var(--spacing-lg);
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: var(--spacing-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}

.form-group input {
  width: 100%;
  padding: var(--spacing-md);
  font-size: 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-md);
  background-color: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  transition: var(--transition-base);
}

.form-group input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-light);
}

.error-message {
  color: #ef4444; /* A shade of red */
  background-color: rgba(239, 68, 68, 0.1);
  padding: var(--spacing-md);
  border-radius: var(--border-radius-md);
  margin-bottom: var(--spacing-lg);
  font-size: 0.875rem;
}

.login-button {
  width: 100%;
  padding: var(--spacing-md);
  font-size: 1rem;
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-on-primary);
  background-color: var(--color-primary);
  border: none;
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: var(--transition-base);
}

.login-button:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-footer {
  margin-top: var(--spacing-xl);
  font-size: 0.875rem;
  color: var(--color-text-tertiary);
}
</style>
