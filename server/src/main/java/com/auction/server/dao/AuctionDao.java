package com.auction.server.dao;

import com.auction.shared.models.Auction;

import java.nio.file.Path;
import java.util.List;

/** Persists auction definitions so seeded auctions keep their original end time. */
public class AuctionDao {
    private final Path file;

    public AuctionDao() {
        this(JsonDao.dataFile("auctions.json"));
    }

    public AuctionDao(Path file) {
        this.file = file;
    }

    public synchronized List<Auction> loadAll() {
        return JsonDao.readList(file, Auction.class);
    }

    public synchronized void saveAll(List<Auction> auctions) {
        JsonDao.writeList(file, auctions);
    }
}
