package com.dmatsanganis.clickawayapp.Classes;

import java.io.Serializable;
import java.util.ArrayList;

public class Store implements Serializable {
    private static final long serialVersionUID = 1L;
    private String  id, title_en, title_gr, phone, address_gr, address_en;
    private double longitude , latitude;
    private ArrayList<ItemInStorage> storage;
    private ArrayList<Order> orders;

    public Store (String id, String title_en, String title_gr, double longitude, double latitude, String phone, String address_gr, String address_en, ArrayList<ItemInStorage> storage, ArrayList<Order> orders){
        this.id = id;
        this.title_en = title_en;
        this.title_gr = title_gr;
        this.longitude = longitude;
        this.latitude = latitude;
        this.phone = phone;
        this.address_gr = address_gr;
        this.address_en = address_en;
        this.storage = storage;
        this.orders = orders;
    }

    public Store() {
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public ArrayList<ItemInStorage> getStorage() {
        return storage;
    }

    public String getId() {
        return id;
    }

    public String getTitle_en() {
        return title_en;
    }

    public String getTitle_gr() {
        return title_gr;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress_gr() {
        return address_gr;
    }

    public String getAddress_en() {
        return address_en;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
