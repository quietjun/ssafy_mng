package com.quietjun.ssafymng.entity;

import lombok.Getter;

@Getter
public enum ExamCategory {
    MONTHLY("월말평가"),
    SUBJECT("과목평가"),
    OTHER("기타평가");

    private final String description;

    ExamCategory(String description) {
        this.description = description;
    }

    public static ExamCategory fromString(String text) {
        if (text == null || text.isBlank()) return OTHER;
        String trimmed = text.trim();
        for (ExamCategory c : ExamCategory.values()) {
            if (c.name().equalsIgnoreCase(trimmed) || c.getDescription().equalsIgnoreCase(trimmed)) {
                return c;
            }
        }
        return OTHER;
    }
}
