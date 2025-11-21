package com.catface996.aiops.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * AIOps Service Application Entry Point
 *
 * @author catface996
 * @since 2025-11-21
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.catface996.aiops")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
