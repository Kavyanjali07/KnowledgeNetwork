package com.knowledgenetwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class KnowledgeNetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeNetworkApplication.class, args);
    }
}
