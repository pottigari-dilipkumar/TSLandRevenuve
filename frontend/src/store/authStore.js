import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '../api/authApi';

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isLoading: false,
      error: null,
      setAuth: ({ user, token }) => set({ user, token, error: null }),
      clearError: () => set({ error: null }),
      logout: () => set({ user: null, token: null, error: null }),
      login: async (credentials) => {
        set({ isLoading: true, error: null });
        try {
          const data = await authApi.login(credentials);
          set({ user: data.user, token: data.token, isLoading: false });
          return data;
        } catch (error) {
          set({ error: error?.response?.data?.message || 'Unable to login.', isLoading: false });
          throw error;
        }
      },
      register: async (payload) => {
        set({ isLoading: true, error: null });
        try {
          const data = await authApi.register(payload);
          set({ user: data.user, token: data.token, isLoading: false });
          return data;
        } catch (error) {
          set({ error: error?.response?.data?.message || 'Unable to register.', isLoading: false });
          throw error;
        }
      },
      hasAnyRole: (roles = []) => {
        const currentRole = get()?.user?.role;
        return roles.length === 0 || roles.includes(currentRole);
      },
    }),
    { name: 'land-revenue-auth' }
  )
);
