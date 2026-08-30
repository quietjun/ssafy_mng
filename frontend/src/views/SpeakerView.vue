<template>
  <div class="speaker-page">
    <div class="card toolbar-card mb-3">
      <div class="toolbar-actions">
        <button class="btn btn-warning" :disabled="isLotteryRunning" @click="runLottery">
          <span>🎲</span> {{ isLotteryRunning ? '추첨 중...' : '발표자 추첨' }}
        </button>
        <button v-if="!isShuffled" class="btn btn-secondary" @click="shuffleSeats">
          <span>🔀</span> 시험배치 (셔플)
        </button>
        <button v-else class="btn btn-primary" @click="restoreOriginalSeats">
          <span>↩️</span> 원래 자리로 복원
        </button>
        <button :class="['btn', isMoveMode ? 'btn-danger' : 'btn-outline']" :disabled="isShuffled" @click="toggleMoveMode">
          <span>✋</span> {{ isMoveMode ? '좌석이동 모드 종료' : '좌석 이동 / 맞교환' }}
        </button>
        <button class="btn btn-success" @click="isRotated = !isRotated">
          <span>🔄</span> 시점 전환
        </button>
        <button class="btn btn-outline" @click="toggleAllCandidates">
          <span>✅</span> 전체 선택/해제
        </button>
        <button class="btn btn-outline" @click="loadLayout">
          <span>🔃</span> 새로고침
        </button>
      </div>

      <div class="view-direction-badge" @click="isRotated = !isRotated">
        {{ isRotated ? '👨‍🎓 STUDENT VIEW (강단을 바라보는 학생 시점)' : '👨‍🏫 TEACHER VIEW (학생을 바라보는 강사 시점)' }}
      </div>
    </div>

    <!-- 시험배치 임시 모드 알림 배너 -->
    <div v-if="isShuffled" class="alert-banner warning mb-3">
      <div class="alert-content">
        <span>📢</span>
        <span><b>임시 시험배치 상태입니다</b> (DB에 저장되지 않음). 시험이 끝난 후 상단의 <b>[↩️ 원래 자리로 복원]</b> 버튼을 누르면 평소 자리로 되돌아갑니다.</span>
      </div>
    </div>

    <!-- 좌석 이동 모드 알림 배너 -->
    <div v-if="isMoveMode && !isShuffled" class="alert-banner info mb-3">
      <div class="alert-content">
        <span>💡</span>
        <span><b>좌석 이동 모드</b>: 학생 카드를 원하는 자리로 <b>드래그 앤 드롭</b>하거나, <b>클릭(선택) 후 이동할 자리(클릭)</b>를 선택하면 자리가 이동/맞교환됩니다.</span>
        <span v-if="selectedSeatForMove" class="badge badge-warning ml-2">선택됨: ({{ selectedSeatForMove.r + 1 }}행 {{ selectedSeatForMove.c + 1 }}열)</span>
      </div>
    </div>

    <!-- Classroom Stage & Seat Grid Box (Centered & Aligned) -->
    <div class="classroom-stage-box">
      <div class="classroom-stage-inner">
        <!-- 1. Student View Top Bar (When rotated, Door at Left edge, Teacher at Right edge) -->
        <div v-if="isRotated" class="student-top-bar">
          <div class="classroom-door-badge">
            <span>🚪 출입문 (Front Door)</span>
          </div>
          <div class="teacher-desk-badge">
            <span>👨‍🏫 강사 / 교탁 (Teacher Desk)</span>
          </div>
        </div>

        <!-- Sections Header Labels (출입문 쪽이 1분단, 열 수 표기 제거) -->
        <div class="sections-header-row">
          <div 
            v-for="(sec, idx) in sectionCols" 
            :key="idx" 
            class="section-label"
            :style="{ width: `${sec.length * 110 + (sec.length - 1) * 10}px`, textAlign: 'center' }"
          >
            📍 {{ getSectionLabel(idx) }}
          </div>
        </div>

        <!-- Interactive Seat Grid -->
        <div :class="['seat-grid-container', { rotated: isRotated }]">
          <div 
            v-for="rowIdx in rows" 
            :key="rowIdx" 
            class="seat-row"
          >
            <!-- Sections (분단별 좌석 그룹) -->
            <div 
              v-for="(sec, secIdx) in sectionCols" 
              :key="secIdx" 
              class="seat-section"
            >
              <div 
                v-for="colIdx in sec" 
                :key="colIdx" 
                :class="['seat-cell', {
                  'drag-target-over': dragTargetOver && dragTargetOver.r === (rowIdx - 1) && dragTargetOver.c === colIdx,
                  'move-selected-cell': selectedSeatForMove && selectedSeatForMove.r === (rowIdx - 1) && selectedSeatForMove.c === colIdx
                }]"
                @dragover.prevent="onDragOver(rowIdx - 1, colIdx)"
                @dragleave="onDragLeave(rowIdx - 1, colIdx)"
                @drop.prevent="onDrop(rowIdx - 1, colIdx)"
                @click="handleSeatCellClick(rowIdx - 1, colIdx)"
              >
                <!-- 학생이 앉아있는 좌석 카드 -->
                <div 
                  v-if="getStudentAt(rowIdx - 1, colIdx)" 
                  :class="['seat-card', { 
                    selected: candidateMap[getStudentAt(rowIdx - 1, colIdx)!.sno],
                    winner: winnerSno === getStudentAt(rowIdx - 1, colIdx)!.sno,
                    highlight: highlightSno === getStudentAt(rowIdx - 1, colIdx)!.sno,
                    'is-dragging': draggedSeat && draggedSeat.r === (rowIdx - 1) && draggedSeat.c === colIdx,
                    'is-move-selected': selectedSeatForMove && selectedSeatForMove.r === (rowIdx - 1) && selectedSeatForMove.c === colIdx
                  }]"
                  :draggable="true"
                  @dragstart="onDragStart($event, rowIdx - 1, colIdx)"
                  @dragend="onDragEnd"
                >
                  <div class="seat-sno">{{ getStudentAt(rowIdx - 1, colIdx)!.sno }}</div>
                  <div class="seat-name">{{ getStudentAt(rowIdx - 1, colIdx)!.name }}</div>
                  <div class="seat-meta">
                    <span class="seat-point">Lv.{{ getStudentAt(rowIdx - 1, colIdx)!.presentationPoint || 1 }}</span>
                    <span class="seat-drag-handle" title="드래그하여 이동">⋮⋮</span>
                  </div>
                </div>

                <!-- 빈자리 -->
                <div 
                  v-else 
                  :class="['seat-empty', {
                    'drag-target-active': dragTargetOver && dragTargetOver.r === (rowIdx - 1) && dragTargetOver.c === colIdx
                  }]"
                >
                  빈자리
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 2. Teacher View Bottom Bar (Teacher at Left, Door at Right) -->
        <div v-if="!isRotated" class="teacher-bottom-bar">
          <div class="teacher-desk-badge">
            <span>👨‍🏫 강사 / 교탁 (Teacher Desk)</span>
          </div>
          <div class="classroom-door-badge">
            <span>🚪 출입문 (Front Door)</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Winner Modal -->
    <div v-if="winner" class="modal-overlay" @click.self="winner = null">
      <div class="modal text-center">
        <div class="modal-header justify-center">
          <h2>🎉 축하합니다! 오늘의 발표자 🎉</h2>
        </div>
        <div class="modal-body py-4">
          <div class="winner-trophy">🏆</div>
          <h1 class="winner-display-name my-3">{{ winner.name }} ({{ winner.sno }})</h1>
          <p style="color:var(--text-muted);">발표 점수 레벨이 상승했습니다! 👏</p>
          <button class="btn btn-primary btn-lg mt-3" @click="winner = null">확인</button>
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

interface StudentSeat {
  sno: string
  name: string
  srow: number
  scol: number
  presentationPoint?: number
}

const rows = ref(6)
const cols = ref(5)
const colGroups = ref<number[]>([2, 3])
const students = ref<StudentSeat[]>([])
const isRotated = ref(false)
const candidateMap = ref<Record<string, boolean>>({})
const isLotteryRunning = ref(false)
const highlightSno = ref<string | null>(null)
const winnerSno = ref<string | null>(null)
const winner = ref<StudentSeat | null>(null)

// 시험배치(임시 셔플) 상태
const isShuffled = ref(false)

// 좌석 이동 / 맞교환 관련 상태
const isMoveMode = ref(false)
const draggedSeat = ref<{ r: number; c: number } | null>(null)
const dragTargetOver = ref<{ r: number; c: number } | null>(null)
const selectedSeatForMove = ref<{ r: number; c: number } | null>(null)

onMounted(async () => {
  if (!authStore.isAdmin) {
    router.push('/assignment')
    return
  }
  await loadLayout()
})

// 원래 자리로 복원
async function restoreOriginalSeats() {
  await loadLayout()
  isShuffled.value = false
  alert('↩️ 시험배치를 종료하고 원래 평소 좌석 배치로 복원되었습니다!')
}

// 좌석 이동 모드 토글
function toggleMoveMode() {
  if (isShuffled.value) {
    alert('시험배치 중에는 좌석 이동 모드를 사용할 수 없습니다. 원래 자리로 복원 후 이용해 주세요.')
    return
  }
  isMoveMode.value = !isMoveMode.value
  selectedSeatForMove.value = null
  dragTargetOver.value = null
}

// 1. 드래그 앤 드롭 이벤트 핸들러
function onDragStart(e: DragEvent, r: number, c: number) {
  if (isShuffled.value) return // 시험배치 중에는 개별 드래그 비활성화
  draggedSeat.value = { r, c }
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
    e.dataTransfer.setData('text/plain', JSON.stringify({ r, c }))
  }
}

function onDragOver(r: number, c: number) {
  if (isShuffled.value || !draggedSeat.value) return
  if (draggedSeat.value.r === r && draggedSeat.value.c === c) {
    dragTargetOver.value = null
    return
  }
  dragTargetOver.value = { r, c }
}

function onDragLeave(r: number, c: number) {
  if (dragTargetOver.value && dragTargetOver.value.r === r && dragTargetOver.value.c === c) {
    dragTargetOver.value = null
  }
}

async function onDrop(targetRow: number, targetCol: number) {
  if (isShuffled.value) return
  dragTargetOver.value = null
  if (!draggedSeat.value) return

  const srcRow = draggedSeat.value.r
  const srcCol = draggedSeat.value.c
  draggedSeat.value = null

  if (srcRow === targetRow && srcCol === targetCol) return
  await executeSeatSwap(srcRow, srcCol, targetRow, targetCol)
}

function onDragEnd() {
  draggedSeat.value = null
  dragTargetOver.value = null
}

// 2. 클릭 기반 이동 (좌석 이동 모드 또는 터치 환경)
async function handleSeatCellClick(r: number, c: number) {
  const student = getStudentAt(r, c)

  // 일반 모드일 때: 학생 카드 클릭 시 추첨 후보 토글
  if (!isMoveMode.value) {
    if (student) {
      toggleCandidate(student.sno)
    }
    return
  }

  // 좌석 이동 모드일 때:
  if (!selectedSeatForMove.value) {
    if (!student) {
      alert('이동할 학생이 앉아있는 좌석을 먼저 선택해 주세요.')
      return
    }
    selectedSeatForMove.value = { r, c }
  } else {
    const src = selectedSeatForMove.value
    selectedSeatForMove.value = null

    if (src.r === r && src.c === c) {
      return // 같은 자리 클릭 시 선택 해제
    }

    await executeSeatSwap(src.r, src.c, r, c)
  }
}

// 좌석 맞교환 / 이동 API 호출 및 실시간 상태 동기화
async function executeSeatSwap(srcRow: number, srcCol: number, targetRow: number, targetCol: number) {
  try {
    const res = await fetch('/api/speaker/swap', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        srcRow,
        srcCol,
        targetRow,
        targetCol
      })
    })

    if (res.ok) {
      // 프론트엔드 상태 즉시 스왑
      const s1 = students.value.find(s => s.srow === srcRow && s.scol === srcCol)
      const s2 = students.value.find(s => s.srow === targetRow && s.scol === targetCol)

      if (s1) {
        s1.srow = targetRow
        s1.scol = targetCol
      }
      if (s2) {
        s2.srow = srcRow
        s2.scol = srcCol
      }
    } else {
      alert('좌석 이동 중 오류가 발생했습니다.')
      await loadLayout()
    }
  } catch (e) {
    alert('서버와 통신 중 오류가 발생했습니다.')
    await loadLayout()
  }
}

// colGroups (예: [2, 2, 2] 또는 [2, 3])를 기반으로 각 분단별 열 인덱스 배열 생성
const sectionCols = computed(() => {
  if (!colGroups.value || colGroups.value.length === 0) {
    return [Array.from({ length: cols.value }, (_, i) => i)]
  }

  let currentCol = 0
  const sections: number[][] = []
  for (const size of colGroups.value) {
    const sec: number[] = []
    for (let i = 0; i < size; i++) {
      sec.push(currentCol)
      currentCol++
    }
    sections.push(sec)
  }
  return sections
})

// 출입문 쪽이 1분단이 되도록 넘버링 (Teacher View 기준: 우측 출입문 쪽이 1분단)
function getSectionLabel(idx: number) {
  const totalSections = sectionCols.value.length
  if (!isRotated.value) {
    // Teacher View: 맨 오른쪽이 1분단
    const sectionNumber = totalSections - idx
    return `${sectionNumber}분단`
  } else {
    // Student View: 회전 시 맨 왼쪽(출입문 위치)이 1분단
    const sectionNumber = idx + 1
    return `${sectionNumber}분단`
  }
}

async function loadLayout() {
  try {
    const res = await fetch('/api/speaker/layout')
    if (res.ok) {
      const data = await res.json()
      rows.value = data.rows || 6
      cols.value = data.cols || 5
      
      if (Array.isArray(data.colGroups) && data.colGroups.length > 0) {
        colGroups.value = data.colGroups
      } else if (data.colPattern || data.colsPattern) {
        const pattern = (data.colPattern || data.colsPattern).trim()
        colGroups.value = pattern.split(/[,: ]+/).map((p: string) => parseInt(p, 10)).filter((n: number) => !isNaN(n) && n > 0)
      } else {
        colGroups.value = [2, 3]
      }

      students.value = data.students || []
      candidateMap.value = {}
      students.value.forEach(s => {
        candidateMap.value[s.sno] = true
      })
    }
  } catch (e) {
    console.error('Failed to load speaker layout:', e)
  }
}

function getStudentAt(r: number, c: number) {
  return students.value.find(s => s.srow === r && s.scol === c)
}

function toggleCandidate(sno: string) {
  candidateMap.value[sno] = !candidateMap.value[sno]
}

function toggleAllCandidates() {
  const allSelected = students.value.every(s => candidateMap.value[s.sno])
  students.value.forEach(s => {
    candidateMap.value[s.sno] = !allSelected
  })
}

function shuffleSeats() {
  // 1. 기존에 좌석이 배정되어 있는 학생들만 추출
  const assigned = students.value.filter(s => s.srow !== null && s.scol !== null)
  if (assigned.length < 2) {
    alert('시험 배치를 진행할 학생이 최소 2명 이상이어야 합니다.')
    return
  }

  // 2. 기존에 학생들이 앉아있던 좌석 좌표 풀 (Slots)
  const seatSlots = assigned.map(s => ({ srow: s.srow, scol: s.scol }))
  const studentOrigin = assigned.map(s => ({ ...s, origRow: s.srow, origCol: s.scol }))

  // 3. 완전 순열 (Derangement): 본인 원래 자리에 다시 앉는 학생이 0명이 될 때까지 셔플
  let isDerangement = false
  let attempts = 0
  let finalSlots = [...seatSlots]

  while (!isDerangement && attempts < 200) {
    attempts++
    finalSlots = [...seatSlots]
    // Fisher-Yates 셔플
    for (let i = finalSlots.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [finalSlots[i], finalSlots[j]] = [finalSlots[j], finalSlots[i]];
    }

    // 본인 원래 자리에 앉은 학생이 0명인지 확인
    const hasSameSeat = studentOrigin.some((s, idx) => 
      s.origRow === finalSlots[idx].srow && s.origCol === finalSlots[idx].scol
    )
    if (!hasSameSeat) {
      isDerangement = true
    }
  }

  // 극단적인 경우 대비: 1칸 순환 시프트(Cyclic shift)로 100% 다른 자리 보장
  if (!isDerangement) {
    finalSlots = seatSlots.map((_, idx) => seatSlots[(idx + 1) % seatSlots.length])
  }

  // 4. 새로운 좌석 좌표 화면에만 임시 배정 (DB 저장 X)
  const updatedAssigned: StudentSeat[] = studentOrigin.map((s, idx) => ({
    sno: s.sno,
    name: s.name,
    presentationPoint: s.presentationPoint,
    srow: finalSlots[idx].srow,
    scol: finalSlots[idx].scol
  }))

  students.value = updatedAssigned
  isShuffled.value = true
  alert(`🔀 기존 좌석 풀(${assigned.length}석) 내에서 전원 100% 다른 자리로 임시 시험배치가 완료되었습니다!\n(DB에 저장되지 않으며 상단 '원래 자리로 복원' 버튼으로 언제든 복구할 수 있습니다.)`)
}

async function runLottery() {
  const candidateSnos = Object.keys(candidateMap.value).filter(sno => candidateMap.value[sno])
  if (candidateSnos.length === 0) {
    alert('추첨 대상 학생을 최소 1명 이상 선택해 주세요.')
    return
  }

  isLotteryRunning.value = true
  highlightSno.value = null
  winnerSno.value = null

  // 룰렛 애니메이션
  let count = 0
  const maxRolls = 25
  const interval = setInterval(async () => {
    const randomSno = candidateSnos[Math.floor(Math.random() * candidateSnos.length)]
    highlightSno.value = randomSno
    count++

    if (count >= maxRolls) {
      clearInterval(interval)
      highlightSno.value = null

      try {
        const res = await fetch('/api/speaker/draw', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(candidateSnos)
        })
        if (res.ok) {
          const result = await res.json()
          winner.value = result
          winnerSno.value = result.sno
          await loadLayout()
        }
      } catch (e: any) {
        alert('추첨 중 오류가 발생했습니다.')
      } finally {
        isLotteryRunning.value = false
      }
    }
  }, 80)
}
</script>
