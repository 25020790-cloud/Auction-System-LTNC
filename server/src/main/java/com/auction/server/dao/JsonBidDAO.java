package com.auction.server.dao;

import com.auction.server.core.AuctionOrchestrator;
import com.auction.shared.models.BidTransaction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** File-backed bid history. Each update replaces the JSON file atomically. */
public class JsonBidDAO implements AuctionOrchestrator.BidDAO {
    private final Path file;
    private final List<BidTransaction> transactions;

    public JsonBidDAO() {
        this(JsonDao.dataFile("bids.json"));
    }

    public JsonBidDAO(Path file) {
        this.file = file;
        this.transactions = new ArrayList<>(JsonDao.readList(file, BidTransaction.class));
    }

    @Override
    public synchronized void save(BidTransaction transaction) {
        transactions.add(transaction);
        try {
            JsonDao.writeList(file, transactions);
        } catch (RuntimeException exception) {
            transactions.remove(transactions.size() - 1);
            throw exception;
        }
    }

    public synchronized List<BidTransaction> findAll() {
        return List.copyOf(transactions);
    }

    @Override
    public synchronized List<BidTransaction> findByUserId(String userId) {
        return transactions.stream()
                .filter(transaction -> userId.equals(transaction.getUserId()))
                .toList();
    }
}
