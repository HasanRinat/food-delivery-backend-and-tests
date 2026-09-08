package com.delivery.model;

public class UserFactory {

    // Статический метод, который возвращает готового Клиента
    public static User createClient(String username, String password) {
        return new User(username, password, Role.CLIENT);
    }

    // Статический метод, который возвращает готового Курьера
    public static User createCourier(String username, String password) {
        return new User(username, password, Role.COURIER);
    }

    // Статический метод, который возвращает Диспетчера
    public static User createDispatcher(String username, String password) {
        return new User(username, password, Role.DISPATCHER);
    }
}

