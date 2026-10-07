import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'
import { cursorManager } from '@/utils/cursorManager'

export interface ShopItem {
  id: string
  name: string
  category: 'AVATAR' | 'FRAME' | 'THEME' | 'TITLE' | 'BANNER' | 'CURSOR'
  icon?: string
  description?: string
  price: number
  defaultOwned?: boolean
  previewColor?: string
  previewClass?: string
}

export interface ShopProfile {
  sno: string
  name: string
  points: number
  totalPointsEarned: number
  equippedAvatar: string
  equippedFrame: string
  equippedTheme: string
  equippedTitle: string
  equippedBanner?: string
  equippedCursor?: string
  unlockedItemIds: string[]
}

const AVATAR_MAP: Record<string, string> = {
  robot: '🤖',
  cat: '🐱',
  wizard: '🧙‍♂️',
  ninja: '🥷',
  alchemist: '☕',
  dragon: '🐉'
}

const TITLE_MAP: Record<string, string> = {
  '새싹 개발자': '🌱',
  'D1 학살자': '⚔️',
  '백준 골드 원정대': '🧭',
  '메모리 최적화 장인': '🧠',
  '칼퇴의 요정': '🧚',
  '알고리즘 연금술사': '🧪'
}

const THEME_MAP: Record<string, string> = {
  'default': '🌑',
  'cyberpunk': '🌌',
  'matrix': '📟',
  'midnight': '☕',
  'retro': '🎮',
  'aurora': '🔮'
}

const CURSOR_MAP: Record<string, string> = {
  'cursor-default': '🖱️',
  'cursor-magic-star': '✨',
  'cursor-cyber-crosshair': '🎯',
  'cursor-pixel-sword': '🗡️',
  'cursor-cat-paw': '🐾',
  'cursor-fire-flame': '🔥',
  'cursor-code-terminal': '⚡',
  'cursor-bubble-pop': '🫧'
}

export const useShopStore = defineStore('shop', () => {
  const catalog = ref<ShopItem[]>([])
  const profile = ref<ShopProfile | null>({
    sno: '',
    name: '',
    points: 0,
    totalPointsEarned: 0,
    equippedAvatar: localStorage.getItem('cached_equipped_avatar') || 'robot',
    equippedFrame: localStorage.getItem('cached_equipped_frame') || 'none',
    equippedTheme: localStorage.getItem('user_theme') || 'default',
    equippedTitle: localStorage.getItem('cached_equipped_title') || '새싹 개발자',
    equippedBanner: localStorage.getItem('cached_equipped_banner') || 'banner-default',
    equippedCursor: localStorage.getItem('cached_equipped_cursor') || 'cursor-default',
    unlockedItemIds: []
  })
  const ranking = ref<ShopProfile[]>([])
  const isLoading = ref(false)
  const isOpenModal = ref(false)
  const activeTab = ref<'AVATAR' | 'FRAME' | 'THEME' | 'TITLE' | 'BANNER' | 'CURSOR' | 'RANKING'>('AVATAR')

  function getAvatarIcon(avatarId?: string): string {
    if (!avatarId) return '🤖'
    if (AVATAR_MAP[avatarId]) return AVATAR_MAP[avatarId]
    const item = catalog.value.find(i => i.id === avatarId && i.category === 'AVATAR')
    return item?.icon || '🤖'
  }

  function getTitleIcon(titleName?: string): string {
    if (!titleName) return '🌱'
    if (TITLE_MAP[titleName]) return TITLE_MAP[titleName]
    const item = catalog.value.find(i => (i.name === titleName || i.id === titleName) && i.category === 'TITLE')
    return item?.icon || '🏷️'
  }

  function getTitleWithIcon(titleName?: string): string {
    const name = titleName || profile.value?.equippedTitle || '새싹 개발자'
    const icon = getTitleIcon(name)
    return `${icon} ${name}`
  }

  function getThemeIcon(themeId?: string): string {
    if (!themeId) return '🌑'
    if (THEME_MAP[themeId]) return THEME_MAP[themeId]
    const item = catalog.value.find(i => i.id === themeId && i.category === 'THEME')
    return item?.icon || '🎨'
  }

  function getCursorIcon(cursorId?: string): string {
    if (!cursorId) return '🖱️'
    if (CURSOR_MAP[cursorId]) return CURSOR_MAP[cursorId]
    const item = catalog.value.find(i => i.id === cursorId && i.category === 'CURSOR')
    return item?.icon || '🖱️'
  }

  function getFrameClass(frameId?: string): string {
    if (!frameId || frameId === 'none') return 'frame-none'
    const pure = frameId.replace('frame-', '')
    return `frame-${pure}`
  }

  const currentAvatarIcon = computed(() => {
    return getAvatarIcon(profile.value?.equippedAvatar)
  })

  const currentFrameClass = computed(() => {
    return getFrameClass(profile.value?.equippedFrame)
  })

  const currentTitleName = computed(() => {
    return profile.value?.equippedTitle || '새싹 개발자'
  })

  const currentTitleIcon = computed(() => {
    return getTitleIcon(profile.value?.equippedTitle)
  })

  const currentTitle = computed(() => {
    return getTitleWithIcon(profile.value?.equippedTitle)
  })

  const currentThemeIcon = computed(() => {
    return getThemeIcon(profile.value?.equippedTheme)
  })

  const currentBannerClass = computed(() => {
    return profile.value?.equippedBanner || localStorage.getItem('cached_equipped_banner') || 'banner-default'
  })

  const currentCursorIcon = computed(() => {
    return getCursorIcon(profile.value?.equippedCursor)
  })

  function applyTheme(themeId?: string) {
    const targetTheme = themeId || profile.value?.equippedTheme || localStorage.getItem('user_theme') || 'default'
    if (targetTheme === 'default') {
      document.documentElement.removeAttribute('data-theme')
    } else {
      document.documentElement.setAttribute('data-theme', targetTheme)
    }
    localStorage.setItem('user_theme', targetTheme)
  }

  function applyCursor(cursorId?: string) {
    const target = cursorId || profile.value?.equippedCursor || localStorage.getItem('cached_equipped_cursor') || 'cursor-default'
    cursorManager.setCursor(target)
    localStorage.setItem('cached_equipped_cursor', target)
  }

  async function loadCatalog() {
    try {
      const { data } = await api.get<ShopItem[]>('/api/shop/catalog')
      catalog.value = Array.isArray(data) ? data : []
    } catch (e) {
      console.error('Failed to load shop catalog:', e)
    }
  }

  async function loadProfile() {
    if (catalog.value.length === 0) {
      loadCatalog()
    }
    try {
      const { data } = await api.get<ShopProfile>('/api/shop/my')
      profile.value = data
      if (data) {
        localStorage.setItem('cached_equipped_avatar', data.equippedAvatar || 'robot')
        localStorage.setItem('cached_equipped_frame', data.equippedFrame || 'none')
        localStorage.setItem('cached_equipped_title', data.equippedTitle || '새싹 개발자')
        localStorage.setItem('cached_equipped_banner', data.equippedBanner || 'banner-default')
        localStorage.setItem('cached_equipped_cursor', data.equippedCursor || 'cursor-default')
        if (data.equippedTheme) {
          applyTheme(data.equippedTheme)
        }
        applyCursor(data.equippedCursor)
      }
    } catch (e) {
      console.warn('Shop profile load failed (maybe guest or admin):', e)
    }
  }

  async function loadRanking() {
    try {
      const { data } = await api.get<ShopProfile[]>('/api/shop/ranking')
      ranking.value = Array.isArray(data) ? data : []
    } catch (e) {
      console.error('Failed to load ranking:', e)
    }
  }

  async function buyItem(itemId: string) {
    try {
      const { data } = await api.post<ShopProfile>('/api/shop/buy', { itemId })
      profile.value = data
      if (data) {
        localStorage.setItem('cached_equipped_avatar', data.equippedAvatar || 'robot')
        localStorage.setItem('cached_equipped_frame', data.equippedFrame || 'none')
        localStorage.setItem('cached_equipped_title', data.equippedTitle || '새싹 개발자')
        localStorage.setItem('cached_equipped_banner', data.equippedBanner || 'banner-default')
        localStorage.setItem('cached_equipped_cursor', data.equippedCursor || 'cursor-default')
      }
      return { success: true }
    } catch (e: any) {
      const msg = e.response?.data?.message || '아이템 구매에 실패했습니다.'
      return { success: false, message: msg }
    }
  }

  async function equipItem(category: string, itemId: string) {
    try {
      const { data } = await api.post<ShopProfile>('/api/shop/equip', { category, itemId })
      profile.value = data
      if (data) {
        localStorage.setItem('cached_equipped_avatar', data.equippedAvatar || 'robot')
        localStorage.setItem('cached_equipped_frame', data.equippedFrame || 'none')
        localStorage.setItem('cached_equipped_title', data.equippedTitle || '새싹 개발자')
        localStorage.setItem('cached_equipped_banner', data.equippedBanner || 'banner-default')
        localStorage.setItem('cached_equipped_cursor', data.equippedCursor || 'cursor-default')
      }
      if (category === 'THEME') {
        applyTheme(itemId)
      } else if (category === 'CURSOR') {
        applyCursor(itemId)
      }
      return { success: true }
    } catch (e: any) {
      const msg = e.response?.data?.message || '아이템 장착에 실패했습니다.'
      return { success: false, message: msg }
    }
  }

  function openShop(tab: 'AVATAR' | 'FRAME' | 'THEME' | 'TITLE' | 'BANNER' | 'CURSOR' | 'RANKING' = 'AVATAR') {
    activeTab.value = tab
    isOpenModal.value = true
    loadCatalog()
    loadProfile()
    if (tab === 'RANKING') {
      loadRanking()
    }
  }

  function closeShop() {
    isOpenModal.value = false
  }

  return {
    catalog,
    profile,
    ranking,
    isLoading,
    isOpenModal,
    activeTab,
    currentAvatarIcon,
    currentFrameClass,
    currentTitle,
    currentTitleName,
    currentTitleIcon,
    currentThemeIcon,
    currentBannerClass,
    currentCursorIcon,
    getAvatarIcon,
    getFrameClass,
    getTitleIcon,
    getTitleWithIcon,
    getThemeIcon,
    getCursorIcon,
    applyTheme,
    applyCursor,
    loadCatalog,
    loadProfile,
    loadRanking,
    buyItem,
    equipItem,
    openShop,
    closeShop
  }
})
