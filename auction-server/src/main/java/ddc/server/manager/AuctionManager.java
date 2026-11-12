package ddc.server.manager;

import ddc.server.exception.AuctionClosedException;
import ddc.server.exception.AuthenticationException;
import ddc.server.exception.InvalidBidException;
import ddc.server.model.Auction;
import ddc.server.model.Bid;
import ddc.server.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lớp quản lý chung các phiên đấu giá và người dùng trong bộ nhớ.
 */
public class AuctionManager {

    private static volatile AuctionManager instance;

    // Các collection này an toàn khi luồng nền đang duyệt dữ liệu.
    private final Map<String, Auction> auctions;
    private final List<User> users;

    private Thread statusCheckerThread;
    private volatile boolean isRunning;

    private AuctionManager() {
        auctions = new ConcurrentHashMap<>();
        users = new CopyOnWriteArrayList<>();
        startStatusChecker();
    }

    /**
     * Trả về đối tượng AuctionManager duy nhất của chương trình.
     */
    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Thêm một người dùng để có thể xác thực khi đăng nhập.
     */
    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        users.add(user);
    }

    /**
     * Kiểm tra tên đăng nhập và mật khẩu trong danh sách users.
     */
    public User authenticateUser(String username, String password) throws AuthenticationException {
        if (username == null || password == null) {
            throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không chính xác.");
        }

        for (User user : users) {
            if (username.equals(user.getUserName()) && password.equals(user.getPassword())) {
                return user;
            }
        }

        throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không chính xác.");
    }

    /**
     * Thêm hoặc cập nhật một phiên đấu giá theo auctionId.
     */
    public void addAuction(Auction auction) {
        if (auction == null || auction.getAuctionId() == null) {
            throw new IllegalArgumentException("Auction and auctionId cannot be null");
        }
        auctions.put(auction.getAuctionId(), auction);
    }

    public Auction getAuctionById(String auctionId) {
        if (auctionId == null) {
            return null;
        }
        return auctions.get(auctionId);
    }

    public Auction getAuctionById(int auctionId) {
        return getAuctionById(String.valueOf(auctionId));
    }

    /**
     * Trả về bản sao để code bên ngoài không sửa trực tiếp collection bên trong.
     */
    public List<Auction> getAllAuctions() {
        return new ArrayList<>(auctions.values());
    }

    /**
     * Tìm phiên đấu giá, sau đó ủy quyền việc kiểm tra và cập nhật giá cho Auction.
     */
    public boolean processBid(int auctionId, Bid bid)
            throws InvalidBidException, AuctionClosedException {
        return processBid(String.valueOf(auctionId), bid);
    }

    /**
     * Phiên bản dùng String để phù hợp với auctionId trong lớp Auction hiện tại.
     */
    public boolean processBid(String auctionId, Bid bid)
            throws InvalidBidException, AuctionClosedException {
        Auction auction = getAuctionById(auctionId);

        if (auction == null) {
            throw new AuctionClosedException("Không tìm thấy phiên đấu giá với ID: " + auctionId);
        }

        return auction.placeBid(bid);
    }

    /**
     * Giữ lại tên hàm cũ để các lớp gọi trước đây vẫn hoạt động.
     */
    public boolean placeBid(String auctionId, Bid bid)
            throws InvalidBidException, AuctionClosedException {
        return processBid(auctionId, bid);
    }

    /**
     * Khởi động một daemon thread để quét trạng thái mỗi giây.
     */
    public synchronized void startStatusChecker() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        statusCheckerThread = new Thread(() -> {
            while (isRunning) {
                checkAuctionStatuses();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    isRunning = false;
                }
            }
        }, "Auction-Status-Checker");

        statusCheckerThread.setDaemon(true);
        statusCheckerThread.start();
    }

    /**
     * Dừng daemon thread khi ứng dụng không cần quản lý đấu giá nữa.
     */
    public synchronized void stopStatusChecker() {
        isRunning = false;

        if (statusCheckerThread != null) {
            statusCheckerThread.interrupt();
        }
    }

    /**
     * Auction tự chuyển OPEN -> RUNNING và RUNNING -> FINISHED theo thời gian hiện tại.
     */
    public void checkAuctionStatuses() {
        long currentTime = System.currentTimeMillis();

        for (Auction auction : auctions.values()) {
            auction.checkTimeAndUpdateStatus(currentTime);
        }
    }
}
