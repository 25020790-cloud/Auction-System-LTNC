package client.service;

import ddc.server.exception.AuctionClosedException;
import ddc.server.exception.InvalidBidException;
import ddc.server.model.Auction;
import ddc.server.model.AuctionStatus;
import ddc.server.model.user.User;

import java.util.ArrayList;
import java.util.List;

// Lớp dịch vụ đảm nhiệm giao tiếp dữ liệu và xử lý logic đặt giá phía Client
public class AuctionClientService {

    // Danh sách lưu tạm trên Client (hoặc sẽ thay bằng dữ liệu nhận từ Server qua Socket)
    private final List<Auction> localAuctions = new ArrayList<>();

    public AuctionClientService() {
        // TODO: Khởi tạo kết nối Socket tới Server hoặc nạp dữ liệu ban đầu
    }

    /**
     * Lấy danh sách các phiên đấu giá đang hoạt động
     *
     * @return Danh sách các đối tượng Auction
     */
    public List<Auction> fetchActiveAuctions() {
        // TODO: Gửi request GET_ACTIVE_AUCTIONS qua Socket tới Server và nhận List<Auction>
        return new ArrayList<>(localAuctions);
    }

    /**
     * Thực hiện gửi yêu cầu đặt giá cho một phiên đấu giá
     *
     * @param auctionId Mã (String ID) của phiên đấu giá
     * @param amount    Số tiền người dùng đặt
     * @param bidder    Đối tượng người dùng đang đặt giá
     * @throws InvalidBidException    Ném ra nếu giá đặt <= 0 hoặc thấp hơn giá hiện tại
     * @throws AuctionClosedException Ném ra nếu phiên đấu giá đã kết thúc hoặc bị hủy
     */
    public void placeBid(String auctionId, double amount, User bidder)
            throws InvalidBidException, AuctionClosedException {

        if (bidder == null) {
            throw new InvalidBidException("Vui lòng đăng nhập trước khi tham gia đặt giá!");
        }

        if (amount <= 0) {
            throw new InvalidBidException("Số tiền đặt giá phải lớn hơn 0!");
        }

        // Tìm phiên đấu giá tương ứng
        Auction targetAuction = findAuctionById(auctionId);
        if (targetAuction == null) {
            throw new InvalidBidException("Không tìm thấy phiên đấu giá có mã: " + auctionId);
        }

        // Kiểm tra trạng thái phiên đấu giá
        if (targetAuction.getStatus() != AuctionStatus.RUNNING && targetAuction.getStatus() != AuctionStatus.OPEN) {
            throw new AuctionClosedException("Phiên đấu giá #" + auctionId + " đã đóng hoặc kết thúc!");
        }

        // Kiểm tra tính hợp lệ của mức giá đặt
        if (amount <= targetAuction.getCurrentPrice()) {
            throw new InvalidBidException(
                    String.format("Giá đặt (%.2f) phải cao hơn giá hiện tại (%.2f)!",
                            amount, targetAuction.getCurrentPrice())
            );
        }

        // TODO: Gửi gói tin PLACE_BID(auctionId, amount, bidder) qua Socket tới Server
        // Cập nhật tạm giá mới nếu chạy test cục bộ trên Client
        targetAuction.setCurrentPrice(amount);
    }

    // Hàm phụ trợ tìm phiên đấu giá theo ID (Dùng equals vì ID giờ là String)
    private Auction findAuctionById(String auctionId) {
        for (Auction auction : localAuctions) {
            if (auction.getAuctionId().equals(auctionId)) {
                return auction;
            }
        }
        return null;
    }
}
