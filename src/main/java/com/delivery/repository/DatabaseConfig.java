package com.delivery.repository;

// Импортируем стандартные классы Java для работы с SQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    // Прописываем данные для подключения (те же, что вводили в Docker и DBeaver)
    private static final String URL = "jdbc:postgresql://localhost:5432/food_delivery_db";
    private static final String USER = "delivery_admin";
    private static final String PASSWORD = "secret_pass";

    // Метод, который будет возвращать активное соединение с базой данных
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
