package com.quietjun.ssafymng.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.quietjun.ssafymng.dto.LiveLectureDto;
import com.quietjun.ssafymng.dto.LiveLectureSummaryResponse;
import com.quietjun.ssafymng.dto.TrackSummaryDto;
import com.quietjun.ssafymng.entity.ConfigMetaData;
import com.quietjun.ssafymng.entity.LiveLecture;
import com.quietjun.ssafymng.repository.ConfigMetaDataRepository;
import com.quietjun.ssafymng.repository.LiveLectureRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveLectureService {

    private static final String METADATA_LAST_PROCESSED = "LIVE_LECTURE_LAST_PROCESSED_AT";

    private final LiveLectureRepository lectureRepository;
    private final ConfigMetaDataRepository metadataRepository;

    @Transactional(readOnly = true)
    public List<LiveLectureDto> getAllLectures() {
        return lectureRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(LiveLecture::getLectureDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(LiveLecture::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(LiveLecture::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public LiveLectureSummaryResponse getSummary() {
        return getSummary(null);
    }

    @Transactional(readOnly = true)
    public LiveLectureSummaryResponse getSummary(String term) {
        List<LiveLecture> allLectures = lectureRepository.findAll();

        List<String> availableTerms = allLectures.stream()
                .map(LiveLecture::getTerm)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .sorted()
                .toList();

        List<LiveLecture> lectures = allLectures;
        if (StringUtils.hasText(term) && !"ALL".equalsIgnoreCase(term)) {
            lectures = allLectures.stream()
                    .filter(l -> l.getTerm() != null && term.trim().equalsIgnoreCase(l.getTerm().trim()))
                    .toList();
        }

        long totalLectures = lectures.size();

        if (totalLectures == 0) {
            LocalDateTime lastProcessedAt = getLastProcessedAt();
            return LiveLectureSummaryResponse.builder()
                    .totalLectures(0)
                    .totalTracks(0)
                    .totalHours(0)
                    .minLectureDate(null)
                    .maxLectureDate(null)
                    .lastProcessedAt(lastProcessedAt)
                    .availableTerms(availableTerms)
                    .selectedTerm(term)
                    .trackSummaries(Collections.emptyList())
                    .instructorCounts(Collections.emptyMap())
                    .locationCounts(Collections.emptyMap())
                    .lectures(Collections.emptyList())
                    .build();
        }

        // 1. Min / Max date
        LocalDate minDate = lectures.stream()
                .map(LiveLecture::getLectureDate)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDate maxDate = lectures.stream()
                .map(LiveLecture::getLectureDate)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        // 2. Track Summary calculation (group by normalized track name)
        Map<String, List<LiveLecture>> trackGroupMap = new LinkedHashMap<>();
        for (LiveLecture l : lectures) {
            String trackName = cleanTrackName(l.getSubject());
            trackGroupMap.computeIfAbsent(trackName, k -> new ArrayList<>()).add(l);
        }

        List<TrackSummaryDto> trackSummaries = new ArrayList<>();
        double grandTotalHours = 0.0;

        for (Map.Entry<String, List<LiveLecture>> entry : trackGroupMap.entrySet()) {
            String trackName = entry.getKey();
            List<LiveLecture> trackLectures = entry.getValue();
            long count = trackLectures.size();

            double trackTotalHours = 0.0;
            Set<String> instructors = new TreeSet<>();
            Set<String> locations = new TreeSet<>();
            LocalDate trackMinDate = null;
            LocalDate trackMaxDate = null;

            for (LiveLecture l : trackLectures) {
                double hours = parseDurationToHours(l.getDuration(), l.getStartTime(), l.getEndTime());
                trackTotalHours += hours;

                if (StringUtils.hasText(l.getInstructor())) {
                    instructors.add(l.getInstructor().trim());
                }
                if (StringUtils.hasText(l.getLocation())) {
                    locations.add(l.getLocation().trim());
                }
                if (l.getLectureDate() != null) {
                    if (trackMinDate == null || l.getLectureDate().isBefore(trackMinDate)) {
                        trackMinDate = l.getLectureDate();
                    }
                    if (trackMaxDate == null || l.getLectureDate().isAfter(trackMaxDate)) {
                        trackMaxDate = l.getLectureDate();
                    }
                }
            }

            grandTotalHours += trackTotalHours;

            // percentage based on lecture count
            double percentage = Math.round((double) count / totalLectures * 1000.0) / 10.0;

            trackSummaries.add(TrackSummaryDto.builder()
                    .trackName(trackName)
                    .lectureCount(count)
                    .percentage(percentage)
                    .totalHours(Math.round(trackTotalHours * 10.0) / 10.0)
                    .hoursPercentage(0.0) // Set after grandTotalHours is calculated
                    .instructors(new ArrayList<>(instructors))
                    .locations(new ArrayList<>(locations))
                    .minDate(trackMinDate)
                    .maxDate(trackMaxDate)
                    .build());
        }

        // Compute hoursPercentage for each track
        final double finalGrandTotalHours = grandTotalHours > 0 ? grandTotalHours : 1.0;
        for (TrackSummaryDto t : trackSummaries) {
            double hPct = Math.round(t.getTotalHours() / finalGrandTotalHours * 1000.0) / 10.0;
            t.setHoursPercentage(hPct);
        }

        // Sort track summaries by lectureCount descending (기본: 강의 횟수 순 정렬)
        trackSummaries.sort((a, b) -> Long.compare(b.getLectureCount(), a.getLectureCount()));

        // 3. Instructor & Location Counts
        Map<String, Long> instructorCounts = new LinkedHashMap<>();
        for (LiveLecture l : lectures) {
            String instructor = StringUtils.hasText(l.getInstructor()) ? l.getInstructor().trim() : "미지정";
            instructorCounts.put(instructor, instructorCounts.getOrDefault(instructor, 0L) + 1);
        }

        Map<String, Long> locationCounts = new LinkedHashMap<>();
        for (LiveLecture l : lectures) {
            String loc = StringUtils.hasText(l.getLocation()) ? l.getLocation().trim() : "미지정";
            locationCounts.put(loc, locationCounts.getOrDefault(loc, 0L) + 1);
        }

        LocalDateTime lastProcessedAt = getLastProcessedAt();

        List<LiveLectureDto> dtoList = lectures.stream()
                .sorted(Comparator.comparing(LiveLecture::getLectureDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(LiveLecture::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(LiveLecture::toDto)
                .toList();

        return LiveLectureSummaryResponse.builder()
                .totalLectures(totalLectures)
                .totalTracks(trackSummaries.size())
                .totalHours(Math.round(grandTotalHours * 10.0) / 10.0)
                .minLectureDate(minDate)
                .maxLectureDate(maxDate)
                .lastProcessedAt(lastProcessedAt)
                .availableTerms(availableTerms)
                .selectedTerm(term)
                .trackSummaries(trackSummaries)
                .instructorCounts(instructorCounts)
                .locationCounts(locationCounts)
                .lectures(dtoList)
                .build();
    }

    @Transactional
    public LiveLectureSummaryResponse processBulkText(String rawText, Boolean append) {
        if (!StringUtils.hasText(rawText)) {
            throw new IllegalArgumentException("붙여넣을 텍스트 데이터가 비어있습니다.");
        }

        List<LiveLecture> parsedLectures = parseRawText(rawText);
        if (parsedLectures.isEmpty()) {
            throw new IllegalArgumentException("유효한 강의 데이터를 파싱하지 못했습니다. (형식을 확인하세요)");
        }

        // Deduplicate parsed lectures within payload itself
        List<LiveLecture> uniqueParsed = new ArrayList<>();
        Set<String> seenInPayload = new java.util.HashSet<>();
        for (LiveLecture l : parsedLectures) {
            String key = buildDedupeKey(l);
            if (seenInPayload.add(key)) {
                uniqueParsed.add(l);
            }
        }

        if (append != null && append) {
            List<LiveLecture> existing = lectureRepository.findAll();
            Set<String> existingKeys = existing.stream()
                    .map(this::buildDedupeKey)
                    .collect(java.util.stream.Collectors.toSet());

            List<LiveLecture> toSave = uniqueParsed.stream()
                    .filter(l -> !existingKeys.contains(buildDedupeKey(l)))
                    .toList();

            lectureRepository.saveAll(toSave);
        } else {
            lectureRepository.deleteAll();
            lectureRepository.saveAll(uniqueParsed);
        }

        // Update last processed timestamp metadata
        LocalDateTime now = LocalDateTime.now();
        saveLastProcessedAt(now);

        return getSummary();
    }

    public static String cleanTrackName(String subject) {
        if (!StringUtils.hasText(subject)) {
            return "미지정 트랙";
        }
        String s = subject.trim();
        if (s.contains("Live강의")) {
            int idx = s.indexOf("Live강의");
            s = s.substring(idx + "Live강의".length()).trim();
        } else if (s.startsWith("코딩 ")) {
            s = s.substring("코딩 ".length()).trim();
        } else if (s.startsWith("데이터 ")) {
            s = s.substring("데이터 ".length()).trim();
        }

        return s.isEmpty() ? subject.trim() : s;
    }

    private String buildDedupeKey(LiveLecture l) {
        String dateStr = l.getLectureDate() != null ? l.getLectureDate().toString() : "";
        String subj = l.getSubject() != null ? l.getSubject().trim() : "";
        String start = l.getStartTime() != null ? l.getStartTime().trim() : "";
        String loc = l.getLocation() != null ? l.getLocation().trim() : "";
        return dateStr + "|" + subj + "|" + start + "|" + loc;
    }

    @Transactional
    public void deleteAllLectures() {
        lectureRepository.deleteAll();
        saveLastProcessedAt(LocalDateTime.now());
    }

    @Transactional
    public void deleteLecture(Long id) {
        lectureRepository.deleteById(id);
    }

    private List<LiveLecture> parseRawText(String rawText) {
        List<LiveLecture> result = new ArrayList<>();
        String[] lines = rawText.split("\\r?\\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // Header line skip logic
            if (trimmed.contains("구분") && trimmed.contains("주제") && trimmed.contains("강사")) {
                continue;
            }
            if (trimmed.startsWith("구분\t") || trimmed.startsWith("구분 ")) {
                continue;
            }

            // Try splitting by Tab first
            String[] tokens = trimmed.split("\t");
            if (tokens.length < 3) {
                // Try splitting by 2 or more spaces
                tokens = trimmed.split("\\s{2,}");
            }
            if (tokens.length < 3) {
                // Try splitting by single space fallback
                tokens = trimmed.split("\\s+");
            }

            if (tokens.length < 3) {
                log.warn("Line skipped due to insufficient columns: {}", trimmed);
                continue;
            }

            LiveLecture lecture = parseTokensToEntity(tokens);
            if (lecture != null && StringUtils.hasText(lecture.getSubject())) {
                result.add(lecture);
            }
        }

        return result;
    }

    private LiveLecture parseTokensToEntity(String[] tokens) {
        String term = getSafeToken(tokens, 0);
        String location = getSafeToken(tokens, 1);
        String subject = getSafeToken(tokens, 2);
        String content = getSafeToken(tokens, 3);
        String instructor = getSafeToken(tokens, 4);

        // Flexible Date detection
        String dateToken = getSafeToken(tokens, 5);
        LocalDate lectureDate = parseDate(dateToken);

        // Fallback: search across all tokens if tokens[5] was not a valid date
        if (lectureDate == null) {
            for (String token : tokens) {
                LocalDate d = parseDate(token);
                if (d != null) {
                    lectureDate = d;
                    break;
                }
            }
        }

        String dayOfWeek = getSafeToken(tokens, 6);
        String startTime = getSafeToken(tokens, 7);
        String endTime = getSafeToken(tokens, 8);
        String duration = getSafeToken(tokens, 9);

        return LiveLecture.builder()
                .term(term)
                .location(location)
                .subject(subject)
                .content(content)
                .instructor(instructor)
                .lectureDate(lectureDate)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .duration(duration)
                .build();
    }

    private String getSafeToken(String[] tokens, int index) {
        if (index >= 0 && index < tokens.length) {
            String val = tokens[index].trim();
            return val.isEmpty() ? null : val;
        }
        return null;
    }

    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return null;
        String cleaned = dateStr.replaceAll("[^0-9\\-./]", "").replace('.', '-').replace('/', '-').trim();
        if (cleaned.isEmpty()) return null;

        try {
            String[] parts = cleaned.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                if (year < 100) year += 2000; // e.g. 26 -> 2026
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                    return LocalDate.of(year, month, day);
                }
            } else if (parts.length == 2) {
                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);
                if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                    return LocalDate.of(2026, month, day); // Default year 2026
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
        }
        return null;
    }

    private double parseDurationToHours(String durationStr, String startTime, String endTime) {
        if (StringUtils.hasText(durationStr)) {
            try {
                if (durationStr.contains(":")) {
                    String[] parts = durationStr.split(":");
                    double hours = Double.parseDouble(parts[0]);
                    double minutes = Double.parseDouble(parts[1]);
                    return hours + (minutes / 60.0);
                } else {
                    return Double.parseDouble(durationStr);
                }
            } catch (Exception ignored) {
            }
        }
        // Fallback: calculate from start & end time
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            try {
                String[] startParts = startTime.split(":");
                String[] endParts = endTime.split(":");
                int startHour = Integer.parseInt(startParts[0]);
                int startMin = startParts.length > 1 ? Integer.parseInt(startParts[1]) : 0;
                int endHour = Integer.parseInt(endParts[0]);
                int endMin = endParts.length > 1 ? Integer.parseInt(endParts[1]) : 0;

                int totalStartMins = startHour * 60 + startMin;
                int totalEndMins = endHour * 60 + endMin;
                if (totalEndMins > totalStartMins) {
                    return (totalEndMins - totalStartMins) / 60.0;
                }
            } catch (Exception ignored) {
            }
        }
        return 2.0; // default 2 hours per session
    }

    private LocalDateTime getLastProcessedAt() {
        return metadataRepository.findById(METADATA_LAST_PROCESSED)
                .map(ConfigMetaData::getValue)
                .map(val -> {
                    try {
                        return LocalDateTime.parse(val);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    private void saveLastProcessedAt(LocalDateTime timestamp) {
        ConfigMetaData meta = ConfigMetaData.builder()
                .keyword(METADATA_LAST_PROCESSED)
                .value(timestamp.toString())
                .build();
        metadataRepository.save(meta);
    }
}
