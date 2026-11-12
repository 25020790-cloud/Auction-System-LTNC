/**
*
*/
package client.controller;

import client.service.AuctionClientService;
import ddc.server.exception.AuctionClosedException;
import ddc.server.exception.InvalidBidException;
import ddc.server.model.Auction;
import ddc.server.model.user.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

// Controller điều khiển giao diện Danh sách phiên đấu giá (Tuân thủ MVC)
public class AuctionListController {

    @FXML
    private TableView<Auction> auctionTableView;

    // Đã đổi sang String cho khớp với getAuctionId()
    @FXML
    private TableColumn<Auction, String> colId;

    @FXML
    private TableColumn<Auction, String> colItemName;

    @FXML
    private TableColumn<Auction, Double> colCurrentPrice;

    @FXML
    private TableColumn<Auction, String> colStatus;

    @FXML
    private TextField bidAmountField;

    // Khởi tạo tầng Service để tách biệt logic nghiệp vụ khỏi Controller
    private final AuctionClientService auctionClientService = new AuctionClientService();
    private final ObservableList<Auction> auctionList = FXCollections.observableArrayList();

    // Lưu thông tin người dùng đang đăng nhập hiện tại (sẽ được truyền từ LoginController sang)
    private User currentUser;

    // Hàm tự động chạy ngay khi FXML được nạp lên giao diện
    @FXML
    public void initialize() {
        setupTableColumns();
        refreshAuctionTable();
    }

    // Setter để LoginController truyền thông tin User sau khi đăng nhập thành công
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    // Cấu hình ánh xạ dữ liệu từ thuộc tính của đối tượng Auction lên từng cột của TableView
    private void setupTableColumns() {
        // Ánh xạ vào thuộc tính "auctionId" để JavaFX tự động gọi hàm getAuctionId()
        colId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        colCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Lấy tên sản phẩm từ đối tượng Item bên trong Auction
        colItemName.setCellValueFactory(cellData -> {
            if (cellData.getValue().getItem() != null) {
                return new SimpleStringProperty(cellData.getValue().getItem().getName());
            }
            return new SimpleStringProperty("Không xác định");
        });

        auctionTableView.setItems(auctionList);
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn nút "Đặt giá"
     */
    @FXML
    private void handlePlaceBidAction() {
        // 1. Kiểm tra người dùng đã chọn phiên đấu giá nào trên bảng chưa
        Auction selectedAuction = auctionTableView.getSelectionModel().getSelectedItem();
        if (selectedAuction == null) {
            showErrorPopup("Chưa chọn phiên đấu giá", "Vui lòng chọn một phiên đấu giá trong bảng trước khi đặt giá!");
            return;
        }

        // 2. Kiểm tra và ép kiểu số tiền người dùng nhập vào TextField
        String amountText = bidAmountField.getText().trim();
        if (amountText.isEmpty()) {
            showErrorPopup("Thiếu thông tin", "Vui lòng nhập số tiền bạn muốn đặt!");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showErrorPopup("Sai định dạng số", "Số tiền đặt giá phải là một con số hợp lệ!");
            return;
        }

        // 3. Lấy ID dạng String
        String selectedAuctionId = selectedAuction.getAuctionId();

        // 4. Gọi tầng Service xử lý đặt giá và bắt các ngoại lệ nghiệp vụ
        try {
            auctionClientService.placeBid(selectedAuctionId, amount, currentUser);
            showSuccessAlert("Đặt giá thành công!");
            bidAmountField.clear();
            refreshAuctionTable();
        } catch (InvalidBidException | AuctionClosedException e) {
            showErrorPopup("Lỗi Đặt Giá", e.getMessage()); // Bật Hộp thoại báo lỗi
        }
    }

    /**
     * Tải lại danh sách phiên đấu giá mới nhất lên TableView
     */
    @FXML
    public void refreshAuctionTable() {
        List<Auction> activeAuctions = auctionClientService.fetchActiveAuctions();
        auctionList.setAll(activeAuctions);
        auctionTableView.refresh();
    }

    // Hiển thị hộp thoại thông báo thành công
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Hiển thị hộp thoại cảnh báo lỗi trực quan cho người dùng
    private void showErrorPopup(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Không thể thực hiện giao dịch");
        alert.setContentText(content);
        alert.showAndWait();
    }
}