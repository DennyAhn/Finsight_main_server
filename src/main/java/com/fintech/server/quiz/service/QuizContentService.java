package com.fintech.server.quiz.service;

import com.fintech.server.quiz.entity.Subsector;
import com.fintech.server.quiz.dto.SectorResponseDto;
import com.fintech.server.quiz.dto.SubsectorDetailResponseDto;
import com.fintech.server.quiz.repository.SectorRepository;
import com.fintech.server.quiz.repository.SubsectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuizContentService {

    private final SectorRepository sectorRepository;
    private final SubsectorRepository subsectorRepository;

    public List<SectorResponseDto> findAllSectors() {
        return sectorRepository.findAllWithSubsectors().stream()
                .map(sector -> SectorResponseDto.builder()
                        .id(sector.getId())
                        .name(sector.getName())
                        .subsectors(sector.getSubsectors().stream()
                                .map(subsector -> SectorResponseDto.SubsectorDto.builder()
                                        .id(subsector.getId())
                                        .name(subsector.getName())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    public SubsectorDetailResponseDto findSubsectorById(Long subsectorId) {
        Subsector subsector = subsectorRepository.findByIdWithLevels(subsectorId)
                .orElseThrow(() -> new RuntimeException("Subsector not found with id: " + subsectorId));

        return SubsectorDetailResponseDto.builder()
                .id(subsector.getId())
                .name(subsector.getName())
                .sectorName(subsector.getSector().getName())
                .description(subsector.getDescription())
                .levels(subsector.getLevels().stream()
                        .map(level -> SubsectorDetailResponseDto.LevelDto.builder()
                                .id(level.getId())
                                .levelNumber(level.getLevelNumber())
                                .title(level.getTitle())
                                .learningGoal(level.getLearningGoal())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
