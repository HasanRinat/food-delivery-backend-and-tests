package com.delivery.model;

public class Dish {
    private String name;
    private double price;
    private int weight;
    private boolean isAvailable;

    public Dish(String name, double price, int weight, boolean isAvailable) {
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.isAvailable = isAvailable;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
