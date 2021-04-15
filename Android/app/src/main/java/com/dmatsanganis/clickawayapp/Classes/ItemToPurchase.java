package com.dmatsanganis.clickawayapp.Classes;

import java.io.Serializable;

public class ItemToPurchase implements Serializable {
    private static final long serialVersionUID = 1L;
    private Smartphone smartphone;
    private Tablet tablet;
    private Laptop laptop;
    private int quantity;
    private double cost;

    public ItemToPurchase(Smartphone smartphone, int quantity){
        this.smartphone = smartphone;
        this.quantity = quantity;
        this.cost = quantity * smartphone.getPrice();
    }

    public ItemToPurchase(Tablet tablet, int quantity){
        this.tablet = tablet;
        this.quantity = quantity;
        this.cost = quantity * tablet.getPrice();
    }

    public ItemToPurchase(Laptop laptop, int quantity){
        this.laptop = laptop;
        this.quantity = quantity;
        this.cost = quantity * laptop.getPrice();
    }

    public ItemToPurchase() {}

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

    public double getCost() {
        return cost;
    }

    public void setQuantity(int quantity, Smartphone smartphone) {
        this.quantity = quantity;
        this.cost = quantity * smartphone.getPrice();
    }
    public void setQuantity(int quantity, Tablet tablet) {
        this.quantity = quantity;
        this.cost = quantity * tablet.getPrice();
    }
    public void setQuantity(int quantity, Laptop laptop) {
        this.quantity = quantity;
        this.cost = quantity * laptop.getPrice();
    }
}
