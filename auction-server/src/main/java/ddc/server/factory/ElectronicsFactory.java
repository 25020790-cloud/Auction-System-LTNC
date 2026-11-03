package ddc.server.factory;

import ddc.server.model.item.Electronics;
import java.util.Map;

public class ElectronicsFactory implements ItemFactory{
    @Override
    public Electronics createItem(String id, String name, String description, double startingPrice, Map<String, Object> extraParams){
        String brand = extraParams != null ? (String) extraParams.getOrDefault("brand", "Unknown") : "Unknown";
        int warranty = 0;
        if (extraParams != null && extraParams.containsKey("warrantyMonths")){
            warranty = ((Number) extraParams.get("warrantyMonths")).intValue();
        }
        return new Electronics(id, name, description, startingPrice, brand, warranty);
    }
}
