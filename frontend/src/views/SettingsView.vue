<template>
  <div class="settings-page">
    <div style="margin-bottom: 1.5rem;">
      <h2 style="font-size: 1.3rem; font-weight: 800; color: #f8fafc; margin-bottom: 0.3rem;">⚙️ 시스템 환경 및 데이터베이스 관리</h2>
      <p style="font-size: 0.88rem; color: var(--text-muted); margin: 0;">클라우드 DB 백업, 좌석 배치 규칙, 과제 출처 사이트를 관리합니다.</p>
    </div>

    <!-- 1. 클라우드 DB 백업 & 로컬 동기화 카드 (최상단 강조) -->
    <div class="card mb-4" style="border: 1px solid rgba(59, 130, 246, 0.4); background: rgba(30, 41, 59, 0.35);">
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
        원격 <strong>Oracle Cloud MySQL (150.230.206.18)</strong>의 최신 데이터(학생, 성적, 문제, 제출 이력 등 8개 테이블)를 현재 로컬 DB(localhost:3306)로 즉시 덮어쓰기 백업 및 동기화합니다.
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
      <div v-if="syncResult" class="p-3 rounded border" style="background: rgba(16, 185, 129, 0.08); border-color: rgba(16, 185, 129, 0.3);">
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
    <div class="content-split" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(380px, 1fr)); gap: 1.25rem; align-items: start;">
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
            <small style="color: var(--text-muted); font-size: 0.78rem;">교실의 앞뒤 줄 수 (기본: 6줄)</small>
          </div>
          <div class="form-group mb-3">
            <label class="form-label" style="font-weight: 600;">분단 형태 / 열 구성 패턴</label>
            <input v-model="gridConfig.colsPattern" type="text" class="form-input form-input-sm" placeholder="예: 2,3 (총 5열)" required>
            <div class="col-presets-row mt-2" style="display:flex; gap:0.4rem; align-items:center; flex-wrap:wrap;">
              <span class="preset-label" style="font-size:0.8rem; color:#94a3b8;">빠른 프리셋:</span>
              <button type="button" class="btn btn-sm btn-outline" @click="gridConfig.colsPattern = '2,3'">2분단 (2:3)</button>
              <button type="button" class="btn btn-sm btn-outline" @click="gridConfig.colsPattern = '3,3'">2분단 (3:3)</button>
              <button type="button" class="btn btn-sm btn-outline" @click="gridConfig.colsPattern = '2,2,2'">3분단 (2:2:2)</button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/utils/api'

const router = useRouter()
const authStore = useAuthStore()

// 클라우드 DB 백업 관련 상태
const isSyncing = ref(false)
const showDbConfig = ref(false)
const syncResult = ref<any>(null)
const dbConfig = ref({
  remoteHost: '150.230.206.18',
  remotePort: 3306,
  remoteDb: 'ssafy_db',
  remoteUser: 'quietjun',
  remotePassword: ''
})

// 플랫폼 및 그리드 설정 상태
const platforms = ref<any[]>([])
const newPlatform = ref({ name: '', url: '' })
const gridConfig = ref({ rows: 6, colsPattern: '2,3' })

onMounted(async () => {
  if (!authStore.isAdmin) {
    router.push('/assignment')
    return
  }
  await Promise.all([
    loadPlatforms(),
    loadGridConfig()
  ])
})

async function handleSyncCloudToLocal() {
  if (!confirm('⚠️ 클라우드 DB(150.230.206.18)의 최신 데이터로 로컬 DB를 동기화(덮어쓰기)하시겠습니까?')) {
    return
  }

  isSyncing.value = true
  syncResult.value = null

  try {
    const payload: any = {
      remoteHost: dbConfig.value.remoteHost,
      remotePort: dbConfig.value.remotePort,
      remoteDb: dbConfig.value.remoteDb,
      remoteUser: dbConfig.value.remoteUser
    }
    if (dbConfig.value.remotePassword) {
      payload.remotePassword = dbConfig.value.remotePassword
    }

    const { data } = await api.post('/api/backup/cloud-to-local', payload)
    if (data.success) {
      syncResult.value = data
      alert('🎉 클라우드 DB 데이터가 로컬 DB로 성공적으로 백업 및 동기화되었습니다!')
      await loadPlatforms()
      await loadGridConfig()
    } else {
      alert('동기화 실패: ' + (data.message || '오류 발생'))
    }
  } catch (e: any) {
    alert('동기화 실패: ' + (e.response?.data?.message || '네트워크 통신 오류'))
  } finally {
    isSyncing.value = false
  }
}

function handleDownloadSqlDump() {
  let url = `/api/backup/export-sql?remoteHost=${encodeURIComponent(dbConfig.value.remoteHost)}&remotePort=${dbConfig.value.remotePort}&remoteDb=${encodeURIComponent(dbConfig.value.remoteDb)}&remoteUser=${encodeURIComponent(dbConfig.value.remoteUser)}`
  if (dbConfig.value.remotePassword) {
    url += `&remotePassword=${encodeURIComponent(dbConfig.value.remotePassword)}`
  }
  window.open(url, '_blank')
}

async function loadPlatforms() {
  try {
    const { data } = await api.get<any[]>('/api/platforms')
    platforms.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('Failed to load platforms:', e)
  }
}

async function loadGridConfig() {
  try {
    const { data } = await api.get<any>('/api/metadata/grid-config')
    gridConfig.value = {
      rows: data.rows || 6,
      colsPattern: data.colsPattern || '2,3'
    }
  } catch (e) {}
}

async function handleAddPlatform() {
  if (!newPlatform.value.name.trim()) return
  try {
    await api.post('/api/platforms', {
      name: newPlatform.value.name.trim(),
      url: newPlatform.value.url.trim()
    })
    alert(`✅ '${newPlatform.value.name}' 사이트가 추가되었습니다.`)
    newPlatform.value = { name: '', url: '' }
    await loadPlatforms()
  } catch (e: any) {
    alert('사이트 추가 실패: ' + (e.response?.data?.message || '다시 시도해 주세요.'))
  }
}

async function handleDeletePlatform(p: any) {
  if (!confirm(`'${p.name}' 출처 사이트를 삭제하시겠습니까?`)) return
  try {
    await api.delete(`/api/platforms/${p.id}`)
    alert(`🗑️ '${p.name}' 사이트가 삭제되었습니다.`)
    await loadPlatforms()
  } catch (e) {
    alert('사이트 삭제 실패')
  }
}

async function handleSaveGridConfig() {
  try {
    await api.post('/api/metadata/grid-config', {
      rows: gridConfig.value.rows,
      colsPattern: gridConfig.value.colsPattern
    })
    alert('✅ 좌석 그리드 및 분단 설정이 저장되었습니다.')
  } catch (e: any) {
    alert('설정 저장 중 오류가 발생했습니다.')
  }
}
</script>
