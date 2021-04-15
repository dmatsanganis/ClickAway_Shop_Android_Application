package com.dmatsanganis.clickawayapp.Classes;

import java.io.Serializable;

public class Laptop implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String os;
    private String cores;
    private String ram;
    private String rom;
    private String name;
    private String desc_gr;
    private String desc_en;
    private String image_url;
    private String cpu;
    private String gpu;
    private double screen_size;
    private double price;

    public Laptop(String id, String name, String image_url, double price, String desc_gr, String desc_en,
                  String os, String cores, String ram, String rom,
                  String cpu, String gpu, double screen_size) {
        this.id = id;
        this.name = name;
        this.image_url = image_url;
        this.price = price;
        this.os = os;
        this.desc_en = desc_en;
        this.desc_gr = desc_gr;
        this.cores = cores;
        this.cpu = cpu;
        this.gpu = gpu;
        this.ram = ram;
        this.rom = rom;
        this.screen_size = screen_size;
    }

    public Laptop() {}

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

    public String getCores() {
        return cores;
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

    public String getCpu() {
        return cpu;
    }

    public String getGpu() {
        return gpu;
    }
}
