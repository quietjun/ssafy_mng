<template>
  <div class="lectures-page">
    <!-- Top Header -->
    <div class="page-header mb-3">
      <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:1rem;">
        <div>
          <h2 style="font-size: 1.4rem; font-weight: 800; color: #f8fafc; margin-bottom: 0.35rem; display:flex; align-items:center; gap:0.5rem;">
            <span>📺 라이브 강의 트랙별 횟수 집계 & 수집일자 달력</span>
            <span class="badge info" style="font-weight: 600;">관리자 모드</span>
          </h2>
          <p style="font-size: 0.88rem; color: var(--text-muted); margin: 0;">
            매일 공개되는 수집 데이터를 등록하여 한 학기 동안 트랙별 총 진행 강의 횟수(회수) 및 날짜별 수집 완료 상태를 확인합니다.
          </p>
        </div>

        <div style="display:flex; gap:0.6rem; align-items:center; flex-wrap:wrap;">
          <!-- Term Filter Select -->
          <div v-if="summary?.availableTerms && summary.availableTerms.length > 0" style="display:flex; align-items:center; gap:0.4rem;">
            <label style="font-size:0.82rem; color:#94a3b8; font-weight:600;">🎓 학기 선택:</label>
            <select 
              v-model="selectedTerm" 
              @change="handleTermChange"
              style="background: rgba(30, 41, 59, 0.8); color: #f8fafc; border: 1px solid rgba(255, 255, 255, 0.15); padding: 0.35rem 0.6rem; border-radius: 6px; font-size: 0.82rem; cursor: pointer;"
            >
              <option value="">전체 학기</option>
              <option v-for="termOption in summary.availableTerms" :key="termOption" :value="termOption">
                {{ termOption }}
              </option>
            </select>
          </div>

          <button 
            class="btn" 
            :class="isPastePanelOpen ? 'btn-primary' : 'btn-primary'"
            @click="isPastePanelOpen = !isPastePanelOpen" 
            style="display:flex; align-items:center; gap:0.4rem;"
          >
            <span>📋</span> 데이터 붙여넣기 {{ isPastePanelOpen ? '창 닫기' : '및 집계' }}
          </button>
          <button v-if="summary && summary.totalLectures > 0" class="btn btn-danger-outline" @click="handleClearData">
            🗑️ 전체 초기화
          </button>
        </div>
      </div>
    </div>

    <!-- Inline Top Data Paste Card (페이지 상단 즉시 붙여넣기 영역) -->
    <div 
      v-if="isPastePanelOpen || (summary && summary.totalLectures === 0)" 
      class="card mb-4" 
      style="border: 2px solid rgba(99, 102, 241, 0.5); background: rgba(19, 27, 46, 0.95); box-shadow: 0 10px 25px rgba(0,0,0,0.5);"
    >
      <div class="card-header" style="padding-bottom: 0.6rem; margin-bottom: 0.8rem;">
        <div style="display:flex; align-items:center; gap:0.5rem;">
          <h3 style="font-size: 1.1rem; font-weight: 800; color: #a5b4fc; margin: 0; display:flex; align-items:center; gap:0.4rem;">
            <span>📋 상단 데이터 붙여넣기 & 실시간 집계</span>
          </h3>
          <span class="badge info" style="font-size:0.72rem;">스크롤 없이 상단에서 즉시 처리</span>
        </div>
        <button v-if="summary && summary.totalLectures > 0" class="btn btn-sm btn-outline" @click="isPastePanelOpen = false">&times; 접기</button>
      </div>

      <div style="background: rgba(59, 130, 246, 0.12); border: 1px solid rgba(59, 130, 246, 0.3); padding: 0.65rem 0.85rem; border-radius: 8px; font-size: 0.8rem; color: #93c5fd; margin-bottom: 0.8rem; line-height: 1.4;">
        💡 <strong>수시 누적 수집 안내:</strong> 동일한 날짜라도 Java 트랙, Python 트랙 등 매일 공개되는 데이터를 상단 상자에 복사 후 <code>기존 데이터에 추가</code> 상태로 파싱을 실행하면 스크롤 없이 누적 집계됩니다.
      </div>

      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 0.4rem;">
        <label class="form-label" style="margin: 0; font-size: 0.82rem; font-weight: 700; color: #cbd5e1;">
          붙여넣을 데이터 (복사된 텍스트):
        </label>
        <div style="display:flex; gap:0.5rem; align-items:center;">
          <span style="font-size:0.78rem; color:#94a3b8;">감지된 줄 수: <strong style="color:#f8fafc;">{{ previewLineCount }}줄</strong></span>
        </div>
      </div>

      <textarea 
        v-model="pasteText"
        class="form-input mb-3" 
        rows="6" 
        placeholder="구분	장소	주제	내용	강사명	날짜	요일	방영시간	종료시간	길이
1학기	구미	코딩 Live강의 Mobile 트랙	WEB : HTML5	허태식 강사	2026-07-20	월	9:00	11:00	2:00
1학기	온택트룸4(17층)	코딩 Live강의 Java 전공 트랙	Java : 기본문법	조용준 강사	2026-07-21	화	9:00	11:00	2:00..."
        style="font-family: monospace; font-size: 0.82rem; line-height: 1.4; resize: vertical; background: rgba(11, 15, 25, 0.9);"
      ></textarea>

      <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:0.75rem;">
        <div style="display:flex; gap: 1.2rem; align-items:center; font-size: 0.82rem;">
          <label style="display:flex; align-items:center; gap:0.35rem; cursor:pointer; color:#e2e8f0; font-weight:600;">
            <input type="radio" :value="true" v-model="appendMode" />
            <span>기존 데이터에 추가 (누적 수집)</span>
          </label>
          <label style="display:flex; align-items:center; gap:0.35rem; cursor:pointer; color:#e2e8f0;">
            <input type="radio" :value="false" v-model="appendMode" />
            <span>기존 데이터 덮어쓰기</span>
          </label>
        </div>

        <div style="display:flex; gap:0.5rem;">
          <button 
            class="btn btn-primary" 
            @click="handleProcessBulkText"
            :disabled="isProcessing || !pasteText.trim()"
            style="padding: 0.55rem 1.25rem; font-weight: 800; font-size: 0.92rem;"
          >
            {{ isProcessing ? '파싱 및 집계 중...' : '🚀 상단에서 파싱 및 집계 즉시 실행' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Status Overview Stat Cards -->
    <div class="stats-grid mb-4" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.25rem;">
      <!-- 1. 총 진행 강의 횟수 (최우선 주요 지표) -->
      <div class="card stat-card" style="border: 1px solid rgba(16, 185, 129, 0.5); background: rgba(16, 185, 129, 0.08);">
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
          <div>
            <div style="font-size: 0.82rem; color: #6ee7b7; font-weight: 700; margin-bottom: 0.3rem;">🎯 총 진행 강의 횟수</div>
            <div style="font-size: 1.65rem; font-weight: 800; color: #34d399;">
              총 {{ summary?.totalLectures || 0 }}회
            </div>
          </div>
          <span style="font-size: 1.8rem; opacity: 0.9;">📚</span>
        </div>
        <div style="font-size: 0.78rem; color: #a7f3d0; margin-top: 0.5rem;">
          {{ selectedTerm ? `${selectedTerm} 기준 집계 완료` : '전체 수집 기간 집계 중' }}
        </div>
      </div>

      <!-- 2. 개설 트랙 수 -->
      <div class="card stat-card" style="border: 1px solid rgba(245, 158, 11, 0.4); background: rgba(30, 41, 59, 0.4);">
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
          <div>
            <div style="font-size: 0.82rem; color: #94a3b8; font-weight: 600; margin-bottom: 0.3rem;">🎓 개설 트랙 수</div>
            <div style="font-size: 1.45rem; font-weight: 800; color: #fbbf24;">
              {{ summary?.totalTracks || 0 }}개 트랙
            </div>
          </div>
          <span style="font-size: 1.8rem; opacity: 0.8;">🚀</span>
        </div>
        <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.5rem;">
          강사 {{ Object.keys(summary?.instructorCounts || {}).length }}명 참여
        </div>
      </div>

      <!-- 3. 수집 강의 기간 범위 (minDate ~ maxDate) -->
      <div class="card stat-card" style="border: 1px solid rgba(59, 130, 246, 0.4); background: rgba(30, 41, 59, 0.4);">
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
          <div>
            <div style="font-size: 0.82rem; color: #94a3b8; font-weight: 600; margin-bottom: 0.3rem;">📅 수집 강의 기간 범위</div>
            <div style="font-size: 1.1rem; font-weight: 800; color: #60a5fa; word-break: break-all;">
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
          {{ summary?.minLectureDate ? `총 ${Object.keys(dateLectureMap).length}일간 수집 완료` : '등록된 수집 기간이 없습니다.' }}
        </div>
      </div>

      <!-- 4. 전체 일정 총 수강 시간 & 처리 일시 -->
      <div class="card stat-card" style="border: 1px solid rgba(168, 85, 247, 0.4); background: rgba(30, 41, 59, 0.4);">
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
          <div>
            <div style="font-size: 0.82rem; color: #94a3b8; font-weight: 600; margin-bottom: 0.3rem;">⏱️ 전체 수강 시간 & 수집시각</div>
            <div style="font-size: 1.3rem; font-weight: 800; color: #c084fc;">
              {{ summary?.totalHours || 0 }}시간
            </div>
          </div>
          <span style="font-size: 1.8rem; opacity: 0.8;">🕒</span>
        </div>
        <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.5rem;">
          마지막 집계: {{ formatTimestamp(summary?.lastProcessedAt) }}
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="content-split" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); gap: 1.5rem; align-items: start;">
      
      <!-- Left Column: Track-by-Track Lecture Count Statistics Panel (트랙별 횟수 집계 강조) -->
      <div class="card">
        <div class="card-header" style="flex-wrap:wrap; gap:0.5rem;">
          <div style="display:flex; align-items:center; gap:0.5rem;">
            <h3 style="font-size: 1.15rem; font-weight: 700; color: #f8fafc; margin: 0;">🎯 트랙별 강의 진행 횟수 (회수)</h3>
            <span class="badge info">{{ summary?.trackSummaries?.length || 0 }}개 트랙</span>
          </div>

          <!-- Sort View Mode toggle -->
          <div style="display:flex; gap:0.4rem; background:rgba(0,0,0,0.3); padding:0.2rem; border-radius:6px;">
            <button 
              :class="['btn', 'btn-xs', sortMode === 'count' ? 'btn-primary' : 'btn-outline']"
              style="padding:0.15rem 0.55rem; font-size:0.75rem;"
              @click="sortMode = 'count'"
            >
              횟수순
            </button>
            <button 
              :class="['btn', 'btn-xs', sortMode === 'hours' ? 'btn-primary' : 'btn-outline']"
              style="padding:0.15rem 0.55rem; font-size:0.75rem;"
              @click="sortMode = 'hours'"
            >
              시간순
            </button>
          </div>
        </div>
        <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1rem;">
          한 학기 동안 각 트랙별로 진행된 **총 강의 회수(N회)** 및 전체 강의 중 비중(%)입니다.
        </p>

        <div v-if="!summary || summary.trackSummaries.length === 0" class="empty-state" style="padding: 3rem 1rem; text-align: center;">
          <span style="font-size: 2.5rem; display: block; margin-bottom: 0.5rem;">📝</span>
          <p style="color: var(--text-muted); font-size: 0.9rem;">등록된 라이브 강의 데이터가 없습니다.</p>
          <button class="btn btn-sm btn-primary mt-2" @click="isPastePanelOpen = true">데이터 붙여넣기</button>
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
                <strong style="font-size: 1.02rem; color: #f8fafc;">{{ t.trackName }}</strong>
              </div>
              
              <!-- Total Lecture Count Highlighted -->
              <div style="text-align:right;">
                <span style="font-size: 1.25rem; font-weight: 800; color: #34d399;">총 {{ t.lectureCount }}회</span>
                <span style="font-size: 0.82rem; color: #94a3b8; margin-left: 0.35rem; font-weight:600;">({{ t.percentage }}%)</span>
              </div>
            </div>

            <!-- Progress Bar based on Lecture Count Percentage -->
            <div class="progress-bar-bg" style="width: 100%; height: 10px; background: rgba(255, 255, 255, 0.1); border-radius: 5px; overflow: hidden; margin-bottom: 0.6rem;">
              <div 
                class="progress-bar-fill"
                :style="{ width: `${t.percentage}%`, background: getTrackColor(t.trackName) }"
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
                <span>⏱️ {{ t.totalHours }}시간 ({{ t.hoursPercentage }}%)</span>
                <span>📍 {{ t.locations.join(', ') || '미지정' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Date Collection Calendar (날짜별 수집 완료 여부 표시 달력) -->
      <div style="display:flex; flex-direction:column; gap:1.5rem; min-width: 0;">
        
        <!-- Calendar Card -->
        <div class="card" style="border: 1px solid rgba(59, 130, 246, 0.3); overflow: hidden;">
          <div class="card-header" style="flex-wrap:wrap; gap:0.5rem;">
            <div style="display:flex; align-items:center; gap:0.5rem;">
              <h3 style="font-size: 1.15rem; font-weight: 700; color: #f8fafc; margin: 0;">📅 수집일자 달력 (수집 완료 여부)</h3>
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
            초록색 뱃지(**✅ 수집 완료**)가 표시된 날짜가 데이터 수집이 완료된 날짜입니다. (날짜 클릭 시 당일 상세 내역)
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
                    'is-selected': !!selectedDate && selectedDate === cell.dateStr,
                    'is-weekend': cell.dayOfWeekNum === 0 || cell.dayOfWeekNum === 6
                  }
                ]"
                @click="cell.dateStr && selectCalendarDate(cell.dateStr)"
              >
                <div v-if="cell.dateStr" class="cell-content">
                  <div style="display:flex; justify-content:space-between; align-items:center;">
                    <span class="day-number">{{ cell.dayNum }}</span>
                    <span v-if="cell.lectures.length > 0" style="font-size:0.65rem; color:#34d399; font-weight:800;">
                      완료
                    </span>
                  </div>

                  <div v-if="cell.lectures.length > 0" class="badge-container">
                    <span class="collected-badge" :title="getTracksTooltip(cell.lectures)">
                      ✅ {{ cell.lectures.length }}건 수집
                    </span>
                  </div>
                  <div v-else class="badge-container">
                    <span class="uncollected-badge">
                      미수집
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Legend -->
            <div style="display:flex; gap:1.5rem; align-items:center; justify-content:center; margin-top: 1rem; font-size: 0.78rem; color: #94a3b8;">
              <div style="display:flex; align-items:center; gap: 0.4rem;">
                <span style="width: 12px; height: 12px; border-radius: 3px; background: rgba(16, 185, 129, 0.3); border: 1px solid #10b981; display: inline-block;"></span>
                <span style="color:#6ee7b7; font-weight:600;">✅ 수집 완료 날짜</span>
              </div>
              <div style="display:flex; align-items:center; gap: 0.4rem;">
                <span style="width: 12px; height: 12px; border-radius: 3px; background: rgba(30, 41, 59, 0.5); border: 1px solid rgba(255, 255, 255, 0.15); display: inline-block;"></span>
                <span>미수집 날짜</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Selected Date Lecture Summary Details Card -->
        <div v-if="selectedDate" class="card" style="border: 1px solid rgba(16, 185, 129, 0.5); background: rgba(30, 41, 59, 0.6);">
          <div class="card-header mb-2" style="justify-content:space-between; align-items:center;">
            <div style="display:flex; align-items:center; gap:0.5rem;">
              <h4 style="font-size: 1.05rem; font-weight: 700; color: #34d399; margin: 0;">
                📅 {{ selectedDate }} 수집 완료 내역
              </h4>
              <span class="badge success">✅ {{ selectedDateLectures.length }}건 수집됨</span>
            </div>
            <button class="btn btn-sm btn-outline" style="padding: 0.1rem 0.4rem; font-size: 0.75rem;" @click="selectedDate = null">&times; 닫기</button>
          </div>

          <div v-if="selectedDateLectures.length === 0" style="font-size:0.85rem; color:var(--text-muted); padding: 1rem 0; text-align:center;">
            해당 날짜에 수집된 강의 정보가 없습니다. 데이터 붙여넣기를 통해 추가할 수 있습니다.
          </div>

          <div v-else style="display:flex; flex-direction:column; gap:0.6rem;">
            <div 
              v-for="l in selectedDateLectures" 
              :key="l.id" 
              class="p-2.5 rounded"
              style="background: rgba(15, 23, 42, 0.7); border: 1px solid rgba(255, 255, 255, 0.08); font-size: 0.83rem;"
            >
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 0.25rem;">
                <div style="display:flex; align-items:center; gap:0.4rem;">
                  <span class="badge primary" style="font-size:0.72rem; font-weight:700;">
                    {{ cleanTrackName(l.subject) }}
                  </span>
                  <strong style="color:#f8fafc;">{{ l.subject }}</strong>
                </div>
                <span class="badge" style="font-size:0.7rem; background:rgba(255,255,255,0.08);">
                  {{ l.startTime }} ~ {{ l.endTime }} ({{ l.duration || '2:00' }})
                </span>
              </div>
              <div style="color:#e2e8f0; margin-bottom:0.2rem; margin-top:0.2rem;">📖 {{ l.content }}</div>
              <div style="display:flex; justify-content:space-between; color:var(--text-muted); font-size:0.75rem; margin-top:0.3rem;">
                <span>👨‍🏫 강사: {{ l.instructor || '미지정' }}</span>
                <span>📍 장소: {{ l.location || '미지정' }}</span>
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
  availableTerms?: string[]
  selectedTerm?: string | null
  trackSummaries: TrackSummary[]
  instructorCounts: Record<string, number>
  locationCounts: Record<string, number>
  lectures: LiveLecture[]
}

const summary = ref<SummaryResponse | null>(null)
const isPastePanelOpen = ref(false) // 상단 즉시 붙여넣기 영역 열림 상태
const pasteText = ref('')
const appendMode = ref(true) // 기본값: 수시 누적 모드
const isProcessing = ref(false)
const sortMode = ref<'count' | 'hours'>('count') // 기본값: 횟수순 정렬
const selectedTerm = ref<string>('')

// Calendar state
const calendarYear = ref(2026)
const calendarMonth = ref(7)
const selectedDate = ref<string | null>(null)



const previewLineCount = computed(() => {
  if (!pasteText.value.trim()) return 0
  return pasteText.value.split(/\r?\n/).filter(line => line.trim().length > 0).length
})

const sortedTrackSummaries = computed(() => {
  if (!summary.value || !summary.value.trackSummaries) return []
  const list = [...summary.value.trackSummaries]
  if (sortMode.value === 'count') {
    return list.sort((a, b) => b.lectureCount - a.lectureCount)
  } else {
    return list.sort((a, b) => b.totalHours - a.totalHours)
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

function cleanTrackName(subject: string | null | undefined): string {
  if (!subject) return '미지정 트랙'
  let s = subject.trim()
  if (s.includes('Live강의')) {
    const idx = s.indexOf('Live강의')
    s = s.substring(idx + 'Live강의'.length).trim()
  } else if (s.startsWith('코딩 ')) {
    s = s.substring('코딩 '.length).trim()
  } else if (s.startsWith('데이터 ')) {
    s = s.substring('데이터 '.length).trim()
  }
  return s || subject.trim()
}

function getTracksTooltip(lectures: LiveLecture[]): string {
  const tracks = lectures.map(l => cleanTrackName(l.subject))
  return tracks.join(', ')
}

async function fetchSummary(term?: string) {
  try {
    const url = term ? `/api/lectures/summary?term=${encodeURIComponent(term)}` : '/api/lectures/summary'
    const res = await fetch(url)
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

function handleTermChange() {
  fetchSummary(selectedTerm.value)
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
    pasteText.value = ''

    if (summary.value?.minLectureDate) {
      const [yStr, mStr] = summary.value.minLectureDate.split('-')
      if (yStr && mStr) {
        calendarYear.value = parseInt(yStr, 10)
        calendarMonth.value = parseInt(mStr, 10)
      }
    }

    alert('✅ 성공적으로 파싱되어 트랙별 강의 횟수 및 날짜별 수집 완료 상태가 업데이트되었습니다!')
  } catch (e) {
    alert('서버 통신 실패: ' + e)
  } finally {
    isProcessing.value = false
  }
}



async function handleClearData() {
  if (!confirm('정말로 모든 라이브 강의 데이터를 초기화(삭제)하시겠습니까?')) return

  try {
    const res = await fetch('/api/lectures', { method: 'DELETE' })
    if (res.ok) {
      selectedDate.value = null
      await fetchSummary(selectedTerm.value)
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
  box-sizing: border-box;
  width: 100%;
}

.calendar-header-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
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
  width: 100%;
  box-sizing: border-box;
}

.calendar-cell {
  aspect-ratio: 1.1;
  background: rgba(30, 41, 59, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  padding: 0.35rem;
  cursor: default;
  position: relative;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
  min-width: 0;
}

.calendar-cell.empty-cell {
  background: transparent !important;
  border: 1px solid transparent !important;
  box-shadow: none !important;
  pointer-events: none;
  visibility: hidden;
}

.calendar-cell.has-data {
  background: rgba(16, 185, 129, 0.15);
  border: 1px solid rgba(16, 185, 129, 0.4);
  cursor: pointer;
}

.calendar-cell.has-data:hover {
  background: rgba(16, 185, 129, 0.28);
  transform: translateY(-1px);
}

.calendar-cell.is-selected {
  border-color: #34d399 !important;
  box-shadow: 0 0 10px rgba(52, 211, 153, 0.5) !important;
  background: rgba(16, 185, 129, 0.32) !important;
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
  box-sizing: border-box;
}

.uncollected-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.03);
  color: #64748b;
  font-size: 0.65rem;
  padding: 0.1rem 0.3rem;
  border-radius: 4px;
  width: 100%;
  text-align: center;
  box-sizing: border-box;
}
</style>
