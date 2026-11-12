package ddc.server.manager;

import ddc.server.exception.AuctionClosedException;
import ddc.server.exception.AuthenticationException;
import ddc.server.exception.InvalidBidException;
import ddc.server.model.Auction;
import ddc.server.model.AuctionStatus;
import ddc.server.model.Bid;
import ddc.server.model.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {

    // 1. Áp dụng Singleton Pattern (Thread-safe với volatile + Double-Checked Locking)
    private static volatile AuctionManager instance;

    // 2. Cấu trúc dữ liệu Thread-Safe tránh ConcurrentModificationException
    private final Map<String, Auction> auctions;
    private final List<User> users;

    // Quản lý luồng ngầm (Daemon Thread)
    private Thread statusCheckerThread;
    private volatile boolean isRunning = false;

    private AuctionManager() {
        this.auctions = new ConcurrentHashMap<>();
        this.users = new CopyOnWriteArrayList<>();
    }

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

    // =========================================================================
    // NHIỆM VỤ 1: LOGIC XÁC THỰC TÀI KHOẢN (AUTHENTICATION)
    // =========================================================================

    /**
     * Xác thực người dùng đăng nhập.
     * @param username Tên đăng nhập (userName trong lớp User)
     * @param password Mật khẩu
     * @return Đối tượng User nếu hợp lệ
     * @throws AuthenticationException Nếu sai tên đăng nhập hoặc mật khẩu
     */
    public User authenticateUser(String username, String password) throws AuthenticationException {
        if (username == null || password == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không chính xác.");
        }

        for (User user : users) {
            if (user.getUserName().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }

        throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không chính xác.");
    }

    // =========================================================================
    // NHIỆM VỤ 2: ỦY QUYỀN VÀ QUẢN LÝ NGOẠI LỆ ĐẶT GIÁ (PROCESS BID)
    // =========================================================================

    /**
     * Xử lý đặt giá theo auctionId kiểu String (Chuẩn theo Auction.java Tuần 7).
     */
    public boolean processBid(String auctionId, Bid bid) throws InvalidBidException, AuctionClosedException {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            throw new AuctionClosedException("Không tìm thấy phiên đấu giá với ID: " + auctionId);
        }
        // Ủy quyền cho Auction.placeBid(bid) và để Exception truyền thẳng ra ngoài
        return auction.placeBid(bid);
    }

    /**
     * Overloading: Hỗ trợ thêm tham số int auctionId theo đúng đặc tả file phân công Tuần 8
     * (Giúp code của TV3 viết Unit Test hoặc TV5 gọi hàm không bị lỗi kiểu dữ liệu).
     */
    public boolean processBid(int auctionId, Bid bid) throws InvalidBidException, AuctionClosedException {
        return processBid(String.valueOf(auctionId), bid);
    }

    // =========================================================================
    // NHIỆM VỤ 3: LUỒNG NGẦM QUẢN LÝ TRẠNG THÁI (DAEMON THREAD)
    // =========================================================================

    /**
     * Khởi chạy Daemon Thread quét định kỳ danh sách phiên đấu giá.
     */
    public synchronized void startStatusChecker() {
        if (isRunning) {
            return; // Tránh khởi tạo trùng lặp nhiều luồng
        }
        isRunning = true;

        statusCheckerThread = new Thread(() -> {
            while (isRunning) {
                try {
                    checkAuctionStatuses();
                    Thread.sleep(1000); // Quét định kỳ mỗi 1 giây
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Auction-Status-Checker-Daemon");

        // Đặt làm Daemon Thread để luồng tự động tắt khi ứng dụng chính kết thúc
        statusCheckerThread.setDaemon(true);
        statusCheckerThread.start();
    }

    /**
     * Dừng luồng quét trạng thái an toàn.
     */
    public synchronized void stopStatusChecker() {
        isRunning = false;
        if (statusCheckerThread != null) {
            statusCheckerThread.interrupt();
        }
    }

    /**
     * Quét và tự động chuyển trạng thái phiên:
     * OPEN -> RUNNING (khi chạm startTime)
     * RUNNING -> FINISHED (khi chạm endTime)
     */
    public void checkAuctionStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // Duyệt trên ConcurrentHashMap hoàn toàn Thread-safe, không bị ConcurrentModificationException
        for (Auction auction : auctions.values()) {
            AuctionStatus currentStatus = auction.getStatus();

            if (currentStatus == AuctionStatus.OPEN) {
                if (auction.getStartTime() != null && !now.isBefore(auction.getStartTime())) {
                    auction