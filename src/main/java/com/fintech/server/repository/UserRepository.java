package com.fintech.server.repository;

import com.fintech.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 닉네임으로 사용자를 조회합니다.
     * @param nickname 조회할 닉네임
     * @return Optional<User>
     */
    Optional<User> findByNickname(String nickname);
}
