package com.dmatsanganis.clickawayapp.Classes;

import java.io.Serializable;

public class Smartphone implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String os;
    private String battery;
    private String cores;
    private String front_camera;
    private String rear_camera;
    private String ram;
    private String rom;
    private String name;
    private String desc_gr;
    private String desc_en;
    private String image_url;
    private double screen_size;
    private double price;

    public Smartphone(String id, String name, String image_url, double price, String desc_gr, String desc_en,
                      String os, String battery, String cores, String front_camera, String rear_camera,
                      String ram, String rom, double screen_size) {
        this.id = id;
        this.name = name;
        this.image_url = image_url;
        this.price = price;
        this.os = os;
        this.desc_en = desc_en;
        this.desc_gr = desc_gr;
        this.battery = battery;
        this.cores = cores;
        this.front_camera = front_camera;
        this.rear_camera = rear_camera;
        this.ram = ram;
        this.rom = rom;
        this.screen_size = screen_size;
    }

    public Smartphone() {}

    public String getId() {
        return id;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getName() {
        return name;
    }

    public String getDesc_gr() {
        return desc_gr;
    }

    public String getDesc_en() {
        return desc_en;
    }

    public double getPrice() {
        return price;
    }

    public String getOs() {
        return os;
    }

    public String getBattery() {
        return battery;
    }

    public String getCores() {
        return cores;
    }

    public String getFront_camera() {
        return front_camera;
    }

    public String getRear_camera() {
        return rear_camera;
    }

    public String getRam() {
        return ram;
    }

    public String getRom() {
        return rom;
    }

    public double getScreen_size() {
        return screen_size;
    }
}
