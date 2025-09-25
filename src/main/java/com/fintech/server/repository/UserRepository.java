package com.fintech.server.repository;

import com.fintech.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 닉네임으로 사용자를 조회합니다.
     * @param nickname 조회할 닉네임
     * @return Optional<User>
     */
    Optional<User> findByNickname(String nickname);
    
    /**
     * 만료된 게스트 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.isGuest = true AND u.createdAt < :expiredTime")
    List<User> findExpiredGuestUsers(@Param("expiredTime") LocalDateTime expiredTime);
}
