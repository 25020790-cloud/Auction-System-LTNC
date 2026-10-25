package com.auction.server;

import com.auction.server.core.AuctionOrchestrator;
import com.auction.server.managers.AuctionManager;
import com.auction.server.network.SocketServer;

/** Application entry point for the auction server. */
public class ServerApp {
    private static final int DEFAULT_PORT = 8888;

    private ServerApp() {
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        AuctionManager auctionManager = new AuctionManager();
        AuctionOrchestrator.InMemoryBidDAO bidDAO = new AuctionOrchestrator.InMemoryBidDAO();
        SocketServer socketServer = new SocketServer(port);
        AuctionOrchestrator orchestrator = new AuctionOrchestrator(
                auctionManager, bidDAO, socketServer);
        socketServer.setOrchestrator(orchestrator);
        socketServer.start();
    }
}
