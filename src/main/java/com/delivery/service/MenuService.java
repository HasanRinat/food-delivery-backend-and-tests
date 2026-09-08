package com.delivery.service;

import com.delivery.model.Dish;
import com.delivery.repository.DishRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MenuService {
    private final DishRepository dishRepository = new DishRepository();

    // 1. СТРИМ АПИ: Фильтрация меню по максимальной цене
    public List<Dish> getDishesCheaperThan(double maxPrice) {
        List<Dish> allDishes = dishRepository.getAllDishes();

        // Превращаем список в поток (Stream), фильтруем и собираем обратно в List
        return allDishes.stream()
                .filter(dish -> dish.getPrice() <= maxPrice) // Фильтр: оставляем только дешевле maxPrice
                .filter(Dish::isAvailable)                  // Фильтр: только те, что есть в наличии
                .collect(Collectors.toList());               // Собираем результат в новый список
    }

    // 2. OPTIONAL: Поиск блюда по точному названию
    // Мы возвращаем Optional, потому что блюда с таким именем в базе может не быть (защита от NullPointerException)
    public Optional<Dish> findDishByName(String name) {
        List<Dish> allDishes = dishRepository.getAllDishes();

        return allDishes.stream()
                .filter(dish -> dish.getName().equalsIgnoreCase(name))
                .findFirst(); // Возвращает Optional<Dish> — коробку, которая может быть пустой или с блюдом
    }
}
