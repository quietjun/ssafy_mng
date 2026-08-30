import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'
import AssignmentView from '@/views/AssignmentView.vue'
import SpeakerView from '@/views/SpeakerView.vue'
import SettingsView from '@/views/SettingsView.vue'

const routes = [
  { path: '/', name: 'login', component: LoginView },
  { path: '/assignment', name: 'assignment', component: AssignmentView, meta: { requiresAuth: true } },
  { path: '/speaker', name: 'speaker', component: SpeakerView, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/settings', name: 'settings', component: SettingsView, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  if (!authStore.isChecked) {
    await authStore.checkAuth()
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/')
  } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next('/assignment')
  } else if (to.path === '/' && authStore.isAuthenticated) {
    next('/assignment')
  } else {
    next()
  }
})

export default router
