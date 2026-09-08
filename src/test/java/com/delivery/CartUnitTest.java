package com.delivery;

import com.delivery.model.Cart;
import com.delivery.model.Dish;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartUnitTest {

    @Test // Эта аннотация говорит Java: "Это автотест!"
    public void testCartTotalCalculation() {
        // 1. GIVEN (Дано): Создаем корзину и два блюда
        Cart cart = new Cart();
        Dish pizza = new Dish("Пицца", 500.0, 400, true);
        Dish fries = new Dish("Картошка", 150.0, 150, true);

        // 2. WHEN (Когда): Добавляем их в корзину
        cart.addDish(pizza);
        cart.addDish(fries);

        // 3. THEN (Тогда): Проверяем, что сумма ровно 650.0
        // assertEquals(ожидаемое_значение, фактическое_значение, сообщение_при_ошибке)
        assertEquals(650.0, cart.calculateTotal(), "Калькулятор корзины ошибся в расчетах!");
    }
}
