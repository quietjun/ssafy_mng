<template>
  <div class="lectures-page">
    <!-- Top Header -->
    <div class="page-header mb-4">
      <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:1rem;">
        <div>
          <h2 style="font-size: 1.4rem; font-weight: 800; color: #f8fafc; margin-bottom: 0.35rem; display:flex; align-items:center; gap:0.5rem;">
            <span>📺 라이브 강의 트랙별 집계 & 수집 기간 달력</span>
            <span class="badge info" style="font-weight: 600;">관리자 모드</span>
          </h2>
          <p style="font-size: 0.88rem; color: var(--text-muted); margin: 0;">
            전체 일정의 트랙별 총 수강 시간 및 수집된 시작일~종료일 범위 상태를 직관적으로 확인합니다.
          </p>
        </div>

        <div style="display:flex; gap:0.5rem; flex-wrap:wrap;">
          <button class="btn btn-primary" @click="isPasteModalOpen = true" style="display:flex; align-items:center; gap:0.4rem;">
            <span>📋</span> 데이터 붙여넣기 및 집계
          </button>
          <button class="btn btn-outline" @click="handleFillSampleData" style="display:flex; align-items:center; gap:0.4rem;">
            <span>⚡</span> 예시 데이터 즉시 채우기
          </button>
          <button v-if="summary && summary.totalLectures > 0" class="btn btn-danger-outline" @click="handleClearData">
            🗑️ 전체 초기화
          </button>
        </div>
      </div>
    </div>

    <!-- Status Overview Stat Cards -->
    <div class="stats-grid mb-4" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.25rem;">
      <!-- 1. 수집 강의 기간 범위 (minDate ~ maxDate) -->
      <div class="card stat-card" style="border: 1px solid rgba(59, 130, 246, 0.4); background: rgba(30, 41, 59, 0.4);">
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
          <div>
            <div style="font-size: 0.82rem; color: #94a3b8; font-weight: 600; margin-bottom: 0.3rem;">📅 수집 강의 기간 범위</div>
            <div style="font-size: 1.25rem; font-weight: 800; color: #60a5fa; word-break: break-all;">
              <template v-if="summary?.minLectureDate && summary?.maxLectureDate">
                <span v-if="summary.minLectureDate === summary.maxLectureDate">{{ summary.minLectureDate }}</span>
                <span v-else>{{ summary.minLectureDate }} ~ {{ summary.maxLectureDate }}</span>
              </template>
              <template v-else>데이터 없음</template>
            </div>
          </div>
          <span style="font-size: 1.8rem; opacity: 0.8;">📆</span>
        </div>
        <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.5rem;">
          {{ summary?.minLectureDate ? `총 ${Object.keys(dateLectureMap).length}일간 수집 등록됨` : '등록된 수집 기간이 없습니다.' }}
        </div>
      </div>

      <!-- 2. 전체 일정 총 수강 시간 (Total Duration Hours) -->
      <div class="card stat-card" style="border: 1px solid rgba(16, 185, 129, 0.4); background: rgba(30, 41, 59, 0.4);">
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
          <div>
            <div style="font-size: 0.82rem; color: #94a3b8; font-weight: 600; margin-bottom: 0.3rem;">⏱️ 전체 일정 총 수강 시간</div>
            <div style="font-size: 1.45rem; font-weight: 800; color: #34d399;">
              {{ summary?.totalHours || 0 }}시간
            </div>
          </div>
          <span style="font-size: 1.8rem; opacity: 0.8;">⏳</span>
        </div>
        <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.5rem;">
          총 {{ summary?.totalLectures || 0 }}개 강의 세션 진행
        </div>
      </div>

      <!-- 3. 개설 트랙 수 -->
      <div class="card stat-card" style="border: 1px solid rgba(245, 158, 11, 0.4); background: rgba(30, 41, 59, 0.4);">
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
          <div>
            <div style="font-size: 0.82rem; color: #94a3b8; font-weight: 600; margin-bottom: 0.3rem;">🎯 개설 트랙 수</div>
            <div style="font-size: 1.45rem; font-weight: 800; color: #fbbf24;">
              {{ summary?.totalTracks || 0 }}개 트랙
            </div>
          </div>
          <span style="font-size: 1.8rem; opacity: 0.8;">🎓</span>
        </div>
        <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.5rem;">
          강사 {{ Object.keys(summary?.instructorCounts || {}).length }}명 참여
        </div>
      </div>

      <!-- 4. 마지막 집계 처리 일시 (lastProcessedAt) -->
      <div class="card stat-card" style="border: 1px solid rgba(168, 85, 247, 0.4); background: rgba(30, 41, 59, 0.4);">
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
          <div>
            <div style="font-size: 0.82rem; color: #94a3b8; font-weight: 600; margin-bottom: 0.3rem;">⏱️ 마지막 데이터 처리 일시</div>
            <div style="font-size: 1.15rem; font-weight: 800; color: #c084fc;">
              {{ formatTimestamp(summary?.lastProcessedAt) }}
            </div>
          </div>
          <span style="font-size: 1.8rem; opacity: 0.8;">🕒</span>
        </div>
        <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.5rem;">
          {{ summary?.lastProcessedAt ? '최신 파싱 집계 상태 유지 중' : '집계 이력 없음' }}
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="content-split" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(430px, 1fr)); gap: 1.5rem; align-items: start;">
      
      <!-- Left Column: Track-by-Track Lecture Hours & Statistics Panel (트랙별 수강 시간 강조) -->
      <div class="card">
        <div class="card-header" style="flex-wrap:wrap; gap:0.5rem;">
          <div style="display:flex; align-items:center; gap:0.5rem;">
            <h3 style="font-size: 1.15rem; font-weight: 700; color: #f8fafc; margin: 0;">⏱️ 전체 일정 트랙별 강의 시간</h3>
            <span class="badge info">{{ summary?.trackSummaries?.length || 0 }}개</span>
          </div>

          <!-- Sort View Mode toggle -->
          <div style="display:flex; gap:0.4rem; background:rgba(0,0,0,0.3); padding:0.2rem; border-radius:6px;">
            <button 
              :class="['btn', 'btn-xs', sortMode === 'hours' ? 'btn-primary' : 'btn-outline']"
              style="padding:0.15rem 0.5rem; font-size:0.75rem;"
              @click="sortMode = 'hours'"
            >
              시간순
            </button>
            <button 
              :class="['btn', 'btn-xs', sortMode === 'count' ? 'btn-primary' : 'btn-outline']"
              style="padding:0.15rem 0.5rem; font-size:0.75rem;"
              @click="sortMode = 'count'"
            >
              횟수순
            </button>
          </div>
        </div>
        <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1rem;">
          전체 일정 동안 각 트랙이 차지하는 **총 수강 시간(시간)** 및 전체 대비 시간 비중(%)입니다.
        </p>

        <div v-if="!summary || summary.trackSummaries.length === 0" class="empty-state" style="padding: 3rem 1rem; text-align: center;">
          <span style="font-size: 2.5rem; display: block; margin-bottom: 0.5rem;">📝</span>
          <p style="color: var(--text-muted); font-size: 0.9rem;">등록된 강의 데이터가 없습니다.</p>
          <button class="btn btn-sm btn-primary mt-2" @click="isPasteModalOpen = true">데이터 붙여넣기</button>
        </div>

        <div v-else class="track-list" style="display:flex; flex-direction:column; gap: 0.95rem;">
          <div 
            v-for="t in sortedTrackSummaries" 
            :key="t.trackName"
            class="track-item p-3 rounded border"
            style="background: rgba(15, 23, 42, 0.5); border-color: rgba(255, 255, 255, 0.08);"
          >
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 0.4rem;">
              <div style="display:flex; align-items:center; gap: 0.5rem;">
                <span class="track-tag" style="background: rgba(59, 130, 246, 0.2); color: #93c5fd; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 700;">
                  트랙
                </span>
                <strong style="font-size: 0.95rem; color: #f8fafc;">{{ t.trackName }}</strong>
              </div>
              
              <!-- Total Hours Highlighted -->
              <div style="text-align:right;">
                <span style="font-size: 1.15rem; font-weight: 800; color: #34d399;">총 {{ t.totalHours }}시간</span>
                <span style="font-size: 0.78rem; color: #94a3b8; margin-left: 0.3rem;">({{ t.hoursPercentage || t.percentage }}%)</span>
              </div>
            </div>

            <!-- Progress Bar based on Total Hours -->
            <div class="progress-bar-bg" style="width: 100%; height: 9px; background: rgba(255, 255, 255, 0.1); border-radius: 5px; overflow: hidden; margin-bottom: 0.55rem;">
              <div 
                class="progress-bar-fill"
                :style="{ width: `${t.hoursPercentage || t.percentage}%`, background: getTrackColor(t.trackName) }"
                style="height: 100%; transition: width 0.3s ease;"
              ></div>
            </div>

            <!-- Track Metadata Sub-row -->
            <div style="display:flex; justify-content:space-between; align-items:center; font-size: 0.78rem; color: var(--text-muted); flex-wrap: wrap; gap: 0.4rem;">
              <div>
                👨‍🏫 강사: 
                <span v-for="inst in t.instructors" :key="inst" class="badge" style="margin-left:0.2rem; font-size:0.7rem; background:rgba(255,255,255,0.06);">
                  {{ inst }}
                </span>
              </div>
              <div style="display:flex; gap:0.6rem; align-items:center;">
                <span>📚 총 {{ t.lectureCount }}회 강의</span>
                <span>📍 {{ t.locations.join(', ') || '미지정' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Date Collection Calendar (날짜별 수집 현황 달력) -->
      <div style="display:flex; flex-direction:column; gap:1.5rem;">
        
        <!-- Calendar Card -->
        <div class="card" style="border: 1px solid rgba(59, 130, 246, 0.3);">
          <div class="card-header" style="flex-wrap:wrap; gap:0.5rem;">
            <div style="display:flex; align-items:center; gap:0.5rem;">
              <h3 style="font-size: 1.15rem; font-weight: 700; color: #f8fafc; margin: 0;">📅 수집 일자 달력 확인</h3>
              <span class="badge success">
                {{ Object.keys(dateLectureMap).length }}일간 수집 완료
              </span>
            </div>

            <!-- Month Controls -->
            <div style="display:flex; align-items:center; gap:0.5rem;">
              <button class="btn btn-sm btn-outline" @click="changeMonth(-1)">&lt;</button>
              <strong style="font-size: 0.95rem; color: #60a5fa; min-width: 95px; text-align: center;">
                {{ calendarYear }}년 {{ calendarMonth }}월
              </strong>
              <button class="btn btn-sm btn-outline" @click="changeMonth(1)">&gt;</button>
            </div>
          </div>

          <!-- Quick Month Jump Buttons if multiple months collected -->
          <div v-if="collectedMonthsList.length > 0" style="display:flex; gap:0.4rem; flex-wrap:wrap; margin-bottom: 0.8rem; align-items:center;">
            <span style="font-size:0.78rem; color:#94a3b8;">수집된 달 바로가기:</span>
            <button 
              v-for="m in collectedMonthsList" 
              :key="m.label"
              :class="['btn', 'btn-xs', calendarYear === m.year && calendarMonth === m.month ? 'btn-primary' : 'btn-outline']"
              style="padding:0.15rem 0.45rem; font-size:0.75rem;"
              @click="setCalendarMonth(m.year, m.month)"
            >
              {{ m.label }} ({{ m.count }}일)
            </button>
          </div>

          <p style="font-size: 0.83rem; color: var(--text-muted); margin-bottom: 0.8rem;">
            초록색 뱃지가 표시된 날짜가 정보가 수집되어 저장된 날짜입니다. (날짜 클릭 시 당일 강의 확인)
          </p>

          <!-- Calendar View Component -->
          <div class="calendar-wrapper">
            <!-- Day of week header -->
            <div class="calendar-header-grid">
              <span v-for="day in ['일', '월', '화', '수', '목', '금', '토']" :key="day" :class="['day-header', { sunday: day === '일', saturday: day === '토' }]">
                {{ day }}
              </span>
            </div>

            <!-- Days Grid -->
            <div class="calendar-days-grid">
              <div 
                v-for="(cell, idx) in calendarCells" 
                :key="idx"
                :class="[
                  'calendar-cell',
                  { 
                    'empty-cell': !cell.dateStr,
                    'has-data': cell.lectures.length > 0,
                    'is-selected': selectedDate === cell.dateStr,
                    'is-weekend': cell.dayOfWeekNum === 0 || cell.dayOfWeekNum === 6
                  }
                ]"
                @click="cell.dateStr && selectCalendarDate(cell.dateStr)"
              >
                <div v-if="cell.dateStr" class="cell-content">
                  <span class="day-number">{{ cell.dayNum }}</span>
                  <div v-if="cell.lectures.length > 0" class="badge-container">
                    <span class="collected-badge">
                      ✅ {{ cell.lectures.length }}건
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Legend -->
            <div style="display:flex; gap:1.2rem; align-items:center; justify-content:center; margin-top: 1rem; font-size: 0.78rem; color: var(--text-muted);">
              <div style="display:flex; align-items:center; gap: 0.3rem;">
                <span style="width: 10px; height: 10px; border-radius: 50%; background: #10b981; display: inline-block;"></span>
                <span>수집 완료 날짜</span>
              </div>
              <div style="display:flex; align-items:center; gap: 0.3rem;">
                <span style="width: 10px; height: 10px; border-radius: 50%; background: rgba(255, 255, 255, 0.15); display: inline-block;"></span>
                <span>수집 미진행 날짜</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Selected Date Lecture Summary Details Card -->
        <div v-if="selectedDate" class="card" style="border: 1px solid rgba(16, 185, 129, 0.4); background: rgba(30, 41, 59, 0.5);">
          <div class="card-header mb-2" style="justify-content:space-between; align-items:center;">
            <div style="display:flex; align-items:center; gap:0.5rem;">
              <h4 style="font-size: 1.05rem; font-weight: 700; color: #34d399; margin: 0;">
                📅 {{ selectedDate }} 수집 내역
              </h4>
              <span class="badge info">{{ selectedDateLectures.length }}건</span>
            </div>
            <button class="btn btn-sm btn-outline" style="padding: 0.1rem 0.4rem; font-size: 0.75rem;" @click="selectedDate = null">&times; 닫기</button>
          </div>

          <div v-if="selectedDateLectures.length === 0" style="font-size:0.85rem; color:var(--text-muted); padding: 1rem 0; text-align:center;">
            해당 날짜에 수집된 강의 정보가 없습니다.
          </div>

          <div v-else style="display:flex; flex-direction:column; gap:0.6rem;">
            <div 
              v-for="l in selectedDateLectures" 
              :key="l.id" 
              class="p-2.5 rounded"
              style="background: rgba(15, 23, 42, 0.7); border: 1px solid rgba(255, 255, 255, 0.08); font-size: 0.83rem;"
            >
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 0.25rem;">
                <strong style="color:#60a5fa;">{{ l.subject }}</strong>
                <span class="badge" style="font-size:0.7rem; background:rgba(255,255,255,0.08);">
                  {{ l.startTime }} ~ {{ l.endTime }} ({{ l.duration || '2:00' }})
                </span>
              </div>
              <div style="color:#e2e8f0; margin-bottom:0.2rem;">📖 {{ l.content }}</div>
              <div style="display:flex; justify-content:space-between; color:var(--text-muted); font-size:0.75rem;">
                <span>👨‍🏫 강사: {{ l.instructor }}</span>
                <span>📍 장소: {{ l.location }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Instructor Summary Card -->
        <div class="card">
          <div class="card-header mb-2">
            <h3 style="font-size: 1.1rem; font-weight: 700; color: #f8fafc; margin: 0;">👨‍🏫 강사별 출강 횟수 요약</h3>
          </div>
          <div v-if="summary && Object.keys(summary.instructorCounts).length > 0" style="display:flex; flex-wrap:wrap; gap:0.5rem;">
            <div 
              v-for="(cnt, inst) in summary.instructorCounts" 
              :key="inst"
              style="padding: 0.4rem 0.75rem; background: rgba(15, 23, 42, 0.6); border: 1px solid var(--border-color); border-radius: 8px; display:flex; align-items:center; gap:0.5rem;"
            >
              <span style="font-size:0.85rem; color:#e2e8f0; font-weight:600;">{{ inst }}</span>
              <span class="badge info" style="font-weight:700;">{{ cnt }}회</span>
            </div>
          </div>
          <div v-else style="font-size:0.85rem; color:var(--text-muted);">집계된 강사 정보가 없습니다.</div>
        </div>

      </div>
    </div>

    <!-- Data Paste & Import Modal -->
    <div v-if="isPasteModalOpen" class="modal-backdrop" @click.self="isPasteModalOpen = false">
      <div class="modal-content" style="max-width: 780px; width: 90%;">
        <div class="modal-header">
          <h3 style="font-size: 1.15rem; font-weight: 800; color: #f8fafc; margin: 0; display:flex; align-items:center; gap:0.5rem;">
            <span>📋 라이브 강의 표 데이터 붙여넣기</span>
          </h3>
          <button class="modal-close" @click="isPasteModalOpen = false">&times;</button>
        </div>

        <div class="modal-body mb-3">
          <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.75rem;">
            SSAFY 포털, 엑셀, 노션 등에서 복사한 강의 표 데이터를 그대로 아래 상자에 붙여넣으세요. (Tab/공백 구분 자동 파싱)
          </p>

          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 0.4rem;">
            <label class="form-label" style="margin: 0; font-size: 0.82rem; font-weight: 700; color: #cbd5e1;">
              붙여넣을 데이터 (복사된 텍스트):
            </label>
            <button class="btn btn-sm btn-outline" @click="handleFillSampleData" style="font-size:0.75rem; padding: 0.2rem 0.5rem;">
              ⚡ 예시 표 데이터 채우기
            </button>
          </div>

          <textarea 
            v-model="pasteText"
            class="form-input" 
            rows="10" 
            placeholder="구분	장소	주제	내용	강사명	날짜	요일	방영시간	종료시간	길이
1학기	구미	코딩 Live강의 Mobile 트랙	WEB : HTML5	허태식 강사	2026-07-20	월	9:00	11:00	2:00
1학기	온택트룸4(17층)	코딩 Live강의 Java 전공 트랙	Java : 기본문법	조용준 강사	2026-07-21	화	9:00	11:00	2:00..."
            style="font-family: monospace; font-size: 0.82rem; line-height: 1.4; resize: vertical;"
          ></textarea>

          <div style="display:flex; justify-content:space-between; align-items:center; margin-top: 0.6rem; font-size: 0.8rem;">
            <div style="color: #94a3b8;">
              감지된 줄 수: <strong style="color:#f8fafc;">{{ previewLineCount }}줄</strong>
            </div>

            <div style="display:flex; gap: 1rem; align-items:center;">
              <label style="display:flex; align-items:center; gap:0.3rem; cursor:pointer; color:#e2e8f0;">
                <input type="radio" :value="false" v-model="appendMode" />
                기존 데이터 덮어쓰기
              </label>
              <label style="display:flex; align-items:center; gap:0.3rem; cursor:pointer; color:#e2e8f0;">
                <input type="radio" :value="true" v-model="appendMode" />
                기존 데이터에 추가 (누적)
              </label>
            </div>
          </div>
        </div>

        <div class="modal-footer" style="display:flex; justify-content:flex-end; gap:0.5rem;">
          <button class="btn btn-outline" @click="isPasteModalOpen = false">취소</button>
          <button 
            class="btn btn-primary" 
            @click="handleProcessBulkText"
            :disabled="isProcessing || !pasteText.trim()"
          >
            {{ isProcessing ? '파싱 및 집계 중...' : '🚀 파싱 및 집계 실행' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface TrackSummary {
  trackName: string
  lectureCount: number
  percentage: number
  totalHours: number
  hoursPercentage: number
  instructors: string[]
  locations: string[]
  minDate: string
  maxDate: string
}

interface LiveLecture {
  id: number
  term: string
  location: string
  subject: string
  content: string
  instructor: string
  lectureDate: string
  dayOfWeek: string
  startTime: string
  endTime: string
  duration: string
}

interface SummaryResponse {
  totalLectures: number
  totalTracks: number
  totalHours: number
  minLectureDate: string | null
  maxLectureDate: string | null
  lastProcessedAt: string | null
  trackSummaries: TrackSummary[]
  instructorCounts: Record<string, number>
  locationCounts: Record<string, number>
  lectures: LiveLecture[]
}

const summary = ref<SummaryResponse | null>(null)
const isPasteModalOpen = ref(false)
const pasteText = ref('')
const appendMode = ref(false)
const isProcessing = ref(false)
const sortMode = ref<'hours' | 'count'>('hours')

// Calendar state
const calendarYear = ref(2026)
const calendarMonth = ref(7)
const selectedDate = ref<string | null>(null)

const samplePromptData = `구분\t장소\t주제\t내용\t강사명\t날짜\t요일\t방영시간\t종료시간\t길이
1학기\t구미\t코딩 Live강의 Mobile 트랙\tWEB : HTML5\t허태식 강사\t2026-07-20\t월\t9:00\t11:00\t2:00
1학기\t온택트룸4(17층)\t코딩 Live강의 Java 전공 트랙\tJava : 기본문법\t조용준 강사\t2026-07-20\t월\t9:00\t11:00\t2:00
1학기\t온택트룸6(17층)\t코딩 Live강의 Java 비전공 트랙\tJava : 기본문법&제어문\t양명균 강사\t2026-07-20\t월\t9:00\t11:00\t2:00
1학기\t광주\t코딩 Live강의 Embedded Robot 트랙\tWeb 기초 : HTML / CSS\t이자룡 강사\t2026-07-20\t월\t9:00\t11:00\t2:00
1학기\t대전\t코딩 Live강의 Data 트랙\tweb : HTML & CSS\t김구현 강사\t2026-07-21\t화\t9:00\t11:00\t2:00
1학기\t17층2호\t코딩 Live강의 Python 트랙\tPython : Basic syntax 1\t김준호 강사\t2026-07-21\t화\t9:00\t11:00\t2:00
1학기\t17층1호\t코딩 Live강의 Embedded 트랙\tWeb 기초 : HTML / CSS\t변성은 강사\t2026-07-21\t화\t9:00\t11:00\t2:00
1학기\t온택트룸2(17층)\t데이터 Live강의 마이스터고 트랙\t데이터 사이언스 : RAG\t홍석진 강사\t2026-07-21\t화\t9:00\t11:00\t2:00
1학기\t온택트룸6(17층)\t코딩 Live강의 Java 비전공 트랙\tJava : Git&실습가이드\t양명균 강사\t2026-07-22\t수\t14:00\t16:00\t2:00
1학기\t광주\t코딩 Live강의 Embedded Robot 트랙\tWeb 기초 : Display / Flex\t이자룡 강사\t2026-07-22\t수\t14:00\t16:00\t2:00
1학기\t17층2호\t코딩 Live강의 Python 트랙\tPython : Basic syntax 2\t김준호 강사\t2026-07-22\t수\t14:00\t16:00\t2:00
1학기\t17층1호\t코딩 Live강의 Embedded 트랙\tWeb 기초 : CSS 연습\t변성은 강사\t2026-07-22\t수\t14:00\t16:00\t2:00`

const previewLineCount = computed(() => {
  if (!pasteText.value.trim()) return 0
  return pasteText.value.split(/\r?\n/).filter(line => line.trim().length > 0).length
})

const sortedTrackSummaries = computed(() => {
  if (!summary.value || !summary.value.trackSummaries) return []
  const list = [...summary.value.trackSummaries]
  if (sortMode.value === 'hours') {
    return list.sort((a, b) => b.totalHours - a.totalHours)
  } else {
    return list.sort((a, b) => b.lectureCount - a.lectureCount)
  }
})

// Map from Date string 'YYYY-MM-DD' -> List of LiveLectures
const dateLectureMap = computed(() => {
  const map: Record<string, LiveLecture[]> = {}
  if (!summary.value || !summary.value.lectures) return map

  for (const l of summary.value.lectures) {
    if (l.lectureDate) {
      const d = l.lectureDate.trim()
      if (!map[d]) map[d] = []
      map[d].push(l)
    }
  }
  return map
})

// List of distinct months that have collected dates
const collectedMonthsList = computed(() => {
  const map: Record<string, { year: number; month: number; label: string; count: number }> = {}
  for (const dateStr of Object.keys(dateLectureMap.value)) {
    const parts = dateStr.split('-')
    if (parts.length >= 2) {
      const y = parseInt(parts[0], 10)
      const m = parseInt(parts[1], 10)
      const key = `${y}-${m}`
      if (!map[key]) {
        map[key] = { year: y, month: m, label: `${y}년 ${m}월`, count: 0 }
      }
      map[key].count++
    }
  }
  return Object.values(map).sort((a, b) => (a.year * 100 + a.month) - (b.year * 100 + b.month))
})

// Calendar cells generator for calendarYear & calendarMonth
const calendarCells = computed(() => {
  const year = calendarYear.value
  const month = calendarMonth.value // 1-indexed (1..12)

  const firstDay = new Date(year, month - 1, 1)
  const startDayOfWeek = firstDay.getDay() // 0 = Sun, 6 = Sat
  const daysInMonth = new Date(year, month, 0).getDate()

  const cells: Array<{
    dayNum: number | null
    dateStr: string | null
    dayOfWeekNum: number
    lectures: LiveLecture[]
  }> = []

  // Leading empty cells
  for (let i = 0; i < startDayOfWeek; i++) {
    cells.push({
      dayNum: null,
      dateStr: null,
      dayOfWeekNum: i,
      lectures: []
    })
  }

  // Days of the month
  for (let day = 1; day <= daysInMonth; day++) {
    const mm = String(month).padStart(2, '0')
    const dd = String(day).padStart(2, '0')
    const dateStr = `${year}-${mm}-${dd}`
    const dayOfWeekNum = (startDayOfWeek + day - 1) % 7
    const lectures = dateLectureMap.value[dateStr] || []

    cells.push({
      dayNum: day,
      dateStr,
      dayOfWeekNum,
      lectures
    })
  }

  return cells
})

const selectedDateLectures = computed(() => {
  if (!selectedDate.value) return []
  return dateLectureMap.value[selectedDate.value] || []
})

function changeMonth(delta: number) {
  let m = calendarMonth.value + delta
  let y = calendarYear.value
  if (m > 12) {
    m = 1
    y++
  } else if (m < 1) {
    m = 12
    y--
  }
  calendarMonth.value = m
  calendarYear.value = y
}

function setCalendarMonth(year: number, month: number) {
  calendarYear.value = year
  calendarMonth.value = month
}

function selectCalendarDate(dateStr: string) {
  if (selectedDate.value === dateStr) {
    selectedDate.value = null
  } else {
    selectedDate.value = dateStr
  }
}

async function fetchSummary() {
  try {
    const res = await fetch('/api/lectures/summary')
    if (res.ok) {
      summary.value = await res.json()
      
      // Auto-set calendar month to min/max date if available
      if (summary.value?.minLectureDate) {
        const [yStr, mStr] = summary.value.minLectureDate.split('-')
        if (yStr && mStr) {
          calendarYear.value = parseInt(yStr, 10)
          calendarMonth.value = parseInt(mStr, 10)
        }
      }
    }
  } catch (e) {
    console.error('Failed to fetch lecture summary:', e)
  }
}

async function handleProcessBulkText() {
  if (!pasteText.value.trim()) return
  isProcessing.value = true

  try {
    const res = await fetch('/api/lectures/bulk', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        rawText: pasteText.value,
        append: appendMode.value
      })
    })

    if (!res.ok) {
      const errData = await res.json()
      alert('오류 발생: ' + (errData.message || '파싱 실패'))
      return
    }

    summary.value = await res.json()
    isPasteModalOpen.value = false
    pasteText.value = ''

    if (summary.value?.minLectureDate) {
      const [yStr, mStr] = summary.value.minLectureDate.split('-')
      if (yStr && mStr) {
        calendarYear.value = parseInt(yStr, 10)
        calendarMonth.value = parseInt(mStr, 10)
      }
    }

    alert('✅ 성공적으로 파싱되어 트랙별 강의시간 및 일자별 달력이 업데이트되었습니다!')
  } catch (e) {
    alert('서버 통신 실패: ' + e)
  } finally {
    isProcessing.value = false
  }
}

function handleFillSampleData() {
  pasteText.value = samplePromptData
  isPasteModalOpen.value = true
}

async function handleClearData() {
  if (!confirm('정말로 모든 라이브 강의 데이터를 초기화(삭제)하시겠습니까?')) return

  try {
    const res = await fetch('/api/lectures', { method: 'DELETE' })
    if (res.ok) {
      selectedDate.value = null
      await fetchSummary()
      alert('데이터가 초기화되었습니다.')
    }
  } catch (e) {
    alert('초기화 실패: ' + e)
  }
}

function formatTimestamp(isoStr: string | null | undefined): string {
  if (!isoStr) return '-'
  try {
    const d = new Date(isoStr)
    const yyyy = d.getFullYear()
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    const hh = String(d.getHours()).padStart(2, '0')
    const min = String(d.getMinutes()).padStart(2, '0')
    const ss = String(d.getSeconds()).padStart(2, '0')
    return `${yyyy}-${mm}-${dd} ${hh}:${min}:${ss}`
  } catch (e) {
    return isoStr
  }
}

const colorPalette = [
  '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16', '#6366f1'
]

function getTrackColor(trackName: string): string {
  let hash = 0
  for (let i = 0; i < trackName.length; i++) {
    hash = trackName.charCodeAt(i) + ((hash << 5) - hash)
  }
  const index = Math.abs(hash) % colorPalette.length
  return colorPalette[index]
}

onMounted(() => {
  fetchSummary()
})
</script>

<style scoped>
.lectures-page {
  padding: 1.5rem;
  max-width: 1400px;
  margin: 0 auto;
}
.stat-card {
  transition: transform 0.2s ease, border-color 0.2s ease;
}
.stat-card:hover {
  transform: translateY(-2px);
}

/* Calendar Grid Styles */
.calendar-wrapper {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 1rem;
}
.calendar-header-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  margin-bottom: 0.5rem;
  padding-bottom: 0.4rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.day-header {
  font-size: 0.8rem;
  font-weight: 700;
  color: #94a3b8;
}
.day-header.sunday {
  color: #f87171;
}
.day-header.saturday {
  color: #60a5fa;
}

.calendar-days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.calendar-cell {
  aspect-ratio: 1.1;
  background: rgba(30, 41, 59, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 0.35rem;
  cursor: default;
  position: relative;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.calendar-cell.empty-cell {
  background: transparent;
  border-color: transparent;
}

.calendar-cell.has-data {
  background: rgba(16, 185, 129, 0.12);
  border-color: rgba(16, 185, 129, 0.4);
  cursor: pointer;
}

.calendar-cell.has-data:hover {
  background: rgba(16, 185, 129, 0.25);
  transform: translateY(-1px);
}

.calendar-cell.is-selected {
  border-color: #34d399 !important;
  box-shadow: 0 0 10px rgba(52, 211, 153, 0.4);
  background: rgba(16, 185, 129, 0.3) !important;
}

.day-number {
  font-size: 0.82rem;
  font-weight: 700;
  color: #f8fafc;
}

.is-weekend .day-number {
  color: #cbd5e1;
}

.badge-container {
  margin-top: auto;
}

.collected-badge {
  display: inline-block;
  background: rgba(16, 185, 129, 0.3);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.5);
  font-size: 0.68rem;
  font-weight: 700;
  padding: 0.1rem 0.3rem;
  border-radius: 4px;
  width: 100%;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
