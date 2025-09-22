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
import com.fintech.server.quiz.repository.UserDailyActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuizContentService {

    private final SectorRepository sectorRepository;
    private final SubsectorRepository subsectorRepository;
    private final LevelRepository levelRepository;
    private final QuizRepository quizRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserDailyActivityRepository userDailyActivityRepository;

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
        
        // 주간 학습 현황 데이터 생성 (이번 주 7일간)
        List<LevelDetailResponseDto.WeeklyProgressDto> weeklyProgress = generateWeeklyProgress(userId);
        
        // 퀴즈 상태 리스트 생성 (개선된 로직 적용)
        List<LevelDetailResponseDto.QuizStatusDto> quizStatusList = generateQuizStatusList(quizzes, userId);

        return LevelDetailResponseDto.builder()
                .levelTitle(level.getTitle())
                .subsectorName(level.getSubsector().getName())
                .weeklyProgress(weeklyProgress)
                .quizzes(quizStatusList)
                .build();
    }

    /**
     * 주간 학습 현황 데이터 생성
     */
    private List<LevelDetailResponseDto.WeeklyProgressDto> generateWeeklyProgress(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // 이번 주 월요일
        
        return IntStream.range(0, 7)
                .mapToObj(i -> {
                    LocalDate date = weekStart.plusDays(i);
                    boolean completed = hasUserActivityOnDate(userId, date);
                    
                    return LevelDetailResponseDto.WeeklyProgressDto.builder()
                            .dayOfMonth(date.getDayOfMonth())
                            .completed(completed)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜에 사용자 활동이 있는지 확인
     */
    private boolean hasUserActivityOnDate(Long userId, LocalDate date) {
        return userDailyActivityRepository.findByIdUserIdAndIdActivityDate(userId, date).isPresent();
    }

    /**
     * 퀴즈 상태 리스트 생성 (개선된 로직)
     */
    private List<LevelDetailResponseDto.QuizStatusDto> generateQuizStatusList(List<Quiz> quizzes, Long userId) {
        List<LevelDetailResponseDto.QuizStatusDto> result = new ArrayList<>();
        boolean hasCompletedQuiz = false;
        
        for (Quiz quiz : quizzes) {
            LevelDetailResponseDto.Status status = determineQuizStatus(quiz, userId, hasCompletedQuiz);
            if (status == LevelDetailResponseDto.Status.COMPLETED) {
                hasCompletedQuiz = true;
            }
            
            result.add(LevelDetailResponseDto.QuizStatusDto.builder()
                    .id(quiz.getId())
                    .title(quiz.getTitle())
                    .sortOrder(quiz.getId().intValue())
                    .status(status)
                    .build());
        }
        
        return result;
    }

    /**
     * 퀴즈 상태 결정 로직 (개선된 버전)
     */
    private LevelDetailResponseDto.Status determineQuizStatus(Quiz quiz, Long userId, boolean hasCompletedQuiz) {
        // 사용자가 해당 퀴즈를 완료했는지 확인
        List<UserAnswer> userAnswers = userAnswerRepository.findByUserIdAndQuizId(userId, quiz.getId());
        
        if (!userAnswers.isEmpty()) {
            // 답변이 있으면 완료 상태
            return LevelDetailResponseDto.Status.COMPLETED;
        } else {
            // 답변이 없는 경우
            if (hasCompletedQuiz) {
                // 이전 퀴즈를 완료했다면 다음 퀴즈는 진행 가능
                return LevelDetailResponseDto.Status.IN_PROGRESS;
            } else {
                // 첫 번째 퀴즈이거나 이전 퀴즈를 완료하지 않았다면 진행 가능
                return LevelDetailResponseDto.Status.IN_PROGRESS;
            }
        }
    }
}
