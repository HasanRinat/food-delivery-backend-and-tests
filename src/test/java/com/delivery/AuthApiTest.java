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
        String uniqueUser = "login_test_user";

        // Сначала ЖЕСТКО РЕГИСТРИРУЕМ этого пользователя в базе GitHub, чтобы он там точно был
        Map<String, String> regBody = new HashMap<>();
        regBody.put("username", uniqueUser);
        regBody.put("password", "pass123");

        // Отправляем скрытый запрос на регистрацию (игнорируем, если он уже создан)
        given().contentType(ContentType.JSON).body(regBody).post("/api/auth/register");

        // ТЕПЕРЬ СПОКОЙНО ЛОГИНИМСЯ — теперь этот пользователь гарантированно есть в Postgres!
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", uniqueUser);
        loginBody.put("password", "pass123");

        String responseBody = given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body(containsString("Успешный вход"))
                .extract().asString();

        System.out.println("🔥 Робот успешно поймал токен из ответа бэкенда:\n" + responseBody);
    }

}
