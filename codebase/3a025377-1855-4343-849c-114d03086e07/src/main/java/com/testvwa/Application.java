package com.testvwa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        // VULNERABILITY: Application runs with default security settings
        System.setProperty("spring.profiles.active", "dev");
        SpringApplication.run(Application.class, args);
    }
}
