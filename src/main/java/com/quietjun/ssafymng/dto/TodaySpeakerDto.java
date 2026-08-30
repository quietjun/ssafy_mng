package com.quietjun.ssafymng.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodaySpeakerDto {
    private int rows;
    private int cols;
    private String colPattern;
    private List<Integer> colGroups;
    private List<StudentDto> students;
    private StudentDto[][] grid;
}
