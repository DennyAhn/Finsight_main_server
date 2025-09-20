package com.fintech.server.quiz.repository;

import com.fintech.server.quiz.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    @Query("SELECT DISTINCT s FROM Sector s LEFT JOIN FETCH s.subsectors ORDER BY s.id ASC")
    List<Sector> findAllWithSubsectors();
}
