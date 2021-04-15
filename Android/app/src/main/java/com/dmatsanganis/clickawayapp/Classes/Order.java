package com.dmatsanganis.clickawayapp.Classes;

import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private User user;
    private String date;
    private String time;
    private boolean completed;

    public Order(User user, String date, String time, boolean completed){
        this.user = user;
        this.date = date;
        this.time = time;
        this.completed = completed;
    }

    public Order() {}

    public User getUser() {
        return user;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public boolean getCompleted() {
        return completed;
    }
}
