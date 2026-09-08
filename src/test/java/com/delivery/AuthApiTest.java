package com.delivery;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class AuthApiTest {

    @BeforeAll
    public static void setup() {
        // Настраиваем базовый URL для Rest Assured.
        // Теперь перед каждым запросом не нужно писать http://localhost:8080
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Test
    public void testSuccessfulRegistration() {
        // Генерируем случайное имя пользователя, чтобы тест не падал при повторном запуске
        // (ведь логин в БД должен быть уникальным UNIQUE)
        String randomUsername = "test_user_" + UUID.randomUUID().toString().substring(0, 5);

        // Создаем тело запроса (JSON) в виде Java-карты (Map) — ключ/значение
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", randomUsername);
        requestBody.put("password", "secret123");

        // Магия синтаксиса Rest Assured: GIVEN -> WHEN -> THEN
        given()
                .contentType(ContentType.JSON) // Говорим серверу: "Мы шлем JSON"
                .body(requestBody)             // Кладем карту в тело запроса (Rest Assured сам превратит её в JSON)
                .when()
                .post("/api/auth/register")    // Отправляем POST запрос на этот эндпоинт
                .then()
                .statusCode(200)               // Проверяем (Assert), что сервер ответил статусом 200 OK
                .body(containsString("успешно зарегистрирован")); // Проверяем, что в ответе есть этот текст
    }
    @Test
    public void testSuccessfulLoginAndGetToken() {
        // 1. GIVEN: Берем существующего пользователя, который точно есть в БД (мы заливали его через скрипт)
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "ivan_client");
        loginBody.put("password", "pass123");

        // 2. WHEN & THEN: Отправляем запрос и проверяем ответ
        String responseBody = given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200) // Проверяем, что код 200 OK
                .body(containsString("Успешный вход")) // Проверяем текст
                .extract().asString(); // ВЫТАСКИВАЕМ весь текст ответа, чтобы прочитать токен!

        // Печатаем токен в консоль тестов, чтобы убедиться, что мы его поймали
        System.out.println("🔥 Робот успешно поймал токен из ответа бэкенда:\n" + responseBody);
    }
}
