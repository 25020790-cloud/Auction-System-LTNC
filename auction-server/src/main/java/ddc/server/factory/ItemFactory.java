package ddc.server.factory;

import ddc.server.model.item.Item;
import java.util.Map;

public interface ItemFactory<T extends Item> {
    T createItem(String id, String name, String description, double startingPrice, Map<String, Object> extraParams);
}
