import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLoadingStore = defineStore('loading', () => {
  const activeRequests = ref(0)
  const isLoading = ref(false)

  function startLoading() {
    activeRequests.value++
    isLoading.value = true
  }

  function stopLoading() {
    activeRequests.value = Math.max(0, activeRequests.value - 1)
    if (activeRequests.value === 0) {
      isLoading.value = false
    }
  }

  return {
    isLoading,
    activeRequests,
    startLoading,
    stopLoading,
  }
})
