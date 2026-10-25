package com.auction.shared.models;

public class Vehicle extends Item {
    private String brand;

    public Vehicle(String brand, String id, String name, String description) {
        super(id, name, description);
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    @Override
    public String getItemType() {
        return "Vehicle";
    }
}
