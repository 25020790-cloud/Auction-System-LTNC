package com.auction.shared.models;

import java.io.Serializable;

/** Represents one auction session shared between the client and server. */
public class Auction implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        PENDING,
        ACTIVE,
        ENDED
    }

    private String auctionId;
    private String itemId;
    private String sellerId;
    private double startPrice;
    private double currentPrice;
    private String currentWinnerId;
    private long startTime;
    private long endTime;
    private Status status = Status.PENDING;

    public Auction() {
    }

    public Auction(String auctionId, String itemId, String sellerId,
                   double startPrice, double currentPrice, String currentWinnerId,
                   long startTime, long endTime, Status status) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.currentWinnerId = currentWinnerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status == null ? Status.PENDING : status;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getCurrentWinnerId() {
        return currentWinnerId;
    }

    public void setCurrentWinnerId(String currentWinnerId) {
        this.currentWinnerId = currentWinnerId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
