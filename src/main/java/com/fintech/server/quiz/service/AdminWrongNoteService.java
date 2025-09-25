package com.fintech.server.quiz.service;

import com.fintech.server.quiz.dto.AdminWrongNoteDto;
import com.fintech.server.quiz.entity.UserWrongNote;
import com.fintech.server.quiz.repository.UserWrongNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminWrongNoteService {

    private final UserWrongNoteRepository wrongNoteRepository;

    /**
     * 전체 오답 노트 통계 조회 (섹터별)
     */
    public AdminWrongNoteDto.OverallStatistics getOverallStatistics() {
        // 섹터별 통계
        List<Object[]> sectorStats = wrongNoteRepository.findWrongNoteStatisticsBySector();
        List<AdminWrongNoteDto.SectorStatistics> sectorStatistics = sectorStats.stream()
                .map(this::convertToSectorStatistics)
                .collect(Collectors.toList());

        // TOP 틀린 문제들
        Pageable topLimit = PageRequest.of(0, 10);
        List<Object[]> topWrongQuestions = wrongNoteRepository.findTopWrongQuestions(topLimit);
        List<AdminWrongNoteDto.TopWrongQuestion> topQuestions = topWrongQuestions.stream()
                .map(this::convertToTopWrongQuestion)
                .collect(Collectors.toList());

        // 전체 통계 계산
        long totalWrongNotes = sectorStatistics.stream()
                .mapToLong(AdminWrongNoteDto.SectorStatistics::getTotalWrongCount)
                .sum();
        
        long totalUniqueUsers = sectorStatistics.stream()
                .mapToLong(AdminWrongNoteDto.SectorStatistics::getUniqueUsersCount)
                .sum();

        return AdminWrongNoteDto.OverallStatistics.builder()
                .totalWrongNotesCount(totalWrongNotes)
                .totalUniqueUsersCount(totalUniqueUsers)
                .sectorStatistics(sectorStatistics)
                .topWrongQuestions(topQuestions)
                .build();
    }

    /**
     * 특정 섹터의 상세 통계 조회
     */
    public AdminWrongNoteDto.SectorStatistics getSectorStatistics(Long sectorId) {
        // 서브섹터별 통계
        List<Object[]> subsectorStats = wrongNoteRepository.findWrongNoteStatisticsBySubsector(sectorId);
        List<AdminWrongNoteDto.SubsectorStatistics> subsectorStatistics = new ArrayList<>();

        for (Object[] stat : subsectorStats) {
            Long subsectorId = (Long) stat[0];
            AdminWrongNoteDto.SubsectorStatistics subsectorStat = convertToSubsectorStatistics(stat);
            
            // 각 서브섹터의 레벨별 통계도 가져오기
            List<Object[]> levelStats = wrongNoteRepository.findWrongNoteStatisticsByLevel(subsectorId);
            List<AdminWrongNoteDto.LevelStatistics> levelStatistics = levelStats.stream()
                    .map(this::convertToLevelStatistics)
                    .collect(Collectors.toList());
            subsectorStat.setLevels(levelStatistics);
            
            subsectorStatistics.add(subsectorStat);
        }

        // 섹터 전체 통계 계산
        long totalWrongCount = subsectorStatistics.stream()
                .mapToLong(AdminWrongNoteDto.SubsectorStatistics::getTotalWrongCount)
                .sum();
        
        long uniqueUsersCount = subsectorStatistics.stream()
                .mapToLong(AdminWrongNoteDto.SubsectorStatistics::getUniqueUsersCount)
                .sum();

        return AdminWrongNoteDto.SectorStatistics.builder()
                .sectorId(sectorId)
                .totalWrongCount(totalWrongCount)
                .uniqueUsersCount(uniqueUsersCount)
                .subsectors(subsectorStatistics)
                .build();
    }

    /**
     * 특정 서브섹터의 상세 통계 조회
     */
    public AdminWrongNoteDto.SubsectorStatistics getSubsectorStatistics(Long subsectorId) {
        // 레벨별 통계
        List<Object[]> levelStats = wrongNoteRepository.findWrongNoteStatisticsByLevel(subsectorId);
        List<AdminWrongNoteDto.LevelStatistics> levelStatistics = new ArrayList<>();

        for (Object[] stat : levelStats) {
            Long levelId = (Long) stat[0];
            AdminWrongNoteDto.LevelStatistics levelStat = convertToLevelStatistics(stat);
            
            // 각 레벨의 퀴즈별 통계도 가져오기
            List<Object[]> quizStats = wrongNoteRepository.findWrongNoteStatisticsByQuiz(levelId);
            List<AdminWrongNoteDto.QuizStatistics> quizStatistics = quizStats.stream()
                    .map(this::convertToQuizStatistics)
                    .collect(Collectors.toList());
            levelStat.setQuizzes(quizStatistics);
            
            levelStatistics.add(levelStat);
        }

        // 서브섹터 전체 통계 계산
        long totalWrongCount = levelStatistics.stream()
                .mapToLong(AdminWrongNoteDto.LevelStatistics::getTotalWrongCount)
                .sum();
        
        long uniqueUsersCount = levelStatistics.stream()
                .mapToLong(AdminWrongNoteDto.LevelStatistics::getUniqueUsersCount)
                .sum();

        return AdminWrongNoteDto.SubsectorStatistics.builder()
                .subsectorId(subsectorId)
                .totalWrongCount(totalWrongCount)
                .uniqueUsersCount(uniqueUsersCount)
                .levels(levelStatistics)
                .build();
    }

    /**
     * 특정 퀴즈의 상세 통계 조회
     */
    public AdminWrongNoteDto.QuizStatistics getQuizStatistics(Long quizId) {
        // 문제별 통계
        List<Object[]> questionStats = wrongNoteRepository.findWrongNoteStatisticsByQuestion(quizId);
        List<AdminWrongNoteDto.QuestionStatistics> questionStatistics = new ArrayList<>();

        for (Object[] stat : questionStats) {
            Long questionId = (Long) stat[0];
            AdminWrongNoteDto.QuestionStatistics questionStat = convertToQuestionStatistics(stat);
            
            // 각 문제의 최근 틀린 사용자들
            Pageable recentLimit = PageRequest.of(0, 5);
            List<UserWrongNote> recentWrongNotes = wrongNoteRepository.findByQuestionId(questionId, recentLimit);
            List<AdminWrongNoteDto.UserWrongInfo> recentUsers = recentWrongNotes.stream()
                    .map(this::convertToUserWrongInfo)
                    .collect(Collectors.toList());
            questionStat.setRecentWrongUsers(recentUsers);
            
            questionStatistics.add(questionStat);
        }

        // 퀴즈 전체 통계 계산
        long totalWrongCount = questionStatistics.stream()
                .mapToLong(AdminWrongNoteDto.QuestionStatistics::getWrongCount)
                .sum();
        
        long uniqueUsersCount = questionStatistics.stream()
                .mapToLong(AdminWrongNoteDto.QuestionStatistics::getUniqueUsersCount)
                .sum();

        return AdminWrongNoteDto.QuizStatistics.builder()
                .quizId(quizId)
                .totalWrongCount(totalWrongCount)
                .uniqueUsersCount(uniqueUsersCount)
                .questions(questionStatistics)
                .build();
    }


    // ===== 변환 메서드들 =====

    private AdminWrongNoteDto.SectorStatistics convertToSectorStatistics(Object[] stat) {
        return AdminWrongNoteDto.SectorStatistics.builder()
                .sectorId((Long) stat[0])
                .sectorName((String) stat[1])
                .sectorSlug((String) stat[2])
                .totalWrongCount((Long) stat[3])
                .uniqueUsersCount((Long) stat[4])
                .build();
    }

    private AdminWrongNoteDto.SubsectorStatistics convertToSubsectorStatistics(Object[] stat) {
        return AdminWrongNoteDto.SubsectorStatistics.builder()
                .subsectorId((Long) stat[0])
                .subsectorName((String) stat[1])
                .subsectorSlug((String) stat[2])
                .totalWrongCount((Long) stat[3])
                .uniqueUsersCount((Long) stat[4])
                .build();
    }

    private AdminWrongNoteDto.LevelStatistics convertToLevelStatistics(Object[] stat) {
        return AdminWrongNoteDto.LevelStatistics.builder()
                .levelId((Long) stat[0])
                .levelNumber((Integer) stat[1])
                .levelTitle((String) stat[2])
                .totalWrongCount((Long) stat[3])
                .uniqueUsersCount((Long) stat[4])
                .build();
    }

    private AdminWrongNoteDto.QuizStatistics convertToQuizStatistics(Object[] stat) {
        return AdminWrongNoteDto.QuizStatistics.builder()
                .quizId((Long) stat[0])
                .quizTitle((String) stat[1])
                .totalWrongCount((Long) stat[2])
                .uniqueUsersCount((Long) stat[3])
                .build();
    }

    private AdminWrongNoteDto.QuestionStatistics convertToQuestionStatistics(Object[] stat) {
        return AdminWrongNoteDto.QuestionStatistics.builder()
                .questionId((Long) stat[0])
                .questionText((String) stat[1])
                .wrongCount((Long) stat[2])
                .uniqueUsersCount((Long) stat[3])
                .build();
    }

    private AdminWrongNoteDto.TopWrongQuestion convertToTopWrongQuestion(Object[] stat) {
        return AdminWrongNoteDto.TopWrongQuestion.builder()
                .questionId((Long) stat[0])
                .questionText((String) stat[1])
                .quizTitle((String) stat[2])
                .sectorName((String) stat[3])
                .subsectorName((String) stat[4])
                .wrongCount((Long) stat[5])
                .build();
    }

    private AdminWrongNoteDto.UserWrongInfo convertToUserWrongInfo(UserWrongNote wrongNote) {
        return AdminWrongNoteDto.UserWrongInfo.builder()
                .userId(wrongNote.getUser().getId())
                .userNickname(wrongNote.getUser().getNickname())
                .userEmail(wrongNote.getUser().getEmail())
                .timesWrong(wrongNote.getTimesWrong())
                .lastWrongAt(wrongNote.getLastWrongAt())
                .resolved(wrongNote.getResolved())
                .build();
    }

}
