package com.aiplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class JuzhiApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JuzhiApiApplication.class, args);
    }
}
