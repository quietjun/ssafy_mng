import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface ProblemItem {
  id: number
  problemDate: string
  title: string
  problemType: '과제' | '워크샵'
  platformName?: string
  platformUrl?: string
  description?: string
  createdAt?: string
  submissionCount?: number
  isSubmittedByMe?: boolean
  submittedByMe?: boolean
  myResultStatus?: string
}

export interface PlatformSite {
  id: number
  name: string
  url: string
}

export const useProblemStore = defineStore('problems', () => {
  const problems = ref<ProblemItem[]>([])
  const selectedProblem = ref<ProblemItem | null>(null)
  const platforms = ref<PlatformSite[]>([])
  const isLoading = ref(false)

  async function loadPlatforms() {
    try {
      const res = await fetch('/api/platforms')
      if (res.ok) {
        const data: PlatformSite[] = await res.json()
        if (Array.isArray(data)) {
          platforms.value = data
        }
      }
    } catch (e) {
      console.error('Failed to load platforms:', e)
    }
  }

  async function loadWeeklyProblems(startDate?: string, endDate?: string) {
    isLoading.value = true
    try {
      let url = '/api/problems/weekly'
      if (startDate && endDate) {
        url += `?startDate=${startDate}&endDate=${endDate}`
      }
      const res = await fetch(url)
      if (res.ok) {
        const data: ProblemItem[] = await res.json()
        problems.value = Array.isArray(data) ? data : []
        if (problems.value.length > 0) {
          if (!selectedProblem.value || !problems.value.some(p => p.id === selectedProblem.value?.id)) {
            selectedProblem.value = problems.value[0]
          }
        } else {
          selectedProblem.value = null
        }
      }
    } catch (e) {
      console.error('Failed to load weekly/range problems:', e)
      problems.value = []
      selectedProblem.value = null
    } finally {
      isLoading.value = false
    }
  }

  async function loadDailyProblems(date: string) {
    isLoading.value = true
    try {
      const res = await fetch(`/api/problems?date=${date}`)
      if (res.ok) {
        const data: ProblemItem[] = await res.json()
        problems.value = Array.isArray(data) ? data : []
        if (problems.value.length > 0) {
          if (!selectedProblem.value || !problems.value.some(p => p.id === selectedProblem.value?.id)) {
            selectedProblem.value = problems.value[0]
          }
        } else {
          selectedProblem.value = null
        }
      }
    } catch (e) {
      console.error('Failed to load daily problems:', e)
      problems.value = []
      selectedProblem.value = null
    } finally {
      isLoading.value = false
    }
  }

  function selectProblem(p: ProblemItem | null) {
    selectedProblem.value = p
  }

  return {
    problems,
    selectedProblem,
    platforms,
    isLoading,
    loadPlatforms,
    loadWeeklyProblems,
    loadDailyProblems,
    selectProblem
  }
})
