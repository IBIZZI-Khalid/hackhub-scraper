package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.hackhub", "com.khalid.scraper" })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
