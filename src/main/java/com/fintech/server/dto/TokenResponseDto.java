package com.fintech.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 로그인 성공 시 Access Token을 담아 응답하는 객체
@Getter
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
}

