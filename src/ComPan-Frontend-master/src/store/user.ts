import { defineStore } from "pinia";
import {ref} from 'vue'
import type { UserProfile, User } from '../types/user';
    

const STORE_KEY = 'user-token';

export const useUserStore = defineStore('user', () => {

    const token = ref<string | null>(localStorage.getItem(STORE_KEY));
    const user = ref<UserProfile | null>(null);

    const setToken = (newToken: string | null) => {
        token.value = newToken;
        if (newToken) {
            localStorage.setItem(STORE_KEY, newToken);
        } else {
            localStorage.removeItem(STORE_KEY);
        }
    };

    const removeToken = () => {
        setToken(null);
        setUser(null); // Also clear user profile on logout
    };

    const setUser = (newUser: UserProfile | User | null) => {
        user.value = newUser as UserProfile;
    };

    return { token, user, setToken, removeToken, setUser };
});
