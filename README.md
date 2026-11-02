# Auction-System-LTNC
Bài tập lớn LTNC 2026 - Hệ thống Đấu giá trực tuyến

## Cấu hình chạy

Server mặc định chỉ lắng nghe trên `127.0.0.1:8888`. Có thể truyền cổng khác làm tham số đầu tiên khi chạy server. Để nhận kết nối từ máy khác, đặt `AUCTION_SERVER_BIND=0.0.0.0` trên server.

Client mặc định kết nối `127.0.0.1:8888`. Khi chạy client trên máy khác, đặt `AUCTION_SERVER_HOST` và có thể đặt `AUCTION_SERVER_PORT` để trỏ tới server. Socket hiện dùng TCP thường, nên chỉ mở kết nối từ xa trên mạng tin cậy.

Tài khoản, phiên đấu giá và lịch sử giá được lưu trong thư mục `data/`. Có thể đổi thư mục bằng biến môi trường `AUCTION_DATA_DIR` hoặc thuộc tính JVM `-Dauction.data.dir=...`.
