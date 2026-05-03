import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  token: string | null
  userId: number | null
  school: string | null
  account: string | null
  isAuthenticated: boolean
  login: (token: string, userId: number, school: string, account: string) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      userId: null,
      school: null,
      account: null,
      isAuthenticated: false,
      login: (token, userId, school, account) =>
        set({ token, userId, school, account, isAuthenticated: true }),
      logout: () =>
        set({ token: null, userId: null, school: null, account: null, isAuthenticated: false }),
    }),
    {
      name: 'auth-storage',
    }
  )
)
