package ddc.server.model;

import ddc.server.exception.AuctionClosedException;
import ddc.server.exception.InvalidBidException;
import ddc.server.model.item.Electronics;
import ddc.server.model.item.Item;
import ddc.server.model.user.Bidder;
import ddc.server.model.user.Seller;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionTest {

    private static final double STARTING_PRICE = 100.0;

    @Test
    void testPlaceBid_Success() throws InvalidBidException, AuctionClosedException {
        Auction auction = createRunningAuction();
        Bid bid = createBid("bid-1", "bidder-1", 150.0);

        assertTrue(auction.placeBid(bid));
        assertEquals(150.0, auction.getCurrentPrice());
        assertEquals(1, auction.getBidHistory().size());
        assertEquals(bid, auction.getBidHistory().get(0));
    }

    @Test
    void testPlaceBid_LowerPrice_ThrowsInvalidBidException() {
        Auction auction = createRunningAuction();

        assertThrows(InvalidBidException.class,
                () -> auction.placeBid(createBid("same-price", "bidder-1", STARTING_PRICE)));
        assertThrows(InvalidBidException.class,
                () -> auction.placeBid(createBid("lower-price", "bidder-2", 99.0)));
    }

    @Test
    void testPlaceBid_ClosedAuction_ThrowsAuctionClosedException() {
        Auction auction = createRunningAuction();
        assertTrue(auction.setStatus(AuctionStatus.FINISHED));

        assertThrows(AuctionClosedException.class,
                () -> auction.placeBid(createBid("late-bid", "bidder-1", 150.0)));
    }

    @Test
    void testConcurrentBidding() throws Exception {
        Auction auction = createRunningAuction();
        int bidderCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(bidderCount);
        CountDownLatch ready = new CountDownLatch(bidderCount);
        CountDownLatch start = new CountDownLatch(1);

        try {
            @SuppressWarnings("unchecked")
            Future<Boolean>[] results = new Future[bidderCount];

            for (int index = 0; index < bidderCount; index++) {
                int bidderNumber = index + 1;
                double amount = STARTING_PRICE + bidderNumber;
                results[index] = executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    try {
                        return auction.placeBid(createBid(
                                "concurrent-bid-" + bidderNumber,
                                "concurrent-bidder-" + bidderNumber,
                                amount));
                    } catch (InvalidBidException | AuctionClosedException exception) {
                        return false;
                    }
                });
            }

            assertTrue(ready.await(2, TimeUnit.SECONDS), "All bidders must be ready before bidding");
            start.countDown();

            int acceptedBidCount = 0;
            for (Future<Boolean> result : results) {
                if (result.get(2, TimeUnit.SECONDS)) {
                    acceptedBidCount++;
                }
            }

            List<Bid> history = auction.getBidHistory();
            assertEquals(STARTING_PRICE + bidderCount, auction.getCurrentPrice());
            assertEquals(acceptedBidCount, history.size());

            double previousAmount = STARTING_PRICE;
            for (Bid bid : history) {
                assertTrue(bid.getAmount() > previousAmount,
                        "Bid history must remain strictly increasing after concurrent bids");
                previousAmount = bid.getAmount();
            }
            assertEquals(STARTING_PRICE + bidderCount, previousAmount);
        } finally {
            start.countDown();
            executor.shutdownNow();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    private Auction createRunningAuction() {
        Item item = new Electronics("item-1", "Laptop", "Test item", STARTING_PRICE, "TestBrand", 12);
        Seller seller = new Seller("seller-1", "seller", "password", "seller@example.com");
        Auction auction = new Auction("auction-1", item, seller);
        assertTrue(auction.setStatus(AuctionStatus.RUNNING));
        return auction;
    }

    private Bid createBid(String bidId, String bidderId, double amount) {
        Bidder bidder = new Bidder(bidderId, bidderId, "password", bidderId + "@example.com", 1_000.0);
        return new Bid(bidId, bidder, amount, System.currentTimeMillis());
    }
}
