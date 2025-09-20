package com.fintech.server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

// 첫 번째 화면을 위한 DTO
@Getter
@Builder
@AllArgsConstructor
public class SectorResponseDto {
    private Long id;
    private String name;
    private List<SubsectorDto> subsectors;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SubsectorDto {
        private Long id;
        private String name;
    }
}

