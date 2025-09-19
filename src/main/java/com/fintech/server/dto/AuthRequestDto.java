package com.fintech.server.dto;

import lombok.Getter;
import lombok.Setter;

// 로그인과 회원가입 요청 데이터를 담는 객체
@Getter
@Setter
public class AuthRequestDto {
    private String email;
    private String password;
    private String nickname; // 회원가입 시에만 사용됩니다.
}

