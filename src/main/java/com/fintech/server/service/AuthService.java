package com.fintech.server.service;

import com.fintech.server.config.JwtUtil;
import com.fintech.server.dto.TokenResponseDto;
import com.fintech.server.entity.Account;
import com.fintech.server.entity.User;
import com.fintech.server.quiz.entity.Badge;
import com.fintech.server.quiz.repository.BadgeRepository;
import com.fintech.server.repository.AccountRepository;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BadgeRepository badgeRepository;
    private final JwtUtil jwtUtil;

    /**
     * 게스트 사용자 생성 및 로그인
     */
    @Transactional
    public TokenResponseDto createGuestUserAndLogin() {
        try {
            // 1. 게스트 사용자 생성
            User guestUser = createGuestUser();
            
            // 2. 게스트 계정 생성
            Account guestAccount = createGuestAccount(guestUser);
            
            // 3. JWT 토큰 생성
            String token = jwtUtil.generateToken(guestUser.getId());
            
            log.info("게스트 사용자 생성 완료: userId={}, accountId={}", 
                    guestUser.getId(), guestAccount.getId());
            
            return TokenResponseDto.builder()
                    .accessToken(token)
                    .userId(guestUser.getId())
                    .build();
                    
        } catch (Exception e) {
            log.error("게스트 사용자 생성 실패", e);
            throw new RuntimeException("게스트 사용자 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 게스트 사용자 생성
     */
    private User createGuestUser() {
        User guestUser = new User();
        
        // 동물 이름 배열
        String[] animalNames = {
            "호랑이", "사자", "코끼리", "기린", "팬더", "고양이", "강아지", "토끼", "다람쥐", "여우",
            "늑대", "곰", "펭귄", "펠리컨", "독수리", "부엉이", "참새", "비둘기", "물고기", "고래",
            "상어", "문어", "게", "새우", "벌", "나비", "거미", "개미", "잠자리", "메뚜기",
            "뱀", "도마뱀", "거북이", "악어", "하마", "코뿔소", "얼룩말", "원숭이", "침팬지", "고릴라",
            "캥거루", "코알라", "오리", "닭", "돼지", "소", "양", "염소", "말", "당나귀"
        };
        
        // 랜덤하게 동물 이름 선택
        String randomAnimal = animalNames[(int) (Math.random() * animalNames.length)];
        guestUser.setNickname(randomAnimal);
        guestUser.setEmail("guest_" + UUID.randomUUID().toString() + "@example.com");
        guestUser.setCreatedAt(java.time.LocalDateTime.now());
        guestUser.setIsGuest(true); // 게스트 사용자로 설정
        
        // 기본 브론즈 배지 부여 (level_number = 1)
        Badge bronzeBadge = badgeRepository.findByLevelNumber(1).orElse(null);
        if (bronzeBadge != null) {
            guestUser.setDisplayedBadge(bronzeBadge);
            log.info("새 게스트 사용자에게 브론즈 배지 부여: {}", bronzeBadge.getName());
        } else {
            log.warn("브론즈 배지를 찾을 수 없습니다. 배지 없이 사용자 생성");
        }
        
        return userRepository.save(guestUser);
    }

    /**
     * 게스트 계정 생성
     */
    private Account createGuestAccount(User user) {
        Account guestAccount = new Account();
        guestAccount.setUser(user);
        guestAccount.setEmail(user.getEmail());
        guestAccount.setEmailNormalized(user.getEmail().toLowerCase());
        guestAccount.setPassword("GUEST_PASSWORD"); // 게스트는 비밀번호 불필요
        guestAccount.setEmailVerified(false);
        guestAccount.setStatus(Account.AccountStatus.active);
        guestAccount.setLastLoginAt(LocalDateTime.now());
        guestAccount.setExpiresAt(LocalDateTime.now().plusHours(12)); // 12시간 후 만료
        
        return accountRepository.save(guestAccount);
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 토큰 형식입니다.");
        }
        
        String jwt = token.substring(7); // "Bearer " 제거
        return jwtUtil.getUserIdFromToken(jwt);
    }

    /**
     * JWT 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        
        String jwt = token.substring(7); // "Bearer " 제거
        return jwtUtil.validateToken(jwt);
    }
}
