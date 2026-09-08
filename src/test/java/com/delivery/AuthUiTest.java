package com.delivery;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class AuthUiTest {

    @BeforeAll
    public static void setup() {
        // Настраиваем Selenide перед стартом: говорим, какой браузер открывать.
        // По умолчанию стоит Chrome, но можно написать "safari" или "firefox"
        Configuration.browser = "chrome";

        // Включаем "быстрый режим" без графики (headless), если не хочешь,
        // чтобы окно постоянно всплывало на экране. Но для первого раза
        // оставим false, чтобы ты глазами увидел, как робот кликает по кнопкам!
        Configuration.headless = false;
    }

    @Test
    public void testSuccessfulLoginViaUi() {
        // 1. Открываем наш красивый желтый сайт Самоката в браузере
        open("http://localhost:8080");

        // 2. Находим элемент по его id и вводим текст
        // Символ $ — это фишка Selenide. Он означает "найти элемент"
        $("#username").setValue("ivan_client");
        $("#password").setValue("pass123");

        // 3. Находим кнопку входа по её id и кликаем
        $("#btn-login").click();

        // 4. Проверяем (Assert), что на экране появился личный кабинет
        // Находим элемент приветствия и проверяем, что он стал видимым и содержит нужный текст
        $("#user-display").shouldBe(Condition.visible).shouldHave(Condition.text("ivan_client"));
    }
    @Test
    public void testFullOrderE2eWorkflow() {
        // 1. Открываем сайт и логинимся
        open("http://localhost:8080");
        $("#username").setValue("ivan_client");
        $("#password").setValue("pass123");
        $("#btn-login").click();

        // 2. Набираем корзину: кликаем "Выбрать" на первых двух кнопках меню
        // Используем поиск по тексту на кнопках, так как их несколько одинаковых
        com.codeborne.selenide.Selenide.$$(com.codeborne.selenide.Selectors.byText("🛒 Выбрать")).get(0).click();
        com.codeborne.selenide.Selenide.$$(com.codeborne.selenide.Selectors.byText("🛒 Выбрать")).get(1).click();

        // 3. Заполняем форму доставки (поля адреса и телефона уже заполнены дефолтом, но обновим дистанцию)
        $("#distance").clear();
        $("#distance").setValue("3"); // Ставим 3 км

        // 4. Кликаем на кнопку оформления заказа (открываем модалку банка)
        $("#btn-checkout").click();

        // 5. Проверяем, что модалка банка открылась, и кликаем "Подтвердить оплату"
        $("#btn-confirm-pay").shouldBe(Condition.visible).click();

        // 6. МАГИЯ SELENIDE: Проверяем динамическую смену статусов

        // Ждем еще немного, пока наш фоновый поток бэкенда переведет заказ в доставку и ожидание
        $("#status-result").shouldHave(Condition.text("привез"), java.time.Duration.ofSeconds(15));

        // 7. Кликаем появившуюся зеленую кнопку подтверждения получения клиентом
        $("#btn-accept").shouldBe(Condition.visible).click();

        // Проверяем финальный текст закрытия заказа
        $("#status-result").shouldHave(Condition.text("Доставка успешно подтверждена"));
    }

}

