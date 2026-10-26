package com.auction.server.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Small atomic JSON-file persistence helper used by the server DAOs. */
public final class JsonDao {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonDao() {
    }

    public static Path dataFile(String fileName) {
        String dataDirectory = System.getProperty("auction.data.dir",
                System.getenv().getOrDefault("AUCTION_DATA_DIR", "data"));
        return Path.of(dataDirectory).resolve(fileName);
    }

    public static <T> List<T> readList(Path file, Class<T> itemType) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonArray()) {
                throw new IllegalStateException("Expected a JSON array in " + file);
            }

            List<T> items = new ArrayList<>();
            JsonArray array = root.getAsJsonArray();
            for (JsonElement element : array) {
                items.add(GSON.fromJson(element, itemType));
            }
            return items;
        } catch (IOException | JsonParseException exception) {
            throw new IllegalStateException("Cannot read JSON data from " + file, exception);
        }
    }

    public static <T> void writeList(Path file, List<T> items) {
        Path absoluteFile = file.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        Path temporaryFile = null;
        try {
            Files.createDirectories(parent);
            temporaryFile = Files.createTempFile(parent, absoluteFile.getFileName().toString(), ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                GSON.toJson(items, writer);
            }

            try {
                Files.move(temporaryFile, absoluteFile,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot save JSON data to " + file, exception);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                    // A leftover temporary file does not affect the committed data file.
                }
            }
        }
    }
}
