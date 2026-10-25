package com.auction.server.managers;

import com.auction.shared.models.Auction;
import com.auction.shared.models.BidTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Manages active auctions and serializes bids to prevent race conditions. */
public class AuctionManager {
    private static final double DEFAULT_MINIMUM_BID_INCREMENT = 1.0;

    private final Map<String, Auction> auctions = new ConcurrentHashMap<>();
    private final Map<String, List<BidTransaction>> bidHistory = new ConcurrentHashMap<>();
    private final double minimumBidIncrement;

    public AuctionManager() {
        this(DEFAULT_MINIMUM_BID_INCREMENT);
    }

    public AuctionManager(double minimumBidIncrement) {
        if (!Double.isFinite(minimumBidIncrement) || minimumBidIncrement < 0) {
            throw new IllegalArgumentException("Minimum bid increment must be a finite non-negative number");
        }
        this.minimumBidIncrement = minimumBidIncrement;
    }

    /** Adds or replaces an auction managed by this server. */
    public synchronized void addAuction(Auction auction) {
        if (auction == null || auction.getAuctionId() == null || auction.getAuctionId().isBlank()) {
            throw new IllegalArgumentException("Auction and auction ID are required");
        }
        auctions.put(auction.getAuctionId(), auction);
        bidHistory.computeIfAbsent(auction.getAuctionId(), ignored -> new ArrayList<>());
    }

    /**
     * Accepts a bid only if the auction is active, has not expired, and the amount
     * exceeds the current price by at least the configured increment.
     */
    public synchronized boolean placeBid(String auctionId, String userId, double amount) {
        if (auctionId == null || userId == null || userId.isBlank() || !Double.isFinite(amount)) {
            return false;
        }

        Auction auction = auctions.get(auctionId);
        if (auction == null || auction.getStatus() != Auction.Status.ACTIVE) {
            return false;
        }

        if (System.currentTimeMillis() >= auction.getEndTime()) {
            auction.setStatus(Auction.Status.ENDED);
            return false;
        }

        if (!Double.isFinite(auction.getCurrentPrice())
                || amount <= auction.getCurrentPrice() + minimumBidIncrement) {
            return false;
        }

        BidTransaction transaction = new BidTransaction(auctionId, userId, amount);
        auction.setCurrentPrice(amount);
        auction.setCurrentWinnerId(userId);
        bidHistory.computeIfAbsent(auctionId, ignored -> new ArrayList<>()).add(transaction);
        return true;
    }

    public Auction getAuction(String auctionId) {
        return auctions.get(auctionId);
    }

    public List<Auction> getAuctions() {
        return List.copyOf(auctions.values());
    }

    public synchronized List<BidTransaction> getBidHistory(String auctionId) {
        List<BidTransaction> history = bidHistory.get(auctionId);
        return history == null ? List.of() : List.copyOf(history);
    }

    public synchronized BidTransaction getLatestBid(String auctionId) {
        List<BidTransaction> history = bidHistory.get(auctionId);
        return history == null || history.isEmpty() ? null : history.get(history.size() - 1);
    }
}
