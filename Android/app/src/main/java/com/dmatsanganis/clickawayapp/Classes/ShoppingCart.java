package com.dmatsanganis.clickawayapp.Classes;

import java.io.Serializable;
import java.util.ArrayList;

public class ShoppingCart implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<ItemToPurchase> items;
    private double total_cost;

    public ShoppingCart(ArrayList<ItemToPurchase> items){
        this.items = items;
        double total_cost = 0;
        for (int i=0; i<items.size(); i++){
            total_cost = total_cost + items.get(i).getCost();
        }
        this.total_cost = total_cost;
    }

    public ShoppingCart() {}

    public ArrayList<ItemToPurchase> getItems() {
        return items;
    }

    public double getTotal_cost() {
        return total_cost;
    }

}
