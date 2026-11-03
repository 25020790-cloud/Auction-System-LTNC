package ddc.server.manager;

import ddc.server.model.Auction;
import ddc.server.model.Bid;

import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    private static AuctionManager instance;

    private final List<Auction> auctions;

    private AuctionManager() {
        auctions = new ArrayList<>();
        startStatusChecker();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public synchronized void addAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException("Auction cannot be null");
        }
        auctions.add(auction);
    }

    public synchronized List<Auction> getAllAuctions() {
        return new ArrayList<>(auctions);
    }

    public synchronized Auction getAuctionById(String auctionId) {
        for (Auction auction : auctions) {
            if (auctionId != null && auctionId.equals(auction.getAuctionId())) {
                return auction;
            }
        }
        return null;
    }

    public boolean placeBid(String auctionId, Bid newBid) {
        if (newBid == null || newBid.getBidder() == null
                || !newBid.getBidder().isActive()) {
            return false;
        }

        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            return false;
        }

        // Auction kiểm tra giá và cập nhật bid bên trong ReentrantLock.
        return auction.placeBid(newBid);
    }

    private void startStatusChecker() {
        Thread statusChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    checkAuctionStatuses();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });

        // Thread nền không ngăn chương trình tắt khi ứng dụng đóng.
        statusChecker.setDaemon(true);
        statusChecker.start();
    }

    private synchronized void checkAuctionStatuses() {
        long currentTime = System.currentTimeMillis();

        for (Auction auction : auctions) {
            auction.checkTimeAndUpdateStatus(currentTime);
        }
    }
}
