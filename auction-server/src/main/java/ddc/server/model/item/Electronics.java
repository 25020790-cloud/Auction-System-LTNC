package ddc.server.model.item;

public class Electronics extends Item{
    private String brand;
    private int warrantyMonths;
    public Electronics(String id, String name, String description, double startingPrice, String brand, int warrantyMonths) {
        super(id, name, description, startingPrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }
    @Override
    public String getInfo(){
        return String.format("Brand: %s, Warranty: %d months", brand, warrantyMonths);
    }
    @Override
    public String getType(){
        return "ELECTRONICS";
    }
}
