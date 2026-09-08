package com.delivery.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<Dish> items = new ArrayList<>();

    public void addDish(Dish dish){
        items.add(dish);
        System.out.println("Добавлено в корзину" + dish.getName());
    }

    public double calculateTotal(){
        double total = 0.0;

        for (Dish dish : items)
        {
            total += dish.getPrice();
        }
        return total;
    }
    public List<Dish> getItems() {
        return items;
    }
}
