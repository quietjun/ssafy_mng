<template>
  <div class="pairs-page">
    <div class="card mb-3">
      <div class="card-header" style="flex-wrap: wrap; gap: 1rem;">
        <div style="display: flex; align-items: center; gap: 0.6rem;">
          <h2 style="font-size: 1.3rem; font-weight: 800; color: #f8fafc;">👥 프로젝트 도메인별 2인 1조 페어 관리</h2>
          <span class="badge">총 {{ domainStudents.length }}명</span>
        </div>

        <!-- Action Control Buttons -->
        <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
          <button class="btn btn-primary btn-sm" @click="autoPairSnake" title="시험 총점 1등과 꼴등, 2등과 (꼴등-1) 순서로 자동 매칭">
            🪄 성적순 자동 매칭
          </button>
          <button class="btn btn-outline btn-sm" @click="autoPairRandom" title="도메인 내 학생 무작위 매칭">
            🎲 랜덤 페어링
          </button>
          <button class="btn btn-outline btn-sm" @click="addNewEmptyPair" title="수동 배치를 위한 새 조 생성">
            ➕ 새 조 생성
          </button>
          <button class="btn btn-success-outline btn-sm" @click="openSaveHistoryModal" title="현재 구성한 페어를 회차별로 DB에 확정 저장">
            💾 현재 페어 이력 저장
          </button>
          <button class="btn btn-outline btn-sm" @click="isHistoryModalOpen = true" title="과거 저장된 페어 이력 조회">
            📜 과거 이력 ({{ domainHistoryCount }}건)
          </button>
          <button class="btn btn-outline btn-sm" @click="copyPairList" title="단톡방 공지용 텍스트 복사">
            📋 결과 텍스트 복사
          </button>
        </div>
      </div>

      <!-- Domain Tabs -->
      <div class="domain-tabs" style="display: flex; gap: 0.5rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem; overflow-x: auto;">
        <button 
          v-for="dom in availableDomains" 
          :key="dom"
          :class="['btn', 'btn-sm', selectedDomain === dom ? 'btn-primary' : 'btn-outline']"
          @click="selectDomain(dom)"
          style="white-space: nowrap;"
        >
          🏷️ {{ dom }} ({{ getStudentCountByDomain(dom) }}명)
        </button>
      </div>
    </div>

    <!-- Main Drag & Drop Workspace -->
    <div class="pairs-grid-layout" style="display: grid; grid-template-columns: 1fr 300px; gap: 1rem; align-items: start;">
      
      <!-- Pairs Cards Area -->
      <div class="pairs-container">
        <div v-if="pairs.length === 0" class="card" style="text-align: center; padding: 3rem;">
          <p class="empty-state">생성된 페어가 없습니다. 상단의 <strong>[🪄 1등-꼴등 자동 매칭]</strong> 또는 <strong>[➕ 새 조 생성]</strong> 버튼을 눌러보세요.</p>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1rem;">
          <div 
            v-for="(pair, idx) in pairs" 
            :key="idx"
            class="card pair-card"
            :class="{ 
              'drop-target': dragOverPairIdx === idx,
              'has-history-warning': getPastHistoryWarning(pair) != null
            }"
            @dragover.prevent="onDragOverPair(idx)"
            @dragleave="onDragLeavePair(idx)"
            @drop="onDropToPair(idx)"
            style="border: 1px solid var(--border-color); transition: all 0.2s ease;"
          >
            <div class="card-header" style="padding: 0.6rem 0.8rem; background: rgba(30, 41, 59, 0.4); border-bottom: 1px solid var(--border-color);">
              <div style="display: flex; align-items: center; gap: 0.4rem;">
                <strong style="color: var(--primary); font-size: 0.95rem;">제 {{ idx + 1 }} 조</strong>
                <span :class="['badge', pair.length === 3 ? 'warning' : pair.length === 1 ? 'info' : '']" style="font-size: 0.72rem;">
                  {{ pair.length === 1 ? '1인 (단독/퇴소자 발생)' : pair.length === 3 ? '3인 조' : '2인 짝' }}
                </span>
              </div>
              <div style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; color: var(--text-muted);">
                <span>합계: <strong style="color: #f8fafc;">{{ getPairTotalScore(pair) }}점</strong></span>
                <button v-if="pair.length === 0" class="btn btn-sm btn-danger-outline" style="padding: 0.1rem 0.3rem; font-size: 0.7rem;" @click="removePairCard(idx)" title="조 삭제">&times;</button>
              </div>
            </div>

            <!-- Warning Badge for Past Pair History Match -->
            <div 
              v-if="getPastHistoryWarning(pair)" 
              style="margin: 0.6rem 0.75rem 0 0.75rem; padding: 0.35rem 0.6rem; background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3); border-radius: var(--radius-sm); font-size: 0.75rem; font-weight: 700; display: flex; align-items: center; gap: 0.4rem;"
            >
              <span>⚠️ 이전 짝 경험 있음</span>
              <span style="font-weight: 400; opacity: 0.9;">({{ getPastHistoryWarning(pair)?.title }})</span>
            </div>

            <!-- Members inside Pair -->
            <div class="pair-members" style="padding: 0.75rem; display: flex; flex-direction: column; gap: 0.5rem; min-height: 90px;">
              <div v-if="pair.length === 0" style="text-align: center; color: var(--text-muted); padding: 1.5rem; border: 1px dashed var(--border-color); border-radius: var(--radius-sm); font-size: 0.8rem;">
                학생 카드를 이곳으로 드롭하세요
              </div>

              <div 
                v-for="st in getSortedPairMembers(pair)" 
                :key="st.sno"
                class="student-card-item"
                draggable="true"
                @dragstart="onDragStart(st, idx)"
                style="display: flex; justify-content: space-between; align-items: center; padding: 0.6rem 0.8rem; background: var(--card-bg); border: 1px solid var(--border-color); border-radius: var(--radius-sm); cursor: grab;"
              >
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                  <span style="font-weight: 700; color: #f8fafc; font-size: 0.9rem;">{{ st.name }}</span>
                  <span style="font-size: 0.75rem; color: var(--text-muted);">({{ st.sno }})</span>
                </div>
                <div style="display: flex; align-items: center; gap: 0.4rem;">
                  <span class="ai-chip chip-pass" style="font-weight: 700; font-size: 0.78rem; padding: 0.1rem 0.4rem;">
                    {{ st.totalExamScore != null ? st.totalExamScore + '점' : '0점' }}
                  </span>
                  <span :class="['ai-chip', st.cert !== false ? 'chip-complexity' : 'chip-time']" style="font-size: 0.7rem; padding: 0.1rem 0.35rem;">
                    {{ st.cert !== false ? 'A형' : '미취득' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Unassigned / Standby Pool -->
      <div 
        class="card standby-pool"
        :class="{ 'drop-target': dragOverPairIdx === -1 }"
        @dragover.prevent="onDragOverPair(-1)"
        @dragleave="onDragLeavePair(-1)"
        @drop="onDropToStandby"
        style="border: 1px dashed var(--border-color); position: sticky; top: 1rem;"
      >
        <div class="card-header" style="padding: 0.6rem 0.8rem;">
          <h3 style="font-size: 0.95rem; color: #f8fafc;">📥 대기 / 미배정 목록</h3>
          <span class="badge">{{ unassignedStudents.length }}명</span>
        </div>

        <div style="padding: 0.75rem; display: flex; flex-direction: column; gap: 0.5rem; max-height: 70vh; overflow-y: auto;">
          <div v-if="unassignedStudents.length === 0" style="text-align: center; color: var(--text-muted); font-size: 0.8rem; padding: 2rem 0;">
            모든 학생이 조에 배치되었습니다.
          </div>

          <div 
            v-for="st in unassignedStudents" 
            :key="st.sno"
            class="student-card-item"
            draggable="true"
            @dragstart="onDragStart(st, -1)"
            style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.75rem; background: var(--card-bg); border: 1px solid var(--border-color); border-radius: var(--radius-sm); cursor: grab;"
          >
            <div>
              <strong style="color: #f8fafc; font-size: 0.85rem;">{{ st.name }}</strong>
              <div style="font-size: 0.72rem; color: var(--text-muted);">{{ st.sno }}</div>
            </div>
            <span class="ai-chip chip-pass" style="font-size: 0.75rem; font-weight: 700;">
              {{ st.totalExamScore != null ? st.totalExamScore + '점' : '0점' }}
            </span>
          </div>
        </div>
      </div>

    </div>

    <!-- Save Pair History Modal -->
    <div v-if="isSaveModalOpen" class="modal-overlay" @click.self="isSaveModalOpen = false">
      <div class="modal">
        <div class="modal-header">
          <h3>💾 현재 페어 이력 저장</h3>
          <button class="modal-close" @click="isSaveModalOpen = false">&times;</button>
        </div>
        <form @submit.prevent="submitSavePairs" class="modal-body">
          <div class="form-group">
            <label class="form-label">프로젝트 도메인</label>
            <input type="text" class="form-input" :value="selectedDomain" disabled />
          </div>
          <div class="form-group">
            <label class="form-label">회차 명칭 / 제목 *</label>
            <input 
              v-model="saveTitle" 
              type="text" 
              class="form-input" 
              placeholder="예: 2026년 3월 1차 페어링"
              required 
            />
          </div>
          <p style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.5rem;">
            * 저장된 페어 기록은 추후 다음 달 페어링 시 <strong>"⚠️ 이전 짝 이력 있음"</strong> 중복 감지에 활용됩니다.
          </p>
          <div class="modal-footer mt-3" style="display: flex; gap: 0.5rem; justify-content: flex-end;">
            <button type="button" class="btn btn-outline" @click="isSaveModalOpen = false">취소</button>
            <button type="submit" class="btn btn-primary" :disabled="isSaving">저장하기</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Past History View Modal -->
    <div v-if="isHistoryModalOpen" class="modal-overlay" @click.self="isHistoryModalOpen = false">
      <div class="modal" style="max-width: 650px;">
        <div class="modal-header">
          <h3>📜 과거 저장된 페어 이력 ({{ selectedDomain }})</h3>
          <button class="modal-close" @click="isHistoryModalOpen = false">&times;</button>
        </div>
        <div class="modal-body" style="max-height: 65vh; overflow-y: auto;">
          <div v-if="groupedHistories.length === 0" class="empty-state" style="text-align: center; padding: 2rem;">
            저장된 과거 페어 회차 기록이 없습니다.
          </div>
          <div v-else style="display: flex; flex-direction: column; gap: 1rem;">
            <div 
              v-for="group in groupedHistories" 
              :key="group.title"
              style="padding: 1rem; background: rgba(30, 41, 59, 0.5); border: 1px solid var(--border-color); border-radius: var(--radius-md);"
            >
              <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-color); padding-bottom: 0.6rem; margin-bottom: 0.75rem;">
                <div>
                  <strong style="color: #f8fafc; font-size: 1.05rem; display: flex; align-items: center; gap: 0.5rem;">
                    📅 {{ group.title }}
                    <span class="badge" style="font-size: 0.75rem;">총 {{ group.items.length }}개 조/짝</span>
                  </strong>
                  <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.2rem;">
                    저장 일시: {{ formatDate(group.createdAt) }}
                  </div>
                </div>
                <button 
                  class="btn btn-sm btn-danger" 
                  style="font-size: 0.8rem; padding: 0.35rem 0.75rem; font-weight: 700;"
                  @click="deletePairHistoryTitle(group.domain, group.title)" 
                  title="해당 페어 회차 전체 삭제"
                >
                  🗑️ 회차 전체 삭제
                </button>
              </div>

              <!-- Pair Members List in Saved Session -->
              <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 0.5rem; font-size: 0.83rem;">
                <div 
                  v-for="(item, idx) in group.items" 
                  :key="item.id"
                  style="padding: 0.4rem 0.6rem; background: var(--card-bg); border: 1px solid var(--border-color); border-radius: var(--radius-sm);"
                >
                  <span style="color: var(--primary); font-weight: 700;">{{ idx + 1 }}조:</span>
                  <span style="color: #f8fafc; margin-left: 0.3rem;">
                    {{ item.student1Name }} &amp; {{ item.student2Name }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer" style="display: flex; justify-content: flex-end;">
          <button class="btn btn-outline" @click="isHistoryModalOpen = false">닫기</button>
        </div>
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

interface PairHistoryItem {
  id: number
  domain: string
  title: string
  student1Sno: string
  student1Name: string
  student2Sno: string
  student2Name: string
  createdAt: string
}

const allStudents = ref<StudentItem[]>([])
const selectedDomain = ref<string>('여행')

const pairs = ref<StudentItem[][]>([])
const unassignedStudents = ref<StudentItem[]>([])

const pastHistories = ref<PairHistoryItem[]>([])

const draggedStudent = ref<StudentItem | null>(null)
const draggedFromPairIdx = ref<number>(-2)
const dragOverPairIdx = ref<number>(-2)

const isSaveModalOpen = ref(false)
const isHistoryModalOpen = ref(false)
const saveTitle = ref('')
const isSaving = ref(false)

onMounted(async () => {
  if (!authStore.isAdmin) {
    router.push('/assignment')
    return
  }
  await Promise.all([loadStudents(), loadPairHistories()])
})

const availableDomains = computed(() => {
  const set = new Set<string>()
  allStudents.value.forEach(s => {
    set.add(s.domain || '여행')
  })
  if (set.size === 0) set.add('여행')
  return Array.from(set)
})

const domainStudents = computed(() => {
  return allStudents.value.filter(s => (s.domain || '여행') === selectedDomain.value)
})

const groupedHistories = computed(() => {
  const domainFiltered = pastHistories.value.filter(h => h.domain === selectedDomain.value)
  const map = new Map<string, { title: string; domain: string; createdAt: string; items: PairHistoryItem[] }>()

  domainFiltered.forEach(h => {
    const key = `${h.domain}___${h.title}`
    if (!map.has(key)) {
      map.set(key, {
        title: h.title,
        domain: h.domain,
        createdAt: h.createdAt,
        items: []
      })
    }
    map.get(key)!.items.push(h)
  })

  return Array.from(map.values())
})

const domainHistoryCount = computed(() => {
  return groupedHistories.value.length
})

function getStudentCountByDomain(dom: string) {
  return allStudents.value.filter(s => (s.domain || '여행') === dom).length
}

async function loadStudents() {
  try {
    const res = await fetch('/api/students')
    if (res.ok) {
      const data = await res.json()
      allStudents.value = Array.isArray(data) ? data : []
      if (availableDomains.value.length > 0 && !availableDomains.value.includes(selectedDomain.value)) {
        selectedDomain.value = availableDomains.value[0]
      }
      resetPairsForCurrentDomain()
    }
  } catch (e) {
    console.error('Failed to load students:', e)
  }
}

async function loadPairHistories() {
  try {
    const res = await fetch('/api/pairs/history')
    if (res.ok) {
      const data = await res.json()
      pastHistories.value = Array.isArray(data) ? data : []
    }
  } catch (e) {
    console.error('Failed to load pair histories:', e)
  }
}

function selectDomain(dom: string) {
  selectedDomain.value = dom
  resetPairsForCurrentDomain()
}

function resetPairsForCurrentDomain() {
  pairs.value = []
  unassignedStudents.value = [...domainStudents.value]
  autoPairSnake()
}

// 🪄 1등 ↔ 꼴등 자동 매칭 (Snake Top-Bottom Pairing)
function autoPairSnake() {
  const sorted = [...domainStudents.value].sort((a, b) => {
    const scoreA = a.totalExamScore != null ? a.totalExamScore : 0
    const scoreB = b.totalExamScore != null ? b.totalExamScore : 0
    return scoreB - scoreA
  })

  const newPairs: StudentItem[][] = []
  let left = 0
  let right = sorted.length - 1

  while (left <= right) {
    if (left === right) {
      if (newPairs.length > 0) {
        newPairs[newPairs.length - 1].push(sorted[left])
      } else {
        newPairs.push([sorted[left]])
      }
      break
    } else {
      newPairs.push([sorted[left], sorted[right]])
      left++
      right--
    }
  }

  pairs.value = newPairs
  unassignedStudents.value = []
}

// 🎲 무작위 랜덤 페어링
function autoPairRandom() {
  const shuffled = [...domainStudents.value].sort(() => Math.random() - 0.5)
  const newPairs: StudentItem[][] = []

  for (let i = 0; i < shuffled.length; i += 2) {
    if (i + 1 < shuffled.length) {
      newPairs.push([shuffled[i], shuffled[i + 1]])
    } else {
      if (newPairs.length > 0) {
        newPairs[newPairs.length - 1].push(shuffled[i])
      } else {
        newPairs.push([shuffled[i]])
      }
    }
  }

  pairs.value = newPairs
  unassignedStudents.value = []
}

function addNewEmptyPair() {
  pairs.value.push([])
}

function removePairCard(idx: number) {
  if (pairs.value[idx].length > 0) {
    unassignedStudents.value.push(...pairs.value[idx])
  }
  pairs.value.splice(idx, 1)
}

// ⚠️ Check if Pair has Past Pair History in DB (Checks all pairwise combinations)
function getPastHistoryWarning(pair: StudentItem[]): PairHistoryItem | null {
  if (pair.length < 2) return null
  for (let i = 0; i < pair.length; i++) {
    for (let j = i + 1; j < pair.length; j++) {
      const sno1 = pair[i].sno
      const sno2 = pair[j].sno
      const match = pastHistories.value.find(h => 
        (h.student1Sno === sno1 && h.student2Sno === sno2) ||
        (h.student1Sno === sno2 && h.student2Sno === sno1)
      )
      if (match) return match
    }
  }
  return null
}

function openSaveHistoryModal() {
  if (pairs.value.length === 0) {
    alert('저장할 페어가 없습니다.')
    return
  }
  const now = new Date()
  saveTitle.value = `${now.getFullYear()}년 ${now.getMonth() + 1}월 ${selectedDomain.value} 페어`
  isSaveModalOpen.value = true
}

async function submitSavePairs() {
  if (!saveTitle.value.trim()) {
    alert('회차 명칭을 입력해주세요.')
    return
  }

  const payloadPairs: { student1Sno: string; student1Name: string; student2Sno: string; student2Name: string }[] = []

  pairs.value.forEach(p => {
    if (p.length === 1) {
      payloadPairs.push({
        student1Sno: p[0].sno,
        student1Name: p[0].name,
        student2Sno: '-',
        student2Name: '단독(1인 조)'
      })
    } else if (p.length >= 2) {
      for (let i = 0; i < p.length; i++) {
        for (let j = i + 1; j < p.length; j++) {
          payloadPairs.push({
            student1Sno: p[i].sno,
            student1Name: p[i].name,
            student2Sno: p[j].sno,
            student2Name: p[j].name
          })
        }
      }
    }
  })

  if (payloadPairs.length === 0) {
    alert('저장할 페어가 없습니다.')
    return
  }

  isSaving.value = true
  try {
    const res = await fetch('/api/pairs/history', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        domain: selectedDomain.value,
        title: saveTitle.value.trim(),
        pairs: payloadPairs
      })
    })

    const data = await res.json()
    if (res.ok) {
      alert('💾 페어 이력이 성공적으로 저장되었습니다!')
      isSaveModalOpen.value = false
      await loadPairHistories()
    } else {
      alert('저장 실패: ' + (data.message || '오류 발생'))
    }
  } catch (e) {
    alert('네트워크 오류')
  } finally {
    isSaving.value = false
  }
}

async function deletePairHistoryItem(id: number) {
  if (confirm('해당 페어 이력 항목을 삭제하시겠습니까?')) {
    try {
      const res = await fetch(`/api/pairs/history/${id}`, { method: 'DELETE' })
      if (res.ok) {
        await loadPairHistories()
      }
    } catch (e) {
      alert('삭제 실패')
    }
  }
}

async function deletePairHistoryTitle(domain: string, title: string) {
  if (confirm(`'${title}' 회차의 전체 페어 이력을 삭제하시겠습니까?\n(삭제 시 이전 짝 경고 표시 대상에서 제외됩니다.)`)) {
    try {
      const res = await fetch(`/api/pairs/history/title?domain=${encodeURIComponent(domain)}&title=${encodeURIComponent(title)}`, { method: 'DELETE' })
      if (res.ok) {
        alert(`🗑️ '${title}' 페어 회차 이력이 삭제되었습니다.`)
        await loadPairHistories()
      }
    } catch (e) {
      alert('삭제 실패')
    }
  }
}

// 📋 페어 결과 텍스트 복사 (단톡방 공지용 - 점수/경고 제거, 가나다순 정렬)
function copyPairList() {
  if (pairs.value.length === 0) {
    alert('복사할 페어 결과가 없습니다.')
    return
  }

  // 1. 각 조 내부 구성원들을 가나다순으로 정렬
  const processedPairs = pairs.value
    .filter(p => p.length > 0)
    .map(p => {
      return [...p].sort((a, b) => (a.name || '').localeCompare(b.name || '', 'ko'))
    })

  // 2. 대표자(첫 번째 멤버)의 이름 가나다순으로 전체 조 정렬
  processedPairs.sort((pairA, pairB) => {
    const nameA = pairA[0]?.name || ''
    const nameB = pairB[0]?.name || ''
    return nameA.localeCompare(nameB, 'ko')
  })

  // 3. 점수/경고 없이 깨끗한 텍스트 포맷 생성
  let text = `[✈️ ${selectedDomain.value} 프로젝트 페어 매칭 결과]\n`
  processedPairs.forEach((p, idx) => {
    const memberNames = p.map(s => s.name).join(', ')
    text += `${idx + 1}조: ${memberNames}\n`
  })

  navigator.clipboard.writeText(text).then(() => {
    alert('📋 페어 명단이 단톡방 공지용(가나다순, 점수/경고 숨김)으로 클립보드에 복사되었습니다!')
  }).catch(() => {
    alert('복사 실패')
  })
}

// Score Calculation Helpers
function getSortedPairMembers(pair: StudentItem[]): StudentItem[] {
  return [...pair].sort((a, b) => {
    const scoreA = a.totalExamScore != null ? a.totalExamScore : 0
    const scoreB = b.totalExamScore != null ? b.totalExamScore : 0
    return scoreB - scoreA // 높은 점수 위, 낮은 점수 아래
  })
}

function getPairTotalScore(pair: StudentItem[]) {
  const sum = pair.reduce((acc, curr) => acc + (curr.totalExamScore != null ? curr.totalExamScore : 0), 0)
  return Math.round(sum * 10.0) / 10.0
}

function getPairAvgScore(pair: StudentItem[]) {
  if (!pair.length) return 0
  const avg = getPairTotalScore(pair) / pair.length
  return Math.round(avg * 10.0) / 10.0
}

// Drag & Drop Handlers
function onDragStart(student: StudentItem, fromIdx: number) {
  draggedStudent.value = student
  draggedFromPairIdx.value = fromIdx
}

function onDragOverPair(pairIdx: number) {
  dragOverPairIdx.value = pairIdx
}

function onDragLeavePair(pairIdx: number) {
  if (dragOverPairIdx.value === pairIdx) {
    dragOverPairIdx.value = -2
  }
}

function onDropToPair(targetPairIdx: number) {
  dragOverPairIdx.value = -2
  if (!draggedStudent.value) return

  removeStudentFromSource()

  pairs.value[targetPairIdx].push(draggedStudent.value)
  draggedStudent.value = null
}

function onDropToStandby() {
  dragOverPairIdx.value = -2
  if (!draggedStudent.value) return

  removeStudentFromSource()
  unassignedStudents.value.push(draggedStudent.value)
  draggedStudent.value = null
}

function removeStudentFromSource() {
  if (!draggedStudent.value) return
  const sno = draggedStudent.value.sno

  if (draggedFromPairIdx.value === -1) {
    unassignedStudents.value = unassignedStudents.value.filter(s => s.sno !== sno)
  } else if (draggedFromPairIdx.value >= 0) {
    const sourcePair = pairs.value[draggedFromPairIdx.value]
    pairs.value[draggedFromPairIdx.value] = sourcePair.filter(s => s.sno !== sno)
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
.pair-card.drop-target, .standby-pool.drop-target {
  border-color: var(--primary) !important;
  box-shadow: 0 0 12px rgba(99, 102, 241, 0.3);
  background: rgba(99, 102, 241, 0.05) !important;
}

.pair-card.has-history-warning {
  border-color: rgba(239, 68, 68, 0.5) !important;
}

.student-card-item:active {
  cursor: grabbing !important;
}
</style>
