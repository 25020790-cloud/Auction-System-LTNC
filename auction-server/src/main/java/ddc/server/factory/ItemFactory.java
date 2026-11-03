package ddc.server.factory;

import ddc.server.model.item.Art;
import ddc.server.model.item.Electronics;
import ddc.server.model.item.Item;
import ddc.server.model.item.Vehicle;

import java.util.Collections;
import java.util.Map;

public class ItemFactory {

    public static Item createItem(String type, String id, String name, String description,
                                  double startingPrice, Map<String, Object> extraParams) {
        if (type == null) {
            throw new IllegalArgumentException("Item type cannot be null");
        }

        Map<String, Object> params = (extraParams != null) ? extraParams : Collections.emptyMap();

        switch (type.trim().toUpperCase()) {
            case "ELECTRONICS": {
                String brand = (String) params.getOrDefault("brand", "Unknown");
                int warranty = extractInt(params, "warrantyMonths", 0);
                return new Electronics(id, name, description, startingPrice, brand, warranty);
            }
            case "ART": {
                String artist = (String) params.getOrDefault("artist", "Anonymous");
                int year = extractInt(params, "yearCreated", 0);
                return new Art(id, name, description, startingPrice, artist, year);
            }
            case "VEHICLE": {
                String vin = (String) params.getOrDefault("vin", "N/A");
                int mileage = extractInt(params, "mileage", 0);
                return new Vehicle(id, name, description, startingPrice, vin, mileage);
            }
            default:
                throw new IllegalArgumentException("Unsupported item type: " + type);
        }
    }

    private static int extractInt(Map<String, Object> map, String key, int defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return defaultValue;
    }
}