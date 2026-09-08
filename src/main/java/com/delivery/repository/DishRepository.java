package com.delivery.repository;

import com.delivery.model.Dish;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishRepository {

    // МЕТОД: Достать все блюда из базы данных Postgres
    public List<Dish> getAllDishes() {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT name, price, weight, is_available FROM dishes";

        // try-with-resources: автоматически закроет соединение и statement после работы
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) { // Выполняем SQL-запрос

            // rs.next() бежит по строчкам таблицы из БД, пока они не закончатся
            while (rs.next()) {
                // Вытаскиваем данные из колонок текущей строки
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int weight = rs.getInt("weight");
                boolean isAvailable = rs.getBoolean("is_available");

                // Превращаем строку из БД в реальный Java-объект Dish!
                Dish dish = new Dish(name, price, weight, isAvailable);

                // Складываем в наш динамический список
                dishes.add(dish);
            }

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при чтении меню из базы данных!");
            e.printStackTrace();
        }

        return dishes;
    }
    // МЕТОД: Добавить новое блюдо в базу данных Postgres
    public void addDish(Dish dish) {
        // Знаки вопросов ? — это плейсхолдеры, куда Java безопасно подставит реальные данные
        String sql = "INSERT INTO dishes (name, price, weight, is_available) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Подставляем данные из объекта Dish вместо знаков вопроса по порядку
            pstmt.setString(1, dish.getName());
            pstmt.setDouble(2, dish.getPrice());
            pstmt.setInt(3, dish.getWeight());
            pstmt.setBoolean(4, dish.isAvailable());

            // Выполняем запрос на запись в базу
            pstmt.executeUpdate();
            System.out.println("🚀 Блюдо успешно сохранено в БД: " + dish.getName());

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при добавлении блюда в базу данных!");
            e.printStackTrace();
        }
    }

}
