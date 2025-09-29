package com.fintech.server.community.controller;

import com.fintech.server.community.dto.PostRequestDto;
import com.fintech.server.community.dto.PostResponseDto;
import com.fintech.server.community.service.CommunityService;
import com.fintech.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/community/posts") // context-path(/api) 제외
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostRequestDto requestDto, 
                                                     HttpServletRequest request) {
        try {
            // Spring Security Context에서 인증된 사용자 ID 가져오기
            Long currentUserId = getCurrentUserId(request);
            System.out.println("Current User ID: " + currentUserId);

            PostResponseDto responseDto = communityService.createPost(requestDto, currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 현재 인증된 사용자 ID를 가져오는 메서드
     * JWT 토큰이 있으면 토큰에서 추출, 없으면 기존 게스트 사용자 ID 사용
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            // 1. Spring Security Context에서 인증된 사용자 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof Long) {
                Long authenticatedUserId = (Long) authentication.getPrincipal();
                return authenticatedUserId;
            }
            
            // 2. Authorization 헤더에서 JWT 토큰 확인
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authService.validateToken(authHeader)) {
                return authService.getUserIdFromToken(authHeader);
            }
            
        } catch (Exception e) {
            // JWT 토큰 처리 실패시 기존 게스트 사용자 ID 사용
        }
        
        // 3. 토큰이 없거나 유효하지 않은 경우 기존 게스트 사용자 ID 64 사용
        return 64L;
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        List<PostResponseDto> posts = communityService.findAllPosts();
        return ResponseEntity.ok(posts);
    }
}
