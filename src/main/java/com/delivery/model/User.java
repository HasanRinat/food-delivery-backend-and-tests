package com.delivery.model;

public class User {
    private String username;
    private String password;
    private Role role;
    private boolean isAvailable;

    public User(String username, String password, Role role){
        this.username = username;
        this.password = password;
        this.role = role;
        this.isAvailable = true;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available){
        isAvailable = available;
    }
}
