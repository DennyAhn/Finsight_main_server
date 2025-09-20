package com.fintech.server.quiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "sectors")
@Getter
@Setter
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    // DB 스키마와 일치하도록 description 컬럼 추가
    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "sector", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<Subsector> subsectors;
}