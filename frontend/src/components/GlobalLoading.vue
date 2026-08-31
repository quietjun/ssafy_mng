<template>
  <Transition name="fade">
    <div v-if="loadingStore.isLoading" class="global-loading-overlay" aria-live="polite" aria-busy="true">
      <div class="loading-card">
        <div class="spinner-ring">
          <div></div><div></div><div></div><div></div>
        </div>
        <div class="loading-message">
          <p class="title">요청을 처리하고 있습니다</p>
          <p class="subtitle">잠시만 기다려 주세요...</p>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { useLoadingStore } from '@/stores/loading'

const loadingStore = useLoadingStore()
</script>

<style scoped>
.global-loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 99999;
  user-select: none;
  cursor: wait;
}

.loading-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 2rem 2.5rem;
  border-radius: 16px;
  box-shadow: 0 20px 35px -5px rgba(0, 0, 0, 0.2), 0 10px 15px -5px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.25rem;
  min-width: 240px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  animation: popIn 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes popIn {
  from {
    opacity: 0;
    transform: scale(0.92);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* Dual Ring Spinner */
.spinner-ring {
  display: inline-block;
  position: relative;
  width: 48px;
  height: 48px;
}
.spinner-ring div {
  box-sizing: border-box;
  display: block;
  position: absolute;
  width: 44px;
  height: 44px;
  margin: 2px;
  border: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spinner-ring 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
  border-color: #3b82f6 transparent transparent transparent;
}
.spinner-ring div:nth-child(1) {
  animation-delay: -0.45s;
}
.spinner-ring div:nth-child(2) {
  animation-delay: -0.3s;
}
.spinner-ring div:nth-child(3) {
  animation-delay: -0.15s;
}

@keyframes spinner-ring {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-message {
  text-align: center;
}

.loading-message .title {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
}

.loading-message .subtitle {
  margin: 0.25rem 0 0 0;
  font-size: 0.85rem;
  color: #64748b;
}

/* Fade Transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
