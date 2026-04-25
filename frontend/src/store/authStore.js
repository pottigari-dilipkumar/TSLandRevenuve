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
          set({
            user: { username: credentials.username, role: data.role },
            token: data.token,
            isLoading: false,
          });
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
          set({
            user: { username: payload.email, role: data.role },
            token: data.token,
            isLoading: false,
          });
          return data;
        } catch (error) {
          set({ error: error?.response?.data?.message || 'Unable to register.', isLoading: false });
          throw error;
        }
      },

      citizenLogin: async (aadhaarNumber, otp) => {
        set({ isLoading: true, error: null });
        try {
          const data = await authApi.citizenVerifyOtp(aadhaarNumber, otp);
          set({
            user: {
              username: data.fullName || `Citizen (${aadhaarNumber.slice(-4)})`,
              role: data.role,
              aadhaarNumber: data.aadhaarNumber,
              fullName: data.fullName,
              mobile: data.mobile,
              email: data.email,
              profileComplete: data.profileComplete,
            },
            token: data.token,
            isLoading: false,
          });
          return data;
        } catch (error) {
          set({ error: error?.response?.data?.message || 'OTP verification failed.', isLoading: false });
          throw error;
        }
      },

      updateCitizenProfile: (profileData) => {
        set((state) => ({
          user: { ...state.user, ...profileData, profileComplete: true },
        }));
      },

      hasAnyRole: (roles = []) => {
        const currentRole = get()?.user?.role;
        return roles.length === 0 || roles.includes(currentRole);
      },
    }),
    { name: 'land-revenue-auth' }
  )
);
