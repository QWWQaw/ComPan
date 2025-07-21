import { createApp } from 'vue'
import { createPinia } from 'pinia';
import './style.css'
import App from './App.vue'
import router from './router';

// --- Mock Service ---
// Note: In a real application, this would be conditional,
// e.g., only in development mode.
import './mock';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);

app.mount('#app');
    
