package com.fintech.server.config;

import com.fintech.server.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 토큰을 검증하고 Spring Security Context에 인증 정보를 설정하는 필터
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        // Authorization 헤더가 있고 Bearer로 시작하는 경우에만 처리
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                // JWT 토큰에서 사용자 ID 추출
                Long userId = authService.getUserIdFromToken(authHeader);
                
                // 토큰이 유효한 경우 Spring Security Context에 인증 정보 설정
                if (userId != null && authService.validateToken(authHeader)) {
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userId, 
                            null, 
                            Collections.emptyList()
                        );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("JWT 토큰 인증 성공: userId={}", userId);
                }
            } catch (Exception e) {
                log.warn("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
                // 토큰이 유효하지 않아도 요청은 계속 진행 (permitAll 설정 때문)
            }
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 인증이 필요 없는 경로는 필터를 건너뛰도록 설정
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 인증이 필요 없는 경로들
        return path.startsWith("/api/auth/") || 
               path.startsWith("/api/health/") ||
               path.equals("/api/health");
    }
}
