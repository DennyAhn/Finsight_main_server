package com.fintech.server.quiz.service;

import com.fintech.server.quiz.repository.UserBadgeRepository;
import com.fintech.server.repository.UserRepository;
import com.fintech.server.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게스트 사용자의 벳지 데이터 정리 서비스
 * 12시간 후 만료된 게스트 사용자의 벳지 데이터를 자동으로 삭제합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuestBadgeCleanupService {

    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;

    /**
     * 매 1시간마다 실행되는 스케줄러
     * 만료된 게스트 사용자의 벳지 데이터를 정리합니다.
     */
    @Scheduled(fixedRate = 3600000) // 1시간 = 3600000ms
    @Transactional
    public void cleanupExpiredGuestBadges() {
        try {
            LocalDateTime twelveHoursAgo = LocalDateTime.now().minusHours(12);
            
            // 12시간 전에 생성된 게스트 사용자들 조회
            List<User> expiredGuestUsers = userRepository.findExpiredGuestUsers(twelveHoursAgo);
            
            if (expiredGuestUsers.isEmpty()) {
                log.info("만료된 게스트 사용자의 벳지 데이터가 없습니다.");
                return;
            }
            
            int cleanedCount = 0;
            for (User guestUser : expiredGuestUsers) {
                // 해당 게스트 사용자의 모든 벳지 데이터 삭제
                int deletedBadges = userBadgeRepository.deleteByUser_Id(guestUser.getId());
                cleanedCount += deletedBadges;
                
                log.info("게스트 사용자 {}의 벳지 데이터 {}개를 삭제했습니다.", 
                        guestUser.getId(), deletedBadges);
            }
            
            log.info("총 {}개의 만료된 게스트 사용자 벳지 데이터를 정리했습니다.", cleanedCount);
            
        } catch (Exception e) {
            log.error("게스트 사용자 벳지 데이터 정리 중 오류 발생", e);
        }
    }
}
