package com.quietjun.ssafymng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointRetroactiveRequest {

    @Builder.Default
    private int workshopPoints = 20;

    @Builder.Default
    private int assignmentPoints = 10;

    /**
     * INCREMENTAL: 과거 미반영분 차액만 추가 지급 (기존 보유 포인트 유지)
     * RECALCULATE: 제출 합산 - 상점 구매액 기준으로 완전 초기화 재계산
     */
    @Builder.Default
    private String mode = "INCREMENTAL";

    /**
     * true인 경우 실제 DB 저장 없이 시뮬레이션 결과만 반환
     */
    @Builder.Default
    private boolean dryRun = false;

    /**
     * 특정 학생만 대상으로 할 경우 학번 (null 또는 공백이면 전체 학생)
     */
    private String targetSno;

    /**
     * 퇴소/휴학 학생(escape=true) 포함 여부 (기본: false)
     */
    @Builder.Default
    private boolean includeEscaped = false;
}
