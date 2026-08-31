import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export interface UserSession {
  authenticated: boolean
  sno: string
  name: string
  role: 'ROLE_ADMIN' | 'ROLE_STUDENT'
  passwordChanged: boolean
}

export interface UserListItem {
  sno: string
  name: string
  role: 'ROLE_ADMIN' | 'ROLE_STUDENT'
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserSession | null>(null)
  const usersList = ref<UserListItem[]>([])
  const isChecked = ref(false)

  const isAuthenticated = computed(() => !!user.value?.authenticated)
  const isAdmin = computed(() => user.value?.role === 'ROLE_ADMIN')

  async function checkAuth() {
    try {
      const { data } = await api.get<UserSession>('/api/auth/current')
      if (data && data.authenticated) {
        user.value = data
      } else {
        user.value = null
      }
    } catch (e) {
      user.value = null
    } finally {
      isChecked.value = true
    }
  }

  async function loadUsersForLogin(retry = 2) {
    try {
      const { data } = await api.get<UserListItem[]>('/api/auth/users')
      if (Array.isArray(data)) {
        usersList.value = data
      }
    } catch (e) {
      if (retry > 0) {
        setTimeout(() => loadUsersForLogin(retry - 1), 600)
      }
    }
  }

  async function login(sno: string, password: string) {
    const formData = new FormData()
    formData.append('sno', sno)
    formData.append('password', password)

    try {
      const res = await api.post('/api/auth/login', formData)
      if (res.status >= 200 && res.status < 300) {
        await checkAuth()
        return true
      }
    } catch (e) {
      return false
    }
    return false
  }

  async function logout() {
    try {
      await api.post('/api/auth/logout')
    } catch (e) {}
    user.value = null
  }

  return {
    user,
    usersList,
    isChecked,
    isAuthenticated,
    isAdmin,
    checkAuth,
    loadUsersForLogin,
    login,
    logout
  }
})
