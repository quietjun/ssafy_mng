<template>
  <div class="guest-login-section">
    <div class="guest-login-card">
      <div class="login-brand-header">
        <div class="login-brand-icon">🔄</div>
        <h2>비밀번호 초기화</h2>
        <p class="login-brand-subtitle">학번과 이름을 확인하여 기본 비밀번호로 초기화합니다.</p>
      </div>

      <!-- 1. 초기화 성공 안내 화면 -->
      <div v-if="isSuccess" class="reset-success-box mt-3 text-center">
        <div style="font-size: 2.2rem; margin-bottom: 0.5rem;">🎉</div>
        <h4 style="color: #34d399; font-weight: 700; margin-bottom: 0.6rem;">초기화 완료!</h4>
        <p style="font-size: 0.88rem; color: #cbd5e1; line-height: 1.5; margin-bottom: 1rem;">
          비밀번호가 <strong>기본값</strong>으로 초기화되었습니다.<br />
          <span style="font-size: 0.8rem; color: #94a3b8;">
            (학생: 본인 학번 / 관리자: 1234)
          </span>
        </p>
        <div class="p-2 mb-3 rounded" style="background: rgba(245, 158, 11, 0.1); border: 1px solid rgba(245, 158, 11, 0.3); font-size: 0.8rem; color: #fbbf24;">
          ⚠️ 로그인 후 상단 프로필에서 비밀번호를 꼭 변경해 주세요.
        </div>
        <button type="button" class="btn btn-primary btn-lg w-100" @click="goToLogin">
          🔐 로그인 화면으로 이동
        </button>
      </div>

      <!-- 2. 초기화 입력 폼 -->
      <form v-else @submit.prevent="handleReset" class="mt-3">
        <div class="form-group mb-3">
          <label class="form-label">학번 (ID)</label>
          <input 
            v-model="sno" 
            type="text" 
            class="form-input" 
            placeholder="예: 20240101 또는 admin" 
            required 
            autofocus
          />
        </div>

        <div class="form-group mb-3">
          <label class="form-label">이름 (본인 확인용)</label>
          <input 
            v-model="name" 
            type="text" 
            class="form-input" 
            placeholder="등록된 성명을 입력하세요" 
            required 
          />
        </div>

        <div v-if="errorMessage" class="error-text mb-2">
          {{ errorMessage }}
        </div>

        <button type="submit" class="btn btn-primary btn-lg w-100 mt-2" :disabled="isLoading">
          {{ isLoading ? '⏳ 확인 및 초기화 중...' : '🔄 기본 비밀번호로 초기화' }}
        </button>

        <div style="text-align: center; margin-top: 1rem;">
          <a href="#" @click.prevent="goToLogin" style="font-size: 0.82rem; color: #94a3b8; text-decoration: none;">
            ← 로그인 화면으로 돌아가기
          </a>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/api'

const router = useRouter()
const sno = ref('')
const name = ref('')
const errorMessage = ref('')
const isLoading = ref(false)
const isSuccess = ref(false)

async function handleReset() {
  if (!sno.value.trim() || !name.value.trim()) return
  isLoading.value = true
  errorMessage.value = ''

  try {
    const res = await api.post('/api/auth/reset-password', {
      sno: sno.value.trim(),
      name: name.value.trim()
    })

    if (res.data?.success) {
      isSuccess.value = true
    } else {
      errorMessage.value = res.data?.message || '비밀번호 초기화에 실패했습니다.'
    }
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || '학번 또는 이름이 일치하지 않습니다.'
  } finally {
    isLoading.value = false
  }
}

function goToLogin() {
  router.push('/')
}
</script>

<style scoped>
.reset-success-box {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
