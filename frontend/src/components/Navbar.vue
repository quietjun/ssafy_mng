<template>
  <header class="navbar">
    <div class="nav-container">
      <div class="nav-brand" @click="router.push('/')">
        <span class="brand-icon">⚡</span>
        <span class="brand-title">SSAFY MNG</span>
      </div>

      <!-- Navigation Tabs (Only shown when authenticated) -->
      <nav v-if="authStore.isAuthenticated" class="nav-tabs">
        <router-link to="/assignment" class="nav-tab" active-class="active">
          <span class="tab-icon">📝</span> 알고리즘 문제
        </router-link>
        <router-link v-if="authStore.isAdmin" to="/speaker" class="nav-tab" active-class="active">
          <span class="tab-icon">🎲</span> Today Speaker
        </router-link>
        <router-link v-if="authStore.isAdmin" to="/students" class="nav-tab" active-class="active">
          <span class="tab-icon">👨‍🎓</span> 학생 관리
        </router-link>
        <router-link v-if="authStore.isAdmin" to="/pairs" class="nav-tab" active-class="active">
          <span class="tab-icon">👥</span> 페어 관리
        </router-link>
        <router-link v-if="authStore.isAdmin" to="/grades" class="nav-tab" active-class="active">
          <span class="tab-icon">📊</span> 성적 관리
        </router-link>
        <router-link v-if="authStore.isAdmin" to="/settings" class="nav-tab" active-class="active">
          <span class="tab-icon">⚙️</span> 관리자 설정
        </router-link>
      </nav>

      <!-- User Auth Profile -->
      <div class="nav-auth">
        <div v-if="authStore.isAuthenticated" class="user-info-box">
          <span :class="['user-role-tag', { admin: authStore.isAdmin }]" style="font-size: 0.82rem; padding: 0.25rem 0.6rem;">
            {{ authStore.isAdmin ? '👑 관리자' : `${authStore.user?.name} (${authStore.user?.sno})` }}
          </span>
          <button class="btn btn-sm btn-outline" @click="$emit('open-password-modal')">비밀번호 변경</button>
          <button class="btn btn-sm btn-danger" @click="handleLogout">로그아웃</button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

defineEmits(['open-password-modal'])

const router = useRouter()
const authStore = useAuthStore()

async function handleLogout() {
  await authStore.logout()
  router.push('/')
}
</script>
