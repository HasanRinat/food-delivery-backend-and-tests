package com.delivery.controller;

import com.delivery.service.OrderLifecycleService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // Подключаем наш единый сервис жизненного цикла заказа
    private final OrderLifecycleService orderLifecycleService = new OrderLifecycleService();

    // 1. ОФОРМЛЕНИЕ ЗАКАЗА (Клиент отправляет форму с фронтенда)
    @PostMapping("/checkout")
    public String checkout(
            @RequestParam String username,
            @RequestParam List<String> dishNames, // Принимаем список выбранных блюд
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam int distance) {

        // Передаем все данные в наш сервис, где крутятся таймеры по 30 секунд
        return orderLifecycleService.createOrder(username, dishNames, address, phone, distance);
    }

    // 2. ОТСЛЕЖИВАНИЕ СТАТУСА (Фронтенд автоматически опрашивает каждые 3 секунды)
    @GetMapping("/status")
    public String getStatus(@RequestParam String username) {
        return orderLifecycleService.getOrderStatus(username);
    }

    // 3. ПОДТВЕРЖДЕНИЕ ПОЛУЧЕНИЯ (Кнопка "Подтвердить" на экране клиента)
    @PostMapping("/accept")
    public String acceptOrder(@RequestParam String username) {
        return orderLifecycleService.acceptOrder(username);
    }
}
