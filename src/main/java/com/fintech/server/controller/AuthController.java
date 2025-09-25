package com.fintech.server.controller;

import com.fintech.server.dto.AuthRequestDto;
import com.fintech.server.dto.TokenResponseDto;
import com.fintech.server.entity.User;
import com.fintech.server.service.UserService;
import com.fintech.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API를 제공하는 컨트롤러
 *
 * 📌 프론트엔드 개발자를 위한 API 가이드:
 *
 * 🔗 Base URL: /api/auth (context-path + controller path)
 *
 * 📋 제공하는 API:
 * - POST /api/auth/signup : 회원가입
 * - POST /api/auth/login  : 로그인 (임시 구현)
 *
 * 💡 사용 예시:
 * - axios.post('/api/auth/signup', { email, password, nickname })
 * - axios.post('/api/auth/login', { email, password })
 *
 * ⚠️ 주의사항:
 * - 현재 로그인은 임시 토큰을 반환합니다 (JWT 구현 예정)
 * - 회원가입 시 이메일과 닉네임 중복 확인이 수행됩니다
 */
@RestController
@RequestMapping("/auth") // context-path(/api)를 제외한 상세 주소만 설정
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 회원가입 API
     *
     * 🌐 HTTP Method: POST
     * 🔗 URL: /api/auth/signup
     *
     * 📥 Request Body:
     * {
     * "email": "user@example.com",     // 필수: 이메일 (중복 불가)
     * "password": "password123",       // 필수: 비밀번호 (암호화되어 저장)
     * "nickname": "사용자닉네임"        // 필수: 닉네임 (중복 불가)
     * }
     *
     * 📤 Response (200 OK):
     * "회원가입이 성공적으로 완료되었습니다."
     *
     * 📤 Error Response (400 Bad Request):
     * - 이메일 중복: "이미 가입된 이메일입니다: user@example.com"
     * - 닉네임 중복: "이미 사용 중인 닉네임입니다: 사용자닉네임"
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AuthRequestDto requestDto) {
        try {
            User savedUser = userService.signup(requestDto);
            // 'username' 대신 'nickname'을 사용하도록 수정
            log.info("새 사용자 회원가입 완료: email={}, nickname={}",
                    savedUser.getEmail(), savedUser.getNickname());
            return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("회원가입 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
    
    /**
     * 로그인 API (임시 구현)
     *
     * 🌐 HTTP Method: POST
     * 🔗 URL: /api/auth/login
     *
     * 📥 Request Body:
     * {
     * "email": "user@example.com",     // 필수: 로그인 이메일
     * "password": "password123"        // 필수: 비밀번호
     * }
     *
     * 📤 Response (200 OK):
     * {
     * "accessToken": "임시_액세스_토큰_입니다._나중에_JWT로_교체하세요."
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody AuthRequestDto requestDto) {
        // [!] 중요: 이 부분은 실제 로그인 및 JWT 토큰 생성 로직으로 교체해야 합니다.
        log.info("로그인 시도: email={}", requestDto.getEmail());
        
        // TODO: 실제 로그인 로직 구현 (이메일/비밀번호 검증, JWT 토큰 생성)
        
        TokenResponseDto token = TokenResponseDto.builder()
                .accessToken("임시_액세스_토큰_입니다._나중에_JWT로_교체하세요.")
                .build();
        
        log.info("임시 토큰 발급 완료: email={}", requestDto.getEmail());
        return ResponseEntity.ok(token);
    }

    /**
     * 게스트 로그인 API
     *
     * 🌐 HTTP Method: POST
     * 🔗 URL: /api/auth/guest
     *
     * 📥 Request Body: 없음 (빈 요청)
     *
     * 📤 Response (200 OK):
     * {
     *   "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
     *   "userId": 123
     * }
     *
     * 📤 Error Response (500 Internal Server Error):
     * - 게스트 사용자 생성 실패 시
     */
    @PostMapping("/guest")
    public ResponseEntity<?> guestLogin() {
        try {
            TokenResponseDto response = authService.createGuestUserAndLogin();
            log.info("게스트 로그인 성공: userId={}", response.getUserId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("게스트 로그인 실패", e);
            return ResponseEntity.internalServerError()
                    .body("게스트 로그인에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("게스트 로그인 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("게스트 로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}

