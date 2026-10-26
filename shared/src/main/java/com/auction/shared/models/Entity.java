package com.auction.shared.models;

public abstract class Entity{
    protected String id;

    public Entity() {
        this(null);
    }
    
    public Entity(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }
}
