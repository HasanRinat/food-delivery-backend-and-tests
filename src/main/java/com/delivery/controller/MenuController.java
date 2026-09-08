package com.delivery.controller;

import com.delivery.model.Dish;
import com.delivery.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService = new MenuService();

    // Обновленный метод: если передать параметр maxPrice, сработает фильтр Stream API
    @GetMapping
    public List<Dish> getMenu(@RequestParam(required = false) Double maxPrice) {
        if (maxPrice != null) {
            return menuService.getDishesCheaperThan(maxPrice);
        }
        // Если параметр не передан, возвращаем все блюда, используя Optional как предохранитель
        return menuService.getDishesCheaperThan(Double.MAX_VALUE);
    }

    // Новый эндпоинт для поиска конкретного блюда
    @GetMapping("/search")
    public String searchDish(@RequestParam String name) {
        // Используем Optional: если блюдо есть — выводим цену, если нет — красивую ошибку
        return menuService.findDishByName(name)
                .map(dish -> "🔎 Найдено блюдо: " + dish.getName() + " стоит " + dish.getPrice() + " руб.")
                .orElse("❌ К сожалению, блюдо '" + name + "' не найдено в нашем меню.");
    }
}
