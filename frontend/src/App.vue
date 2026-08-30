<template>
  <div id="app">
    <!-- Top Navigation Bar -->
    <Navbar @open-password-modal="isPasswordModalOpen = true" />

    <!-- Main Router View -->
    <main class="main-container">
      <!-- First-time Password Change Notice Banner -->
      <div 
        v-if="authStore.isAuthenticated && !authStore.isAdmin && !authStore.user?.passwordChanged" 
        class="alert-banner warning mb-3"
      >
        <div class="alert-content">
          <span class="alert-icon">⚠️</span>
          <span>안전한 서비스 이용을 위해 초기 비밀번호를 변경해 주세요.</span>
          <button class="btn btn-sm btn-warning" @click="isPasswordModalOpen = true">비밀번호 변경하기</button>
        </div>
      </div>

      <router-view />
    </main>

    <!-- Password Change Modal -->
    <div v-if="isPasswordModalOpen" class="modal-overlay" @click.self="isPasswordModalOpen = false">
      <div class="modal">
        <div class="modal-header">
          <h3>🔐 비밀번호 변경</h3>
          <button class="modal-close" @click="isPasswordModalOpen = false">&times;</button>
        </div>
        <form @submit.prevent="handleChangePassword" class="modal-body">
          <div class="form-group">
            <label class="form-label">현재 비밀번호</label>
            <input v-model="oldPassword" type="password" class="form-input" required>
          </div>
          <div class="form-group">
            <label class="form-label">새 비밀번호 (4자 이상)</label>
            <input v-model="newPassword" type="password" class="form-input" minlength="4" required>
          </div>
          <div class="form-group">
            <label class="form-label">새 비밀번호 확인</label>
            <input v-model="newPasswordConfirm" type="password" class="form-input" required>
          </div>
          <div v-if="pwError" class="error-text mb-2">{{ pwError }}</div>
          <div class="modal-footer" style="display:flex; gap:0.5rem; justify-content:flex-end;">
            <button type="button" class="btn btn-outline" @click="isPasswordModalOpen = false">취소</button>
            <button type="submit" class="btn btn-primary" :disabled="isChangingPw">
              {{ isChangingPw ? '변경 중...' : '비밀번호 변경' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import Navbar from '@/components/Navbar.vue'

const authStore = useAuthStore()
const isPasswordModalOpen = ref(false)
const oldPassword = ref('')
const newPassword = ref('')
const newPasswordConfirm = ref('')
const pwError = ref('')
const isChangingPw = ref(false)

onMounted(async () => {
  await authStore.checkAuth()
})

async function handleChangePassword() {
  if (newPassword.value !== newPasswordConfirm.value) {
    pwError.value = '새 비밀번호와 확인이 일치하지 않습니다.'
    return
  }
  isChangingPw.value = true
  pwError.value = ''

  try {
    const res = await fetch('/api/auth/change-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        oldPassword: oldPassword.value,
        newPassword: newPassword.value
      })
    })
    const data = await res.json()
    if (res.ok && data.success) {
      alert('✅ 비밀번호가 성공적으로 변경되었습니다!')
      isPasswordModalOpen.value = false
      oldPassword.value = ''
      newPassword.value = ''
      newPasswordConfirm.value = ''
      await authStore.checkAuth()
    } else {
      pwError.value = data.message || '비밀번호 변경 실패'
    }
  } catch (e: any) {
    pwError.value = '비밀번호 변경 중 오류가 발생했습니다.'
  } finally {
    isChangingPw.value = false
  }
}
</script>
