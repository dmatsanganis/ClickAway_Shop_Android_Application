package com.dmatsanganis.clickawayapp.Classes;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username, email, phone;
    private ShoppingCart Cart;
    private String language;
    private String app_theme;

    public User(String username, String email, String phone, String language, String app_theme, ShoppingCart cart){
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.Cart = cart;
        this.language = language;
        this.app_theme = app_theme;
    }
    
    public User() {}

    public String getLanguage() {
        return language;
    }

    public String getApp_theme() {
        return app_theme;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public ShoppingCart getCart() {
        return Cart;
    }

    public void setCart(ShoppingCart cart) {
        this.Cart = cart;
    }
}
