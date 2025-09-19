package com.fintech.server.service;

import com.fintech.server.dto.AuthRequestDto;
import com.fintech.server.entity.Account;
import com.fintech.server.entity.User;
import com.fintech.server.repository.AccountRepository;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 로직
     * User와 Account 테이블에 데이터를 분리하여 저장합니다.
     */
    @Transactional
    public User signup(AuthRequestDto requestDto) {
        // 이메일 중복 확인 (인증 정보가 저장되는 Account 테이블 기준)
        if (accountRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + requestDto.getEmail());
        }
        
        // 닉네임 중복 확인 (프로필 정보가 저장되는 User 테이블 기준)
        if (userRepository.findByNickname(requestDto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + requestDto.getNickname());
        }

        // 1. User 테이블에 사용자 프로필 정보 저장
        User user = new User();
        user.setNickname(requestDto.getNickname());
        user.setEmail(requestDto.getEmail()); // 연락처용 이메일
        User savedUser = userRepository.save(user);
        log.info("User 테이블에 사용자 정보 저장 완료: {}", savedUser.getNickname());

        // 2. Account 테이블에 인증 정보 저장
        Account account = new Account();
        account.setUser(savedUser); // User와의 관계 설정
        account.setEmail(requestDto.getEmail());
        
        // 이메일을 정규화(소문자 변환, 공백 제거)하여 email_normalized 필드에 저장
        String normalizedEmail = requestDto.getEmail().trim().toLowerCase();
        account.setEmailNormalized(normalizedEmail);

        // 비밀번호를 암호화하여 저장
        account.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        
        accountRepository.save(account);
        log.info("Account 테이블에 인증 정보 저장 완료: email={}", account.getEmail());

        return savedUser;
    }
}

