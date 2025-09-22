package com.fintech.server.quiz.service;

import com.fintech.server.quiz.entity.Subsector;
import com.fintech.server.quiz.entity.Level;
import com.fintech.server.quiz.entity.Quiz;
import com.fintech.server.quiz.entity.UserAnswer;
import com.fintech.server.quiz.dto.SectorResponseDto;
import com.fintech.server.quiz.dto.SubsectorDetailResponseDto;
import com.fintech.server.quiz.dto.LevelDetailResponseDto;
import com.fintech.server.quiz.repository.SectorRepository;
import com.fintech.server.quiz.repository.SubsectorRepository;
import com.fintech.server.quiz.repository.LevelRepository;
import com.fintech.server.quiz.repository.QuizRepository;
import com.fintech.server.quiz.repository.UserAnswerRepository;
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
    private final LevelRepository levelRepository;
    private final QuizRepository quizRepository;
    private final UserAnswerRepository userAnswerRepository;

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

    /**
     * 탐험지 화면 데이터 조회 - 레벨별 퀴즈 상태 정보
     */
    public LevelDetailResponseDto getLevelDetail(Long levelId, Long userId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        List<Quiz> quizzes = quizRepository.findByLevelIdOrderById(levelId);
        
        List<LevelDetailResponseDto.QuizStatusDto> quizStatusList = quizzes.stream()
                .map(quiz -> {
                    LevelDetailResponseDto.Status status = determineQuizStatus(quiz, userId);
                    return LevelDetailResponseDto.QuizStatusDto.builder()
                            .id(quiz.getId())
                            .title(quiz.getTitle())
                            .sortOrder(quiz.getId().intValue()) // ID를 sortOrder로 사용
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());

        return LevelDetailResponseDto.builder()
                .levelTitle(level.getTitle())
                .subsectorName(level.getSubsector().getName())
                .quizzes(quizStatusList)
                .build();
    }

    /**
     * 퀴즈 상태 결정 로직
     */
    private LevelDetailResponseDto.Status determineQuizStatus(Quiz quiz, Long userId) {
        // 사용자가 해당 퀴즈를 완료했는지 확인
        List<UserAnswer> userAnswers = userAnswerRepository.findByUserIdAndQuizId(userId, quiz.getId());
        
        if (userAnswers.isEmpty()) {
            // 답변이 없으면 LOCKED 상태 (이전 퀴즈 완료 여부에 따라 결정)
            return LevelDetailResponseDto.Status.LOCKED;
        } else {
            // 답변이 있으면 완료 상태로 간주
            return LevelDetailResponseDto.Status.COMPLETED;
        }
    }
}
