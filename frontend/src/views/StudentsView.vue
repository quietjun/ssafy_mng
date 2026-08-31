<template>
  <div class="students-page">
    <div class="content-split">
      <!-- Left Column: Master Student List (1:1 Ratio, Interactive Sort) -->
      <div class="split-col left-col" style="flex: 1; min-width: 0;">
        <div class="card">
          <div class="card-header">
            <div style="display:flex; align-items:center; gap:0.5rem;">
              <h3>👨‍🎓 학생 계정 목록</h3>
              <span class="badge">{{ filteredStudents.length }} / {{ students.length }}명</span>
            </div>
            <button class="btn btn-sm btn-primary" @click="openAddStudentModal">+ 신규 학생 등록</button>
          </div>

          <!-- Search Filter -->
          <div class="table-search-box mb-2">
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
                  <th style="width: 22%; cursor: pointer; user-select: none;" @click="toggleSort('sno')" title="학번순 정렬">
                    학번 <span style="font-size:0.72rem; opacity:0.8;">{{ getSortIcon('sno') }}</span>
                  </th>
                  <th style="width: 16%; cursor: pointer; user-select: none;" @click="toggleSort('name')" title="이름순 정렬">
                    이름 <span style="font-size:0.72rem; opacity:0.8;">{{ getSortIcon('name') }}</span>
                  </th>
                  <th style="width: 20%; text-align:center; cursor: pointer; user-select: none;" @click="toggleSort('totalExamScore')" title="시험 총점순 정렬">
                    시험 총점 <span style="font-size:0.72rem; opacity:0.8;">{{ getSortIcon('totalExamScore') }}</span>
                  </th>
                  <th style="width: 18%; text-align:center; cursor: pointer; user-select: none;" @click="toggleSort('presentationPoint')" title="발표 레벨순 정렬">
                    발표 Lv <span style="font-size:0.72rem; opacity:0.8;">{{ getSortIcon('presentationPoint') }}</span>
                  </th>
                  <th style="width: 24%; text-align:center; cursor: pointer; user-select: none;" @click="toggleSort('passwordChanged')" title="비번 상태순 정렬">
                    비번 상태 <span style="font-size:0.72rem; opacity:0.8;">{{ getSortIcon('passwordChanged') }}</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="filteredStudents.length === 0">
                  <td colspan="5" style="text-align:center; padding: 2rem;" class="empty-state">
                    {{ searchQuery ? '검색 결과와 일치하는 학생이 없습니다.' : '등록된 학생이 없습니다.' }}
                  </td>
                </tr>
                <tr 
                  v-for="s in filteredStudents" 
                  :key="s.sno"
                  :class="['clickable-row', { active: selectedStudent?.sno === s.sno }]"
                  @click="selectStudent(s)"
                  style="cursor: pointer;"
                >
                  <td><strong>{{ s.sno }}</strong></td>
                  <td style="font-weight: 700; color: #f8fafc;">{{ s.name }}</td>
                  <td style="text-align:center;">
                    <span class="ai-chip chip-pass" style="padding: 0.15rem 0.5rem; font-size: 0.78rem; font-weight:700;">
                      {{ s.totalExamScore != null ? s.totalExamScore + '점' : '0점' }}
                    </span>
                  </td>
                  <td style="text-align:center;">
                    <span class="ai-chip chip-complexity" style="padding: 0.15rem 0.45rem; font-size: 0.72rem;">
                      Lv.{{ s.presentationPoint || 1 }}
                    </span>
                  </td>
                  <td style="text-align:center;">
                    <span :class="['ai-chip', s.passwordChanged ? 'chip-pass' : 'chip-time']" style="padding: 0.15rem 0.45rem; font-size: 0.72rem;">
                      {{ s.passwordChanged ? '변경완료' : '초기비번' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Right Column: Selected Student Detail Viewer (1:1 Ratio) -->
      <div class="split-col right-col" style="flex: 1; min-width: 0;">
        <div class="card">
          <!-- Student Header Banner -->
          <div v-if="selectedStudent" class="card-header" style="flex-wrap:wrap; gap:0.5rem; border-bottom:1px solid var(--border-color); padding-bottom:0.75rem; margin-bottom:0.75rem;">
            <div style="display:flex; align-items:center; gap:0.6rem;">
              <span class="user-role-tag admin" style="font-size:0.8rem; padding:0.25rem 0.6rem;">STUDENT</span>
              <h2 style="font-size:1.3rem; font-weight:800; color:#f8fafc;">{{ selectedStudent.name }}</h2>
              <span class="badge" style="font-size:0.85rem;">학번: {{ selectedStudent.sno }}</span>
            </div>

            <div style="display:flex; gap:0.4rem;">
              <button class="btn btn-sm btn-outline" @click="openEditStudentModal(selectedStudent)">✏️ 정보 수정</button>
              <button class="btn btn-sm btn-warning-outline" @click="resetPassword(selectedStudent)">🔑 비번 초기화</button>
              <button class="btn btn-sm btn-danger-outline" @click="deleteStudent(selectedStudent)">🗑️ 계정 삭제</button>
            </div>
          </div>
          <div v-else class="card-header">
            <h3>학생을 선택해 주세요</h3>
          </div>

          <template v-if="selectedStudent">
            <!-- Stats Summary Cards -->
            <div class="stats-row mb-3" style="display:grid; grid-template-columns: repeat(4, 1fr); gap:0.5rem;">
              <div class="stat-card">
                <div class="stat-num" style="color:var(--info);">{{ studentExamScores.length }}회</div>
                <div class="stat-label">응시 시험 수</div>
              </div>
              <div class="stat-card success">
                <div class="stat-num">{{ studentTotalScore }}점</div>
                <div class="stat-label">시험 총점</div>
              </div>
              <div class="stat-card">
                <div class="stat-num" style="color:var(--primary);">{{ studentAverageScore }}점</div>
                <div class="stat-label">시험 평균</div>
              </div>
              <div class="stat-card">
                <div class="stat-num" style="color:var(--warning);">{{ selectedStudent.solved || 0 }}개</div>
                <div class="stat-label">알고리즘 해결 수</div>
              </div>
            </div>

            <!-- Student Basic Info Section -->
            <div class="admin-problem-create-box mb-3" style="background:rgba(30, 41, 59, 0.4); padding:1rem; border-radius:var(--radius-md);">
              <h4 style="margin-bottom:0.75rem; color:#f8fafc; font-size:0.95rem; display:flex; align-items:center; gap:0.4rem;">
                📌 학생 기본 설정 정보
              </h4>
              <div style="display:grid; grid-template-columns: repeat(3, 1fr); gap:0.75rem; font-size:0.85rem;">
                <div>
                  <span style="color:var(--text-muted); display:block;">학번 (SNO)</span>
                  <strong style="color:#f8fafc;">{{ selectedStudent.sno }}</strong>
                </div>
                <div>
                  <span style="color:var(--text-muted); display:block;">이름</span>
                  <strong style="color:#f8fafc;">{{ selectedStudent.name }}</strong>
                </div>
                <div>
                  <span style="color:var(--text-muted); display:block;">프로젝트 도메인</span>
                  <strong style="color:#f8fafc;">{{ selectedStudent.domain || '여행' }}</strong>
                </div>
                <div>
                  <span style="color:var(--text-muted); display:block;">A 형 취득 여부</span>
                  <span :class="['ai-chip', selectedStudent.cert !== false ? 'chip-pass' : 'chip-fail']" style="padding:0.1rem 0.45rem; font-size:0.75rem;">
                    {{ selectedStudent.cert !== false ? '✅ 취득 (True)' : '❌ 미취득 (False)' }}
                  </span>
                </div>
                <div>
                  <span style="color:var(--text-muted); display:block;">발표 포인트 Level</span>
                  <span class="ai-chip chip-complexity" style="padding:0.1rem 0.4rem; font-size:0.75rem;">
                    Lv.{{ selectedStudent.presentationPoint || 1 }}
                  </span>
                </div>
                <div>
                  <span style="color:var(--text-muted); display:block;">배치 좌석 좌표</span>
                  <strong style="color:#f8fafc;">
                    {{ (selectedStudent.srow != null && selectedStudent.scol != null) ? `${selectedStudent.srow + 1}행 ${selectedStudent.scol + 1}열` : '좌석 미배치' }}
                  </strong>
                </div>
                <div>
                  <span style="color:var(--text-muted); display:block;">비밀번호 변경 상태</span>
                  <span :class="['ai-chip', selectedStudent.passwordChanged ? 'chip-pass' : 'chip-time']" style="padding:0.1rem 0.4rem; font-size:0.75rem;">
                    {{ selectedStudent.passwordChanged ? '사용자 변경 완료' : '초기 비밀번호 사용중' }}
                  </span>
                </div>
                <div>
                  <span style="color:var(--text-muted); display:block;">계정 권한</span>
                  <span class="user-role-tag" style="font-size:0.7rem; padding:0.1rem 0.35rem;">
                    {{ selectedStudent.role || 'ROLE_STUDENT' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Student Exam Scores Breakdown Section -->
            <div>
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.6rem;">
                <h4 style="color:#f8fafc; font-size:0.95rem;">📊 시험별 성적 이력 ({{ studentExamScores.length }}건)</h4>
                <button class="btn btn-sm btn-outline" style="font-size:0.75rem;" @click="loadStudentScores(selectedStudent.sno)">🔄 새로고침</button>
              </div>

              <div class="table-responsive">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th style="width: 35%;">시험 항목명</th>
                      <th style="width: 25%; text-align:center;">취득 점수</th>
                      <th style="width: 20%; text-align:center;">비고 / 메모</th>
                      <th style="width: 20%; text-align:center;">업데이트 일시</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="isLoadingScores">
                      <td colspan="4" class="empty-state" style="text-align:center;">성적 이력을 불러오는 중...</td>
                    </tr>
                    <tr v-else-if="studentExamScores.length === 0">
                      <td colspan="4" class="empty-state" style="text-align:center;">등록된 시험 점수가 없습니다.</td>
                    </tr>
                    <tr v-for="sc in studentExamScores" :key="sc.id || sc.examId">
                      <td>
                        <strong style="color:#f8fafc; font-size:0.9rem; display:block;">{{ sc.examTitle || '평가 항목' }}</strong>
                      </td>
                      <td style="text-align:center;">
                        <span class="ai-chip chip-pass" style="font-size:0.85rem; font-weight:800; padding:0.2rem 0.6rem;">
                          {{ sc.score }}점
                        </span>
                      </td>
                      <td style="text-align:center; color:var(--text-muted); font-size:0.82rem;">
                        {{ sc.note || '-' }}
                      </td>
                      <td style="text-align:center; color:var(--text-muted); font-size:0.78rem;">
                        {{ formatDate(sc.updatedAt) }}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- Student Edit / Add Modal -->
    <div v-if="isStudentModalOpen" class="modal-overlay" @click.self="isStudentModalOpen = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingStudent.isNew ? '➕ 신규 학생 등록' : '✏️ 학생 정보 수정' }}</h3>
          <button class="modal-close" @click="isStudentModalOpen = false">&times;</button>
        </div>
        <form @submit.prevent="saveStudent" class="modal-body">
          <div class="form-group">
            <label class="form-label">학번 (SNO) *</label>
            <input 
              v-model="editingStudent.sno" 
              type="text" 
              class="form-input" 
              :disabled="!editingStudent.isNew"
              required 
            />
          </div>
          <div class="form-group">
            <label class="form-label">이름 *</label>
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
          <div class="form-group">
            <label class="form-label">프로젝트 도메인 (기본: 여행)</label>
            <input v-model="editingStudent.domain" type="text" class="form-input" placeholder="예: 여행">
          </div>
          <div class="form-group">
            <label class="form-label">A 형 취득 여부</label>
            <select v-model="editingStudent.cert" class="form-select">
              <option :value="true">✅ 취득 (True)</option>
              <option :value="false">❌ 미취득 (False)</option>
            </select>
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

interface StudentItem {
  sno: string
  name: string
  role?: string
  presentationPoint?: number
  srow?: number
  scol?: number
  solved?: number
  escape?: boolean
  passwordChanged?: boolean
  totalExamScore?: number
  domain?: string
  cert?: boolean
}

interface ExamScoreItem {
  id?: number
  examId: number
  examTitle: string
  studentSno: string
  studentName: string
  score: number
  note?: string
  updatedAt?: string
}

const students = ref<StudentItem[]>([])
const selectedStudent = ref<StudentItem | null>(null)
const studentExamScores = ref<ExamScoreItem[]>([])
const isLoadingScores = ref(false)

const searchQuery = ref('')
const sortKey = ref<string>('sno')
const sortOrder = ref<'asc' | 'desc'>('asc')

const isStudentModalOpen = ref(false)
const editingStudent = ref({
  isNew: false,
  sno: '',
  name: '',
  password: '',
  presentationPoint: 1,
  domain: '여행',
  cert: true
})

onMounted(async () => {
  if (!authStore.isAdmin) {
    router.push('/assignment')
    return
  }
  await loadStudents()
})

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
    let valA = (a as any)[sortKey.value]
    let valB = (b as any)[sortKey.value]

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

const studentTotalScore = computed(() => {
  if (!studentExamScores.value.length) return 0
  const sum = studentExamScores.value.reduce((acc, curr) => acc + (curr.score || 0), 0)
  return Math.round(sum * 10.0) / 10.0
})

const studentAverageScore = computed(() => {
  if (!studentExamScores.value.length) return 0
  const avg = studentTotalScore.value / studentExamScores.value.length
  return Math.round(avg * 10.0) / 10.0
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

async function loadStudents() {
  try {
    const res = await fetch('/api/students')
    if (res.ok) {
      const data = await res.json()
      students.value = Array.isArray(data) ? data : []
      if (students.value.length > 0 && !selectedStudent.value) {
        selectStudent(students.value[0])
      } else if (selectedStudent.value) {
        const refreshed = students.value.find(s => s.sno === selectedStudent.value?.sno)
        if (refreshed) selectStudent(refreshed)
      }
    }
  } catch (e) {
    console.error('Failed to load students:', e)
  }
}

async function selectStudent(student: StudentItem) {
  selectedStudent.value = student
  await loadStudentScores(student.sno)
}

async function loadStudentScores(sno: string) {
  isLoadingScores.value = true
  try {
    const res = await fetch(`/api/exams/scores/student/${sno}`)
    if (res.ok) {
      const data = await res.json()
      studentExamScores.value = Array.isArray(data) ? data : []
    } else {
      studentExamScores.value = []
    }
  } catch (e) {
    studentExamScores.value = []
  } finally {
    isLoadingScores.value = false
  }
}

function openAddStudentModal() {
  editingStudent.value = {
    isNew: true,
    sno: '',
    name: '',
    password: '',
    presentationPoint: 1,
    domain: '여행',
    cert: true
  }
  isStudentModalOpen.value = true
}

function openEditStudentModal(st: StudentItem) {
  editingStudent.value = {
    isNew: false,
    sno: st.sno,
    name: st.name,
    password: '',
    presentationPoint: st.presentationPoint || 1,
    domain: st.domain || '여행',
    cert: st.cert !== false
  }
  isStudentModalOpen.value = true
}

async function saveStudent() {
  const url = editingStudent.value.isNew ? '/api/students' : `/api/students/${editingStudent.value.sno}`
  const method = editingStudent.value.isNew ? 'POST' : 'PUT'

  try {
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editingStudent.value)
    })
    const data = await res.json()
    if (res.ok) {
      alert(editingStudent.value.isNew ? '✅ 학생이 등록되었습니다.' : '✅ 학생 정보가 수정되었습니다.')
      isStudentModalOpen.value = false
      await loadStudents()
    } else {
      alert('오류: ' + (data.message || '저장 실패'))
    }
  } catch (e) {
    alert('네트워크 오류')
  }
}

async function resetPassword(st: StudentItem) {
  if (confirm(`'${st.name}' (${st.sno}) 학생의 비밀번호를 학번으로 초기화하시겠습니까?`)) {
    try {
      const res = await fetch(`/api/students/${st.sno}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: st.name,
          password: st.sno,
          presentationPoint: st.presentationPoint
        })
      })
      if (res.ok) {
        alert('🔑 비밀번호가 학번으로 초기화되었습니다.')
        await loadStudents()
      }
    } catch (e) {
      alert('비밀번호 초기화 실패')
    }
  }
}

async function deleteStudent(st: StudentItem) {
  if (confirm(`'${st.name}' (${st.sno}) 학생을 삭제하시겠습니까?`)) {
    try {
      const res = await fetch(`/api/students/${st.sno}`, { method: 'DELETE' })
      if (res.ok) {
        alert('🗑️ 학생이 삭제되었습니다.')
        if (selectedStudent.value?.sno === st.sno) selectedStudent.value = null
        await loadStudents()
      }
    } catch (e) {
      alert('학생 삭제 실패')
    }
  }
}

function formatDate(dateStr?: string) {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  } catch (e) {
    return dateStr
  }
}
</script>

<style scoped>
.clickable-row:hover {
  background: rgba(99, 102, 241, 0.08) !important;
}
.clickable-row.active {
  background: rgba(99, 102, 241, 0.15) !important;
  border-left: 3px solid var(--primary);
}
</style>
