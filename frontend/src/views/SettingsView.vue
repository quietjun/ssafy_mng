<template>
  <div class="settings-page">
    <div class="content-split">
      <!-- Left: Student Account Management (1:1 Ratio, Sortable Columns) -->
      <div class="split-col left-col" style="flex: 1; min-width: 0;">
        <div class="card">
          <div class="card-header">
            <div style="display:flex; align-items:center; gap:0.5rem;">
              <h3>👥 학생 계정 목록</h3>
              <span class="badge">{{ filteredStudents.length }} / {{ students.length }}명</span>
            </div>
            <button class="btn btn-sm btn-primary" @click="openAddStudentModal">+ 신규 학생 등록</button>
          </div>

          <!-- Search Filter -->
          <div class="table-search-box">
            <input 
              v-model="searchQuery" 
              type="text" 
              class="form-input form-input-sm" 
              placeholder="🔍 학생 이름 또는 학번으로 빠른 검색..."
            />
            <button v-if="searchQuery" class="btn btn-sm btn-outline" @click="searchQuery = ''">초기화</button>
          </div>

          <!-- Student Table -->
          <div class="table-scroll-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th style="width: 32%; cursor: pointer; user-select: none;" @click="toggleSort('sno')" title="학번순 정렬">
                    학번 (SNO) <span style="font-size:0.72rem; opacity:0.8; margin-left:0.15rem;">{{ getSortIcon('sno') }}</span>
                  </th>
                  <th style="width: 26%; cursor: pointer; user-select: none;" @click="toggleSort('name')" title="이름순 정렬">
                    이름 <span style="font-size:0.72rem; opacity:0.8; margin-left:0.15rem;">{{ getSortIcon('name') }}</span>
                  </th>
                  <th style="width: 22%; text-align:center; cursor: pointer; user-select: none;" @click="toggleSort('passwordChanged')" title="비밀번호 상태순 정렬">
                    비번 상태 <span style="font-size:0.72rem; opacity:0.8; margin-left:0.15rem;">{{ getSortIcon('passwordChanged') }}</span>
                  </th>
                  <th style="width: 20%; text-align:center;">관리</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="filteredStudents.length === 0">
                  <td colspan="4" style="text-align:center; padding: 2rem;" class="empty-state">
                    {{ searchQuery ? '검색 결과와 일치하는 학생이 없습니다.' : '등록된 학생이 없습니다.' }}
                  </td>
                </tr>
                <tr v-for="s in filteredStudents" :key="s.sno">
                  <td><strong>{{ s.sno }}</strong></td>
                  <td style="font-weight: 700; color: #f8fafc;">{{ s.name }}</td>
                  <td style="text-align:center;">
                    <span :class="['ai-chip', s.passwordChanged ? 'chip-pass' : 'chip-time']" style="padding: 0.15rem 0.45rem; font-size: 0.72rem;">
                      {{ s.passwordChanged ? '변경완료' : '초기비번' }}
                    </span>
                  </td>
                  <td style="text-align:center;">
                    <div style="display:inline-flex; gap:0.4rem; justify-content:center;">
                      <button class="btn btn-sm btn-outline" style="padding:0.25rem 0.55rem; font-size:0.78rem;" @click="openEditStudentModal(s)">수정</button>
                      <button class="btn btn-sm btn-danger-outline" style="padding:0.25rem 0.55rem; font-size:0.78rem;" @click="deleteStudent(s)">삭제</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Right: Ordered Cards (1:1 Ratio) -->
      <div class="split-col right-col" style="flex: 1; min-width: 0;">
        <!-- 1. 학생 텍스트(CSV) 붙여넣기 등록 -->
        <div class="card mb-3">
          <div class="card-header">
            <h3>📋 학생 일괄 등록 (CSV / 텍스트 붙여넣기)</h3>
          </div>
          <p style="font-size:0.85rem; color:var(--text-muted); margin-bottom:0.6rem;">
            엑셀이나 텍스트에서 <strong>학번, 이름</strong> 목록을 복사하여 아래에 붙여넣으세요. (나머지 정보는 기본값으로 자동 설정됩니다)
          </p>
          <form @submit.prevent="handleBulkTextImport">
            <textarea 
              v-model="bulkCsvText" 
              class="form-textarea mb-2" 
              rows="5"
              style="font-family: 'JetBrains Mono', monospace; font-size: 0.85rem;"
              placeholder="예시 (쉼표 또는 탭 구분):
1647021, 강상택
1643716, 강현준
1648035, 고아라"
              required
            ></textarea>
            <button type="submit" class="btn btn-primary w-100" :disabled="isUploadingBulk">
              {{ isUploadingBulk ? '등록 중...' : '📥 학생 일괄 등록 / 갱신' }}
            </button>
          </form>
        </div>

        <!-- 2. 좌석 그리드 & 분단 설정 -->
        <div class="card mb-3">
          <div class="card-header">
            <h3>⚙️ 좌석 그리드 & 분단 설정</h3>
          </div>
          <form @submit.prevent="handleSaveGridConfig">
            <div class="form-group">
              <label class="form-label">행 수 (Rows):</label>
              <input v-model.number="gridConfig.rows" type="number" class="form-input form-input-sm" min="1" max="15" required>
            </div>
            <div class="form-group">
              <label class="form-label">분단 형태 / 열 구성 (예: 2,3 또는 2:2:2):</label>
              <input v-model="gridConfig.colsPattern" type="text" class="form-input form-input-sm" placeholder="예: 2,3 (총 5열)" required>
              <div class="col-presets-row mt-2" style="display:flex; gap:0.3rem; align-items:center; flex-wrap:wrap;">
                <span class="preset-label" style="font-size:0.8rem; color:#94a3b8;">빠른 설정:</span>
                <button type="button" class="btn btn-sm btn-outline" @click="gridConfig.colsPattern = '2,3'">2분단 (2:3)</button>
                <button type="button" class="btn btn-sm btn-outline" @click="gridConfig.colsPattern = '3,3'">2분단 (3:3)</button>
                <button type="button" class="btn btn-sm btn-outline" @click="gridConfig.colsPattern = '2,2,2'">3분단 (2:2:2)</button>
              </div>
            </div>
            <button type="submit" class="btn btn-primary w-100 mt-2">설정 저장 및 좌석표 적용</button>
          </form>
        </div>

        <!-- 3. 문제 출처 사이트 관리 -->
        <div class="card">
          <div class="card-header">
            <h3>🌐 문제 출처 사이트 관리</h3>
            <span class="badge">{{ platforms.length }}개 사이트</span>
          </div>
          <p style="font-size:0.85rem; color:var(--text-muted); margin-bottom:0.75rem;">
            문제 등록 시 선택할 수 있는 알고리즘 사이트(이름, 기본 URL)를 관리합니다.
          </p>
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
            <button type="submit" class="btn btn-sm btn-success">+ 사이트 추가</button>
          </form>

          <div class="table-responsive" style="max-height: 220px; overflow-y: auto;">
            <table class="data-table">
              <thead>
                <tr>
                  <th style="width: 28%;">사이트명</th>
                  <th style="width: 52%;">링크 URL</th>
                  <th style="width: 20%; text-align:center;">삭제</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="platforms.length === 0">
                  <td colspan="3" style="text-align:center;" class="empty-state">등록된 사이트가 없습니다.</td>
                </tr>
                <tr v-for="p in platforms" :key="p.id">
                  <td><strong>{{ p.name }}</strong></td>
                  <td>
                    <a v-if="p.url" :href="p.url" target="_blank" rel="noopener noreferrer" style="color:#60a5fa; text-decoration:none;">
                      {{ p.url }}
                    </a>
                    <span v-else style="color:#64748b;">(URL 없음)</span>
                  </td>
                  <td style="text-align:center;">
                    <button class="btn btn-sm btn-danger-outline" @click="handleDeletePlatform(p)">삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Single Student Edit / Add Modal -->
    <div v-if="isStudentModalOpen" class="modal-overlay" @click.self="isStudentModalOpen = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingStudent.isNew ? '신규 학생 등록' : '학생 정보 수정' }}</h3>
          <button class="modal-close" @click="isStudentModalOpen = false">&times;</button>
        </div>
        <form @submit.prevent="handleSaveStudent" class="modal-body">
          <div class="form-group">
            <label class="form-label">학번 (SNO)</label>
            <input 
              v-model="editingStudent.sno" 
              type="text" 
              class="form-input" 
              :readonly="!editingStudent.isNew" 
              required
            />
          </div>
          <div class="form-group">
            <label class="form-label">이름</label>
            <input v-model="editingStudent.name" type="text" class="form-input" required>
          </div>
          <div class="form-group">
            <label class="form-label">비밀번호 {{ editingStudent.isNew ? '(기본: 학번)' : '(변경 시에만 입력)' }}</label>
            <input 
              v-model="editingStudent.password" 
              type="password" 
              class="form-input" 
              :placeholder="editingStudent.isNew ? '미입력 시 학번과 동일' : '변경하지 않으려면 공란'"
            />
          </div>
          <div class="form-group">
            <label class="form-label">발표 포인트 (Level)</label>
            <input v-model.number="editingStudent.presentationPoint" type="number" class="form-input" min="1">
          </div>
          <div class="modal-footer mt-3" style="display:flex; gap:0.5rem; justify-content:flex-end;">
            <button type="button" class="btn btn-outline" @click="isStudentModalOpen = false">취소</button>
            <button type="submit" class="btn btn-primary">저장하기</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const students = ref<any[]>([])
const platforms = ref<any[]>([])
const searchQuery = ref('')
const isStudentModalOpen = ref(false)
const isUploadingBulk = ref(false)
const bulkCsvText = ref('')

const newPlatform = ref({ name: '', url: '' })
const gridConfig = ref({ rows: 6, colsPattern: '2,3' })

const editingStudent = ref({
  isNew: false,
  sno: '',
  name: '',
  password: '',
  presentationPoint: 1
})

const sortKey = ref<string>('sno')
const sortOrder = ref<'asc' | 'desc'>('asc')

onMounted(async () => {
  if (!authStore.isAdmin) {
    router.push('/assignment')
    return
  }
  await Promise.all([
    loadStudents(),
    loadPlatforms(),
    loadGridConfig()
  ])
})

function toggleSort(key: string) {
  if (sortKey.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortOrder.value = (key === 'totalExamScore' || key === 'presentationPoint') ? 'desc' : 'asc'
  }
}

function getSortIcon(key: string) {
  if (sortKey.value !== key) return '↕'
  return sortOrder.value === 'asc' ? '▲' : '▼'
}

const filteredStudents = computed(() => {
  let list = students.value
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(s => 
      (s.name && s.name.toLowerCase().includes(q)) || 
      (s.sno && s.sno.toLowerCase().includes(q))
    )
  }

  return [...list].sort((a, b) => {
    let valA = a[sortKey.value]
    let valB = b[sortKey.value]

    if (sortKey.value === 'totalExamScore') {
      valA = valA != null ? Number(valA) : 0
      valB = valB != null ? Number(valB) : 0
    } else if (sortKey.value === 'presentationPoint') {
      valA = valA != null ? Number(valA) : 1
      valB = valB != null ? Number(valB) : 1
    } else if (sortKey.value === 'passwordChanged') {
      valA = a.passwordChanged ? 1 : 0
      valB = b.passwordChanged ? 1 : 0
    } else if (typeof valA === 'string') {
      valA = valA.toLowerCase()
      valB = (valB || '').toLowerCase()
      const cmp = valA.localeCompare(valB, 'ko')
      return sortOrder.value === 'asc' ? cmp : -cmp
    }

    if (valA < valB) return sortOrder.value === 'asc' ? -1 : 1
    if (valA > valB) return sortOrder.value === 'asc' ? 1 : -1
    return 0
  })
})

async function loadStudents() {
  try {
    const res = await fetch('/api/students')
    if (res.ok) {
      const data = await res.json()
      students.value = Array.isArray(data) ? data : []
    }
  } catch (e) {
    console.error('Failed to load students:', e)
  }
}

async function loadPlatforms() {
  try {
    const res = await fetch('/api/platforms')
    if (res.ok) {
      const data = await res.json()
      platforms.value = Array.isArray(data) ? data : []
    }
  } catch (e) {
    console.error('Failed to load platforms:', e)
  }
}

async function loadGridConfig() {
  try {
    const res = await fetch('/api/metadata/grid-config')
    if (res.ok) {
      const data = await res.json()
      gridConfig.value = {
        rows: data.rows || 6,
        colsPattern: data.colsPattern || '2,3'
      }
    }
  } catch (e) {}
}

async function handleBulkTextImport() {
  if (!bulkCsvText.value.trim()) return
  isUploadingBulk.value = true

  try {
    const res = await fetch('/api/students/bulk', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ csvText: bulkCsvText.value.trim() })
    })
    if (res.ok) {
      const data = await res.json()
      alert(`✅ ${data.message || '학생 등록이 완료되었습니다.'}`)
      bulkCsvText.value = ''
      await loadStudents()
    } else {
      const err = await res.json().catch(() => ({}))
      alert('학생 일괄 등록 실패: ' + (err.message || '입력 내용을 확인해 주세요.'))
    }
  } catch (e: any) {
    alert('등록 중 네트워크 오류가 발생했습니다.')
  } finally {
    isUploadingBulk.value = false
  }
}

async function handleAddPlatform() {
  if (!newPlatform.value.name.trim()) return
  try {
    const res = await fetch('/api/platforms', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: newPlatform.value.name.trim(),
        url: newPlatform.value.url.trim()
      })
    })
    if (res.ok) {
      alert(`✅ '${newPlatform.value.name}' 사이트가 추가되었습니다.`)
      newPlatform.value = { name: '', url: '' }
      await loadPlatforms()
    } else {
      const err = await res.json().catch(() => ({}))
      alert('사이트 추가 실패: ' + (err.message || '다시 시도해 주세요.'))
    }
  } catch (e: any) {
    alert('사이트 추가 중 오류가 발생했습니다.')
  }
}

async function handleDeletePlatform(p: any) {
  if (!confirm(`'${p.name}' 출처 사이트를 삭제하시겠습니까?`)) return
  try {
    const res = await fetch(`/api/platforms/${p.id}`, { method: 'DELETE' })
    if (res.ok) {
      alert(`🗑️ '${p.name}' 사이트가 삭제되었습니다.`)
      await loadPlatforms()
    }
  } catch (e) {
    alert('사이트 삭제 실패')
  }
}

function openAddStudentModal() {
  editingStudent.value = {
    isNew: true,
    sno: '',
    name: '',
    password: '',
    presentationPoint: 1
  }
  isStudentModalOpen.value = true
}

function openEditStudentModal(s: any) {
  editingStudent.value = {
    isNew: false,
    sno: s.sno,
    name: s.name,
    password: '',
    presentationPoint: s.presentationPoint || 1
  }
  isStudentModalOpen.value = true
}

async function handleSaveStudent() {
  const payload = {
    sno: editingStudent.value.sno,
    name: editingStudent.value.name,
    password: editingStudent.value.password || undefined,
    presentationPoint: editingStudent.value.presentationPoint || 1
  }

  try {
    const url = editingStudent.value.isNew ? '/api/students' : `/api/students/${payload.sno}`
    const method = editingStudent.value.isNew ? 'POST' : 'PUT'
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    if (res.ok) {
      alert(`✅ ${payload.name} (${payload.sno}) 학생 정보가 저장되었습니다.`)
      isStudentModalOpen.value = false
      await loadStudents()
    } else {
      const err = await res.json().catch(() => ({}))
      alert('학생 저장 실패: ' + (err.message || '다시 시도해 주세요.'))
    }
  } catch (e: any) {
    alert('학생 저장 중 오류가 발생했습니다.')
  }
}

async function deleteStudent(s: any) {
  if (!confirm(`${s.name} (${s.sno}) 학생을 삭제하시겠습니까?`)) return
  try {
    const res = await fetch(`/api/students/${s.sno}`, { method: 'DELETE' })
    if (res.ok) {
      alert(`🗑️ ${s.name} 학생이 삭제되었습니다.`)
      await loadStudents()
    }
  } catch (e) {
    alert('학생 삭제 실패')
  }
}

async function handleSaveGridConfig() {
  try {
    const res = await fetch('/api/metadata/grid-config', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        rows: gridConfig.value.rows,
        colsPattern: gridConfig.value.colsPattern
      })
    })
    if (res.ok) {
      alert('✅ 좌석 그리드 및 분단 설정이 저장되었습니다.')
    }
  } catch (e: any) {
    alert('설정 저장 중 오류가 발생했습니다.')
  }
}
</script>
