package client.auction;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AuctionController {
    @FXML
    private TextField searchField;

    @FXML
    private Label totalCount;

    @FXML
    private Label activeCount;

    @FXML
    private Label upcomingCount;

    @FXML
    private Label resultCount;

    @FXML
    private FlowPane auctionCards;

    @FXML
    private Label emptyLabel;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    private final List<AuctionItem> auctionList = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private static class AuctionItem {

        private final String name;
        private final String category;
        private final double startingPrice;
        private final double currentPrice;
        private final String status;
        private final String icon;

        public AuctionItem(String name, String category, double startingPrice, double currentPrice, String status, String icon) {
            this.name = name;
            this.category = category;
            this.startingPrice = startingPrice;
            this.currentPrice = currentPrice;
            this.status = status;
            this.icon = icon;
        }
    }

    //Khởi tạo giao diện
    public void initialize() {
        categoryComboBox.getItems().addAll("Tất cả danh mục", "Nghệ thuật", "Thiết bị điện tử", "Phương tiện");
        statusComboBox.getItems().addAll("Tất cả trạng thái", "Đang diễn ra", "Sắp diễn ra", "Đã kết thúc");
        updateStatistics();
        applyFilters();
    }

    //Cập nhật các ô thống kê
    private void updateStatistics() {
        //Tổng số phiên
        totalCount.setText(String.valueOf(auctionList.size()));
        //Phiên đang diễn ra
        long active = auctionList.stream().filter(a -> a.status.equals("Đang diễn ra")).count();
        activeCount.setText(String.valueOf(active));
        //Phiên sắp diễn ra
        long upcoming = auctionList.stream().filter(a -> a.status.equals("Sắp diễn ra")).count();
        upcomingCount.setText(String.valueOf(upcoming));
    }

    //Tìm kiếm và lọc
    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleFilter() {
        applyFilters();
    }

    public void applyFilters() {
        String keyword = searchField.getText();

        if (keyword == null) {
            keyword = "";
        }

        String category = categoryComboBox.getValue();

        String status = statusComboBox.getValue();

        keyword = keyword.trim().toLowerCase();
        final String searchKeyword = keyword;

        List<AuctionItem> filteredList = auctionList.stream()
                .filter(a ->
                        searchKeyword.isEmpty()
                                || a.name.toLowerCase().contains(searchKeyword)
                )
                .filter(a ->
                        category == null
                                || category.equals("Tất cả danh mục")
                                || a.category.equals(category)
                )
                .filter(a ->
                        status == null
                                || status.equals("Tất cả trạng thái")
                                || a.status.equals(status)
                )
                .collect(Collectors.toList());

        displayAuctions(filteredList);
    }

    //Hiển thị giao diện
    private void displayAuctions(List<AuctionItem> auctions) {
        auctionCards.getChildren().clear();
        resultCount.setText(auctions.size() + " kết quả");
        boolean isEmpty = auctions.isEmpty();
        emptyLabel.setVisible(isEmpty);
        emptyLabel.setManaged(isEmpty);

        for (AuctionItem auction : auctions) {
            auctionCards.getChildren().add(createAuctionCard(auction));
        }
    }

    //Tạo thẻ giao diện
    private VBox createAuctionCard(AuctionItem auction) {
        // Thẻ chính
        VBox card = new VBox(12);
        card.getStyleClass().add("auction-card");
        card.setPrefWidth(280);

        // Biểu tượng sản phẩm
        Label iconLabel = new Label(auction.icon);
        iconLabel.getStyleClass().add("product-icon");

        // Tên sản phẩm
        Label nameLabel = new Label(auction.name);
        nameLabel.getStyleClass().add("product-name");

        // Danh mục
        Label categoryLabel = new Label(auction.category);
        categoryLabel.getStyleClass().add("product-category");

        // Trạng thái
        Label statusLabel = new Label(auction.status);
        statusLabel.getStyleClass().add("status-label");

        // Thêm CSS tương ứng với trạng thái
        switch (auction.status) {
            case "Đang diễn ra":
                statusLabel.getStyleClass().add("status-active");
                break;

            case "Sắp diễn ra":
                statusLabel.getStyleClass().add("status-upcoming");
                break;

            case "Đã kết thúc":
                statusLabel.getStyleClass().add("status-finished");
                break;
        }

        // Giá khởi điểm
        Label startingPriceDescription = new Label("Giá khởi điểm");
        startingPriceDescription.getStyleClass().add("price-description");
        Label startingPriceLabel = new Label(currencyFormat.format(auction.startingPrice) + " VNĐ");
        startingPriceLabel.getStyleClass().add("price-description");

        // Giá hiện tại
        Label currentPriceDescription = new Label("Giá hiện tại");
        currentPriceDescription.getStyleClass().add("price-description");
        Label currentPriceLabel = new Label(currencyFormat.format(auction.currentPrice) + " VNĐ");
        currentPriceLabel.getStyleClass().add("current-price");

        // Nút xem chi tiết
        Button detailButton = new Button("Xem chi tiết");
        detailButton.getStyleClass().add("detail-button");

        // Khi nhấn nút sẽ hiển thị thông tin phiên
        detailButton.setOnAction(event -> showAuctionDetails(auction));

        // Thêm các thành phần vào thẻ
        card.getChildren().addAll(
                iconLabel,
                nameLabel,
                categoryLabel,
                statusLabel,
                startingPriceDescription,
                startingPriceLabel,
                currentPriceDescription,
                currentPriceLabel,
                detailButton
        );

        return card;
    }

    //Thông tin phiên
    private void showAuctionDetails(AuctionItem auction) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Chi tiết phiên đấu giá");
        alert.setHeaderText(auction.name);

        alert.setContentText(
                "Danh mục: " + auction.category
                        + "\nGiá khởi điểm: "
                        + currencyFormat.format(auction.startingPrice) + " VNĐ"
                        + "\nGiá hiện tại: "
                        + currencyFormat.format(auction.currentPrice) + " VNĐ"
                        + "\nTrạng thái: " + auction.status
        );

        alert.showAndWait();
    }

    //Đăng xuất
    @FXML
    private void handleLogout() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Chức năng đăng xuất sẽ được hoàn thiện sau.");

        alert.showAndWait();
    }

}