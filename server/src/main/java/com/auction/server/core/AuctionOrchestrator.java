package com.auction.server.core;

import com.auction.server.managers.AuctionManager;
import com.auction.server.managers.UserManager;
import com.auction.server.network.ClientSession;
import com.auction.server.network.SocketServer;
import com.auction.shared.constants.Actions;
import com.auction.shared.models.Auction;
import com.auction.shared.models.BidTransaction;
import com.auction.shared.models.Role;
import com.auction.shared.models.User;
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
    private final UserManager userManager;
    private final Gson gson = new Gson();

    public AuctionOrchestrator(AuctionManager auctionManager, BidDAO bidDAO,
                               SocketServer socketServer) {
        this(auctionManager, bidDAO, socketServer, new UserManager());
    }

    public AuctionOrchestrator(AuctionManager auctionManager, BidDAO bidDAO,
                               SocketServer socketServer, UserManager userManager) {
        if (auctionManager == null || bidDAO == null || socketServer == null) {
            throw new IllegalArgumentException("Auction manager, DAO, and socket server are required");
        }
        if (userManager == null) {
            throw new IllegalArgumentException("User manager is required");
        }
        this.auctionManager = auctionManager;
        this.bidDAO = bidDAO;
        this.socketServer = socketServer;
        this.userManager = userManager;
    }

    /** Handles one JSON message and returns the response to the requesting client. */
    public String handleClientMessage(String rawJson) {
        return handleClientMessage(rawJson, new ClientSession());
    }

    /** Handles a message using authentication state scoped to the current connection. */
    public String handleClientMessage(String rawJson, ClientSession session) {
        if (session == null) {
            return response(Actions.ERROR, "Invalid client session");
        }
        try {
            JsonObject request = JsonParser.parseString(rawJson).getAsJsonObject();
            String action = stringValue(request.get("action"));
            if (Actions.LOGIN.equals(action)) {
                return handleLogin(request, session);
            }
            if (Actions.REGISTER.equals(action)) {
                return handleRegister(request);
            }
            if (Actions.LOGOUT.equals(action)) {
                session.clear();
                return response(Actions.LOGOUT_SUCCESS, "Đã đăng xuất.");
            }
            if (Actions.GET_AUCTIONS.equals(action)) {
                return gson.toJson(new MessageDTO(
                        Actions.AUCTION_LIST, "server", auctionManager.getAuctions()));
            }
            if (Actions.GET_MY_BID_HISTORY.equals(action)) {
                if (!session.isAuthenticated()) {
                    return response(Actions.ERROR, "Hãy đăng nhập để xem lịch sử đấu giá.");
                }
                return gson.toJson(new MessageDTO(Actions.MY_BID_HISTORY, "server",
                        bidDAO.findByUserId(session.getUserId())));
            }
            if (Actions.BID.equals(action)) {
                return handleBid(request, session);
            }
            return response(Actions.ERROR, "Unsupported action");
        } catch (RuntimeException exception) {
            return response(Actions.ERROR, "Invalid request");
        }
    }

    private String handleLogin(JsonObject request, ClientSession session) {
        session.clear();
        JsonObject payload = objectValue(request.get("payload"));
        if (payload == null) {
            return response(Actions.LOGIN_FAILED, "Login details are missing");
        }
        Role role = roleValue(stringValue(payload.get("role")));
        User user = userManager.authenticate(
                stringValue(payload.get("username")),
                stringValue(payload.get("password")), role);
        if (user == null) {
            return response(Actions.LOGIN_FAILED, "Tên đăng nhập, mật khẩu hoặc vai trò không đúng.");
        }
        session.authenticate(user);

        JsonObject account = new JsonObject();
        account.addProperty("userId", user.getId());
        account.addProperty("username", user.getUsername());
        account.addProperty("role", user.getRole().name());
        return gson.toJson(new MessageDTO(Actions.LOGIN_SUCCESS, "server", account));
    }

    private String handleRegister(JsonObject request) {
        JsonObject payload = objectValue(request.get("payload"));
        if (payload == null) {
            return response(Actions.REGISTER_FAILED, "Registration details are missing");
        }
        Role role = roleValue(stringValue(payload.get("role")));
        if (role == null || role == Role.ADMIN) {
            return response(Actions.REGISTER_FAILED, "Vai trò đăng ký không hợp lệ.");
        }

        boolean registered = userManager.register(
                stringValue(payload.get("username")),
                stringValue(payload.get("password")),
                stringValue(payload.get("email")), role);
        if (!registered) {
            return response(Actions.REGISTER_FAILED,
                    "Thông tin đăng ký không hợp lệ hoặc tên đăng nhập đã tồn tại.");
        }
        return response(Actions.REGISTER_SUCCESS, "Tạo tài khoản thành công. Hãy đăng nhập.");
    }

    private synchronized String handleBid(JsonObject request, ClientSession session) {
        if (!session.isAuthenticated() || session.getRole() != Role.BIDDER) {
            return response(Actions.BID_FAILED,
                    "Hãy đăng nhập bằng tài khoản người đấu giá trước khi đặt giá.");
        }

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

        String userId = session.getUserId();
        double amount = amountElement.getAsDouble();
        BidTransaction transaction = auctionManager.placeBidAndPersist(
                auctionId, userId, amount, bidDAO::save);
        if (transaction == null) {
            return response(Actions.BID_FAILED, "Bid was rejected");
        }

        Auction auction = auctionManager.getAuction(auctionId);
        socketServer.broadcast(gson.toJson(
                new MessageDTO(Actions.AUCTION_UPDATE, "server", auction)));
        return gson.toJson(new MessageDTO(Actions.BID_SUCCESS, "server", transaction));
    }

    private String response(String action, String message) {
        return gson.toJson(new MessageDTO(action, "server", message));
    }

    private JsonObject objectValue(JsonElement element) {
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private Role roleValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String stringValue(JsonElement element) {
        return element == null || element.isJsonNull() ? null : element.getAsString();
    }

    @FunctionalInterface
    public interface BidDAO {
        void save(BidTransaction transaction);

        default List<BidTransaction> findByUserId(String userId) {
            return List.of();
        }
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

        @Override
        public List<BidTransaction> findByUserId(String userId) {
            return transactions.stream()
                    .filter(transaction -> userId.equals(transaction.getUserId()))
                    .toList();
        }
    }
}
