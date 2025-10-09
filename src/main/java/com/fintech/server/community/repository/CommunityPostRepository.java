package com.fintech.server.community.repository;

import com.fintech.server.community.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.author a LEFT JOIN FETCH p.authorBadge")
    List<CommunityPost> findAllWithAuthorAndBadge();
    
    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.author a LEFT JOIN FETCH p.authorBadge LEFT JOIN FETCH p.tags t LEFT JOIN FETCH t.tag WHERE p.id = :id")
    Optional<CommunityPost> findByIdWithAllDetails(@Param("id") Long id);
    
    /**
     * 특정 사용자가 작성한 모든 게시글을 삭제합니다.
     * 게스트 계정 정리 시 사용됩니다.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM CommunityPost p WHERE p.author.id = :authorId")
    void deleteByAuthorId(@Param("authorId") Long authorId);
}
