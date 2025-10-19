package com.fintech.server.repository;

import com.fintech.server.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * 이메일로 계정을 조회합니다.
     * @param email 조회할 이메일
     * @return Optional<Account>
     */
    Optional<Account> findByEmail(String email);
    
    /**
     * 만료된 게스트 계정 조회 (User와 함께 페치)
     */
    @Query("SELECT a FROM Account a JOIN FETCH a.user u WHERE u.isGuest = true AND a.expiresAt < :now")
    List<Account> findExpiredGuestAccounts(@Param("now") LocalDateTime now);
}
