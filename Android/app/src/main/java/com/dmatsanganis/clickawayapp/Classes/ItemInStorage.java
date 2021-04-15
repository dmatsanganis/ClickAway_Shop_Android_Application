package com.dmatsanganis.clickawayapp.Classes;

import java.io.Serializable;

public class ItemInStorage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Smartphone smartphone;
    private Tablet tablet;
    private Laptop laptop;
    private int quantity;

    public ItemInStorage(Smartphone smartphone, int quantity){
        this.smartphone = smartphone;
        this.quantity = quantity;
    }

    public ItemInStorage(Tablet tablet, int quantity){
        this.tablet = tablet;
        this.quantity = quantity;
    }

    public ItemInStorage(Laptop laptop, int quantity){
        this.laptop = laptop;
        this.quantity = quantity;
    }

    public ItemInStorage() {}

    public Tablet getTablet() {
        return tablet;
    }

    public Laptop getLaptop() {
        return laptop;
    }

    public Smartphone getSmartphone() {
        return smartphone;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
