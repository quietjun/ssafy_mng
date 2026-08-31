<template>
  <div class="settings-page">
    <div style="margin-bottom: 2rem;">
      <h2 style="font-size: 1.35rem; font-weight: 800; color: #f8fafc; margin-bottom: 0.35rem;">⚙️ 시스템 환경 및 데이터베이스 관리</h2>
      <p style="font-size: 0.88rem; color: var(--text-muted); margin: 0;">클라우드 DB 백업, 좌석 배치 규칙, 과제 출처 사이트, 튜터 추출 스크립트를 관리합니다.</p>
    </div>

    <!-- 1. 클라우드 DB 백업 & 로컬 동기화 카드 (최상단 강조) -->
    <div class="card mb-4" style="border: 1px solid rgba(59, 130, 246, 0.4); background: rgba(30, 41, 59, 0.35); margin-bottom: 2.25rem;">
      <div class="card-header" style="flex-wrap: wrap; gap: 0.8rem;">
        <div style="display:flex; align-items:center; gap:0.6rem;">
          <h3 style="font-size: 1.15rem; font-weight: 700; color: #60a5fa; margin: 0;">☁️ 클라우드 DB 로컬 백업 및 동기화</h3>
          <span class="badge info">Oracle Cloud ➔ Localhost</span>
        </div>
        <div style="display:flex; gap:0.5rem; flex-wrap:wrap;">
          <button 
            type="button" 
            class="btn btn-sm btn-outline" 
            @click="showDbConfig = !showDbConfig"
          >
            🔧 {{ showDbConfig ? '접속 설정 닫기' : '원격 DB 접속 설정' }}
          </button>
          <button 
            type="button" 
            class="btn btn-sm btn-outline" 
            @click="handleDownloadSqlDump"
            title="원격 DB의 .sql 백업 파일을 브라우저로 다운로드합니다"
          >
            📥 .sql 덤프 다운로드
          </button>
          <button 
            type="button" 
            class="btn btn-sm btn-primary" 
            @click="handleSyncCloudToLocal"
            :disabled="isSyncing"
          >
            {{ isSyncing ? '동기화 진행 중...' : '🚀 클라우드 DB ➔ 로컬 즉시 동기화' }}
          </button>
        </div>
      </div>

      <p style="font-size:0.86rem; color:var(--text-muted); margin-bottom: 0.8rem;">
        원격 <strong>Oracle Cloud MySQL (150.230.206.18)</strong>의 최신 데이터(학생, 성적, 문제, 제출 이력, 스크립트 등 전체 테이블)를 현재 로컬 DB(localhost:3306)로 즉시 덮어쓰기 백업 및 동기화합니다.
      </p>

      <!-- 고급 원격 접속 설정 (토글) -->
      <div v-if="showDbConfig" class="p-3 mb-3 rounded border" style="background: rgba(15, 23, 42, 0.6); border-color: var(--border-color);">
        <h5 style="font-size: 0.9rem; font-weight: 700; color: #f8fafc; margin-bottom: 0.6rem;">원격 Oracle Cloud DB 접속 정보</h5>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 0.6rem;">
          <div class="form-group">
            <label class="form-label" style="font-size:0.78rem;">호스트 IP</label>
            <input v-model="dbConfig.remoteHost" type="text" class="form-input form-input-sm" />
          </div>
          <div class="form-group">
            <label class="form-label" style="font-size:0.78rem;">포트</label>
            <input v-model.number="dbConfig.remotePort" type="number" class="form-input form-input-sm" />
          </div>
          <div class="form-group">
            <label class="form-label" style="font-size:0.78rem;">데이터베이스명</label>
            <input v-model="dbConfig.remoteDb" type="text" class="form-input form-input-sm" />
          </div>
          <div class="form-group">
            <label class="form-label" style="font-size:0.78rem;">계정명</label>
            <input v-model="dbConfig.remoteUser" type="text" class="form-input form-input-sm" />
          </div>
          <div class="form-group">
            <label class="form-label" style="font-size:0.78rem;">비밀번호</label>
            <input v-model="dbConfig.remotePassword" type="password" class="form-input form-input-sm" />
          </div>
        </div>
      </div>

      <!-- 동기화 결과 요약 리포트 (성공 시 노출) -->
      <div v-if="syncResult" class="p-3 rounded border" style="background: rgba(160, 185, 129, 0.08); border-color: rgba(16, 185, 129, 0.3);">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem;">
          <strong style="color:#34d399; font-size:0.92rem;">
            ✅ 동기화 완료 (소요 시간: {{ (syncResult.durationMs / 1000).toFixed(2) }}초)
          </strong>
          <button class="btn btn-sm btn-outline" style="padding:0.1rem 0.4rem; font-size:0.75rem;" @click="syncResult = null">&times; 닫기</button>
        </div>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 0.5rem; font-size: 0.8rem;">
          <div v-for="(cnt, tbl) in syncResult.tableCounts" :key="tbl" style="padding: 0.4rem 0.6rem; background: rgba(0,0,0,0.3); border-radius: 6px; display:flex; justify-content:space-between;">
            <span style="color:#94a3b8;">{{ tbl }}</span>
            <strong style="color:#f8fafc;">{{ cnt }}건</strong>
          </div>
        </div>
      </div>
    </div>

    <!-- 2열 그리드: 좌석 설정 & 플랫폼 관리 -->
    <div class="content-split" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(380px, 1fr)); gap: 1.75rem; align-items: start; margin-bottom: 2.25rem;">
      <!-- 2. 좌석 그리드 & 분단 설정 -->
      <div class="card">
        <div class="card-header">
          <div style="display:flex; align-items:center; gap:0.5rem;">
            <h3 style="font-size: 1.1rem; font-weight: 700; color: #f8fafc;">🪑 좌석 그리드 & 분단 설정</h3>
          </div>
        </div>
        <p style="font-size:0.85rem; color:var(--text-muted); margin-bottom:1rem;">
          발표자 추첨 화면의 좌석 배치 행/열 크기 및 분단 형태를 설정합니다.
        </p>
        <form @submit.prevent="handleSaveGridConfig">
          <div class="form-group mb-3">
            <label class="form-label" style="font-weight: 600;">행 수 (Rows)</label>
            <input v-model.number="gridConfig.rows" type="number" class="form-input form-input-sm" min="1" max="15" required>
            <small style="color: var(--text-muted); font-size: 0.78rem;">교실의 앞뒤 줄 수 (기본: 5줄)</small>
          </div>
          <div class="form-group mb-3">
            <label class="form-label" style="font-weight: 600;">분단 형태 / 열 구성 패턴</label>
            <input v-model="gridConfig.colsPattern" type="text" class="form-input form-input-sm" placeholder="예: 2,2,2 (총 6열)" required>
            <div class="col-presets-row mt-2" style="display:flex; gap:0.4rem; align-items:center; flex-wrap:wrap;">
              <span class="preset-label" style="font-size:0.8rem; color:#94a3b8;">빠른 프리셋:</span>
              <button type="button" class="btn btn-sm btn-outline" @click="gridConfig.colsPattern = '2,2,2'">3분단 (2:2:2)</button>
              <button type="button" class="btn btn-sm btn-outline" @click="gridConfig.colsPattern = '2,3'">2분단 (2:3)</button>
              <button type="button" class="btn btn-sm btn-outline" @click="gridConfig.colsPattern = '3,3'">2분단 (3:3)</button>
            </div>
          </div>
          <button type="submit" class="btn btn-primary w-100 mt-2">💾 좌석표 설정 저장</button>
        </form>
      </div>

      <!-- 3. 문제 출처 사이트 관리 -->
      <div class="card">
        <div class="card-header">
          <div style="display:flex; align-items:center; gap:0.5rem;">
            <h3 style="font-size: 1.1rem; font-weight: 700; color: #f8fafc;">🌐 문제 출처 사이트 관리</h3>
            <span class="badge">{{ platforms.length }}개</span>
          </div>
        </div>
        <p style="font-size:0.85rem; color:var(--text-muted); margin-bottom:0.75rem;">
          과제 문제 등록 시 드롭다운에서 선택할 알고리즘 플랫폼 목록입니다.
        </p>

        <!-- 신규 사이트 추가 폼 -->
        <form @submit.prevent="handleAddPlatform" class="mb-3" style="display:flex; gap:0.4rem; flex-wrap:wrap;">
          <input 
            v-model="newPlatform.name" 
            type="text" 
            class="form-input form-input-sm" 
            placeholder="사이트명 (예: LeetCode)" 
            style="flex:1; min-width:110px;" 
            required
          />
          <input 
            v-model="newPlatform.url" 
            type="url" 
            class="form-input form-input-sm" 
            placeholder="URL (예: https://leetcode.com)" 
            style="flex:2; min-width:160px;"
          />
          <button type="submit" class="btn btn-sm btn-success">+ 추가</button>
        </form>

        <!-- 사이트 목록 테이블 -->
        <div class="table-scroll-container" style="max-height: 280px;">
          <table class="data-table">
            <thead>
              <tr>
                <th style="width: 30%;">사이트명</th>
                <th style="width: 50%;">기본 URL</th>
                <th style="width: 20%; text-align:center;">관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="platforms.length === 0">
                <td colspan="3" style="text-align:center; padding: 2rem;" class="empty-state">
                  등록된 사이트가 없습니다.
                </td>
              </tr>
              <tr v-for="p in platforms" :key="p.id">
                <td style="font-weight: 700; color: #f8fafc;">{{ p.name }}</td>
                <td style="font-size:0.82rem; color:var(--text-muted); word-break:break-all;">
                  <a v-if="p.url" :href="p.url" target="_blank" style="color:var(--primary); text-decoration:none;">{{ p.url }}</a>
                  <span v-else>-</span>
                </td>
                <td style="text-align:center;">
                  <button class="btn btn-sm btn-danger-outline" style="padding:0.2rem 0.5rem; font-size:0.75rem;" @click="handleDeletePlatform(p)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 4. Tutor 추출 유틸리티 스크립트 관리 (하단 풀 너비 카드) -->
    <div class="card" style="border: 1px solid rgba(168, 85, 247, 0.4); background: rgba(30, 41, 59, 0.35); margin-bottom: 2.5rem;">
      <div class="card-header" style="flex-wrap: wrap; gap: 0.8rem;">
        <div style="display:flex; align-items:center; gap:0.6rem;">
          <h3 style="font-size: 1.15rem; font-weight: 700; color: #c084fc; margin: 0;">🛠️ Tutor 데이터 추출 유틸리티 스크립트</h3>
          <span class="badge" style="background: rgba(168, 85, 247, 0.2); color: #e9d5ff; border: 1px solid rgba(168, 85, 247, 0.4);">
            {{ scripts.length }}개 스크립트
          </span>
        </div>
        <button 
          type="button" 
          class="btn btn-sm btn-primary" 
          @click="openScriptModal()"
          style="background: #9333ea; border-color: #9333ea;"
        >
          + 새 스크립트 추가
        </button>
      </div>

      <p style="font-size:0.86rem; color:var(--text-muted); margin-bottom: 1.25rem;">
        SSAFY Tutor 사이트의 개발자 도구(F12 ➔ Console)에서 실행할 스크립트 목록입니다. 좌측 목록에서 스크립트를 선택한 뒤 <strong>[📋 자바스크립트 코드 복사]</strong> 버튼을 눌러 바로 사용하세요.
      </p>

      <!-- 스크립트 로딩/빈 상태 -->
      <div v-if="isLoadingScripts" style="text-align:center; padding:2.5rem; color:var(--text-muted);">
        스크립트 목록을 불러오는 중...
      </div>
      <div v-else-if="scripts.length === 0" class="empty-state" style="text-align:center; padding:2.5rem;">
        등록된 스크립트가 없습니다. 우측 상단의 '+ 새 스크립트 추가' 버튼을 눌러 등록해 보세요.
      </div>
      
      <!-- 목록화 & 선택 상세 뷰어 (Master - Detail 레이아웃) -->
      <div v-else class="script-master-detail">
        <!-- 좌측: 스크립트 목록 (선택기) -->
        <div class="script-list-pane">
          <div 
            v-for="s in scripts" 
            :key="s.id"
            class="script-list-item"
            :class="{ active: selectedScript?.id === s.id }"
            @click="selectedScriptId = s.id ?? null"
          >
            <div class="script-item-header">
              <span class="script-order-badge">#{{ s.orderIndex || 0 }}</span>
              <strong class="script-item-title">{{ s.title }}</strong>
            </div>
            <p v-if="s.description" class="script-item-desc">{{ s.description }}</p>
          </div>
        </div>

        <!-- 우측: 선택된 스크립트 상세 및 코드 뷰어 -->
        <div v-if="selectedScript" class="script-detail-pane">
          <div class="script-detail-header">
            <div style="flex: 1; min-width: 200px;">
              <div style="display:flex; align-items:center; gap:0.5rem; margin-bottom:0.3rem;">
                <span class="badge" style="background: rgba(168, 85, 247, 0.25); color: #e9d5ff; font-size:0.75rem; border: 1px solid rgba(168, 85, 247, 0.4);">
                  순서: {{ selectedScript.orderIndex || 0 }}
                </span>
                <h4 style="font-size: 1.05rem; font-weight: 700; color: #f8fafc; margin: 0;">
                  {{ selectedScript.title }}
                </h4>
              </div>
              <p v-if="selectedScript.description" style="font-size: 0.84rem; color: #94a3b8; margin: 0;">
                {{ selectedScript.description }}
              </p>
            </div>
            
            <!-- 조작 액션 버튼 -->
            <div style="display: flex; gap: 0.4rem; flex-shrink: 0; align-items: center; flex-wrap: wrap;">
              <button 
                type="button" 
                class="btn btn-sm" 
                :class="copiedScriptId === selectedScript.id ? 'btn-success' : 'btn-primary'"
                style="font-size: 0.8rem; padding: 0.35rem 0.75rem; font-weight: 700; background: copiedScriptId === selectedScript.id ? '' : '#9333ea'; border-color: #9333ea;"
                @click="handleCopyScript(selectedScript)"
              >
                {{ copiedScriptId === selectedScript.id ? '✅ 클립보드 복사됨!' : '📋 코드 복사' }}
              </button>
              <button 
                type="button" 
                class="btn btn-sm btn-outline" 
                style="font-size: 0.78rem; padding: 0.35rem 0.55rem;"
                @click="openScriptModal(selectedScript)"
                title="스크립트 수정"
              >
                ✏️ 수정
              </button>
              <button 
                type="button" 
                class="btn btn-sm btn-danger-outline" 
                style="font-size: 0.78rem; padding: 0.35rem 0.55rem;"
                @click="handleDeleteScript(selectedScript)"
                title="스크립트 삭제"
              >
                🗑️ 삭제
              </button>
            </div>
          </div>

          <!-- 코드 박스 헤더 & 코드 블록 -->
          <div class="script-code-wrapper">
            <div class="code-top-bar">
              <span class="lang-tag">JavaScript Snippet</span>
              <span class="char-count">{{ selectedScript.scriptContent.length }}자</span>
            </div>
            <pre class="script-code-block"><code>{{ selectedScript.scriptContent }}</code></pre>
          </div>
        </div>
      </div>
    </div>

    <!-- 스크립트 추가/수정 모달 -->
    <div v-if="isScriptModalOpen" class="modal-overlay" @click.self="isScriptModalOpen = false">
      <div class="modal" style="max-width: 680px; width: 95%;">
        <div class="modal-header">
          <h3>{{ editingScript.id ? '✏️ 스크립트 수정' : '➕ 새 스크립트 등록' }}</h3>
          <button class="modal-close" @click="isScriptModalOpen = false">&times;</button>
        </div>
        <form @submit.prevent="handleSaveScript" class="modal-body">
          <div style="display: grid; grid-template-columns: 3fr 1fr; gap: 0.75rem;">
            <div class="form-group mb-3">
              <label class="form-label">스크립트 제목</label>
              <input v-model="editingScript.title" type="text" class="form-input" placeholder="예: Tutor 학생 정보 가져오기" required />
            </div>
            <div class="form-group mb-3">
              <label class="form-label">정렬 순서</label>
              <input v-model.number="editingScript.orderIndex" type="number" class="form-input" min="0" />
            </div>
          </div>

          <div class="form-group mb-3">
            <label class="form-label">설명 / 사용 목적</label>
            <input v-model="editingScript.description" type="text" class="form-input" placeholder="예: 학생 목록 그리드에서 학번/이름 추출" />
          </div>

          <div class="form-group mb-3">
            <label class="form-label">자바스크립트 코드 (JavaScript)</label>
            <textarea 
              v-model="editingScript.scriptContent" 
              class="form-textarea" 
              rows="9" 
              style="font-family: 'JetBrains Mono', Consolas, monospace; font-size: 0.82rem; background: #090d16; color: #38bdf8;"
              placeholder="// 여기에 실행할 자바스크립트 코드를 입력하세요" 
              required
            ></textarea>
          </div>

          <div class="modal-footer" style="display:flex; justify-content:flex-end; gap:0.5rem; margin-top:1rem;">
            <button type="button" class="btn btn-outline" @click="isScriptModalOpen = false">취소</button>
            <button type="submit" class="btn btn-primary" :disabled="isSavingScript">
              {{ isSavingScript ? '저장 중...' : '💾 스크립트 저장' }}
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '@/utils/api'

// ----------------------------------------------------
// 1. Cloud DB Backup & Sync State
// ----------------------------------------------------
const showDbConfig = ref(false)
const isSyncing = ref(false)
const syncResult = ref<any>(null)

const dbConfig = ref({
  remoteHost: '150.230.206.18',
  remotePort: 3306,
  remoteDb: 'ssafy_db',
  remoteUser: 'quietjun',
  remotePassword: 'dmstj@0205M'
})

async function handleSyncCloudToLocal() {
  if (!confirm('원격 Oracle Cloud DB의 데이터를 로컬 DB로 백업 및 덮어쓰기 동기화하시겠습니까?')) {
    return
  }
  isSyncing.value = true
  syncResult.value = null
  try {
    const res = await api.post('/api/backup/cloud-to-local', dbConfig.value)
    syncResult.value = res.data
    alert('클라우드 DB 동기화가 성공적으로 완료되었습니다!')
  } catch (err: any) {
    alert('동기화 실패: ' + (err.response?.data?.message || err.message))
  } finally {
    isSyncing.value = false
  }
}

async function handleDownloadSqlDump() {
  try {
    const res = await api.post('/api/backup/export-sql', dbConfig.value, {
      responseType: 'blob'
    })
    const blob = new Blob([res.data], { type: 'application/sql' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `ssafy_db_oracle_backup_${new Date().toISOString().slice(0,10)}.sql`
    link.click()
    URL.revokeObjectURL(link.href)
  } catch (err: any) {
    alert('SQL 덤프 다운로드 실패: ' + (err.response?.data?.message || err.message))
  }
}

// ----------------------------------------------------
// 2. Seating Grid Configuration
// ----------------------------------------------------
const gridConfig = ref({
  rows: 5,
  colsPattern: '2,2,2'
})

async function fetchGridConfig() {
  try {
    const res = await api.get('/api/metadata/grid-config')
    if (res.data) {
      if (res.data.rows) gridConfig.value.rows = Number(res.data.rows)
      if (res.data.colsPattern) gridConfig.value.colsPattern = res.data.colsPattern
    }
  } catch (err) {
    console.error('Failed to load seating grid config via grid-config, trying metadata fallback', err)
    try {
      const res = await api.get('/api/metadata')
      const list = res.data
      const rowsMeta = list.find((m: any) => m.keyword === 'classrow' || m.keyword === 'SEATING_ROWS')
      const colsMeta = list.find((m: any) => m.keyword === 'classcol_pattern' || m.keyword === 'SEATING_COLS_PATTERN')
      if (rowsMeta) gridConfig.value.rows = parseInt(rowsMeta.value, 10) || 5
      if (colsMeta) gridConfig.value.colsPattern = colsMeta.value || '2,2,2'
    } catch (e) {
      console.error('Failed to load metadata fallback', e)
    }
  }
}

async function handleSaveGridConfig() {
  try {
    await api.post('/api/metadata/grid-config', {
      rows: gridConfig.value.rows,
      colsPattern: gridConfig.value.colsPattern
    })
    alert('좌석표 설정이 저장되었습니다.')
  } catch (err: any) {
    alert('좌석표 설정 저장 실패: ' + (err.response?.data?.message || err.message))
  }
}

// ----------------------------------------------------
// 3. Problem Platform Management
// ----------------------------------------------------
interface PlatformItem {
  id?: number
  name: string
  url?: string
}

const platforms = ref<PlatformItem[]>([])
const newPlatform = ref<PlatformItem>({ name: '', url: '' })

async function fetchPlatforms() {
  try {
    const res = await api.get('/api/platforms')
    platforms.value = res.data
  } catch (err) {
    console.error('Failed to load platforms', err)
  }
}

async function handleAddPlatform() {
  if (!newPlatform.value.name.trim()) return
  try {
    await api.post('/api/platforms', newPlatform.value)
    newPlatform.value = { name: '', url: '' }
    await fetchPlatforms()
  } catch (err: any) {
    alert('플랫폼 추가 실패: ' + (err.response?.data?.message || err.message))
  }
}

async function handleDeletePlatform(p: PlatformItem) {
  if (!p.id) return
  if (!confirm(`'${p.name}' 플랫폼을 삭제하시겠습니까?`)) return
  try {
    await api.delete(`/api/platforms/${p.id}`)
    await fetchPlatforms()
  } catch (err: any) {
    alert('플랫폼 삭제 실패: ' + (err.response?.data?.message || err.message))
  }
}

// ----------------------------------------------------
// 4. Tutor Admin Scripts Management
// ----------------------------------------------------
interface AdminScriptItem {
  id?: number
  title: string
  description?: string
  scriptContent: string
  orderIndex: number
}

const scripts = ref<AdminScriptItem[]>([])
const isLoadingScripts = ref(false)
const isScriptModalOpen = ref(false)
const isSavingScript = ref(false)
const copiedScriptId = ref<number | null>(null)
const selectedScriptId = ref<number | null>(null)

const selectedScript = computed(() => {
  if (!scripts.value || scripts.value.length === 0) return null
  if (selectedScriptId.value == null) {
    return scripts.value[0]
  }
  return scripts.value.find(s => s.id === selectedScriptId.value) || scripts.value[0]
})

const editingScript = ref<AdminScriptItem>({
  title: '',
  description: '',
  scriptContent: '',
  orderIndex: 0
})

async function fetchScripts() {
  isLoadingScripts.value = true
  try {
    const res = await api.get('/api/admin/scripts')
    scripts.value = res.data
    if (scripts.value.length > 0) {
      if (selectedScriptId.value == null || !scripts.value.some(s => s.id === selectedScriptId.value)) {
        selectedScriptId.value = scripts.value[0].id ?? null
      }
    } else {
      selectedScriptId.value = null
    }
  } catch (err: any) {
    console.error('Failed to load admin scripts', err)
  } finally {
    isLoadingScripts.value = false
  }
}

function openScriptModal(item?: AdminScriptItem) {
  if (item) {
    editingScript.value = { ...item }
  } else {
    editingScript.value = {
      title: '',
      description: '',
      scriptContent: '',
      orderIndex: (scripts.value.length > 0 ? Math.max(...scripts.value.map(s => s.orderIndex || 0)) + 1 : 1)
    }
  }
  isScriptModalOpen.value = true
}

async function handleSaveScript() {
  if (!editingScript.value.title.trim() || !editingScript.value.scriptContent.trim()) {
    alert('제목과 스크립트 내용을 모두 입력해 주세요.')
    return
  }
  isSavingScript.value = true
  try {
    let savedId = editingScript.value.id
    if (editingScript.value.id) {
      const res = await api.put(`/api/admin/scripts/${editingScript.value.id}`, editingScript.value)
      savedId = res.data?.id || savedId
    } else {
      const res = await api.post('/api/admin/scripts', editingScript.value)
      savedId = res.data?.id
    }
    isScriptModalOpen.value = false
    await fetchScripts()
    if (savedId != null) {
      selectedScriptId.value = savedId
    }
  } catch (err: any) {
    alert('스크립트 저장 실패: ' + (err.response?.data?.message || err.message))
  } finally {
    isSavingScript.value = false
  }
}

async function handleDeleteScript(item: AdminScriptItem) {
  if (!item.id) return
  if (!confirm(`'${item.title}' 스크립트를 삭제하시겠습니까?`)) return
  try {
    await api.delete(`/api/admin/scripts/${item.id}`)
    if (selectedScriptId.value === item.id) {
      selectedScriptId.value = null
    }
    await fetchScripts()
  } catch (err: any) {
    alert('스크립트 삭제 실패: ' + (err.response?.data?.message || err.message))
  }
}

async function handleCopyScript(item: AdminScriptItem) {
  try {
    await navigator.clipboard.writeText(item.scriptContent)
    if (item.id != null) {
      copiedScriptId.value = item.id
      setTimeout(() => {
        if (copiedScriptId.value === item.id) {
          copiedScriptId.value = null
        }
      }, 2000)
    }
  } catch (err) {
    alert('클립보드 복사 실패: ' + err)
  }
}

// ----------------------------------------------------
// Lifecycle Hooks
// ----------------------------------------------------
onMounted(() => {
  fetchGridConfig()
  fetchPlatforms()
  fetchScripts()
})
</script>

<style scoped>
.settings-page {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 3rem;
}

.script-master-detail {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 1.25rem;
  align-items: stretch;
  min-height: 380px;
}

@media (max-width: 820px) {
  .script-master-detail {
    grid-template-columns: 1fr;
  }
}

.script-list-pane {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 480px;
  overflow-y: auto;
  padding-right: 0.35rem;
}

.script-list-pane::-webkit-scrollbar {
  width: 5px;
}
.script-list-pane::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 3px;
}

.script-list-item {
  padding: 0.8rem 0.95rem;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.script-list-item:hover {
  background: rgba(30, 41, 59, 0.85);
  border-color: rgba(168, 85, 247, 0.45);
  transform: translateX(2px);
}

.script-list-item.active {
  background: rgba(147, 51, 234, 0.18);
  border-color: #a855f7;
  box-shadow: 0 0 12px rgba(168, 85, 247, 0.25);
}

.script-item-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.3rem;
}

.script-order-badge {
  font-size: 0.72rem;
  font-weight: 700;
  color: #c084fc;
  background: rgba(168, 85, 247, 0.2);
  border: 1px solid rgba(168, 85, 247, 0.3);
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  flex-shrink: 0;
}

.script-item-title {
  font-size: 0.88rem;
  color: #f8fafc;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.script-item-desc {
  font-size: 0.76rem;
  color: var(--text-muted);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.35;
}

.script-detail-pane {
  background: rgba(15, 23, 42, 0.75);
  border: 1px solid rgba(168, 85, 247, 0.3);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.script-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding-bottom: 0.9rem;
}

.script-code-wrapper {
  background: #090d16;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.code-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.45rem 0.85rem;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.lang-tag {
  font-size: 0.72rem;
  font-weight: 600;
  color: #c084fc;
  font-family: 'JetBrains Mono', Consolas, monospace;
}

.char-count {
  font-size: 0.72rem;
  color: var(--text-muted);
}

.script-code-block {
  padding: 0.85rem 1rem;
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, monospace;
  font-size: 0.82rem;
  color: #38bdf8;
  max-height: 360px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  line-height: 1.5;
}

.script-code-block::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.script-code-block::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}
</style>
