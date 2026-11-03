package ddc.server.model;

import ddc.server.model.user.Bidder;

public class Bid {
    private String bidId;
    private Bidder bidder;
    private double amount;
    private long timestamp;

    public Bid(String bidId, Bidder bidder, double amount, long timestamp) {
        this.bidId = bidId;
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getBidId() {
        return bidId;
    }

    public void setBidId(String bidId) {
        this.bidId = bidId;
    }

    public Bidder getBidder() {
        return bidder;
    }

    public void setBidder(Bidder bidder) {
        this.bidder = bidder;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
