<template>
  <div class="guest-login-section">
    <div class="guest-login-card">
      <div class="login-brand-header">
        <div class="login-brand-icon">⚡</div>
        <h2>SSAFY MNG</h2>
        <p class="login-brand-subtitle">알고리즘 과제 AI 검수 & 학습 매니지먼트</p>
      </div>
      <form @submit.prevent="handleSubmit" class="mt-3">
        <div class="form-group">
          <label class="form-label">사용자 선택 (이름 / 학번)</label>
          <select v-model="selectedSno" class="form-input" required @focus="handleFocus">
            <option value="">-- 사용자(이름 / 학번)를 선택하세요 --</option>
            <option v-for="u in authStore.usersList" :key="u.sno" :value="u.sno">
              {{ u.role === 'ROLE_ADMIN' ? `🛡️ [관리자] ${u.name} (${u.sno})` : `👤 ${u.name} (${u.sno})` }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">비밀번호</label>
          <input 
            ref="passwordInput"
            v-model="password" 
            type="password" 
            class="form-input" 
            placeholder="비밀번호 입력" 
            required
          />
        </div>

        <div v-if="errorMessage" class="error-text mb-2">
          {{ errorMessage }}
        </div>

        <button type="submit" class="btn btn-primary btn-lg w-100 mt-2" :disabled="isLoading">
          {{ isLoading ? '⏳ 로그인 중...' : '🔐 로그인' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const selectedSno = ref('')
const password = ref('')
const errorMessage = ref('')
const isLoading = ref(false)
const passwordInput = ref<HTMLInputElement | null>(null)

onMounted(() => {
  authStore.loadUsersForLogin()
})

function handleFocus() {
  if (authStore.usersList.length === 0) {
    authStore.loadUsersForLogin()
  }
}

watch(selectedSno, (newVal) => {
  if (newVal) {
    passwordInput.value?.focus()
  }
})

async function handleSubmit() {
  if (!selectedSno.value || !password.value) return
  isLoading.value = true
  errorMessage.value = ''

  try {
    const ok = await authStore.login(selectedSno.value, password.value)
    if (ok) {
      router.push('/assignment')
    } else {
      errorMessage.value = '학번 또는 비밀번호가 올바르지 않습니다.'
    }
  } catch (e) {
    errorMessage.value = '로그인 처리 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}
</script>
