package com.auction.shared.models;

public class Art extends Item {
    private String artist;
    
    public Art(String artist, String id, String name, String description) {
        super(id, name, description);
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public String getItemType() {
        return "Art";
    }
}
