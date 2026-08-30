<template>
  <div class="assignment-page">
    <div class="content-split">
      <!-- Left: Problem List & Admin Create -->
      <div class="split-col left-col">
        <div class="card">
          <div class="card-header">
            <h3>{{ authStore.isAdmin ? '📅 일자별 문제 목록 (오늘 기준 ±3일)' : '📅 최근 1주일 과제 & 워크샵' }}</h3>
          </div>

          <!-- Admin Create Problem Accordion -->
          <div v-if="authStore.isAdmin" class="admin-problem-create-box mb-3">
            <button 
              class="btn btn-sm btn-primary w-100" 
              @click="isCreating = !isCreating"
            >
              {{ isCreating ? '✖️ 등록 닫기' : '+ 문제 등록 (관리자)' }}
            </button>

            <form v-if="isCreating" @submit.prevent="handleCreateProblem" class="sub-form mt-2">
              <div class="form-group">
                <label class="form-label">문제 날짜</label>
                <input 
                  type="date" 
                  v-model="newProblem.problemDate" 
                  class="form-input" 
                  @click="triggerDatePicker"
                  required
                />
              </div>

              <div class="form-group">
                <label class="form-label">문제 구분</label>
                <div class="radio-toggle-group">
                  <label class="radio-badge-label">
                    <input type="radio" v-model="newProblem.problemType" value="과제">
                    <span class="type-tag tag-hw">📘 과제 (Homework)</span>
                  </label>
                  <label class="radio-badge-label">
                    <input type="radio" v-model="newProblem.problemType" value="워크샵">
                    <span class="type-tag tag-ws">🛠️ 워크샵 (Workshop)</span>
                  </label>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">문제 출처 사이트</label>
                <select v-model="newProblem.platformName" class="form-select" required>
                  <option value="">-- 출처 사이트 선택 --</option>
                  <option v-for="p in problemStore.platforms" :key="p.id" :value="p.name">
                    {{ p.name }} {{ p.url ? `(${p.url})` : '' }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label">문제 이름 / 번호</label>
                <input 
                  type="text" 
                  v-model="newProblem.title" 
                  class="form-input" 
                  placeholder="예: 2072. 홀수만 더하기 (D1)" 
                  required
                />
              </div>

              <div class="form-group">
                <label class="form-label">추가 설명 / 비고 (선택사항)</label>
                <textarea 
                  v-model="newProblem.description" 
                  class="form-textarea" 
                  rows="2" 
                  placeholder="특이사항이나 추가 안내가 있을 경우 작성"
                ></textarea>
              </div>

              <div style="display:flex; gap:0.4rem;">
                <button type="submit" class="btn btn-sm btn-success" style="flex:1;">등록하기</button>
                <button type="button" class="btn btn-sm btn-outline" @click="isCreating = false">취소</button>
              </div>
            </form>
          </div>

          <!-- Problem List Grouped by Date (Admin: ±3 days, Student: 1 week) -->
          <div class="problem-list">
            <div v-if="problemStore.isLoading" class="empty-state">
              문제를 불러오는 중...
            </div>
            <div v-else-if="dateGroupedProblems.length === 0" class="empty-state">
              해당 기간에 등록된 문제가 없습니다.
            </div>

            <!-- Grouped by Date -->
            <template v-else>
              <div v-for="group in dateGroupedProblems" :key="group.date">
                <div class="problem-date-group-header">
                  <span>📅 {{ group.date }} {{ group.date === todayStr ? '⭐ (오늘)' : '' }}</span>
                  <span style="font-size:0.75rem; color:#94a3b8; font-weight:normal;">
                    과제 {{ group.hwCount }}개 · 워크샵 {{ group.wsCount }}개
                  </span>
                </div>

                <div 
                  v-for="p in group.items" 
                  :key="p.id"
                  :class="['problem-item', { active: problemStore.selectedProblem?.id === p.id }]"
                  @click="problemStore.selectProblem(p)"
                >
                  <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:0.5rem;">
                    <div class="problem-title" style="flex:1;">
                      <span :class="['problem-type-badge', p.problemType === '워크샵' ? 'badge-ws' : 'badge-hw']" style="margin-right:4px;">
                        {{ p.problemType || '과제' }}
                      </span>
                      <span v-if="p.platformName" class="ai-chip chip-time" style="font-size:0.75rem; padding:0.15rem 0.4rem; margin-right:4px;">
                        {{ p.platformName }}
                      </span>
                      {{ p.title }}
                    </div>

                    <!-- Admin: Submission count badge -->
                    <span v-if="authStore.isAdmin" class="ai-chip chip-complexity" style="font-size:0.75rem; padding:0.15rem 0.45rem;">
                      제출 {{ p.submissionCount || 0 }}건
                    </span>

                    <!-- Student: Submission status tag -->
                    <span v-else :class="['problem-status-tag', (p.isSubmittedByMe || p.submittedByMe) ? 'status-submitted' : 'status-unsubmitted']">
                      {{ (p.isSubmittedByMe || p.submittedByMe) ? `✅ 제출완료 ${p.myResultStatus ? '(' + p.myResultStatus + ')' : ''}` : '⏳ 미제출' }}
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- Right: Problem Detail & 2-Step Submission / Dashboard -->
      <div class="split-col right-col">
        <div class="card">
          <!-- Card Header -->
          <div class="card-header">
            <div style="display:flex; align-items:center; gap:0.5rem; flex-wrap:wrap;">
              <template v-if="problemStore.selectedProblem">
                <span :class="['problem-type-badge', problemStore.selectedProblem.problemType === '워크샵' ? 'badge-ws' : 'badge-hw']">
                  {{ problemStore.selectedProblem.problemType || '과제' }}
                </span>
                <span v-if="problemStore.selectedProblem.platformName" class="ai-chip chip-time">
                  {{ problemStore.selectedProblem.platformName }}
                </span>
                <h3>{{ problemStore.selectedProblem.title }}</h3>
                <span class="badge">{{ problemStore.selectedProblem.problemDate }}</span>
                <a 
                  v-if="problemStore.selectedProblem.platformUrl" 
                  :href="problemStore.selectedProblem.platformUrl" 
                  target="_blank" 
                  rel="noopener noreferrer" 
                  class="btn btn-sm btn-outline" 
                  style="text-decoration:none;"
                >
                  🔗 사이트 이동
                </a>
              </template>
              <h3 v-else>문제를 선택해 주세요</h3>
            </div>

            <!-- Admin Actions -->
            <div v-if="authStore.isAdmin && problemStore.selectedProblem" class="admin-problem-actions">
              <button class="btn btn-sm btn-outline" @click="handleToggleEdit">
                ✏️ 문제 수정
              </button>
              <button class="btn btn-sm btn-danger-outline" @click="handleDeleteProblem">
                🗑️ 삭제
              </button>
            </div>
          </div>

          <!-- Problem Description (if note exists) -->
          <div v-if="cleanDescription" class="problem-desc-box mb-3" v-html="cleanDescription"></div>

          <!-- Admin Edit Form -->
          <form v-if="isEditing && problemStore.selectedProblem" @submit.prevent="handleUpdateProblem" class="sub-form mb-3">
            <div class="form-group">
              <label class="form-label">문제 날짜</label>
              <input type="date" v-model="editProblem.problemDate" class="form-input" @click="triggerDatePicker" required>
            </div>
            <div class="form-group">
              <label class="form-label">문제 구분</label>
              <div class="radio-toggle-group">
                <label class="radio-badge-label">
                  <input type="radio" v-model="editProblem.problemType" value="과제">
                  <span class="type-tag tag-hw">📘 과제</span>
                </label>
                <label class="radio-badge-label">
                  <input type="radio" v-model="editProblem.problemType" value="워크샵">
                  <span class="type-tag tag-ws">🛠️ 워크샵</span>
                </label>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">문제 출처 사이트</label>
              <select v-model="editProblem.platformName" class="form-select" required>
                <option value="">-- 출처 사이트 선택 --</option>
                <option v-for="p in problemStore.platforms" :key="p.id" :value="p.name">
                  {{ p.name }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">문제 이름 / 번호</label>
              <input type="text" v-model="editProblem.title" class="form-input" required>
            </div>

            <div class="form-group">
              <label class="form-label">추가 설명 / 비고</label>
              <textarea v-model="editProblem.description" class="form-textarea" rows="2"></textarea>
            </div>

            <div style="display:flex; gap:0.5rem;">
              <button type="submit" class="btn btn-sm btn-success">💾 저장하기</button>
              <button type="button" class="btn btn-sm btn-outline" @click="isEditing = false">취소</button>
            </div>
          </form>

          <!-- 1) ADMIN DASHBOARD -->
          <div v-if="authStore.isAdmin && problemStore.selectedProblem" class="admin-problem-dash mt-3">
            <div class="admin-dash-header">
              <h4>📊 학생 과제 제출 실시간 현황</h4>
              <span class="badge">{{ stats.submittedCount }} / {{ stats.totalStudents }}명 제출</span>
            </div>
            
            <div class="stats-row mt-2">
              <div class="stat-card success">
                <div class="stat-num">{{ stats.submittedCount }}</div>
                <div class="stat-label">제출 완료</div>
              </div>
              <div class="stat-card warning">
                <div class="stat-num">{{ stats.unsubmittedCount }}</div>
                <div class="stat-label">미제출</div>
              </div>
              <div class="stat-card">
                <div class="stat-num" style="color:var(--success);">{{ stats.passCount }}</div>
                <div class="stat-label">Pass</div>
              </div>
              <div class="stat-card">
                <div class="stat-num" style="color:var(--danger);">{{ stats.failCount }}</div>
                <div class="stat-label">Fail / 기타</div>
              </div>
            </div>
          </div>

          <!-- 2) STUDENT 2-STEP SUBMISSION WORKFLOW -->
          <div v-if="!authStore.isAdmin && problemStore.selectedProblem" class="student-submission-box mt-3">
            <!-- Step Indicator -->
            <div class="step-indicator-bar">
              <div :class="['step-item', { active: submissionStep === 1 }]">
                <span class="step-badge">1</span>
                <span>1단계: AI 분석 및 검수</span>
              </div>
              <span style="color:var(--border-color);">▶</span>
              <div :class="['step-item', { active: submissionStep === 2 }]">
                <span class="step-badge">2</span>
                <span>2단계: 검수 확인 & 최종 제출</span>
              </div>
            </div>

            <!-- STEP 1: Code & Capture Upload for AI Inspection -->
            <div v-if="submissionStep === 1">
              <div class="code-tabs">
                <button 
                  type="button" 
                  :class="['code-tab-btn', { active: inputMode === 'paste' }]"
                  @click="inputMode = 'paste'"
                >
                  📝 코드 직접 붙여넣기
                </button>
                <button 
                  type="button" 
                  :class="['code-tab-btn', { active: inputMode === 'file' }]"
                  @click="inputMode = 'file'"
                >
                  📁 Java 파일 첨부
                </button>
              </div>

              <div v-if="inputMode === 'paste'" class="mt-2">
                <textarea 
                  v-model="sourceCode" 
                  class="code-textarea" 
                  placeholder="// 여기에 제출할 Java 소스코드를 붙여넣으세요..."
                ></textarea>
              </div>
              <div v-else class="mt-2">
                <div class="file-dropzone" @click="fileInput?.click()">
                  <input ref="fileInput" type="file" accept=".java" class="hidden" @change="handleFileChange">
                  <div class="dropzone-text">
                    {{ attachedFileName || '📂 클릭하여 .java 파일을 첨부하세요' }}
                  </div>
                </div>
              </div>

              <!-- Result Capture Image Upload OR Manual Text Metrics Input -->
              <div class="form-group mt-3">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem; flex-wrap:wrap; gap:0.4rem;">
                  <label class="form-label mb-0">📊 채점 결과 입력 방식 선택</label>
                  <div class="radio-toggle-group" style="margin-bottom:0;">
                    <label class="radio-badge-label">
                      <input type="radio" v-model="captureMode" value="paste_image">
                      <span class="type-tag tag-hw">🖼️ 캡처 이미지 (클릭 or Ctrl+V)</span>
                    </label>
                    <label class="radio-badge-label">
                      <input type="radio" v-model="captureMode" value="manual_text">
                      <span class="type-tag tag-ws">⌨️ 시간/메모리 직접 입력</span>
                    </label>
                  </div>
                </div>

                <!-- 1) Image Dropzone & Ctrl+V Paste -->
                <div v-if="captureMode === 'paste_image'" class="file-dropzone image-dropzone" @click="imageInput?.click()">
                  <input ref="imageInput" type="file" accept="image/*" class="hidden" @change="handleImageChange">
                  <div v-if="!imagePreviewUrl" class="dropzone-text">
                    🖼️ 클릭하여 이미지 첨부 또는 <strong>화면 캡처 후 Ctrl+V 로 바로 붙여넣기</strong>
                  </div>
                  <div v-else class="capture-preview">
                    <img :src="imagePreviewUrl" alt="Capture Preview" style="max-height:130px; border-radius:6px;">
                    <button type="button" class="btn btn-sm btn-outline mt-1" @click.stop="imagePreviewUrl = ''; attachedImage = null">삭제</button>
                  </div>
                </div>

                <!-- 2) Manual Text Inputs (Execution time, Memory, Code length is optional) -->
                <div v-else class="manual-metrics-grid" style="display:grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 0.5rem; background:rgba(0,0,0,0.2); padding:0.75rem; border-radius:var(--radius-md); border:1px solid var(--border-color);">
                  <div>
                    <span class="meta-label">결과 상태</span>
                    <select v-model="manualMetrics.resultStatus" class="form-select form-input-sm">
                      <option value="Pass">Pass (맞았습니다)</option>
                      <option value="Fail">Fail (오답)</option>
                      <option value="TimeLimit">시간 초과</option>
                    </select>
                  </div>
                  <div>
                    <span class="meta-label">실행 시간 *</span>
                    <input v-model="manualMetrics.executionTime" type="text" class="form-input form-input-sm" placeholder="예: 120 ms">
                  </div>
                  <div>
                    <span class="meta-label">메모리 사용량 *</span>
                    <input v-model="manualMetrics.memoryUsage" type="text" class="form-input form-input-sm" placeholder="예: 24 MB / 396 kb">
                  </div>
                  <div>
                    <span class="meta-label">코드 길이 (선택)</span>
                    <input v-model="manualMetrics.codeLength" type="text" class="form-input form-input-sm" placeholder="예: 1024 B (생략 가능)">
                  </div>
                </div>
              </div>

              <button 
                type="button" 
                class="btn btn-primary btn-lg w-100 mt-3" 
                :disabled="isInspecting" 
                @click="handleInspectWithAi"
              >
                {{ isInspecting ? '🤖 Gemini AI가 소스코드와 채점 결과를 정밀 분석 중입니다...' : '🤖 1단계: AI 분석 및 검수 시작' }}
              </button>
            </div>

            <!-- STEP 2: Review, Edit Extracted Text & Final Submit -->
            <div v-else-if="submissionStep === 2" class="step2-container">
              <div class="ai-review-banner mb-3">
                <span style="font-size:1.4rem;">💡</span>
                <div>
                  <strong>AI 분석 완료!</strong> 캡처 이미지에서 추출된 채점 데이터와 AI 분석 결과를 확인하세요. 필요 시 수정한 후 최종 제출할 수 있습니다.
                </div>
              </div>

              <!-- Extracted Score & Performance Metrics Grid -->
              <div class="form-group">
                <label class="form-label">📊 채점 및 성능 분석 결과 (수정 가능)</label>
                <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 0.5rem;">
                  <div>
                    <span class="meta-label">결과 상태</span>
                    <input v-model="step2Data.resultStatus" type="text" class="form-input form-input-sm" placeholder="Pass">
                  </div>
                  <div>
                    <span class="meta-label">실행 시간</span>
                    <input v-model="step2Data.executionTime" type="text" class="form-input form-input-sm" placeholder="4 ms">
                  </div>
                  <div>
                    <span class="meta-label">메모리 사용량</span>
                    <input v-model="step2Data.memoryUsage" type="text" class="form-input form-input-sm" placeholder="396 kb">
                  </div>
                  <div>
                    <span class="meta-label">코드 길이</span>
                    <input v-model="step2Data.codeLength" type="text" class="form-input form-input-sm" placeholder="204 B">
                  </div>
                  <div>
                    <span class="meta-label">시간 복잡도</span>
                    <input v-model="step2Data.aiTimeComplexity" type="text" class="form-input form-input-sm" placeholder="O(N log N)">
                  </div>
                </div>
              </div>

              <!-- AI Key Idea -->
              <div class="form-group mt-3">
                <label class="form-label">💡 풀이 핵심 및 알고리즘 설명 (수정 가능)</label>
                <textarea 
                  v-model="step2Data.aiKeyIdea" 
                  class="form-textarea" 
                  rows="3" 
                  placeholder="풀이 핵심 아이디어 설명"
                ></textarea>
              </div>

              <!-- Keywords Extracted -->
              <div class="form-group mt-3">
                <label class="form-label">🏷️ AI 추출 핵심 키워드 (최대 10개)</label>
                <div class="keyword-tags-box">
                  <div v-for="(kw, idx) in step2Data.keywords" :key="idx" class="keyword-tag-badge">
                    <span>#{{ kw }}</span>
                    <span class="keyword-tag-remove" @click="removeKeyword(idx)">&times;</span>
                  </div>
                  <div style="display:inline-flex; gap:0.3rem; align-items:center;">
                    <input 
                      v-model="newKeywordInput" 
                      type="text" 
                      class="form-input form-input-sm" 
                      placeholder="+ 키워드 추가" 
                      style="width: 110px; height: 26px; font-size:0.75rem; padding:0 0.4rem;"
                      @keyup.enter="addKeyword"
                    >
                    <button type="button" class="btn btn-sm btn-outline" style="height:26px; padding:0 0.5rem;" @click="addKeyword">추가</button>
                  </div>
                </div>
              </div>

              <!-- Action Buttons -->
              <div style="display:flex; gap:0.5rem;" class="mt-4">
                <button type="button" class="btn btn-outline" style="flex:1;" @click="submissionStep = 1">
                  ↺ 1단계로 돌아가기
                </button>
                <button 
                  type="button" 
                  class="btn btn-primary btn-lg" 
                  style="flex:2;" 
                  :disabled="isSubmittingFinal" 
                  @click="handleFinalSubmit"
                >
                  {{ isSubmittingFinal ? '저장 중...' : '🚀 2단계: 최종 과제 제출하기' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 3) ADMIN ONLY: UN-SUBMITTED STUDENTS LIST -->
        <div v-if="authStore.isAdmin && problemStore.selectedProblem" class="card mt-3">
          <div class="unsubmitted-box">
            <div class="unsubmitted-header">
              <span>⏳ 미제출 학생 명단 ({{ unsubmittedStudents.length }}명)</span>
              <span v-if="unsubmittedStudents.length === 0" class="all-submitted-badge">
                🎉 전원 제출 완료!
              </span>
            </div>
            <div v-if="unsubmittedStudents.length > 0" class="unsubmitted-list">
              <span v-for="st in unsubmittedStudents" :key="st.sno" class="unsubmitted-chip">
                {{ st.name }} ({{ st.sno }})
              </span>
            </div>
          </div>
        </div>

        <!-- 4) PEER SOLUTIONS & AI REVIEW TABLE (ALL SUBMISSIONS HISTORY) -->
        <div v-if="problemStore.selectedProblem" class="card mt-3">
          <div class="card-header">
            <h3>💡 학생 제출 풀이 현황 <span class="badge">총 {{ peerSubmissions.length }}건 이력</span></h3>
            <button class="btn btn-sm btn-outline" @click="loadPeerSubmissions">🔄 새로고침</button>
          </div>

          <div class="table-responsive mt-2">
            <table class="data-table peer-table" style="table-layout: fixed; width: 100%;">
              <thead>
                <tr>
                  <th @click="sortBy('studentName')" class="sortable-th" style="width: 17%;">제출자 ↕</th>
                  <th @click="sortBy('submittedAt')" class="sortable-th" style="width: 14%;">제출시각 ↕</th>
                  <th @click="sortBy('resultStatus')" class="sortable-th" style="width: 11%; text-align:center;">결과 ↕</th>
                  <th @click="sortBy('executionTime')" class="sortable-th" style="width: 12%; text-align:center;">실행시간(ms) ↕</th>
                  <th @click="sortBy('memoryUsage')" class="sortable-th" style="width: 12%; text-align:center;">메모리(KB) ↕</th>
                  <th @click="sortBy('codeLength')" class="sortable-th" style="width: 10%; text-align:center;">길이(B) ↕</th>
                  <th style="width: 12%; text-align:center;">시간 복잡도</th>
                  <th style="width: 12%; text-align:center;">코드 보기</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="peerSubmissions.length === 0">
                  <td colspan="8" style="text-align:center;" class="empty-state">제출된 풀이가 없습니다.</td>
                </tr>
                <tr v-for="s in sortedSubmissions" :key="s.id">
                  <td><strong>{{ s.studentName }}</strong> ({{ s.studentSno || s.sno }})</td>
                  <td style="color:#94a3b8; font-size:0.82rem;">{{ formatShortDateTime(s.submittedAt) }}</td>
                  <td style="text-align:center;">
                    <span :class="['ai-chip', s.resultStatus === 'Pass' ? 'chip-pass' : 'chip-fail']" style="padding: 0.15rem 0.45rem; font-size: 0.75rem;">
                      {{ s.resultStatus || 'Pass' }}
                    </span>
                  </td>
                  <td style="text-align:center;"><span class="ai-chip chip-time" style="padding: 0.15rem 0.45rem; font-size: 0.75rem;">{{ stripUnit(s.executionTime) }}</span></td>
                  <td style="text-align:center;"><span class="ai-chip chip-mem" style="padding: 0.15rem 0.45rem; font-size: 0.75rem;">{{ stripUnit(s.memoryUsage) }}</span></td>
                  <td style="text-align:center; font-size:0.82rem;">{{ stripUnit(s.codeLength) }}</td>
                  <td style="text-align:center;"><span class="ai-chip chip-complexity" style="padding: 0.15rem 0.45rem; font-size: 0.75rem;">{{ s.aiTimeComplexity || '-' }}</span></td>
                  <td style="text-align:center;">
                    <button class="btn btn-sm btn-outline" style="padding: 0.2rem 0.5rem; font-size: 0.78rem;" @click="openCodeModal(s)">🔍 코드</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Large Fullscreen Code Viewer Modal -->
    <div v-if="modalSubmission" class="modal-overlay" @click.self="modalSubmission = null">
      <div class="modal modal-code-fullscreen">
        <div class="modal-header" style="padding-bottom: 0.75rem; border-bottom: 1px solid var(--border-color); display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:0.5rem;">
          <div style="display:flex; align-items:center; gap:0.6rem; flex-wrap:wrap;">
            <h3>🔍 {{ modalSubmission.studentName }} ({{ modalSubmission.studentSno }}) 학생의 풀이 코드</h3>
            <span :class="['ai-chip', modalSubmission.resultStatus === 'Pass' ? 'chip-pass' : 'chip-fail']">
              {{ modalSubmission.resultStatus || 'Pass' }}
            </span>
            <span class="ai-chip chip-time">{{ modalSubmission.executionTime || '-' }}</span>
            <span class="ai-chip chip-mem">{{ modalSubmission.memoryUsage || '-' }}</span>
            <span class="ai-chip chip-complexity">{{ modalSubmission.aiTimeComplexity || '-' }}</span>
          </div>
          <div style="display:flex; align-items:center; gap:0.5rem;">
            <button 
              type="button" 
              class="btn btn-sm btn-outline" 
              style="font-size:0.8rem; padding:0.25rem 0.6rem; display:flex; align-items:center; gap:0.3rem;"
              @click="showModalSummary = !showModalSummary"
            >
              <span>{{ showModalSummary ? '🔼 상단 AI 요약 접기' : '🔽 상단 AI 요약 펼치기' }}</span>
            </button>
            <button class="modal-close" style="position:static;" @click="modalSubmission = null">&times;</button>
          </div>
        </div>

        <div class="modal-body">
          <div class="modal-code-layout">
            <!-- Row 1: Top Summary Box (Explain + Keywords) - Toggleable -->
            <transition name="fade">
              <div v-if="showModalSummary" class="modal-top-summary">
                <div class="modal-top-explain">
                  <div class="explain-header" style="font-weight:700; color:#38bdf8; font-size:0.88rem; margin-bottom:0.2rem;">
                    💡 AI 코드 요약 & 풀이 핵심
                  </div>
                  <div style="line-height: 1.6; font-size: 0.88rem; color: #e2e8f0;">
                    {{ modalSubmission.aiKeyIdea || modalSubmission.aiFeedback || '풀이 요약 분석을 불러오는 중입니다...' }}
                  </div>
                  <div style="font-size: 0.8rem; color: var(--text-muted); display: flex; gap: 1rem; flex-wrap: wrap; margin-top: 0.4rem; padding-top: 0.4rem; border-top: 1px dashed rgba(255,255,255,0.1);">
                    <span><strong>파일명:</strong> {{ modalSubmission.originalFileName || 'Solution.java' }}</span>
                    <span><strong>제출시각:</strong> {{ modalSubmission.submittedAt ? modalSubmission.submittedAt.replace('T', ' ') : '-' }}</span>
                    <span><strong>코드 길이:</strong> {{ modalSubmission.codeLength || '-' }}</span>
                  </div>
                </div>

                <!-- Keywords on the Right -->
                <div v-if="modalKeywords.length > 0" class="modal-top-keywords">
                  <label class="form-label" style="margin-bottom: 0.3rem; font-size:0.82rem;">🏷️ 핵심 알고리즘 키워드 (최대 10개)</label>
                  <div class="keyword-tags-box" style="padding: 0.5rem; background: rgba(0,0,0,0.3);">
                    <div v-for="(kw, idx) in modalKeywords" :key="idx" class="keyword-tag-badge" style="font-size:0.78rem; padding:0.15rem 0.45rem;">
                      #{{ kw }}
                    </div>
                  </div>
                </div>
              </div>
            </transition>

            <!-- Row 2: Full Width Large Code Editor with Syntax Highlighting & Line Numbers -->
            <div class="modal-code-main">
              <div class="code-view-container">
                <div class="code-header">
                  <span style="font-weight: 700; color: #94a3b8; font-size: 0.88rem;">☕ Java Source Code (총 {{ highlightedModalLines.length }}줄)</span>
                  <button class="btn btn-sm btn-outline" @click="copyCode">📋 코드 전체 복사</button>
                </div>
                <pre class="code-block" :style="{ maxHeight: showModalSummary ? '58vh' : '75vh' }"><code class="hljs language-java"><div v-for="(line, idx) in highlightedModalLines" :key="idx" class="code-line-row"><span class="line-number">{{ Number(idx) + 1 }}</span><span class="line-content" v-html="line || '&nbsp;'"></span></div></code></pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProblemStore, type ProblemItem } from '@/stores/problems'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import 'highlight.js/styles/atom-one-dark.css'

hljs.registerLanguage('java', java)

const router = useRouter()
const authStore = useAuthStore()
const problemStore = useProblemStore()

const todayStr = new Date().toISOString().split('T')[0]
const selectedDate = ref(todayStr)
const isCreating = ref(false)
const isEditing = ref(false)

// 2-Step Workflow State
const submissionStep = ref<1 | 2>(1)
const isInspecting = ref(false)
const isSubmittingFinal = ref(false)

const inputMode = ref<'paste' | 'file'>('paste')
const sourceCode = ref('')
const attachedFileName = ref('')
const attachedImage = ref<File | null>(null)
const imagePreviewUrl = ref('')

// Step 1 Capture Options: 'paste_image' vs 'manual_text'
const captureMode = ref<'paste_image' | 'manual_text'>('paste_image')
const manualMetrics = ref({
  resultStatus: 'Pass',
  executionTime: '',
  memoryUsage: '',
  codeLength: ''
})

const step2Data = ref({
  resultStatus: 'Pass',
  executionTime: '',
  memoryUsage: '',
  codeLength: '',
  submissionDateText: '',
  aiTimeComplexity: '',
  aiSpaceComplexity: '',
  aiKeyIdea: '',
  aiFeedback: '',
  keywords: [] as string[]
})

const newKeywordInput = ref('')
const modalSubmission = ref<any>(null)
const showModalSummary = ref(true)
const peerSubmissions = ref<any[]>([])
const allStudents = ref<{ sno: string; name: string }[]>([])
const fileInput = ref<HTMLInputElement | null>(null)
const imageInput = ref<HTMLInputElement | null>(null)

async function loadAllStudents() {
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

const unsubmittedStudents = computed(() => {
  if (!problemStore.selectedProblem || allStudents.value.length === 0) return []
  const submittedSnos = new Set(peerSubmissions.value.map(s => s.studentSno || s.sno))
  return allStudents.value.filter(st => !submittedSnos.has(st.sno))
})

const sortField = ref('submittedAt')
const sortAsc = ref(false)

const newProblem = ref({
  problemDate: todayStr,
  problemType: '과제' as '과제' | '워크샵',
  platformName: '',
  title: '',
  description: ''
})

const editProblem = ref({
  problemDate: todayStr,
  problemType: '과제' as '과제' | '워크샵',
  platformName: '',
  title: '',
  description: ''
})

function triggerDatePicker(e: MouseEvent) {
  const target = e.target as HTMLInputElement
  if (target && typeof target.showPicker === 'function') {
    target.showPicker()
  }
}

const stats = ref({
  totalStudents: 23,
  submittedCount: 0,
  unsubmittedCount: 23,
  passCount: 0,
  failCount: 0
})

function getFormattedDateOffset(offsetDays: number) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  return d.toISOString().split('T')[0]
}

async function loadProblems() {
  if (authStore.isAdmin) {
    // 오늘 기준 ±3일 (총 7일)
    const startDate = getFormattedDateOffset(-3)
    const endDate = getFormattedDateOffset(3)
    await problemStore.loadWeeklyProblems(startDate, endDate)
  } else {
    // 학생: 최근 1주일
    await problemStore.loadWeeklyProblems()
  }
}

function handleGlobalPaste(e: ClipboardEvent) {
  if (submissionStep.value !== 1) return
  const items = e.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.type.indexOf('image') !== -1) {
      const file = item.getAsFile()
      if (file) {
        attachedImage.value = file
        captureMode.value = 'paste_image'
        const reader = new FileReader()
        reader.onload = (evt) => {
          imagePreviewUrl.value = evt.target?.result as string
        }
        reader.readAsDataURL(file)
        break
      }
    }
  }
}

onMounted(async () => {
  window.addEventListener('paste', handleGlobalPaste)
  if (!authStore.isAuthenticated) {
    router.push('/')
    return
  }
  await loadAllStudents()
  await problemStore.loadPlatforms()
  await loadProblems()
  if (problemStore.selectedProblem) {
    await loadPeerSubmissions()
  }
})

onUnmounted(() => {
  window.removeEventListener('paste', handleGlobalPaste)
})

watch(() => problemStore.selectedProblem, async (newVal) => {
  submissionStep.value = 1
  if (newVal) {
    await loadPeerSubmissions()
  } else {
    peerSubmissions.value = []
  }
})

const dateGroupedProblems = computed(() => {
  const groups: Record<string, { date: string; items: ProblemItem[]; hwCount: number; wsCount: number }> = {}
  problemStore.problems.forEach(p => {
    const d = p.problemDate || '날짜 미지정'
    if (!groups[d]) {
      groups[d] = { date: d, items: [], hwCount: 0, wsCount: 0 }
    }
    groups[d].items.push(p)
    if (p.problemType === '워크샵') groups[d].wsCount++
    else groups[d].hwCount++
  })
  return Object.values(groups).sort((a, b) => b.date.localeCompare(a.date))
})

const cleanDescription = computed(() => {
  const desc = problemStore.selectedProblem?.description
  if (!desc) return ''
  let cleaned = desc.replace(/문제 링크:\s*https?:\/\/[^\s]+/gi, '').trim()
  cleaned = cleaned.replace(/^https?:\/\/[^\s]+$/gm, '').trim()
  if (!cleaned) return ''
  return cleaned.replace(/\n/g, '<br>')
})

function formatShortDateTime(dtStr?: string) {
  if (!dtStr) return '-'
  const clean = dtStr.replace('T', ' ')
  // "2026-08-30 22:15:30" -> "08-30 22:15"
  if (clean.length >= 10 && clean.includes('-')) {
    const parts = clean.split(' ')
    const datePart = parts[0].substring(parts[0].indexOf('-') + 1) // "08-30"
    const timePart = parts[1] ? parts[1].substring(0, 5) : '' // "22:15"
    return timePart ? `${datePart} ${timePart}` : datePart
  }
  return clean
}

function stripUnit(val?: string | number) {
  if (val === null || val === undefined || val === '') return '-'
  const str = String(val).trim()
  const stripped = str.replace(/\s*(ms|s|kb|mb|bytes|byte|b)\s*$/gi, '').trim()
  return stripped || '-'
}

const modalKeywords = computed(() => {
  if (!modalSubmission.value?.aiKeywords) return []
  if (Array.isArray(modalSubmission.value.aiKeywords)) return modalSubmission.value.aiKeywords
  return String(modalSubmission.value.aiKeywords).split(',').map(k => k.trim()).filter(Boolean)
})

const highlightedModalLines = computed(() => {
  if (!modalSubmission.value?.sourceCode) return []
  try {
    const raw = hljs.highlight(modalSubmission.value.sourceCode, { language: 'java' }).value
    return raw.split('\n')
  } catch (e) {
    return (modalSubmission.value.sourceCode || '').split('\n')
  }
})

function handleToggleEdit() {
  if (!problemStore.selectedProblem) return
  editProblem.value = {
    problemDate: problemStore.selectedProblem.problemDate || todayStr,
    problemType: problemStore.selectedProblem.problemType || '과제',
    platformName: problemStore.selectedProblem.platformName || '',
    title: problemStore.selectedProblem.title || '',
    description: problemStore.selectedProblem.description || ''
  }
  isEditing.value = !isEditing.value
}

async function handleCreateProblem() {
  const selectedPlatform = problemStore.platforms.find(p => p.name === newProblem.value.platformName)
  const targetDate = newProblem.value.problemDate || todayStr
  try {
    const res = await fetch('/api/problems', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title: newProblem.value.title,
        problemType: newProblem.value.problemType,
        platformName: newProblem.value.platformName,
        platformUrl: selectedPlatform?.url || '',
        description: newProblem.value.description,
        problemDate: targetDate
      })
    })
    if (res.ok) {
      isCreating.value = false
      newProblem.value = { problemDate: targetDate, problemType: '과제', platformName: '', title: '', description: '' }
      alert('✅ 문제가 성공적으로 등록되었습니다.')
      await loadProblems()
    } else {
      const err = await res.json().catch(() => ({}))
      alert('문제 등록 실패: ' + (err.message || '다시 시도해 주세요.'))
    }
  } catch (e: any) {
    alert('문제 등록 중 오류가 발생했습니다.')
  }
}

async function handleUpdateProblem() {
  if (!problemStore.selectedProblem) return
  const selectedPlatform = problemStore.platforms.find(p => p.name === editProblem.value.platformName)
  try {
    const res = await fetch(`/api/problems/${problemStore.selectedProblem.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title: editProblem.value.title,
        problemType: editProblem.value.problemType,
        platformName: editProblem.value.platformName,
        platformUrl: selectedPlatform?.url || '',
        description: editProblem.value.description,
        problemDate: editProblem.value.problemDate
      })
    })
    if (res.ok) {
      const updated = await res.json()
      problemStore.selectProblem(updated)
      isEditing.value = false
      alert('✅ 문제가 성공적으로 수정되었습니다.')
      await loadProblems()
    }
  } catch (e: any) {
    alert('문제 수정 중 오류가 발생했습니다.')
  }
}

async function handleDeleteProblem() {
  if (!problemStore.selectedProblem) return
  const count = problemStore.selectedProblem.submissionCount || 0
  const msg = count > 0 
    ? `'${problemStore.selectedProblem.title}' 문제에는 총 ${count}건의 학생 제출 기록이 있습니다.\n함께 모두 삭제하시겠습니까?`
    : `'${problemStore.selectedProblem.title}' 문제를 정말 삭제하시겠습니까?`
  
  if (!confirm(msg)) return
  try {
    const res = await fetch(`/api/problems/${problemStore.selectedProblem.id}`, { method: 'DELETE' })
    if (res.ok) {
      alert('🗑️ 문제가 삭제되었습니다.')
      problemStore.selectedProblem = null
      await loadProblems()
    }
  } catch (e) {
    alert('문제 삭제 실패')
  }
}

async function loadPeerSubmissions() {
  if (!problemStore.selectedProblem) return
  try {
    const res = await fetch(`/api/submissions/problem/${problemStore.selectedProblem.id}`)
    if (res.ok) {
      const data = await res.json()
      peerSubmissions.value = Array.isArray(data) ? data : []

      const submitted = peerSubmissions.value.length
      const passes = peerSubmissions.value.filter(s => s.resultStatus === 'Pass').length
      stats.value = {
        totalStudents: 23,
        submittedCount: submitted,
        unsubmittedCount: Math.max(0, 23 - submitted),
        passCount: passes,
        failCount: submitted - passes
      }
    }
  } catch (e) {
    peerSubmissions.value = []
  }
}

function sortBy(field: string) {
  if (sortField.value === field) {
    sortAsc.value = !sortAsc.value
  } else {
    sortField.value = field
    sortAsc.value = true
  }
}

const sortedSubmissions = computed(() => {
  return [...peerSubmissions.value].sort((a, b) => {
    let valA = a[sortField.value] ?? ''
    let valB = b[sortField.value] ?? ''
    let res = 0
    if (typeof valA === 'number' && typeof valB === 'number') {
      res = valA - valB
    } else {
      res = String(valA).localeCompare(String(valB))
    }
    return sortAsc.value ? res : -res
  })
})

function handleFileChange(e: any) {
  const file = e.target.files[0]
  if (!file) return
  attachedFileName.value = file.name
  const reader = new FileReader()
  reader.onload = (evt) => {
    sourceCode.value = evt.target?.result as string
  }
  reader.readAsText(file)
}

function handleImageChange(e: any) {
  const file = e.target.files[0]
  if (!file) return
  attachedImage.value = file
  const reader = new FileReader()
  reader.onload = (evt) => {
    imagePreviewUrl.value = evt.target?.result as string
  }
  reader.readAsDataURL(file)
}

// 1단계: AI 검수 요청 (이미지 분석 또는 직접 입력 수동 분석)
async function handleInspectWithAi() {
  if (!sourceCode.value.trim()) {
    alert('제출할 Java 소스코드를 입력하거나 파일을 첨부해 주세요.')
    return
  }

  isInspecting.value = true
  const formData = new FormData()
  formData.append('sourceCodeText', sourceCode.value)

  if (captureMode.value === 'paste_image') {
    if (attachedImage.value) {
      formData.append('imageFile', attachedImage.value)
    }
  } else {
    // 수동 입력 모드 검증 및 첨부
    if (!manualMetrics.value.executionTime.trim()) {
      alert('실행 시간을 입력해 주세요 (예: 120 ms).')
      isInspecting.value = false
      return
    }
    if (!manualMetrics.value.memoryUsage.trim()) {
      alert('메모리 사용량을 입력해 주세요 (예: 24 MB 또는 24576 kb).')
      isInspecting.value = false
      return
    }
    formData.append('manualResultStatus', manualMetrics.value.resultStatus || 'Pass')
    formData.append('manualExecutionTime', manualMetrics.value.executionTime.trim())
    formData.append('manualMemoryUsage', manualMetrics.value.memoryUsage.trim())
    if (manualMetrics.value.codeLength.trim()) {
      formData.append('manualCodeLength', manualMetrics.value.codeLength.trim())
    }
  }

  try {
    const res = await fetch('/api/submissions/ai-inspect', {
      method: 'POST',
      body: formData
    })
    if (res.ok) {
      const result = await res.json()
      step2Data.value = {
        resultStatus: result.resultStatus || manualMetrics.value.resultStatus || 'Pass',
        executionTime: result.executionTime || manualMetrics.value.executionTime || '',
        memoryUsage: result.memoryUsage || manualMetrics.value.memoryUsage || '',
        codeLength: result.codeLength || manualMetrics.value.codeLength || '',
        submissionDateText: result.submissionDateText || '',
        aiTimeComplexity: result.timeComplexity || '',
        aiSpaceComplexity: result.spaceComplexity || '',
        aiKeyIdea: result.keyIdea || '',
        aiFeedback: result.feedback || '',
        keywords: Array.isArray(result.keywords) ? [...result.keywords] : []
      }
      submissionStep.value = 2
    } else {
      const err = await res.json().catch(() => ({}))
      alert('AI 검수 실패: ' + (err.message || '다시 시도해 주세요.'))
    }
  } catch (e: any) {
    alert('AI 검수 중 네트워크 오류가 발생했습니다.')
  } finally {
    isInspecting.value = false
  }
}

function addKeyword() {
  const kw = newKeywordInput.value.trim()
  if (kw && !step2Data.value.keywords.includes(kw)) {
    step2Data.value.keywords.push(kw)
    newKeywordInput.value = ''
  }
}

function removeKeyword(idx: number) {
  step2Data.value.keywords.splice(idx, 1)
}

// 2단계: 최종 텍스트 데이터 제출
async function handleFinalSubmit() {
  if (!problemStore.selectedProblem) return
  isSubmittingFinal.value = true

  const payload = {
    problemId: problemStore.selectedProblem.id,
    sourceCode: sourceCode.value,
    originalFileName: attachedFileName.value || 'Solution.java',
    resultStatus: step2Data.value.resultStatus || 'Pass',
    memoryUsage: step2Data.value.memoryUsage,
    executionTime: step2Data.value.executionTime,
    codeLength: step2Data.value.codeLength,
    submissionDateText: step2Data.value.submissionDateText,
    aiTimeComplexity: step2Data.value.aiTimeComplexity,
    aiSpaceComplexity: step2Data.value.aiSpaceComplexity,
    aiKeyIdea: step2Data.value.aiKeyIdea,
    aiFeedback: step2Data.value.aiFeedback,
    aiKeywords: step2Data.value.keywords
  }

  try {
    const res = await fetch('/api/submissions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    if (res.ok) {
      alert('🎉 과제가 성공적으로 최종 제출되었습니다!')
      submissionStep.value = 1
      sourceCode.value = ''
      attachedFileName.value = ''
      attachedImage.value = null
      imagePreviewUrl.value = ''
      manualMetrics.value = { resultStatus: 'Pass', executionTime: '', memoryUsage: '', codeLength: '' }
      await authStore.checkAuth()
      await loadProblems()
      await loadPeerSubmissions()
    } else {
      const err = await res.json().catch(() => ({}))
      alert('제출 실패: ' + (err.message || '다시 시도해 주세요.'))
    }
  } catch (e: any) {
    alert('최종 제출 중 오류가 발생했습니다.')
  } finally {
    isSubmittingFinal.value = false
  }
}

function openCodeModal(s: any) {
  modalSubmission.value = s
}

function copyCode() {
  if (modalSubmission.value?.sourceCode) {
    navigator.clipboard.writeText(modalSubmission.value.sourceCode)
    alert('코드가 클립보드에 복사되었습니다!')
  }
}
</script>
