package com.auction.server.core;

import com.auction.server.managers.AuctionManager;
import com.auction.server.network.SocketServer;
import com.auction.shared.constants.Actions;
import com.auction.shared.models.Auction;
import com.auction.shared.models.BidTransaction;
import com.auction.shared.network.MessageDTO;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Connects bid requests, auction logic, persistence, and socket broadcasts. */
public class AuctionOrchestrator {
    private final AuctionManager auctionManager;
    private final BidDAO bidDAO;
    private final SocketServer socketServer;
    private final Gson gson = new Gson();

    public AuctionOrchestrator(AuctionManager auctionManager, BidDAO bidDAO,
                               SocketServer socketServer) {
        if (auctionManager == null || bidDAO == null || socketServer == null) {
            throw new IllegalArgumentException("Auction manager, DAO, and socket server are required");
        }
        this.auctionManager = auctionManager;
        this.bidDAO = bidDAO;
        this.socketServer = socketServer;
    }

    /** Handles one JSON message and returns the response to the requesting client. */
    public String handleClientMessage(String rawJson) {
        try {
            JsonObject request = JsonParser.parseString(rawJson).getAsJsonObject();
            String action = stringValue(request.get("action"));
            if (!Actions.BID.equals(action)) {
                return response(Actions.ERROR, "Unsupported action");
            }

            String userId = stringValue(request.get("sender"));
            JsonElement payloadElement = request.get("payload");
            if (payloadElement == null || !payloadElement.isJsonObject()) {
                return response(Actions.BID_FAILED, "Bid payload is missing");
            }
            JsonObject payload = payloadElement.getAsJsonObject();
            String auctionId = stringValue(payload.get("auctionId"));
            JsonElement amountElement = payload.get("amount");
            if (auctionId == null || amountElement == null || !amountElement.isJsonPrimitive()
                    || !amountElement.getAsJsonPrimitive().isNumber()) {
                return response(Actions.BID_FAILED, "Auction ID and numeric amount are required");
            }

            double amount = amountElement.getAsDouble();
            if (!auctionManager.placeBid(auctionId, userId, amount)) {
                return response(Actions.BID_FAILED, "Bid was rejected");
            }

            BidTransaction transaction = auctionManager.getLatestBid(auctionId);
            bidDAO.save(transaction);

            Auction auction = auctionManager.getAuction(auctionId);
            socketServer.broadcast(gson.toJson(
                    new MessageDTO(Actions.AUCTION_UPDATE, "server", auction)));
            return gson.toJson(new MessageDTO(Actions.BID_SUCCESS, "server", transaction));
        } catch (RuntimeException exception) {
            return response(Actions.BID_FAILED, "Invalid bid request");
        }
    }

    private String response(String action, String message) {
        return gson.toJson(new MessageDTO(action, "server", message));
    }

    private String stringValue(JsonElement element) {
        return element == null || element.isJsonNull() ? null : element.getAsString();
    }

    @FunctionalInterface
    public interface BidDAO {
        void save(BidTransaction transaction);
    }

    /** Temporary in-memory DAO for running the server before a database is configured. */
    public static class InMemoryBidDAO implements BidDAO {
        private final List<BidTransaction> transactions = new CopyOnWriteArrayList<>();

        @Override
        public void save(BidTransaction transaction) {
            transactions.add(transaction);
        }

        public List<BidTransaction> findAll() {
            return List.copyOf(transactions);
        }
    }
}
