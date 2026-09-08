package com.delivery.service;

public class PaymentProcessor {

    // Метод запускает асинхронную (фоновую) проверку денег
    public void checkPaymentAsync(int orderId) {

        // Создаем отдельный независимый поток для общения с банком
        Thread paymentCheckThread = new Thread(() -> {
            try {
                System.out.println("💳 [Поток Оплаты]: Началась фоновая проверка платежа для заказа №" + orderId);

                // Имитируем, что мы ждем ответ от серверов банка 4 секунды
                Thread.sleep(4000);

                // Прошло 4 секунды — ответ пришел!
                System.out.println("💰 [Поток Оплаты]: Банк подтвердил списание средств для заказа №" + orderId + ". Меняем статус на 'Оплачено'!");

                // Здесь в реальном коде шел бы SQL-запрос: UPDATE orders SET is_paid = true WHERE id = orderId;

            } catch (InterruptedException e) {
                System.err.println("❌ Поток оплаты был прерван!");
                e.getStackTrace();
            }
        });

        // Запускаем поток! Код внутри побежал сам по себе,
        // а этот метод ТУТ ЖЕ завершается и возвращает управление сайту
        paymentCheckThread.start();
    }
}
