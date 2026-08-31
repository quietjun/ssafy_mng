<template>
  <div class="grades-page">
    <div class="content-split">
      <!-- Left Column: Exam List & Admin Create/Edit Exam -->
      <div class="split-col left-col" style="flex: 1.1; min-width: 440px;">
        <div class="card">
          <div class="card-header">
            <div style="display:flex; align-items:center; gap:0.5rem;">
              <h3>📋 시험 및 평가 항목 목록</h3>
              <span class="badge">{{ filteredExams.length }}개 평가</span>
            </div>
            <button class="btn btn-sm btn-primary" @click="isCreatingExam = !isCreatingExam">
              {{ isCreatingExam ? '✖️ 등록 닫기' : '+ 시험 항목 등록' }}
            </button>
          </div>

          <!-- Category Filter Tabs -->
          <div class="category-filter-tabs mb-3" style="display:flex; gap:0.4rem; flex-wrap:wrap;">
            <button 
              v-for="cat in categoryOptions" 
              :key="cat.value"
              :class="['code-tab-btn', { active: selectedCategoryFilter === cat.value }]"
              @click="selectedCategoryFilter = cat.value"
              style="font-size:0.82rem; padding:0.3rem 0.65rem;"
            >
              {{ cat.label }}
            </button>
          </div>

          <!-- Admin Create Exam Form -->
          <form v-if="isCreatingExam" @submit.prevent="handleCreateExam" class="sub-form mb-3">
            <div class="form-group">
              <label class="form-label">시험 이름 / 평가명 *</label>
              <input v-model="newExam.title" type="text" class="form-input" placeholder="예: 1월 과목평가 (Java)" required>
            </div>
            <div class="form-group">
              <label class="form-label">시험 구분 *</label>
              <select v-model="newExam.category" class="form-select" required>
                <option value="MONTHLY">📘 월말평가</option>
                <option value="SUBJECT">🛠️ 과목평가</option>
                <option value="OTHER">💡 기타평가</option>
              </select>
            </div>
            <div class="form-group" style="display:grid; grid-template-columns: 1fr 1fr; gap:0.5rem;">
              <div>
                <label class="form-label">시험 날짜</label>
                <input v-model="newExam.examDate" type="date" class="form-input">
              </div>
              <div>
                <label class="form-label">만점 기준 (점)</label>
                <input v-model.number="newExam.perfectScore" type="number" class="form-input" min="10" max="1000" required>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">시험 설명 / 비고 (선택)</label>
              <textarea v-model="newExam.description" class="form-textarea" rows="2" placeholder="평가 범위 및 유의사항"></textarea>
            </div>
            <div style="display:flex; gap:0.4rem;">
              <button type="submit" class="btn btn-sm btn-success" style="flex:1;">등록하기</button>
              <button type="button" class="btn btn-sm btn-outline" @click="isCreatingExam = false">취소</button>
            </div>
          </form>

          <!-- Exam Cards List -->
          <div class="exam-list" style="display:flex; flex-direction:column; gap:0.6rem;">
            <div v-if="isLoadingExams" class="empty-state">시험 목록을 불러오는 중...</div>
            <div v-else-if="filteredExams.length === 0" class="empty-state">등록된 시험 항목이 없습니다.</div>

            <div 
              v-for="exam in filteredExams" 
              :key="exam.id"
              :class="['exam-item-card', { active: selectedExam?.id === exam.id }]"
              @click="selectExam(exam)"
            >
              <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:0.5rem;">
                <div>
                  <div style="display:flex; align-items:center; gap:0.4rem; margin-bottom:0.25rem;">
                    <span :class="['problem-type-badge', getCategoryBadgeClass(exam.category)]">
                      {{ exam.categoryName || '평가' }}
                    </span>
                    <strong style="font-size:0.95rem; color:#f8fafc;">{{ exam.title }}</strong>
                  </div>
                  <div style="font-size:0.78rem; color:var(--text-muted); display:flex; gap:0.75rem;">
                    <span>📅 {{ exam.examDate || '날짜 미지정' }}</span>
                    <span>🎯 만점: {{ exam.perfectScore }}점</span>
                    <span>👥 응시: {{ exam.scoreCount }}명</span>
                  </div>
                </div>

                <div style="display:flex; align-items:center; gap:0.4rem;" @click.stop>
                  <span class="ai-chip chip-pass" style="font-size:0.75rem; padding:0.15rem 0.45rem;">
                    평균 {{ exam.averageScore }}점
                  </span>
                  <button class="btn btn-sm btn-outline" style="padding:0.2rem 0.4rem; font-size:0.75rem;" @click="startEditExam(exam)">✏️</button>
                  <button class="btn btn-sm btn-danger-outline" style="padding:0.2rem 0.4rem; font-size:0.75rem;" @click="handleDeleteExam(exam)">🗑️</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Exam Detail, CSV Import & Student Score Table -->
      <div class="split-col right-col" style="flex: 1.35; min-width: 500px;">
        <div class="card">
          <!-- Card Header & Selected Exam Summary -->
          <div class="card-header">
            <div v-if="selectedExam" style="display:flex; align-items:center; gap:0.5rem; flex-wrap:wrap;">
              <span :class="['problem-type-badge', getCategoryBadgeClass(selectedExam.category)]">
                {{ selectedExam.categoryName }}
              </span>
              <h3>{{ selectedExam.title }}</h3>
              <span class="badge">만점 {{ selectedExam.perfectScore }}점</span>
            </div>
            <h3 v-else>시험을 선택해 주세요</h3>
          </div>

          <template v-if="selectedExam">
            <!-- Edit Exam Accordion -->
            <form v-if="isEditingExam" @submit.prevent="handleUpdateExam" class="sub-form mb-3">
              <div class="form-group">
                <label class="form-label">시험 이름 수정</label>
                <input v-model="editExam.title" type="text" class="form-input" required>
              </div>
              <div class="form-group" style="display:grid; grid-template-columns: 1fr 1fr 1fr; gap:0.5rem;">
                <div>
                  <label class="form-label">구분</label>
                  <select v-model="editExam.category" class="form-select">
                    <option value="MONTHLY">월말평가</option>
                    <option value="SUBJECT">과목평가</option>
                    <option value="OTHER">기타평가</option>
                  </select>
                </div>
                <div>
                  <label class="form-label">시험 날짜</label>
                  <input v-model="editExam.examDate" type="date" class="form-input">
                </div>
                <div>
                  <label class="form-label">만점 기준</label>
                  <input v-model.number="editExam.perfectScore" type="number" class="form-input" required>
                </div>
              </div>
              <div style="display:flex; gap:0.4rem;">
                <button type="submit" class="btn btn-sm btn-success" style="flex:1;">💾 저장하기</button>
                <button type="button" class="btn btn-sm btn-outline" @click="isEditingExam = false">취소</button>
              </div>
            </form>

            <!-- Stats Bar -->
            <div class="stats-row mb-3" style="display:grid; grid-template-columns: repeat(4, 1fr); gap:0.5rem;">
              <div class="stat-card">
                <div class="stat-num" style="color:var(--info);">{{ selectedExam.scoreCount }}명</div>
                <div class="stat-label">응시 학생 수</div>
              </div>
              <div class="stat-card success">
                <div class="stat-num">{{ selectedExam.averageScore }}점</div>
                <div class="stat-label">평균 점수</div>
              </div>
              <div class="stat-card">
                <div class="stat-num" style="color:var(--success);">{{ selectedExam.maxScore }}점</div>
                <div class="stat-label">최고 점수</div>
              </div>
              <div class="stat-card">
                <div class="stat-num" style="color:var(--warning);">{{ selectedExam.minScore }}점</div>
                <div class="stat-label">최저 점수</div>
              </div>
            </div>

            <!-- CSV / Text Bulk Score Import Accordion -->
            <div class="admin-problem-create-box mb-3">
              <button 
                class="btn btn-sm btn-outline w-100 mb-2" 
                @click="showBulkBox = !showBulkBox"
                style="display:flex; justify-content:space-between; align-items:center;"
              >
                <span>📊 엑셀/텍스트(CSV) 성적 일괄 등록</span>
                <span>{{ showBulkBox ? '▲ 접기' : '▼ 펼치기' }}</span>
              </button>

              <form v-if="showBulkBox" @submit.prevent="handleBulkImportScores" class="sub-form">
                <p style="font-size:0.82rem; color:var(--text-muted); margin-bottom:0.4rem;">
                  엑셀이나 텍스트에서 <strong>학번, 점수</strong> (또는 <strong>학번, 이름, 점수</strong>)를 복사하여 아래에 붙여넣으세요. (쉼표 또는 탭 구분 지원)
                </p>
                <textarea 
                  v-model="bulkCsvText" 
                  class="form-textarea mb-2" 
                  rows="4" 
                  style="font-family:'JetBrains Mono', monospace; font-size:0.82rem;"
                  placeholder="예시 (쉼표 또는 탭 구분):&#10;20240101, 95&#10;20240102	이순신	88.5"
                ></textarea>
                <div style="display:flex; gap:0.4rem;">
                  <button type="submit" class="btn btn-sm btn-primary" style="flex:1;" :disabled="isSubmittingBulk">
                    {{ isSubmittingBulk ? '저장 중...' : '💾 점수 일괄 저장/업데이트' }}
                  </button>
                  <button type="button" class="btn btn-sm btn-outline" @click="bulkCsvText = ''">초기화</button>
                </div>
              </form>
            </div>

            <!-- Full Student Scores Table -->
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th style="width: 25%;">학번 (SNO)</th>
                    <th style="width: 20%;">이름</th>
                    <th style="width: 25%; text-align:center;">점수 (만점: {{ selectedExam.perfectScore }})</th>
                    <th style="width: 30%; text-align:center;">비고 / 수정</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="combinedStudentScores.length === 0">
                    <td colspan="4" class="empty-state" style="text-align:center;">등록된 학생 정보가 없습니다.</td>
                  </tr>
                  <tr v-for="st in combinedStudentScores" :key="st.sno">
                    <td><strong>{{ st.sno }}</strong></td>
                    <td style="font-weight:600; color:#f8fafc;">{{ st.name }}</td>
                    <td style="text-align:center;">
                      <span v-if="st.hasScore" :class="['ai-chip', getScoreChipClass(st.score, selectedExam.perfectScore)]" style="font-size:0.82rem; padding:0.2rem 0.55rem; font-weight:700;">
                        {{ st.score }}점
                      </span>
                      <span v-else style="color:#64748b; font-size:0.82rem;">미응시</span>
                    </td>
                    <td style="text-align:center;">
                      <button class="btn btn-sm btn-outline" style="padding:0.2rem 0.5rem; font-size:0.75rem;" @click="openSingleScoreModal(st)">
                        {{ st.hasScore ? '점수 수정' : '+ 점수 입력' }}
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- Single Student Score Edit Modal -->
    <div v-if="singleScoreModal" class="modal-overlay" @click.self="singleScoreModal = null">
      <div class="modal">
        <div class="modal-header">
          <h3>📝 {{ singleScoreModal.studentName }} ({{ singleScoreModal.studentSno }}) 학생 점수 입력</h3>
          <button class="modal-close" @click="singleScoreModal = null">&times;</button>
        </div>
        <form @submit.prevent="handleSaveSingleScore" class="modal-body">
          <div class="form-group">
            <label class="form-label">시험 점수 (만점: {{ selectedExam?.perfectScore }}점)</label>
            <input v-model.number="singleScoreModal.score" type="number" step="0.1" min="0" :max="selectedExam?.perfectScore" class="form-input" required>
          </div>
          <div class="form-group">
            <label class="form-label">비고 / 메모 (선택)</label>
            <input v-model="singleScoreModal.note" type="text" class="form-input" placeholder="특이사항 메모">
          </div>
          <div class="modal-footer" style="display:flex; gap:0.5rem; justify-content:flex-end; margin-top:1rem;">
            <button type="button" class="btn btn-outline" @click="singleScoreModal = null">취소</button>
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

interface ExamItem {
  id: number
  title: string
  category: 'MONTHLY' | 'SUBJECT' | 'OTHER'
  categoryName: string
  examDate?: string
  perfectScore: number
  description?: string
  scoreCount: number
  averageScore: number
  maxScore: number
  minScore: number
}

interface ExamScoreItem {
  id?: number
  examId: number
  studentSno: string
  studentName: string
  score: number
  note?: string
}

interface StudentItem {
  sno: string
  name: string
}

const exams = ref<ExamItem[]>([])
const selectedExam = ref<ExamItem | null>(null)
const examScores = ref<ExamScoreItem[]>([])
const allStudents = ref<StudentItem[]>([])

const isLoadingExams = ref(false)
const isCreatingExam = ref(false)
const isEditingExam = ref(false)
const showBulkBox = ref(false)
const isSubmittingBulk = ref(false)
const bulkCsvText = ref('')

const selectedCategoryFilter = ref<string>('ALL')

const categoryOptions = [
  { label: '전체 보기', value: 'ALL' },
  { label: '📘 월말평가', value: 'MONTHLY' },
  { label: '🛠️ 과목평가', value: 'SUBJECT' },
  { label: '💡 기타평가', value: 'OTHER' }
]

const newExam = ref({
  title: '',
  category: 'MONTHLY',
  examDate: new Date().toISOString().split('T')[0],
  perfectScore: 100,
  description: ''
})

const editExam = ref({
  id: 0,
  title: '',
  category: 'MONTHLY',
  examDate: '',
  perfectScore: 100,
  description: ''
})

const singleScoreModal = ref<{
  studentSno: string
  studentName: string
  score: number
  note: string
} | null>(null)

const filteredExams = computed(() => {
  if (selectedCategoryFilter.value === 'ALL') return exams.value
  return exams.value.filter(e => e.category === selectedCategoryFilter.value)
})

const combinedStudentScores = computed(() => {
  const scoreMap = new Map<string, ExamScoreItem>()
  examScores.value.forEach(s => {
    scoreMap.set(s.studentSno, s)
  })

  return allStudents.value.map(st => {
    const sc = scoreMap.get(st.sno)
    return {
      sno: st.sno,
      name: st.name,
      hasScore: !!sc,
      score: sc ? sc.score : 0,
      note: sc ? sc.note || '' : ''
    }
  })
})

onMounted(async () => {
  if (!authStore.isAdmin) {
    router.push('/assignment')
    return
  }
  await Promise.all([loadStudents(), loadExams()])
})

async function loadStudents() {
  try {
    const res = await fetch('/api/students')
    if (res.ok) {
      const data = await res.json()
      if (Array.isArray(data)) {
        allStudents.value = data.filter((s: any) => s.role !== 'ROLE_ADMIN' && !s.escape)
      }
    }
  } catch (e) {
    console.error('Failed to load students:', e)
  }
}

async function loadExams() {
  isLoadingExams.value = true
  try {
    const res = await fetch('/api/exams')
    if (res.ok) {
      const data = await res.json()
      exams.value = Array.isArray(data) ? data : []
      if (exams.value.length > 0 && !selectedExam.value) {
        selectExam(exams.value[0])
      } else if (selectedExam.value) {
        const refreshed = exams.value.find(e => e.id === selectedExam.value?.id)
        if (refreshed) selectedExam.value = refreshed
      }
    }
  } catch (e) {
    console.error('Failed to load exams:', e)
  } finally {
    isLoadingExams.value = false
  }
}

async function selectExam(exam: ExamItem) {
  selectedExam.value = exam
  isEditingExam.value = false
  try {
    const res = await fetch(`/api/exams/${exam.id}/scores`)
    if (res.ok) {
      const data = await res.json()
      examScores.value = Array.isArray(data) ? data : []
    }
  } catch (e) {
    examScores.value = []
  }
}

async function handleCreateExam() {
  try {
    const res = await fetch('/api/exams', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newExam.value)
    })
    if (res.ok) {
      const created = await res.json()
      alert(`✅ '${created.title}' 시험이 등록되었습니다.`)
      isCreatingExam.value = false
      newExam.value = {
        title: '',
        category: 'MONTHLY',
        examDate: new Date().toISOString().split('T')[0],
        perfectScore: 100,
        description: ''
      }
      await loadExams()
      selectExam(created)
    } else {
      const err = await res.json().catch(() => ({}))
      alert('시험 등록 실패: ' + (err.message || '다시 시도해 주세요.'))
    }
  } catch (e) {
    alert('시험 등록 중 네트워크 오류가 발생했습니다.')
  }
}

function startEditExam(exam: ExamItem) {
  selectedExam.value = exam
  editExam.value = {
    id: exam.id,
    title: exam.title,
    category: exam.category,
    examDate: exam.examDate || '',
    perfectScore: exam.perfectScore,
    description: exam.description || ''
  }
  isEditingExam.value = true
}

async function handleUpdateExam() {
  try {
    const res = await fetch(`/api/exams/${editExam.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editExam.value)
    })
    if (res.ok) {
      const updated = await res.json()
      alert('✅ 시험 정보가 수정되었습니다.')
      isEditingExam.value = false
      await loadExams()
      selectExam(updated)
    }
  } catch (e) {
    alert('시험 수정 실패')
  }
}

async function handleDeleteExam(exam: ExamItem) {
  if (confirm(`'${exam.title}' 시험 항목과 등록된 학생 점수를 정말 삭제하시겠습니까?`)) {
    try {
      const res = await fetch(`/api/exams/${exam.id}`, { method: 'DELETE' })
      if (res.ok) {
        alert('🗑️ 시험이 삭제되었습니다.')
        if (selectedExam.value?.id === exam.id) selectedExam.value = null
        await loadExams()
      }
    } catch (e) {
      alert('시험 삭제 실패')
    }
  }
}

async function handleBulkImportScores() {
  if (!selectedExam.value) return
  if (!bulkCsvText.value.trim()) {
    alert('등록할 점수 데이터를 입력해 주세요.')
    return
  }
  isSubmittingBulk.value = true
  try {
    const res = await fetch('/api/exams/scores/bulk', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        examId: selectedExam.value.id,
        csvText: bulkCsvText.value.trim()
      })
    })
    if (res.ok) {
      const result = await res.json()
      alert(`✅ ${result.message}`)
      bulkCsvText.value = ''
      showBulkBox.value = false
      await loadExams()
      if (selectedExam.value) await selectExam(selectedExam.value)
    } else {
      const err = await res.json().catch(() => ({}))
      alert('점수 일괄 등록 실패: ' + (err.message || '입력 데이터를 확인해 주세요.'))
    }
  } catch (e) {
    alert('점수 등록 중 오류가 발생했습니다.')
  } finally {
    isSubmittingBulk.value = false
  }
}

function openSingleScoreModal(st: { sno: string; name: string; score: number; note: string }) {
  singleScoreModal.value = {
    studentSno: st.sno,
    studentName: st.name,
    score: st.score,
    note: st.note
  }
}

async function handleSaveSingleScore() {
  if (!selectedExam.value || !singleScoreModal.value) return
  try {
    const res = await fetch(`/api/exams/${selectedExam.value.id}/scores`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        studentSno: singleScoreModal.value.studentSno,
        score: singleScoreModal.value.score,
        note: singleScoreModal.value.note
      })
    })
    if (res.ok) {
      singleScoreModal.value = null
      await loadExams()
      if (selectedExam.value) await selectExam(selectedExam.value)
    }
  } catch (e) {
    alert('점수 저장 중 오류가 발생했습니다.')
  }
}

function getCategoryBadgeClass(category?: string) {
  if (category === 'MONTHLY') return 'badge-hw'
  if (category === 'SUBJECT') return 'badge-ws'
  return 'chip-time'
}

function getScoreChipClass(score: number, perfectScore: number) {
  const ratio = score / (perfectScore || 100)
  if (ratio >= 0.8) return 'chip-pass'
  if (ratio >= 0.6) return 'chip-time'
  return 'chip-fail'
}
</script>

<style scoped>
.exam-item-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 0.85rem 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
}
.exam-item-card:hover {
  border-color: var(--primary);
  background: rgba(99, 102, 241, 0.05);
}
.exam-item-card.active {
  border-color: var(--primary);
  background: rgba(99, 102, 241, 0.12);
}
</style>
