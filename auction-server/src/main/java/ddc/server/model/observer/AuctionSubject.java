package ddc.server.model.observer;

import ddc.server.model.Auction;
import ddc.server.model.Bid;

import java.util.ArrayList;
import java.util.List;

public class AuctionSubject {
    private final List<AuctionObserver> observers = new ArrayList<>();

    public void attach(AuctionObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(AuctionObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Auction auction, Bid newBid) {
        // Dùng bản sao để observer có thể tự detach khi đang nhận thông báo.
        List<AuctionObserver> observerList = new ArrayList<>(observers);

        for (AuctionObserver observer : observerList) {
            observer.update(auction, newBid);
        }
    }
}
