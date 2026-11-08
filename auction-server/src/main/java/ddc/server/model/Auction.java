package ddc.server.model;

import ddc.server.model.item.Item;
import ddc.server.model.user.Seller;
import ddc.server.model.observer.AuctionSubject;

import java.util.ArrayList;
import java.util.List;

public class Auction extends AuctionSubject {
    private String auctionId;
    private Item item;
    private Seller seller;
    private double currentPrice;
    private String status;
    private List<Bid> bidHistory;

    public Auction(String auctionId, Item item, Seller seller) {
        this.auctionId = auctionId;
        this.item = item;
        this.seller = seller;
        this.currentPrice = item.getStartingPrice();
        this.status = "OPEN";
        this.bidHistory = new ArrayList<>();
    }

    public Auction(String auctionId, Item item, Seller seller, double currentPrice,
                   String status, List<Bid> bidHistory) {
        this.auctionId = auctionId;
        this.item = item;
        this.seller = seller;
        this.currentPrice = currentPrice;
        this.status = status;
        this.bidHistory = bidHistory;
        if (this.bidHistory == null) {
            this.bidHistory = new ArrayList<>();
        }
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status != null && !status.equals(this.status)) {
            this.status = status;
            notifyObservers(this, null);
        }
    }

    public boolean placeBid(Bid newBid) {
        if (newBid == null || newBid.getAmount() <= currentPrice) {
            return false;
        }

        currentPrice = newBid.getAmount();
        bidHistory.add(newBid);
        notifyObservers(this, newBid);
        return true;
    }

    public List<Bid> getBidHistory() {
        return bidHistory;
    }

    public void setBidHistory(List<Bid> bidHistory) {
        this.bidHistory = bidHistory;
        if (this.bidHistory == null) {
            this.bidHistory = new ArrayList<>();
        }
    }
}
