package com.maxmin.tda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringBootFormHandingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootFormHandingApplication.class, args);
    }
}