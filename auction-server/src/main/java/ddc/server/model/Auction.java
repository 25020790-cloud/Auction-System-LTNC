package ddc.server.model;

import ddc.server.exception.AuctionClosedException;
import ddc.server.exception.InvalidBidException;
import ddc.server.model.item.Item;
import ddc.server.model.user.Seller;
import ddc.server.observer.AuctionSubject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Auction extends AuctionSubject {
    private String auctionId;
    private Item item;
    private Seller seller;
    private double currentPrice;
    private AuctionStatus status;
    private List<Bid> bidHistory;
    
    private final long startTime;
    private final long endTime;

    private final ReentrantLock lock = new ReentrantLock();

    public Auction(String auctionId, Item item, Seller seller) {
        this(auctionId, item, seller, System.currentTimeMillis(), Long.MAX_VALUE);
    }

    public Auction(String auctionId, Item item, Seller seller, long startTime, long endTime) {
        this.auctionId = auctionId;
        this.item = item;
        this.seller = seller;
        this.currentPrice = item.getStartingPrice();
        this.status = AuctionStatus.OPEN;
        this.bidHistory = new ArrayList<>();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean placeBid(Bid newBid) throws InvalidBidException, AuctionClosedException{
        lock.lock();
        try {
            if (status != AuctionStatus.RUNNING) {
                throw new AuctionClosedException("Phiên đấu giá đã đóng hoặc chưa bắt đầu.");
            }

            if (newBid == null || newBid.getBidder() == null || !newBid.getBidder().isActive()) {
                throw new InvalidBidException("Bid không hợp lệ hoặc người đấu giá không hoạt động.");
            }

            if (newBid.getAmount() <= currentPrice) {
                throw new InvalidBidException(("Giá đặt (" + newBid.getAmount() + ") phải lớn hơn giá hiện tại (" + currentPrice + ")."));
            }

            currentPrice = newBid.getAmount();
            bidHistory.add(newBid);
            notifyObservers(this, newBid);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void checkTimeAndUpdateStatus(long currentTime) {
        lock.lock();
        try {
            if (status == AuctionStatus.OPEN && currentTime >= startTime) {
                status = AuctionStatus.RUNNING;
                notifyObservers(this, null);
            }

            if (status == AuctionStatus.RUNNING && currentTime >= endTime) {
                status = AuctionStatus.FINISHED;
                notifyObservers(this, null);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean setStatus(AuctionStatus newStatus) {
        lock.lock();
        try {
            boolean allowed =
                    (status == AuctionStatus.OPEN
                            && (newStatus == AuctionStatus.RUNNING || newStatus == AuctionStatus.CANCELED))
                    || (status == AuctionStatus.RUNNING
                            && (newStatus == AuctionStatus.FINISHED || newStatus == AuctionStatus.CANCELED))
                    || (status == AuctionStatus.FINISHED && newStatus == AuctionStatus.PAID);

            if (allowed) {
                status = newStatus;
                notifyObservers(this, null);
            }
            return allowed;
        } finally {
            lock.unlock();
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
        lock.lock();
        try {
            return currentPrice;
        } finally {
            lock.unlock();
        }
    }

    public void setCurrentPrice(double currentPrice) {
        lock.lock();
        try {
            this.currentPrice = currentPrice;
        } finally {
            lock.unlock();
        }
    }

    public AuctionStatus getStatus() {
        lock.lock();
        try {
            return status;
        } finally {
            lock.unlock();
        }
    }

    public List<Bid> getBidHistory() {
        lock.lock();
        try {
            return new ArrayList<>(bidHistory);
        } finally {
            lock.unlock();
        }
    }

    public void setBidHistory(List<Bid> bidHistory) {
        lock.lock();
        try {
            if (bidHistory == null) {
                this.bidHistory = new ArrayList<>();
            } else {
                this.bidHistory = new ArrayList<>(bidHistory);
            }
        } finally {
            lock.unlock();
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
