package com.fintech.server.quiz.service;

import com.fintech.server.quiz.dto.AnswerRequestDto;
import com.fintech.server.quiz.dto.AnswerResponseDto;
import com.fintech.server.entity.User;
import com.fintech.server.quiz.dto.QuizResponseDto;
import com.fintech.server.quiz.entity.QuestionOption;
import com.fintech.server.quiz.entity.Quiz;
import com.fintech.server.quiz.entity.UserAnswer;
import com.fintech.server.quiz.repository.QuestionOptionRepository;
import com.fintech.server.quiz.repository.QuizRepository;
import com.fintech.server.quiz.repository.UserAnswerRepository;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserRepository userRepository; // 임시로 사용자 정보를 가져오기 위해 추가

    /**
     * 특정 ID의 퀴즈 정보를 조회하는 메소드
     */
    public QuizResponseDto findQuizById(Long quizId) {
        Quiz quiz = quizRepository.findByIdWithDetails(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with id: " + quizId));
        return convertToDto(quiz);
    }

    /**
     * 사용자가 제출한 답안을 채점하고 기록하는 메소드
     */
    @Transactional
    public AnswerResponseDto submitAnswer(AnswerRequestDto requestDto) {
        // [!] 중요: 실제 서비스에서는 Spring Security의 인증 정보에서 사용자 ID를 가져와야 합니다.
        // 지금은 테스트를 위해 ID가 1인 사용자를 임시로 사용합니다.
        User currentUser = userRepository.findById(1L)
                .orElseGet(() -> {
                    // 사용자가 없으면 테스트용 사용자를 생성
                    User testUser = new User();
                    testUser.setId(1L);
                    testUser.setNickname("테스트사용자");
                    testUser.setEmail("test@example.com");
                    return userRepository.save(testUser);
                });

        QuestionOption selectedOption = questionOptionRepository.findById(requestDto.getSelectedOptionId())
                .orElseThrow(() -> new RuntimeException("Option not found with id: " + requestDto.getSelectedOptionId()));

        boolean isCorrect = selectedOption.isCorrect();

        // 사용자 답변 기록 저장
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUser(currentUser);
        userAnswer.setQuestion(selectedOption.getQuestion());
        userAnswer.setSelectedOption(selectedOption);
        userAnswer.setCorrect(isCorrect);
        userAnswerRepository.save(userAnswer);

        // 정답 옵션 정보 조회
        QuestionOption correctOption = questionOptionRepository.findCorrectOptionByQuestionId(requestDto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Correct option not found for question id: " + requestDto.getQuestionId()));
        
        return AnswerResponseDto.builder()
                .isCorrect(isCorrect)
                .correctOptionId(correctOption.getId())
                .feedback(selectedOption.getQuestion().getAnswerExplanationMd())
                .build();
    }

    private QuizResponseDto convertToDto(Quiz quiz) {
        return QuizResponseDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .questions(quiz.getQuestions() != null ? quiz.getQuestions().stream().map(question ->
                        QuizResponseDto.QuestionDto.builder()
                                .id(question.getId())
                                .stemMd(question.getStemMd())
                                .answerExplanationMd(question.getAnswerExplanationMd())
                                .hintMd(question.getHintMd())                           // 힌트 정보 추가
                                .teachingExplainerMd(question.getTeachingExplainerMd()) // 학습 패널 추가
                                .solvingKeypointsMd(question.getSolvingKeypointsMd())   // 핵심 포인트 추가
                                .options(question.getOptions() != null ? question.getOptions().stream()
                                        .sorted((o1, o2) -> o1.getId().compareTo(o2.getId())) // ID 기준 오름차순 정렬
                                        .map(option ->
                                        QuizResponseDto.OptionDto.builder()
                                                .id(option.getId())
                                                .label(option.getLabel())
                                                .contentMd(option.getContentMd())
                                                .build()
                                ).collect(Collectors.toList()) : Collections.emptyList())
                                .build()
                ).collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }
}

