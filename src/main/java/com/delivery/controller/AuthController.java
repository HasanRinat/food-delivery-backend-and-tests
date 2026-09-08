package com.delivery.controller;

import com.delivery.model.User;
import com.delivery.model.UserFactory;
import com.delivery.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository = new UserRepository();

    // 1. ЭНДПОИНТ РЕГИСТРАЦИИ КЛИЕНТА
    @PostMapping("/register")
    public String register(@RequestBody User request) {
        // Проверяем, нет ли уже такого пользователя
        if (userRepository.findByUsername(request.getUsername()) != null) {
            return "❌ Ошибка: Логин уже занят!";
        }

        // Создаем пользователя-клиента через нашу Фабрику
        User newClient = UserFactory.createClient(request.getUsername(), request.getPassword());

        // Сохраняем в Postgres
        userRepository.saveUser(newClient);
        return "✅ Пользователь " + request.getUsername() + " успешно зарегистрирован!";
    }

    // 2. ЭНДПОИНТ ЛОГИНА (ВХОДА)
    @PostMapping("/login")
    public String login(@RequestBody User request) {
        // Ищем пользователя в базе данных
        User userFromDb = userRepository.findByUsername(request.getUsername());

        if (userFromDb == null) {
            return "❌ Ошибка: Пользователь не найден!";
        }

        // Проверяем пароль
        if (!userFromDb.getPassword().equals(request.getPassword())) {
            return "❌ Ошибка: Неверный пароль!";
        }

        // Генерируем уникальный случайный токен сессии (имитация JWT-токена)
        String fakeJwtToken = "fake-jwt-token-" + UUID.randomUUID() + "-role-" + userFromDb.getRole();

        return "✅ Успешный вход! Твой токен доступа:\n" + fakeJwtToken;
    }
}
