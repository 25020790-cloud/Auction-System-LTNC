package ddc.server.manager;

import ddc.server.exception.AuthenticationException;
import ddc.server.model.Auction;
import ddc.server.model.AuctionStatus;
import ddc.server.model.item.Electronics;
import ddc.server.model.user.Bidder;
import ddc.server.model.user.Seller;
import ddc.server.model.user.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class AuctionManagerTest {

    private final AuctionManager manager = AuctionManager.getInstance();

    @Test
    void testAuthenticateUser_Success() throws AuthenticationException {
        String suffix = uniqueSuffix();
        User user = new Bidder("user-" + suffix, "bidder-" + suffix,
                "correct-password", "bidder-" + suffix + "@example.com", 1_000.0);
        manager.addUser(user);

        User authenticatedUser = manager.authenticateUser(user.getUserName(), "correct-password");

        assertSame(user, authenticatedUser);
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        String suffix = uniqueSuffix();
        User user = new Bidder("user-" + suffix, "bidder-" + suffix,
                "correct-password", "bidder-" + suffix + "@example.com", 1_000.0);
        manager.addUser(user);

        assertThrows(AuthenticationException.class,
                () -> manager.authenticateUser(user.getUserName(), "wrong-password"));
    }

    @Test
    void testAutomaticStatusTransition() throws InterruptedException {
        String suffix = uniqueSuffix();
        long now = System.currentTimeMillis();

        Auction auctionThatStarts = new Auction(
                "start-" + suffix,
                new Electronics("item-start-" + suffix, "Laptop", "Test item", 100.0, "TestBrand", 12),
                new Seller("seller-start-" + suffix, "seller-start-" + suffix, "password", "seller@example.com"),
                now + 100,
                Long.MAX_VALUE);
        Auction auctionThatFinishes = new Auction(
                "finish-" + suffix,
                new Electronics("item-finish-" + suffix, "Phone", "Test item", 100.0, "TestBrand", 12),
                new Seller("seller-finish-" + suffix, "seller-finish-" + suffix, "password", "seller@example.com"),
                now - 1_000,
                now + 100);

        manager.addAuction(auctionThatStarts);
        manager.addAuction(auctionThatFinishes);
        manager.startStatusChecker();

        awaitStatus(auctionThatStarts, AuctionStatus.RUNNING);
        awaitStatus(auctionThatFinishes, AuctionStatus.FINISHED);
    }

    private void awaitStatus(Auction auction, AuctionStatus expectedStatus) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 4_000;

        while (System.currentTimeMillis() < deadline) {
            if (auction.getStatus() == expectedStatus) {
                return;
            }
            Thread.sleep(20);
        }

        fail("Auction " + auction.getAuctionId() + " did not reach status " + expectedStatus);
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString();
    }
}
