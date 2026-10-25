package com.auction.shared.models;

import java.io.Serializable;
import java.util.UUID;

/** Records one user's bid on an auction. */
public class BidTransaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private String auctionId;
    private String userId;
    private double bidAmount;
    private long timestamp;

    public BidTransaction() {
    }

    public BidTransaction(String transactionId, String auctionId, String userId,
                          double bidAmount, long timestamp) {
        this.transactionId = transactionId;
        this.auctionId = auctionId;
        this.userId = userId;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
    }

    public BidTransaction(String auctionId, String userId, double bidAmount) {
        this(UUID.randomUUID().toString(), auctionId, userId, bidAmount,
                System.currentTimeMillis());
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
