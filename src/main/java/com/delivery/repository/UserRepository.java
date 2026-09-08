package com.delivery.repository;

import com.delivery.model.User;
import com.delivery.model.Role;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    // 1. Поиск пользователя по логину (нужно для авторизации)
    public User findByUsername(String username) {
        String sql = "SELECT username, password, role FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Вытаскиваем роль как строку и превращаем её обратно в Enum Java
                    Role role = Role.valueOf(rs.getString("role"));
                    return new User(rs.getString("username"), rs.getString("password"), role);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Если пользователь не найден
    }

    // 2. Сохранение нового пользователя в базу данных (нужно для регистрации)
    public void saveUser(User user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // В реальных проектах тут делают хэширование!
            pstmt.setString(3, user.getRole().name()); // Переводим Enum в строку для БД

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

