package ddc.server.model.item;

public class Art extends Item{
    private String artist;
    private int yearCreated;

    public Art(String id, String name, String description, double startingPrice, String artist, int yearCreated){
        super(id, name, description, startingPrice);
        this.artist = artist;
        this.yearCreated = yearCreated;
    }
    @Override
    public String getInfo(){
        return String.format("Artist: %s, Year: %d", artist, yearCreated);
    }
    @Override
    public String getType(){
        return "ART";
    }
}
