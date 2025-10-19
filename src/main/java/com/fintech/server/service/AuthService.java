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
     * 게스트 사용자 생성 및 로그인 (기존 계정 재사용 로직 포함)
     */
    @Transactional
    public TokenResponseDto createGuestUserAndLogin() {
        try {
            // 1. 기존 유효한 게스트 계정이 있는지 확인 (최근 24시간 내) - 임시 주석처리
            // Optional<Account> existingAccount = findRecentValidGuestAccount();
            
            // if (existingAccount.isPresent()) {
            //     // 기존 계정 재사용 - 같은 닉네임 유지
            //     Account account = existingAccount.get();
            //     User user = account.getUser();
            //     
            //     // 만료 시간 연장 (12시간 추가)
            //     account.setExpiresAt(LocalDateTime.now().plusHours(12));
            //     account.setLastLoginAt(LocalDateTime.now());
            //     accountRepository.save(account);
            //     
            //     // JWT 토큰 생성
            //     String token = jwtUtil.generateToken(user.getId());
            //     
            //     log.info("기존 게스트 계정 재사용: userId={}, nickname={}, accountId={}", 
            //             user.getId(), user.getNickname(), account.getId());
            //     
            //     return TokenResponseDto.builder()
            //             .accessToken(token)
            //             .userId(user.getId())
            //             .build();
            // }
            
            // 2. 기존 계정이 없으면 새로 생성 (항상 새 계정 생성하도록 변경)
            return createNewGuestAccount();
                    
        } catch (Exception e) {
            log.error("게스트 사용자 생성/로그인 실패", e);
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

    /**
     * 최근 유효한 게스트 계정 조회 (24시간 내, 만료되지 않은 계정)
     * 현재 주석처리된 로직에서만 사용되므로 임시 주석처리
     */
    // private Optional<Account> findRecentValidGuestAccount() {
    //     LocalDateTime now = LocalDateTime.now();
    //     LocalDateTime recentTime = now.minusHours(24); // 24시간 전
    //     
    //     List<Account> recentAccounts = accountRepository.findRecentValidGuestAccounts(now, recentTime);
    //     
    //     if (!recentAccounts.isEmpty()) {
    //         // 가장 최근 계정 반환
    //         return Optional.of(recentAccounts.get(0));
    //     }
    //     
    //     return Optional.empty();
    // }

    /**
     * 기존 사용자 ID로 게스트 계정 재사용
     */
    @Transactional
    public TokenResponseDto reuseGuestAccount(Long userId) {
        try {
            // 1. 사용자 존재 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
            
            // 2. 게스트 사용자인지 확인
            if (!user.getIsGuest()) {
                throw new RuntimeException("게스트 사용자가 아닙니다: " + userId);
            }
            
            // 3. 계정 정보 조회
            Account account = accountRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("계정을 찾을 수 없습니다: " + userId));
            
            // 4. 계정이 만료되었는지 확인
            if (account.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("계정이 만료되었습니다: " + userId);
            }
            
            // 5. 만료 시간 연장 (12시간 추가)
            account.setExpiresAt(LocalDateTime.now().plusHours(12));
            account.setLastLoginAt(LocalDateTime.now());
            accountRepository.save(account);
            
            // 6. JWT 토큰 생성
            String token = jwtUtil.generateToken(user.getId());
            
            log.info("기존 게스트 계정 재사용 완료: userId={}, nickname={}, accountId={}", 
                    user.getId(), user.getNickname(), account.getId());
            
            return TokenResponseDto.builder()
                    .accessToken(token)
                    .userId(user.getId())
                    .build();
                    
        } catch (RuntimeException e) {
            // 계정 재사용 실패 시 새 계정 생성
            log.warn("기존 계정 재사용 실패, 새 계정 생성: userId={}, error={}", userId, e.getMessage());
            return createNewGuestAccount();
        } catch (Exception e) {
            log.error("기존 게스트 계정 재사용 중 오류 발생: userId={}", userId, e);
            throw new RuntimeException("기존 게스트 계정 재사용에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 새로운 게스트 계정 생성 및 로그인
     */
    private TokenResponseDto createNewGuestAccount() {
        // 1. 게스트 사용자 생성
        User guestUser = createGuestUser();
        
        // 2. 게스트 계정 생성
        Account guestAccount = createGuestAccount(guestUser);
        
        // 3. JWT 토큰 생성
        String token = jwtUtil.generateToken(guestUser.getId());
        
        log.info("새 게스트 사용자 생성 완료: userId={}, nickname={}, accountId={}", 
                guestUser.getId(), guestUser.getNickname(), guestAccount.getId());
        
        return TokenResponseDto.builder()
                .accessToken(token)
                .userId(guestUser.getId())
                .build();
    }
}
