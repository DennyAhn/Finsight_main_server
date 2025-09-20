package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.Subsector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SubsectorRepository extends JpaRepository<Subsector, Long> {
    @Query("SELECT ss FROM Subsector ss LEFT JOIN FETCH ss.levels WHERE ss.id = :id")
    Optional<Subsector> findByIdWithLevels(@Param("id") Long id);
}
