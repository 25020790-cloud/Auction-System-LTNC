package ddc.server.model.item;

public class Vehicle extends Item {
    private String vin;
    private int mileage;

    public Vehicle(String id, String name, String description, double startingPrice, String vin, int mileage) {
        super(id, name, description, startingPrice);
        this.vin = vin;
        this.mileage = mileage;
    }

    @Override
    public String getType() { return "VEHICLE"; }

    @Override
    public String getInfo() {
        return String.format("VIN: %s, Mileage: %d km", vin, mileage);
    }
}