package ddc.server.model.observer;

import ddc.server.model.Auction;
import ddc.server.model.Bid;

public interface AuctionObserver {

    void update(Auction auction, Bid newBid);
}
