package com.fintech.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing // @CreatedDate, @LastModifiedDate 어노테이션을 활성화합니다.
// ▼▼▼ 아래 두 어노테이션을 추가하여 스캔 경로를 명확히 지정합니다. ▼▼▼
@EnableJpaRepositories(basePackages = {"com.fintech.server.repository", "com.fintech.server.quiz.repository"})
@EntityScan(basePackages = {"com.fintech.server.entity", "com.fintech.server.quiz.entity"})
public class FinMainServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinMainServerApplication.class, args);
    }
}