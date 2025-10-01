package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    // 기본적인 CRUD 작업은 JpaRepository에서 제공됩니다.
    // 필요시 커스텀 쿼리 메서드를 여기에 추가할 수 있습니다.
}
