package com.auction.server;

import com.auction.server.core.AuctionOrchestrator;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.JsonBidDAO;
import com.auction.server.dao.UserDao;
import com.auction.server.managers.AuctionManager;
import com.auction.server.managers.UserManager;
import com.auction.server.network.SocketServer;
import com.auction.shared.models.Auction;

import java.util.List;

/** Application entry point for the auction server. */
public class ServerApp {
    private static final int DEFAULT_PORT = 8888;

    private ServerApp() {
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        AuctionManager auctionManager = new AuctionManager();
        AuctionDao auctionDao = new AuctionDao();
        List<Auction> auctions = auctionDao.loadAll();
        if (auctions.isEmpty()) {
            seedAuctions(auctionManager);
            auctions = auctionManager.getAuctions();
            auctionDao.saveAll(auctions);
        } else {
            auctions.forEach(auctionManager::addAuction);
        }

        JsonBidDAO bidDAO = new JsonBidDAO();
        bidDAO.findAll().forEach(auctionManager::restoreBid);
        auctionManager.closeExpiredAuctions();
        SocketServer socketServer = new SocketServer(port);
        AuctionOrchestrator orchestrator = new AuctionOrchestrator(
                auctionManager, bidDAO, socketServer, new UserManager(new UserDao()));
        socketServer.setOrchestrator(orchestrator);
        socketServer.start();
    }

    private static void seedAuctions(AuctionManager auctionManager) {
        long now = System.currentTimeMillis();
        long endTime = now + 24L * 60 * 60 * 1000;

        Auction watch = new Auction(
                "auction-watch-001", "item-watch-001", "demo-seller",
                1500, 1500, null, now, endTime,
                Auction.Status.ACTIVE);
        watch.setProductName("Đồng hồ cơ cổ điển");
        watch.setCategory("Watches");
        auctionManager.addAuction(watch);

        Auction art = new Auction(
                "auction-art-001", "item-art-001", "demo-seller",
                800, 800, null, now, endTime,
                Auction.Status.ACTIVE);
        art.setProductName("Tranh sơn dầu phong cảnh");
        art.setCategory("Fine Art");
        auctionManager.addAuction(art);
    }
}
