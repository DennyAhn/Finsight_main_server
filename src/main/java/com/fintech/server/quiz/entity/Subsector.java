package com.fintech.server.quiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "subsectors")
@Getter
@Setter
public class Subsector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DB의 'sector_id' 컬럼과 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;
    
    // DB 스키마와 일치하도록 description 컬럼 추가
    @Column(columnDefinition = "TEXT")
    private String description;

    // DB 스키마와 일치하도록 sort_order 컬럼 추가
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @OneToMany(mappedBy = "subsector", fetch = FetchType.LAZY)
    @OrderBy("levelNumber ASC")
    private List<Level> levels;
}