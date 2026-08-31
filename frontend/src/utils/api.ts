import axios from 'axios'
import { useLoadingStore } from '@/stores/loading'

const api = axios.create({
  timeout: 60000,
})

// 요청 인터셉터: 로딩 시작
api.interceptors.request.use(
  (config) => {
    // 특정 요청에서 로딩 스피너를 제외하고 싶은 경우 config.headers['X-Skip-Loading'] = 'true' 처리 가능
    if (!config.headers?.['X-Skip-Loading']) {
      const loadingStore = useLoadingStore()
      loadingStore.startLoading()
    }
    return config
  },
  (error) => {
    const loadingStore = useLoadingStore()
    loadingStore.stopLoading()
    return Promise.reject(error)
  }
)

// 응답 인터셉터: 로딩 종료
api.interceptors.response.use(
  (response) => {
    if (!response.config.headers?.['X-Skip-Loading']) {
      const loadingStore = useLoadingStore()
      loadingStore.stopLoading()
    }
    return response
  },
  (error) => {
    if (!error.config?.headers?.['X-Skip-Loading']) {
      const loadingStore = useLoadingStore()
      loadingStore.stopLoading()
    }
    return Promise.reject(error)
  }
)

export default api
