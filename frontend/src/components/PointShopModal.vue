<template>
  <div v-if="shopStore.isOpenModal" class="modal-overlay" @click.self="shopStore.closeShop">
    <div class="modal shop-modal">
      <!-- Modal Header -->
      <div class="modal-header shop-modal-header">
        <div class="header-left">
          <span class="shop-title-icon">🎮</span>
          <div>
            <h3 class="shop-title">포인트 상점 & 커스텀 스튜디오</h3>
            <div class="shop-subtitle-row">
              <span class="shop-subtitle-tag hw">📘 과제 Pass <strong>+10P</strong></span>
              <span class="shop-subtitle-tag ws">🛠️ 워크샵 Pass <strong>+20P</strong></span>
              <span class="shop-subtitle-desc">모은 포인트로 나만의 스타일을 꾸며보세요!</span>
            </div>
          </div>
        </div>

        <div class="header-right">
          <div class="my-points-badge" title="현재 보유 포인트">
            <span class="coin-icon">🪙</span>
            <span class="coin-amount">{{ shopStore.profile?.points ?? 0 }}</span>
            <span class="coin-unit">P</span>
          </div>
          <button class="modal-close" @click="shopStore.closeShop">&times;</button>
        </div>
      </div>

      <!-- Modal Body -->
      <div class="modal-body shop-modal-body">
        <!-- 1) My Live Avatar & Theme Preview Card with Equipped Banner Skin -->
        <div class="preview-showcase-card" :class="shopStore.currentBannerClass">
          <div class="preview-avatar-box">
            <div :class="['avatar-frame-badge', currentFrameClass, 'avatar-preview-circle']">
              <span class="avatar-emoji">{{ currentAvatarIcon }}</span>
            </div>
          </div>

          <div class="preview-info-box">
            <div class="preview-title-row">
              <span class="preview-title-badge">{{ shopStore.currentTitle }}</span>
              <span class="preview-theme-badge">{{ shopStore.currentThemeIcon }} {{ currentThemeName }}</span>
              <span class="preview-cursor-badge" title="현재 장착된 마우스 커서">🖱️ {{ currentCursorName }}</span>
            </div>
            <div class="preview-name-row">
              <h4 class="preview-student-name">{{ shopStore.profile?.name || '학생' }}</h4>
              <span class="preview-student-sno">({{ shopStore.profile?.sno }})</span>
            </div>
            <div class="preview-stats-row">
              <span>누적 획득: <strong style="color:#fbbf24;">{{ shopStore.profile?.totalPointsEarned ?? 0 }}P</strong></span>
              <span>·</span>
              <span>보유 아이템: <strong style="color:#38bdf8;">{{ shopStore.profile?.unlockedItemIds?.length ?? 4 }}개</strong></span>
            </div>
          </div>
        </div>

        <!-- 2) Category Navigation Tabs -->
        <div class="shop-tabs">
          <button 
            type="button" 
            :class="['shop-tab', { active: shopStore.activeTab === 'AVATAR' }]"
            @click="shopStore.activeTab = 'AVATAR'"
          >
            🎭 아바타
          </button>
          <button 
            type="button" 
            :class="['shop-tab', { active: shopStore.activeTab === 'FRAME' }]"
            @click="shopStore.activeTab = 'FRAME'"
          >
            🖼️ 오라
          </button>
          <button 
            type="button" 
            :class="['shop-tab', { active: shopStore.activeTab === 'THEME' }]"
            @click="shopStore.activeTab = 'THEME'"
          >
            🎨 테마
          </button>
          <button 
            type="button" 
            :class="['shop-tab', { active: shopStore.activeTab === 'TITLE' }]"
            @click="shopStore.activeTab = 'TITLE'"
          >
            🏷️ 칭호
          </button>
          <button 
            type="button" 
            :class="['shop-tab', { active: shopStore.activeTab === 'BANNER' }]"
            @click="shopStore.activeTab = 'BANNER'"
          >
            👜 배너
          </button>
          <button 
            type="button" 
            :class="['shop-tab', { active: shopStore.activeTab === 'CURSOR' }]"
            @click="shopStore.activeTab = 'CURSOR'"
          >
            🖱️ 커서
          </button>
          <button 
            type="button" 
            :class="['shop-tab', { active: shopStore.activeTab === 'RANKING' }]"
            @click="switchToRanking"
          >
            🏆 랭킹
          </button>
        </div>

        <!-- 3) Tab Content: Item Grid Cards (For AVATAR, FRAME, THEME, TITLE, BANNER, CURSOR) -->
        <div v-if="shopStore.activeTab !== 'RANKING'" class="shop-items-container">
          <div class="shop-items-grid">
            <div 
              v-for="item in currentCategoryItems" 
              :key="item.id" 
              :class="['shop-item-card', { equipped: isEquipped(item), owned: isOwned(item) }]"
              @mouseenter="item.category === 'CURSOR' ? previewCursor(item.id) : null"
              @mouseleave="item.category === 'CURSOR' ? restoreCursor() : null"
            >
              <!-- Card Top Preview Icon -->
              <div class="item-icon-area">
                <!-- If category is FRAME, show preview avatar inside frame aura -->
                <div v-if="item.category === 'FRAME'" :class="['avatar-frame-badge', getFrameClass(item), 'item-frame-box']">
                  <span class="frame-preview-emoji">{{ currentAvatarIcon }}</span>
                </div>
                <!-- If category is THEME, show color preview square -->
                <div v-else-if="item.category === 'THEME'" class="item-theme-preview" :style="{ backgroundColor: item.previewColor || '#0b0f19' }">
                  <span style="font-size:1.6rem;">{{ item.icon }}</span>
                </div>
                <!-- If category is BANNER, show luxury brand pattern box -->
                <div v-else-if="item.category === 'BANNER'" :class="['item-banner-preview', item.previewClass || 'banner-default']">
                  <div class="banner-preview-overlay">
                    <span class="banner-preview-badge">{{ item.icon }} {{ item.name.split(' (')[0] }}</span>
                  </div>
                </div>
                <!-- If category is CURSOR, show cursor preview box with hover test label -->
                <div v-else-if="item.category === 'CURSOR'" class="item-cursor-preview">
                  <span class="cursor-preview-icon">{{ item.icon }}</span>
                  <span class="cursor-test-hint">마우스 올려 체험 ✨</span>
                </div>
                <!-- Default Avatar / Title Icon -->
                <span v-else class="item-emoji">{{ item.icon || '✨' }}</span>
              </div>

              <!-- Card Content Info -->
              <div class="item-info-area">
                <div class="item-header-row">
                  <h4 class="item-name">{{ item.name }}</h4>
                  <span v-if="item.price === 0" class="price-tag free">무료</span>
                  <span v-else class="price-tag">🪙 {{ item.price }}P</span>
                </div>
                <p class="item-desc">{{ item.description }}</p>
              </div>

              <!-- Card Action Button -->
              <div class="item-action-area">
                <button 
                  v-if="isEquipped(item)" 
                  type="button" 
                  class="btn btn-sm btn-equipped" 
                  disabled
                >
                  ✨ 장착 중
                </button>
                <button 
                  v-else-if="isOwned(item)" 
                  type="button" 
                  class="btn btn-sm btn-primary w-100" 
                  @click="handleEquip(item)"
                >
                  장착하기
                </button>
                <button 
                  v-else 
                  type="button" 
                  class="btn btn-sm btn-success w-100" 
                  :disabled="(shopStore.profile?.points ?? 0) < item.price"
                  @click="handleBuy(item)"
                >
                  <span v-if="(shopStore.profile?.points ?? 0) >= item.price">🪙 {{ item.price }}P 구매</span>
                  <span v-else style="font-size:0.78rem; opacity:0.8;">포인트 부족</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 4) Tab Content: Ranking Leaderboard -->
        <div v-else class="ranking-container">
          <div class="ranking-header-desc">
            <span>🏆 과제 및 워크샵을 열심히 풀고 포인트를 가장 많이 모은 학생 순위입니다.</span>
            <button class="btn btn-sm btn-outline" @click="shopStore.loadRanking">🔄 새로고침</button>
          </div>

          <div class="ranking-list">
            <div 
              v-for="(st, idx) in shopStore.ranking" 
              :key="st.sno" 
              :class="['ranking-row', { 'my-ranking': st.sno === shopStore.profile?.sno }]"
            >
              <div class="ranking-rank">
                <span v-if="idx === 0" class="rank-medal gold">🥇 1위</span>
                <span v-else-if="idx === 1" class="rank-medal silver">🥈 2위</span>
                <span v-else-if="idx === 2" class="rank-medal bronze">🥉 3위</span>
                <span v-else class="rank-num">{{ idx + 1 }}위</span>
              </div>

              <div class="ranking-student">
                <div :class="['avatar-frame-badge', getFrameClass(st.equippedFrame), 'rank-avatar']">
                  {{ getAvatarIcon(st.equippedAvatar) }}
                </div>
                <div>
                  <div style="display:flex; align-items:center; gap:0.4rem;">
                    <strong class="rank-name">{{ st.name }}</strong>
                    <span class="rank-title-badge">{{ shopStore.getTitleWithIcon(st.equippedTitle) }}</span>
                  </div>
                  <span class="rank-sno">({{ st.sno }})</span>
                </div>
              </div>

              <div class="ranking-points">
                <span class="rank-point-label">누적 획득:</span>
                <span class="rank-point-val">🪙 {{ st.totalPointsEarned }}P</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useShopStore, type ShopItem } from '@/stores/shop'

const shopStore = useShopStore()

function switchToRanking() {
  shopStore.activeTab = 'RANKING'
  shopStore.loadRanking()
}

const currentCategoryItems = computed(() => {
  return shopStore.catalog.filter(i => i.category === shopStore.activeTab)
})

const currentAvatarIcon = computed(() => {
  const avatarId = shopStore.profile?.equippedAvatar || 'robot'
  const item = shopStore.catalog.find(i => i.id === avatarId && i.category === 'AVATAR')
  return item?.icon || '🤖'
})

const currentFrameClass = computed(() => {
  return shopStore.currentFrameClass
})

const currentThemeName = computed(() => {
  const themeId = shopStore.profile?.equippedTheme || 'default'
  const item = shopStore.catalog.find(i => i.id === themeId && i.category === 'THEME')
  return item?.name || '다크 슬레이트'
})

const currentCursorName = computed(() => {
  const cursorId = shopStore.profile?.equippedCursor || 'cursor-default'
  const item = shopStore.catalog.find(i => i.id === cursorId && i.category === 'CURSOR')
  return item?.name || '클래식 포인터'
})

function previewCursor(cursorId: string) {
  shopStore.applyCursor(cursorId)
}

function restoreCursor() {
  shopStore.applyCursor(shopStore.profile?.equippedCursor || 'cursor-default')
}

function isOwned(item: ShopItem): boolean {
  if (item.defaultOwned) return true
  return shopStore.profile?.unlockedItemIds?.includes(item.id) ?? false
}

function isEquipped(item: ShopItem): boolean {
  if (!shopStore.profile) return false
  switch (item.category) {
    case 'AVATAR': return shopStore.profile.equippedAvatar === item.id
    case 'FRAME': return shopStore.profile.equippedFrame === item.id
    case 'THEME': return shopStore.profile.equippedTheme === item.id
    case 'TITLE': return shopStore.profile.equippedTitle === item.name
    case 'BANNER': return (shopStore.profile.equippedBanner || 'banner-default') === item.id
    case 'CURSOR': return (shopStore.profile.equippedCursor || 'cursor-default') === item.id
    default: return false
  }
}

async function handleBuy(item: ShopItem) {
  if (confirm(`'${item.name}' 아이템을 🪙 ${item.price}P에 구매하시겠습니까?`)) {
    const res = await shopStore.buyItem(item.id)
    if (res.success) {
      alert(`🎉 '${item.name}' 구매를 완료했습니다! 바로 장착해보세요.`)
    } else {
      alert(res.message || '구매 실패')
    }
  }
}

async function handleEquip(item: ShopItem) {
  const res = await shopStore.equipItem(item.category, item.id)
  if (res.success) {
    // 테마나 아바타가 즉시 변경됨
  } else {
    alert(res.message || '장착 실패')
  }
}

function getAvatarIcon(avatarId?: string) {
  const item = shopStore.catalog.find(i => i.id === avatarId && i.category === 'AVATAR')
  return item?.icon || '🤖'
}

function getFrameClass(input?: string | ShopItem): string {
  if (!input) return 'frame-none'
  const targetId = typeof input === 'string' ? input : (input.previewClass || input.id)
  if (!targetId || targetId === 'none') return 'frame-none'
  if (targetId.includes('cyan')) return 'frame-cyan'
  if (targetId.includes('purple')) return 'frame-purple'
  if (targetId.includes('gold')) return 'frame-gold'
  if (targetId.startsWith('frame-')) return targetId
  return `frame-${targetId}`
}
</script>

<style scoped>
.shop-modal {
  max-width: 860px;
  width: 95%;
  max-height: 88vh;
  display: flex;
  flex-direction: column;
}

.shop-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--border-color);
  background: rgba(15, 23, 42, 0.6);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.shop-title-icon {
  font-size: 1.8rem;
}

.shop-title {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
  color: #f8fafc;
}

.shop-subtitle-row {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  flex-wrap: wrap;
  margin-top: 0.25rem;
}

.shop-subtitle-tag {
  font-size: 0.74rem;
  font-weight: 600;
  padding: 0.15rem 0.5rem;
  border-radius: 9999px;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  line-height: 1.2;
}

.shop-subtitle-tag.hw {
  background: rgba(59, 130, 246, 0.15);
  color: #93c5fd;
  border: 1px solid rgba(59, 130, 246, 0.35);
}

.shop-subtitle-tag.hw strong {
  color: #60a5fa;
  font-weight: 700;
}

.shop-subtitle-tag.ws {
  background: rgba(16, 185, 129, 0.15);
  color: #6ee7b7;
  border: 1px solid rgba(16, 185, 129, 0.35);
}

.shop-subtitle-tag.ws strong {
  color: #34d399;
  font-weight: 700;
}

.shop-subtitle-desc {
  font-size: 0.78rem;
  color: #94a3b8;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.my-points-badge {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  background: rgba(251, 191, 36, 0.15);
  border: 1px solid rgba(251, 191, 36, 0.4);
  padding: 0.35rem 0.8rem;
  border-radius: 20px;
}

.coin-icon {
  font-size: 1.1rem;
}

.coin-amount {
  font-size: 1.05rem;
  font-weight: 800;
  color: #fbbf24;
}

.coin-unit {
  font-size: 0.78rem;
  font-weight: 700;
  color: #fde68a;
}

.shop-modal-body {
  padding: 1.2rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

/* 1) Showcase Card */
.preview-showcase-card {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(15, 23, 42, 0.95) 100%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  padding: 1rem 1.25rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.avatar-preview-circle {
  width: 74px;
  height: 74px;
  background: rgba(0, 0, 0, 0.4);
}

.avatar-emoji {
  font-size: 2.6rem;
}

.preview-info-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.preview-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.preview-title-badge {
  font-size: 0.75rem;
  font-weight: 700;
  background: rgba(99, 102, 241, 0.2);
  color: #a5b4fc;
  border: 1px solid rgba(99, 102, 241, 0.4);
  padding: 0.15rem 0.5rem;
  border-radius: 12px;
}

.preview-theme-badge {
  font-size: 0.75rem;
  background: rgba(56, 189, 248, 0.15);
  color: #38bdf8;
  padding: 0.15rem 0.5rem;
  border-radius: 12px;
}

.preview-cursor-badge {
  font-size: 0.75rem;
  background: rgba(244, 114, 182, 0.15);
  color: #f472b6;
  border: 1px solid rgba(244, 114, 182, 0.35);
  padding: 0.15rem 0.5rem;
  border-radius: 12px;
}

.preview-name-row {
  display: flex;
  align-items: baseline;
  gap: 0.4rem;
}

.preview-student-name {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
  color: #f8fafc;
}

.preview-student-sno {
  font-size: 0.82rem;
  color: #94a3b8;
}

.preview-stats-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #94a3b8;
}

/* 2) Shop Tabs */
.shop-tabs {
  display: flex;
  gap: 0.35rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.5rem;
  overflow-x: hidden;
  width: 100%;
}

.shop-tab {
  flex: 1 1 0px;
  min-width: 0;
  padding: 0.5rem 0.2rem;
  background: rgba(30, 41, 59, 0.5);
  border: 1px solid transparent;
  border-radius: 8px;
  color: #94a3b8;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  text-align: center;
  transition: all 0.2s ease;
  overflow: hidden;
  text-overflow: ellipsis;
}

.shop-tab:hover {
  background: rgba(30, 41, 59, 0.9);
  color: #f8fafc;
}

.shop-tab.active {
  background: rgba(99, 102, 241, 0.2);
  border-color: #6366f1;
  color: #818cf8;
}

/* 3) Items Grid */
.shop-items-container {
  overflow-y: auto;
  max-height: 460px;
  padding-right: 0.3rem;
}

.shop-items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 0.85rem;
}

.shop-item-card {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 0.75rem;
  transition: all 0.2s ease;
}

.shop-item-card:hover {
  transform: translateY(-2px);
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.3);
}

.shop-item-card.equipped {
  border-color: #10b981;
  background: rgba(16, 185, 129, 0.08);
}

.item-icon-area {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1.25rem 0.5rem;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 10px;
  min-height: 95px;
  overflow: visible;
  position: relative;
}

.item-emoji {
  font-size: 2.4rem;
}

.item-frame-box {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  flex-shrink: 0;
  position: relative;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.shop-item-card:hover .item-frame-box {
  transform: scale(1.12);
}

.frame-preview-emoji {
  font-size: 2.2rem;
  line-height: 1;
  user-select: none;
}

.item-theme-preview {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.item-banner-preview {
  width: 100%;
  height: 64px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 0 12px rgba(0, 0, 0, 0.6);
  transition: transform 0.2s ease;
}

.shop-item-card:hover .item-banner-preview {
  transform: scale(1.03);
}

.banner-preview-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem 0.65rem;
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  backdrop-filter: blur(6px);
}

.banner-preview-badge {
  font-size: 0.78rem;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: -0.2px;
}

.item-cursor-preview {
  width: 100%;
  height: 64px;
  border-radius: 8px;
  background: radial-gradient(circle at 50% 50%, rgba(244, 114, 182, 0.15) 0%, rgba(15, 23, 42, 0.6) 100%);
  border: 1px dashed rgba(244, 114, 182, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.2rem;
  transition: all 0.25s ease;
}

.shop-item-card:hover .item-cursor-preview {
  border-color: #f472b6;
  background: radial-gradient(circle at 50% 50%, rgba(244, 114, 182, 0.28) 0%, rgba(15, 23, 42, 0.8) 100%);
  transform: scale(1.03);
}

.cursor-preview-icon {
  font-size: 1.6rem;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.6));
}

.cursor-test-hint {
  font-size: 0.68rem;
  font-weight: 700;
  color: #f472b6;
  background: rgba(0, 0, 0, 0.4);
  padding: 0.08rem 0.45rem;
  border-radius: 999px;
  border: 1px solid rgba(244, 114, 182, 0.3);
}

.item-info-area {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.item-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-name {
  margin: 0;
  font-size: 0.92rem;
  font-weight: 700;
  color: #f8fafc;
}

.price-tag {
  font-size: 0.78rem;
  font-weight: 700;
  color: #fbbf24;
}

.price-tag.free {
  color: #10b981;
}

.item-desc {
  margin: 0;
  font-size: 0.78rem;
  color: #94a3b8;
  line-height: 1.4;
  min-height: 2.2rem;
}

.btn-equipped {
  background: rgba(16, 185, 129, 0.2);
  border: 1px solid #10b981;
  color: #6ee7b7;
  width: 100%;
  cursor: default;
}

/* 4) Ranking Tab */
.ranking-container {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.ranking-header-desc {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.82rem;
  color: #94a3b8;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 440px;
  overflow-y: auto;
}

.ranking-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  transition: all 0.2s ease;
}

.ranking-row.my-ranking {
  border-color: #fbbf24;
  background: rgba(251, 191, 36, 0.1);
}

.ranking-rank {
  width: 70px;
  font-weight: 700;
  font-size: 0.9rem;
}

.rank-medal {
  font-weight: 800;
}

.rank-medal.gold { color: #fbbf24; }
.rank-medal.silver { color: #cbd5e1; }
.rank-medal.bronze { color: #d97706; }
.rank-num { color: #94a3b8; }

.ranking-student {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.rank-avatar {
  width: 38px;
  height: 38px;
  font-size: 1.3rem;
  background: rgba(0, 0, 0, 0.3);
}

.rank-name {
  color: #f8fafc;
  font-size: 0.92rem;
}

.rank-title-badge {
  font-size: 0.72rem;
  background: rgba(99, 102, 241, 0.15);
  color: #a5b4fc;
  padding: 0.1rem 0.4rem;
  border-radius: 10px;
}

.rank-sno {
  font-size: 0.78rem;
  color: #94a3b8;
}

.ranking-points {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.rank-point-label {
  font-size: 0.78rem;
  color: #94a3b8;
}

.rank-point-val {
  font-weight: 800;
  color: #fbbf24;
  font-size: 0.92rem;
}

/* ========================================================
   Frame Aura Specific Glow & Pulse Effects in Modal
   ======================================================== */
:deep(.frame-none),
.frame-none {
  border: 2px dashed rgba(255, 255, 255, 0.25) !important;
  background: rgba(255, 255, 255, 0.03) !important;
}

:deep(.frame-cyan),
.frame-cyan {
  border: 3px solid #38bdf8 !important;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.45) 0%, rgba(56, 189, 248, 0.1) 60%, transparent 80%) !important;
  box-shadow: 0 0 15px #38bdf8, 0 0 30px rgba(56, 189, 248, 0.8), 0 0 50px rgba(56, 189, 248, 0.4), inset 0 0 10px #38bdf8 !important;
  animation: pulse-frame-cyan 1.4s infinite alternate ease-in-out !important;
}

:deep(.frame-purple),
.frame-purple {
  border: 3px solid #c084fc !important;
  background: radial-gradient(circle, rgba(192, 132, 252, 0.5) 0%, rgba(168, 85, 247, 0.15) 60%, transparent 80%) !important;
  box-shadow: 0 0 18px #c084fc, 0 0 35px rgba(168, 85, 247, 0.9), 0 0 55px rgba(232, 121, 249, 0.5), inset 0 0 12px #e879f9 !important;
  animation: pulse-frame-purple 1.6s infinite alternate ease-in-out !important;
}

:deep(.frame-gold),
.frame-gold {
  border: 3px solid #fbbf24 !important;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.55) 0%, rgba(245, 158, 11, 0.2) 60%, transparent 80%) !important;
  box-shadow: 0 0 20px #fbbf24, 0 0 40px rgba(245, 158, 11, 0.95), 0 0 60px rgba(254, 240, 138, 0.6), inset 0 0 14px #fef08a !important;
  animation: pulse-frame-gold 1.2s infinite alternate ease-in-out !important;
}
</style>
