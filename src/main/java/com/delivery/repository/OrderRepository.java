package com.delivery.repository;

import com.delivery.service.OrderLifecycleService;
import java.sql.*;

public class OrderRepository {

    private final OrderLifecycleService lifecycleService = new OrderLifecycleService();

    public String createComplexOrder(String username, String itemsSummary, double itemsPrice, String address, String phone, int distance) {
        // Исключение: Проверка расстояния
        if (distance > 15) {
            return "❌ ОТКАЗ: Расстояние " + distance + " км слишком большое. Доставка осуществляется только до 15 км!";
        }

        // Считаем стоимость доставки в зависимости от расстояния
        double deliveryPrice = (distance <= 5) ? 100.00 : 300.00;
        double totalPrice = itemsPrice + deliveryPrice;

        // Ищем курьера
        String courierName = findAvailableCourier();
        if (courierName == null) {
            return "❌ Извините, сейчас нет свободных курьеров. Попробуйте заказать позже.";
        }

        String sql = "INSERT INTO orders (username, items_summary, delivery_address, client_phone, distance_km, delivery_price, total_price, status, courier_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, itemsSummary);
            pstmt.setString(3, address);
            pstmt.setString(4, phone);
            pstmt.setInt(5, distance);
            pstmt.setDouble(6, deliveryPrice);
            pstmt.setDouble(7, totalPrice);
            pstmt.setString(8, courierName);
            pstmt.executeUpdate();

            // Получаем ID созданного заказа, чтобы передать его в поток симуляции
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);

                    // Запускаем МНОГОПОТОЧНЫЙ конвейер статусов!
                    lifecycleService.startOrderLifecycleAsync(orderId, username, courierName);

                    return "✅ Оплата прошла успешно! Заказ #" + orderId + " создан. Сумма: " + totalPrice + " ₽ (Доставка: " + deliveryPrice + " ₽). Курьер: " + courierName;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "❌ Ошибка при создании заказа";
    }

    private String findAvailableCourier() {
        String findSql = "SELECT username FROM users WHERE role = 'COURIER' AND is_available = true LIMIT 1";
        String updateSql = "UPDATE users SET is_available = false WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(findSql)) {
            if (rs.next()) {
                String courier = rs.getString("username");
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(1, courier);
                    pstmt.executeUpdate();
                }
                return courier;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Получить полную информацию о последнем заказе
    public String getFullOrderStatusJson(String username) {
        String sql = "SELECT id, status, courier_name, items_summary, total_price FROM orders WHERE username = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return String.format("{\"id\":%d,\"status\":\"%s\",\"courier\":\"%s\",\"items\":\"%s\",\"total\":%.2f}",
                            rs.getInt("id"), rs.getString("status"), rs.getString("courier_name"), rs.getString("items_summary"), rs.getDouble("total_price"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "{}";
    }
}
