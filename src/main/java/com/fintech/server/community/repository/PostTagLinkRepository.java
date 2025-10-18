package com.fintech.server.community.repository;

import com.fintech.server.community.entity.PostTagLink;
import com.fintech.server.community.entity.PostTagLinkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostTagLinkRepository extends JpaRepository<PostTagLink, PostTagLinkId> {
    
    /**
     * 특정 게시글의 모든 태그 링크를 삭제합니다.
     * 게시글 수정/삭제 시 사용됩니다.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PostTagLink ptl WHERE ptl.id.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
