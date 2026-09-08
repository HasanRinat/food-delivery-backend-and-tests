package com.delivery.service;

import com.delivery.repository.DatabaseConfig;
import java.sql.*;
import java.util.List;

public class OrderLifecycleService {

    // 1. МЕТОД СОЗДАНИЯ ЗАКАЗА И ЗАПУСКА ТАЙМЕРОВ
    public String createOrder(String username, List<String> dishNames, String address, String phone, int distance) {
        // Ограничение по расстоянию бизнеса
        if (distance > 15) {
            return "❌ К сожалению, доставка дальше 15 км невозможна!";
        }

        // Считаем цену (базовая логика: 300 руб за еду + 50 руб за каждый км)
        double total = 300.0 + (distance * 50.0);
        String summary = String.join(", ", dishNames);

        // Ищем свободного курьера прямо в момент заказа
        String courierName = findAvailableCourier();
        if (courierName == null) {
            return "❌ Ошибка: Нет свободных курьеров! Попробуйте позже.";
        }

        // Блокируем курьера, чтобы он не взял другой заказ
        lockCourier(courierName);

        String sql = "INSERT INTO orders (username, items_summary, total_price, status, courier_name, address, phone, distance_km) VALUES (?, ?, ?, 'PENDING', ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, summary);
            pstmt.setDouble(3, total);
            pstmt.setString(4, courierName);
            pstmt.setString(5, address);
            pstmt.setString(6, phone);
            pstmt.setInt(7, distance);
            pstmt.executeUpdate();

            // Вытаскиваем ID сгенерированного заказа, чтобы передать его в поток таймеров
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);

                    // 🔥 ЗАПУСКАЕМ ТВОЙ АСИНХРОННЫЙ ПОТОК ТАЙМЕРОВ!
                    startOrderLifecycleAsync(orderId, username, courierName);
                }
            }

            return "✅ Заказ успешно оформлен и передан в обработку! Итого: " + total + " руб.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Критическая ошибка при записи заказа в БД";
        }
    }

    // 2. МЕТОД ПОЛУЧЕНИЯ СТАТУСА ДЛЯ КОНТРОЛЛЕРА
    public String getOrderStatus(String username) {
        String sql = "SELECT status, courier_name, items_summary FROM orders WHERE username = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    String courier = rs.getString("courier_name");
                    String items = rs.getString("items_summary");

                    if ("WAITING_CONFIRMATION".equals(status)) {
                        return "📦 Курьер " + courier + " привез [" + items + "]! Ожидает у двери. Подтвердите получение.";
                    }
                    return "Статус заказа: " + status + " | Курьер: " + courier;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "У вас пока нет активных заказов.";
    }

    // 3. МЕТОД ПОДТВЕРЖДЕНИЯ ЗАКАЗА КЛИЕНТОМ (Твоя логика confirmOrderCompletion)
    public String acceptOrder(String username) {
        // Находим последний заказ пользователя, чтобы узнать id и имя курьера
        String findSql = "SELECT id, courier_name FROM orders WHERE username = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(findSql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int orderId = rs.getInt("id");
                    String courierName = rs.getString("courier_name");

                    // Вызываем твой метод финального закрытия
                    confirmOrderCompletion(orderId, courierName);
                    return "✅ Доставка успешно подтверждена. Спасибо за заказ!";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "❌ Не удалось найти заказ для подтверждения.";
    }

    // Твой асинхронный поток таймеров (оставил как у тебя, 5 сек для удобства)
    public void startOrderLifecycleAsync(int orderId, String username, String courierName) {
        Thread lifecycleThread = new Thread(() -> {
            try {
                // Фаза 0: Заказ оформлен, но ждем оплату (уже сделано в БД при создании)
                System.out.println("💳 [Заказ #" + orderId + "]: Ожидает оплаты от клиента " + username);
                Thread.sleep(3000); // Даем пару секунд на модалку

                // Фаза 1: Оплачено и готовится (Ждем 5 секунд для тестов)
                updateStatus(orderId, "COOKING");
                System.out.println("🍳 [Заказ #" + orderId + "]: Оплачен! Передан на кухню. Началось приготовление.");
                Thread.sleep(5000);

                // Фаза 2: Передано в доставку (Курьер забрал)
                updateStatus(orderId, "DELIVERING");
                System.out.println("🛵 [Заказ #" + orderId + "]: Курьер " + courierName + " забрал заказ и выехал.");
                Thread.sleep(5000);

                // ... остальной код (WAITING_CONFIRMATION) остается без изменений
                updateStatus(orderId, "WAITING_CONFIRMATION");
                System.out.println("📦 [Заказ #" + orderId + "]: Курьер на месте. Ожидает подтверждения клиента.");

            } catch (InterruptedException e) {
                System.err.println("❌ Поток жизненного цикла заказа был прерван!");
            }
        });
        lifecycleThread.start();
    }

    private void updateStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void confirmOrderCompletion(int orderId, String courierName) {
        String sqlOrder = "UPDATE orders SET status = 'COMPLETED' WHERE id = ?";
        String sqlCourier = "UPDATE users SET is_available = true WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(sqlOrder);
                 PreparedStatement p2 = conn.prepareStatement(sqlCourier)) {

                p1.setInt(1, orderId);
                p1.executeUpdate();

                p2.setString(1, courierName);
                p2.executeUpdate();

                conn.commit();
                System.out.println("✅ [Заказ #" + orderId + "]: Клиент подтвердил доставку. Курьер " + courierName + " свободен.");
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Вспомогательные методы поиска и блокировки курьера в БД
    private String findAvailableCourier() {
        String sql = "SELECT username FROM users WHERE role = 'COURIER' AND is_available = true LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void lockCourier(String courierName) {
        String sql = "UPDATE users SET is_available = false WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courierName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
