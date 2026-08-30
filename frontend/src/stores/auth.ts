import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

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
      const res = await fetch('/api/auth/current')
      if (res.ok) {
        const data: UserSession = await res.json()
        if (data && data.authenticated) {
          user.value = data
        } else {
          user.value = null
        }
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
      const res = await fetch('/api/auth/users', { cache: 'no-store' })
      if (res.ok) {
        const list: UserListItem[] = await res.json()
        if (Array.isArray(list)) {
          usersList.value = list
        }
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

    const res = await fetch('/api/auth/login', {
      method: 'POST',
      body: formData
    })

    if (res.ok) {
      await checkAuth()
      return true
    }
    return false
  }

  async function logout() {
    try {
      await fetch('/api/auth/logout', { method: 'POST' })
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
