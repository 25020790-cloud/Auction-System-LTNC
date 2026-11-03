package ddc.server.model.item;

public abstract class Item {
    protected String itemId;
    protected String name;
    protected String description;
    protected double startingPrice;
    public Item(String itemId, String name, String description, double startingPrice){
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }
    public abstract String getInfo();
    public abstract String getType();

    public String getName() {
        return name;
    }

    public String getItemId() {
        return itemId;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }
}
