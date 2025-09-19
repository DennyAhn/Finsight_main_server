package com.fintech.server.repository;

import com.fintech.server.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * 이메일로 계정을 조회합니다.
     * @param email 조회할 이메일
     * @return Optional<Account>
     */
    Optional<Account> findByEmail(String email);
}
