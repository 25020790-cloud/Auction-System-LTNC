package ddc.server.factory;

import ddc.server.model.item.Vehicle;
import java.util.Map;

public class VehicleFactory implements ItemFactory<Vehicle> {
    @Override
    public Vehicle createItem(String id, String name, String description, double startingPrice, Map<String, Object> extraParams) {
        String vin = extraParams != null ? (String) extraParams.getOrDefault("vin", "N/A") : "N/A";
        int mileage = 0;
        if (extraParams != null && extraParams.containsKey("mileage")) {
            mileage = ((Number) extraParams.get("mileage")).intValue();
        }
        return new Vehicle(id, name, description, startingPrice, vin, mileage);
    }
}