package com.quietjun.ssafymng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeatSwapRequest {
    private int srcRow;
    private int srcCol;
    private int targetRow;
    private int targetCol;
}
