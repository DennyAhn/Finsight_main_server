package com.fintech.server.quiz.service;

import com.fintech.server.entity.User;
import com.fintech.server.quiz.dto.WrongNoteDto;
import com.fintech.server.quiz.entity.*;
import com.fintech.server.quiz.repository.*;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WrongNoteService {

    private final UserWrongNoteRepository wrongNoteRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    /**
     * 오답 노트 생성 또는 업데이트
     * 이미 존재하는 문제면 틀린 횟수를 증가시킴
     */
    @Transactional
    public WrongNoteDto.Response createOrUpdateWrongNote(Long userId, Long questionId, Long wrongOptionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("문제를 찾을 수 없습니다: " + questionId));

        QuestionOption wrongOption = questionOptionRepository.findById(wrongOptionId)
                .orElseThrow(() -> new RuntimeException("선택지를 찾을 수 없습니다: " + wrongOptionId));

        QuestionOption correctOption = questionOptionRepository.findCorrectOptionByQuestionId(questionId)
                .orElseThrow(() -> new RuntimeException("정답을 찾을 수 없습니다: " + questionId));

        // 기존 오답 노트가 있는지 확인
        Optional<UserWrongNote> existingNote = wrongNoteRepository.findByUserIdAndQuestionId(userId, questionId);
        
        UserWrongNote wrongNote;
        if (existingNote.isPresent()) {
            // 기존 노트 업데이트
            wrongNote = existingNote.get();
            wrongNote.incrementTimesWrong();
            wrongNote.setLastAnswerOption(wrongOption);
            // 해결되었던 문제를 다시 틀렸으면 미해결로 변경
            if (wrongNote.getResolved()) {
                wrongNote.setResolved(false);
            }
        } else {
            // 새 오답 노트 생성
            wrongNote = new UserWrongNote();
            wrongNote.setUser(user);
            wrongNote.setQuestion(question);
            wrongNote.setLastAnswerOption(wrongOption);
            wrongNote.setCorrectOption(correctOption);
            wrongNote.setFirstWrongAt(LocalDateTime.now());
            wrongNote.setLastWrongAt(LocalDateTime.now());
            
            // 문제의 현재 학습 패널 내용을 스냅샷으로 저장
            wrongNote.setSnapshotTeachingSummaryMd(question.getHintMd());
            wrongNote.setSnapshotTeachingExplainerMd(question.getTeachingExplainerMd());
            wrongNote.setSnapshotKeypointsMd(question.getSolvingKeypointsMd());
        }

        wrongNote = wrongNoteRepository.save(wrongNote);
        log.info("오답 노트 저장 완료: userId={}, questionId={}, timesWrong={}", 
                userId, questionId, wrongNote.getTimesWrong());

        return convertToDto(wrongNote);
    }

    /**
     * 사용자의 오답 노트 목록 조회
     */
    public WrongNoteDto.ListResponse getUserWrongNotes(Long userId, int page, int size, String filter) {
        List<UserWrongNote> wrongNotes;
        
        switch (filter.toLowerCase()) {
            case "unresolved":
                wrongNotes = wrongNoteRepository.findUnresolvedByUserId(userId);
                break;
            case "resolved":
                wrongNotes = wrongNoteRepository.findResolvedByUserId(userId);
                break;
            case "needreview":
                wrongNotes = wrongNoteRepository.findNeedReviewByUserId(userId);
                break;
            default:
                wrongNotes = wrongNoteRepository.findByUserIdOrderByLastWrongAtDesc(userId);
        }

        // 페이징 처리
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), wrongNotes.size());
        List<UserWrongNote> pagedNotes = wrongNotes.subList(start, end);
        
        List<WrongNoteDto.Response> responseList = pagedNotes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 통계 정보 조회
        WrongNoteDto.Statistics statistics = getWrongNoteStatistics(userId);
        
        // 서브섹터별 통계 정보 조회
        List<WrongNoteDto.SubsectorStatistics> subsectorStatistics = getSubsectorStatistics(userId);
        
        // 레벨별 통계 정보 조회
        List<WrongNoteDto.LevelStatistics> levelStatistics = getLevelStatistics(userId);

        return WrongNoteDto.ListResponse.builder()
                .wrongNotes(responseList)
                .statistics(statistics)
                .subsectorStatistics(subsectorStatistics)
                .levelStatistics(levelStatistics)
                .totalPages((int) Math.ceil((double) wrongNotes.size() / size))
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    /**
     * 특정 오답 노트 조회
     */
    public WrongNoteDto.Response getWrongNote(Long userId, Long noteId) {
        UserWrongNote wrongNote = wrongNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("오답 노트를 찾을 수 없습니다: " + noteId));
        
        if (!wrongNote.getUser().getId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        return convertToDto(wrongNote);
    }

    /**
     * 오답 노트 개인 메모 업데이트
     */
    @Transactional
    public WrongNoteDto.Response updatePersonalNote(Long userId, Long noteId, String personalNoteMd) {
        UserWrongNote wrongNote = wrongNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("오답 노트를 찾을 수 없습니다: " + noteId));
        
        if (!wrongNote.getUser().getId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        wrongNote.setPersonalNoteMd(personalNoteMd);
        wrongNote = wrongNoteRepository.save(wrongNote);

        return convertToDto(wrongNote);
    }

    /**
     * 오답 노트 해결 상태 변경
     */
    @Transactional
    public WrongNoteDto.Response toggleResolved(Long userId, Long noteId) {
        UserWrongNote wrongNote = wrongNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("오답 노트를 찾을 수 없습니다: " + noteId));
        
        if (!wrongNote.getUser().getId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        if (wrongNote.getResolved()) {
            wrongNote.setResolved(false);
        } else {
            wrongNote.markAsResolved();
        }
        
        wrongNote = wrongNoteRepository.save(wrongNote);
        return convertToDto(wrongNote);
    }


    /**
     * 오답 노트 복습 완료 처리
     */
    @Transactional
    public WrongNoteDto.Response markAsReviewed(Long userId, Long noteId) {
        UserWrongNote wrongNote = wrongNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("오답 노트를 찾을 수 없습니다: " + noteId));
        
        if (!wrongNote.getUser().getId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        wrongNote.markAsReviewed();
        wrongNote = wrongNoteRepository.save(wrongNote);

        return convertToDto(wrongNote);
    }

    /**
     * 오답 노트 삭제
     */
    @Transactional
    public void deleteWrongNote(Long userId, Long noteId) {
        UserWrongNote wrongNote = wrongNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("오답 노트를 찾을 수 없습니다: " + noteId));
        
        if (!wrongNote.getUser().getId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        wrongNoteRepository.delete(wrongNote);
        log.info("오답 노트 삭제 완료: userId={}, noteId={}", userId, noteId);
    }

    /**
     * 오답 노트 통계 조회
     */
    public WrongNoteDto.Statistics getWrongNoteStatistics(Long userId) {
        return WrongNoteDto.Statistics.builder()
                .totalCount(wrongNoteRepository.countByUserId(userId))
                .unresolvedCount(wrongNoteRepository.countUnresolvedByUserId(userId))
                .resolvedCount(wrongNoteRepository.countResolvedByUserId(userId))
                .needReviewCount((long) wrongNoteRepository.findNeedReviewByUserId(userId).size())
                .build();
    }

    /**
     * 서브섹터별 틀린 문제 수 통계 조회
     */
    public List<WrongNoteDto.SubsectorStatistics> getSubsectorStatistics(Long userId) {
        List<UserWrongNote> wrongNotes = wrongNoteRepository.findByUserIdOrderByLastWrongAtDesc(userId);
        
        // 서브섹터별로 그룹화하여 틀린 문제 수 계산
        Map<Long, Map<String, Long>> subsectorMap = wrongNotes.stream()
                .collect(Collectors.groupingBy(
                        note -> note.getQuestion().getQuiz().getLevel().getSubsector().getId(),
                        Collectors.groupingBy(
                                note -> note.getQuestion().getQuiz().getLevel().getSubsector().getName(),
                                Collectors.counting()
                        )
                ));
        
        return subsectorMap.entrySet().stream()
                .map(entry -> {
                    Long subsectorId = entry.getKey();
                    Map<String, Long> nameCountMap = entry.getValue();
                    String subsectorName = nameCountMap.keySet().iterator().next();
                    Long wrongCount = nameCountMap.values().iterator().next();
                    
                    return WrongNoteDto.SubsectorStatistics.builder()
                            .subsectorId(subsectorId)
                            .subsectorName(subsectorName)
                            .wrongCount(wrongCount)
                            .build();
                })
                .sorted(Comparator.comparing(WrongNoteDto.SubsectorStatistics::getSubsectorId))
                .collect(Collectors.toList());
    }

    /**
     * 레벨별 틀린 문제 수 통계 조회
     */
    public List<WrongNoteDto.LevelStatistics> getLevelStatistics(Long userId) {
        List<UserWrongNote> wrongNotes = wrongNoteRepository.findByUserIdOrderByLastWrongAtDesc(userId);
        
        // 레벨별로 그룹화하여 틀린 문제 수 계산
        Map<Long, List<UserWrongNote>> levelMap = wrongNotes.stream()
                .collect(Collectors.groupingBy(
                        note -> note.getQuestion().getQuiz().getLevel().getId()
                ));
        
        return levelMap.entrySet().stream()
                .map(entry -> {
                    Long levelId = entry.getKey();
                    List<UserWrongNote> levelNotes = entry.getValue();
                    
                    // 첫 번째 요소에서 레벨 정보 추출
                    UserWrongNote firstNote = levelNotes.get(0);
                    
                    return WrongNoteDto.LevelStatistics.builder()
                            .levelId(levelId)
                            .levelNumber(firstNote.getQuestion().getQuiz().getLevel().getLevelNumber())
                            .levelTitle(firstNote.getQuestion().getQuiz().getLevel().getTitle())
                            .subsectorName(firstNote.getQuestion().getQuiz().getLevel().getSubsector().getName())
                            .wrongCount((long) levelNotes.size())
                            .build();
                })
                .sorted(Comparator.comparing(WrongNoteDto.LevelStatistics::getLevelId))
                .collect(Collectors.toList());
    }

    /**
     * UserWrongNote 엔티티를 DTO로 변환
     */
    private WrongNoteDto.Response convertToDto(UserWrongNote wrongNote) {
        Question question = wrongNote.getQuestion();
        
        // 모든 선택지 정보
        List<WrongNoteDto.OptionInfo> allOptions = question.getOptions().stream()
                .map(option -> WrongNoteDto.OptionInfo.builder()
                        .id(option.getId())
                        .text(option.getContentMd())
                        .isCorrect(option.isCorrect())
                        .build())
                .collect(Collectors.toList());

        return WrongNoteDto.Response.builder()
                .id(wrongNote.getId())
                .userId(wrongNote.getUser().getId())
                .questionId(question.getId())
                .questionText(question.getStemMd())
                .lastAnswerOptionId(wrongNote.getLastAnswerOption() != null ? wrongNote.getLastAnswerOption().getId() : null)
                .lastAnswerText(wrongNote.getLastAnswerOption() != null ? wrongNote.getLastAnswerOption().getContentMd() : null)
                .correctOptionId(wrongNote.getCorrectOption().getId())
                .correctAnswerText(wrongNote.getCorrectOption().getContentMd())
                .timesWrong(wrongNote.getTimesWrong())
                .firstWrongAt(wrongNote.getFirstWrongAt())
                .lastWrongAt(wrongNote.getLastWrongAt())
                .reviewedAt(wrongNote.getReviewedAt())
                .resolved(wrongNote.getResolved())
                .personalNoteMd(wrongNote.getPersonalNoteMd())
                .snapshotTeachingSummaryMd(wrongNote.getSnapshotTeachingSummaryMd())
                .snapshotTeachingExplainerMd(wrongNote.getSnapshotTeachingExplainerMd())
                .snapshotKeypointsMd(wrongNote.getSnapshotKeypointsMd())
                .createdAt(wrongNote.getCreatedAt())
                .updatedAt(wrongNote.getUpdatedAt())
                .quizTitle(question.getQuiz() != null ? question.getQuiz().getTitle() : null)
                .allOptions(allOptions)
                .build();
    }
}
