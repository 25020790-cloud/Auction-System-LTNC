package ddc.server.manager;

import ddc.server.model.Auction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AuctionManager {
    private static final AuctionManager INSTANCE = new AuctionManager();

    private final List<Auction> auctions = new ArrayList<>();

    private AuctionManager() {
    }

    public static AuctionManager getInstance() {
        return INSTANCE;
    }

    public synchronized void addAuction(Auction auction) {
        auctions.add(Objects.requireNonNull(auction, "auction cannot be null"));
    }

    public synchronized List<Auction> getAllAuctions() {
        return Collections.unmodifiableList(new ArrayList<>(auctions));
    }

    public synchronized Auction getAuctionById(String auctionId) {
        for (Auction auction : auctions) {
            if (Objects.equals(auction.getAuctionId(), auctionId)) {
                return auction;
            }
        }
        return null;
    }
}
