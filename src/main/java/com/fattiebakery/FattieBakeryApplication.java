package com.fattiebakery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // 1. Import thư viện này vào

@SpringBootApplication
@EnableScheduling
public class FattieBakeryApplication {

    public static void main(String[] args) {
        SpringApplication.run(FattieBakeryApplication.class, args);
    }

}