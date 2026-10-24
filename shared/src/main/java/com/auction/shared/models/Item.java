package com.auction.shared.models;

public abstract class Item extends Entity {
    private String name;
    private String description;

public Item(String id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

    public String getName(){
        return name;
    }
    
    public String getDescription(){
        return description;
    }

    public abstract String getItemType();
}
