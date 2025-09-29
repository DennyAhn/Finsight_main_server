package com.fintech.server.community.repository;

import com.fintech.server.community.entity.PostTagLink;
import com.fintech.server.community.entity.PostTagLinkId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagLinkRepository extends JpaRepository<PostTagLink, PostTagLinkId> {
}
