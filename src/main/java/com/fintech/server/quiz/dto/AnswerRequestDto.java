package com.fintech.server.quiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor; // 이 어노테이션을 추가합니다.
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 파라미터가 없는 기본 생성자를 자동으로 만들어줍니다.
public class AnswerRequestDto {
    private Long questionId;
    private Long selectedOptionId;
    private Long userId; // 테스트를 위해 임시로 추가
}
