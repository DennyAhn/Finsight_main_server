package com.fintech.server.service;

import com.fintech.server.entity.Account;
import com.fintech.server.repository.AccountRepository;
import com.fintech.server.repository.UserRepository;
import com.fintech.server.quiz.repository.UserAnswerRepository;
import com.fintech.server.quiz.repository.UserDailyActivityRepository;
import com.fintech.server.quiz.repository.UserProgressRepository;
import com.fintech.server.quiz.repository.UserWrongNoteRepository;
import com.fintech.server.community.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestAccountCleanupService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserDailyActivityRepository userDailyActivityRepository;
    private final UserWrongNoteRepository userWrongNoteRepository;
    private final UserProgressRepository userProgressRepository;
    private final CommunityPostRepository communityPostRepository;

    /**
     * 만료된 게스트 계정 정리 (매 시간마다 실행)
     */
    @Scheduled(fixedRate = 3600000) // 1시간 = 3600000ms
    @Transactional
    public void cleanupExpiredGuestAccounts() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 만료된 게스트 계정 조회
            List<Account> expiredAccounts = accountRepository.findExpiredGuestAccounts(now);
            
            if (expiredAccounts.isEmpty()) {
                log.info("만료된 게스트 계정이 없습니다.");
                return;
            }
            
            log.info("만료된 게스트 계정 {}개를 정리합니다.", expiredAccounts.size());
            
            for (Account account : expiredAccounts) {
                Long userId = account.getUser().getId();
                
                // 관련 데이터 삭제 (외래키 순서대로)
                userAnswerRepository.deleteByUserId(userId);
                userDailyActivityRepository.deleteByIdUserId(userId);
                userProgressRepository.deleteByUserId(userId);
                userWrongNoteRepository.deleteByUserId(userId);
                communityPostRepository.deleteByAuthorId(userId); // 커뮤니티 게시글 삭제 추가
                
                // 계정 삭제
                accountRepository.delete(account);
                
                // 사용자 삭제
                userRepository.deleteById(userId);
                
                log.info("게스트 계정 삭제 완료: userId={}, email={}", 
                        userId, account.getEmail());
            }
            
            log.info("게스트 계정 정리 완료: {}개 계정 삭제", expiredAccounts.size());
            
        } catch (Exception e) {
            log.error("게스트 계정 정리 중 오류 발생", e);
        }
    }

    /**
     * 수동으로 만료된 게스트 계정 정리
     */
    @Transactional
    public int cleanupExpiredGuestAccountsManually() {
        LocalDateTime now = LocalDateTime.now();
        List<Account> expiredAccounts = accountRepository.findExpiredGuestAccounts(now);
        
        for (Account account : expiredAccounts) {
            Long userId = account.getUser().getId();
            
            // 관련 데이터 삭제
            userAnswerRepository.deleteByUserId(userId);
            userDailyActivityRepository.deleteByIdUserId(userId);
            userProgressRepository.deleteByUserId(userId);
            userWrongNoteRepository.deleteByUserId(userId);
            communityPostRepository.deleteByAuthorId(userId); // 커뮤니티 게시글 삭제 추가
            
            // 계정 및 사용자 삭제
            accountRepository.delete(account);
            userRepository.deleteById(userId);
        }
        
        return expiredAccounts.size();
    }
}
