package ddc.server.factory;

import ddc.server.model.item.Art;
import java.util.Map;

public class ArtFactory implements ItemFactory<Art> {
    @Override
    public Art createItem(String id, String name, String description, double startingPrice, Map<String, Object> extraParams) {
        String artist = extraParams != null ? (String) extraParams.getOrDefault("artist", "Anonymous") : "Anonymous";
        int year = 0;
        if (extraParams != null && extraParams.containsKey("yearCreated")) {
            year = ((Number) extraParams.get("yearCreated")).intValue();
        }
        return new Art(id, name, description, startingPrice, artist, year);
    }
}