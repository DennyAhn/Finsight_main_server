package com.fintech.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF(Cross-Site Request Forgery) 보호 비활성화
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. CORS(Cross-Origin Resource Sharing) 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 3. 세션을 사용하지 않는 Stateless 방식으로 설정
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 4. JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 앞에 추가)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 5. 모든 HTTP 요청에 대한 접근을 허용 (JWT 토큰이 있으면 인증 정보 설정)
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // 모든 경로에 대해 인증 없이 접근 허용
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .contentTypeOptions(contentTypeOptions -> contentTypeOptions.and())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig.disable())
            );

        return http.build();
    }
            
    

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

    /*
     * 🔒 향후 인증이 필요한 경우 사용할 설정 (현재는 비활성화)
     * 
     * public SecurityFilterChain securityFilterChainWithAuth(HttpSecurity http) throws Exception {
     *     http
     *         .csrf(AbstractHttpConfigurer::disable)
     *         .cors(cors -> cors.configurationSource(corsConfigurationSource()))
     *         .sessionManagement(session -> 
     *             session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
     *         .authorizeHttpRequests(authz -> authz
     *             // context-path(/api)를 제외한 내부 경로를 기준으로 규칙을 작성합니다.
     *             .requestMatchers("/auth/**").permitAll() 
     *             .requestMatchers("/quizzes/**").permitAll()
     *             // 나중에 추가할 다른 공개 API 경로들도 여기에 추가합니다.
     *             
     *             // 위에서 허용한 경로 외의 모든 요청은 인증이 필요합니다.
     *             .anyRequest().authenticated() 
     *         );
     * 
     *     return http.build();
     * }
     */