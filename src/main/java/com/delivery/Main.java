package com.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // Возвращаем запуск полноценного веб-сервера Spring Boot
        SpringApplication.run(Main.class, args);
    }
}
