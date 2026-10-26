
package com.auction.client;

import com.auction.client.network.SocketClientService;
import com.auction.client.session.SessionContext;
import com.auction.shared.constants.Actions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class AuctionListController {

    @FXML private FlowPane productGrid;
    @FXML private Label balanceLabel;
    @FXML private Button filterWatches;
    @FXML private Button filterBags;
    @FXML private Button filterJewelry;
    @FXML private Button filterFineart;
    @FXML private Button filterElectronics;
    @FXML private Button filterVehicles;
    @FXML private Button filterCollectibles;
    @FXML private Button filterOther;
    @FXML private Button filterAll;
    @FXML private Button filterMyHistory;
    @FXML private Button filterStatusAll;
    @FXML private Button filterStatusActive;
    @FXML private Button filterStatusEnded;

    private final List<JsonObject> auctionList = new ArrayList<>();
    private final List<Timeline> timers = new ArrayList<>();

    private String currentCategory = "ALL";
    private String currentStatus = "ALL";

    @FXML
    public void initialize() {
        SocketClientService service =
                SocketClientService.getInstance();

        service.setMessageListener(this::handleServerMessage);
        service.requestAuctionList();
    }

    private void handleServerMessage(JsonObject message) {
        String action = getString(message, "action", "");

        if (Actions.BID_SUCCESS.equals(action)) {
            Platform.runLater(() -> showMessage(
                    Alert.AlertType.INFORMATION, "Đặt giá thành công",
                    "Giá của phiên đấu giá đã được cập nhật."));
            return;
        }
        if (Actions.BID_FAILED.equals(action)) {
            Platform.runLater(() -> showMessage(
                    Alert.AlertType.ERROR, "Không thể đặt giá",
                    getString(message, "payload", "Yêu cầu không hợp lệ.")));
            return;
        }
        if (Actions.ERROR.equals(action)) {
            Platform.runLater(() -> showMessage(
                    Alert.AlertType.ERROR, "Yêu cầu thất bại",
                    getString(message, "payload", "Yêu cầu không hợp lệ.")));
            return;
        }
        if (Actions.MY_BID_HISTORY.equals(action)) {
            Platform.runLater(() -> showBidHistory(message.get("payload")));
            return;
        }

        if (!Actions.AUCTION_UPDATE.equals(action)
                && !Actions.AUCTION_LIST.equals(action)
                && !"GET_AUCTIONS_SUCCESS".equals(action)) {
            return;
        }

        JsonElement payload = message.get("payload");

        if (payload == null || payload.isJsonNull()) {
            return;
        }

        JsonArray auctions = null;

        boolean fullList = false;
        if (payload.isJsonArray()) {
            auctions = payload.getAsJsonArray();
            fullList = true;

        } else if (payload.isJsonObject()) {
            JsonObject data = payload.getAsJsonObject();

            if (data.has("auctions")
                    && data.get("auctions").isJsonArray()) {
                auctions = data.getAsJsonArray("auctions");
                fullList = true;

            } else if (data.has("items")
                    && data.get("items").isJsonArray()) {
                auctions = data.getAsJsonArray("items");
                fullList = true;
            } else if (Actions.AUCTION_UPDATE.equals(action)
                    && data.has("auctionId")) {
                receivedAuctionUpdate(data);
                return;
            }
        }

        if (auctions == null) {
            return;
        }

        List<JsonObject> received = new ArrayList<>();

        for (JsonElement element : auctions) {
            if (element.isJsonObject()) {
                received.add(element.getAsJsonObject());
            }
        }

        boolean replaceList = fullList;
        Platform.runLater(() -> {
            if (replaceList) {
                auctionList.clear();
                auctionList.addAll(received);
            }
            renderAuctionList();
        });
    }

    private void receivedAuctionUpdate(JsonObject updatedAuction) {
        Platform.runLater(() -> {
            String updatedId = getString(updatedAuction, "auctionId", "");
            boolean replaced = false;
            for (int index = 0; index < auctionList.size(); index++) {
                if (updatedId.equals(getString(auctionList.get(index), "auctionId", ""))) {
                    auctionList.set(index, updatedAuction);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                auctionList.add(updatedAuction);
            }
            renderAuctionList();
        });
    }

    private void renderAuctionList() {
        stopTimers();
        productGrid.getChildren().clear();

        for (JsonObject auction : auctionList) {
            if (!matchesFilters(auction)) {
                continue;
            }

            productGrid.getChildren().add(createProductCard(auction));
        }
    }

    private boolean matchesFilters(JsonObject auction) {
        String category = getString(auction, "category", "");
        String status = getString(auction, "status", "");

        if (!"ALL".equals(currentCategory)
                && !category.equalsIgnoreCase(currentCategory)) {
            return false;
        }

        boolean ended = isEnded(status) || hasExpired(auction);

        if ("ACTIVE".equals(currentStatus) && ended) {
            return false;
        }

        if ("ENDED".equals(currentStatus) && !ended) {
            return false;
        }

        return true;
    }

    private VBox createProductCard(JsonObject auction) {
        VBox card = new VBox(8);
        card.setPrefWidth(220);
        card.getStyleClass().add("product-card");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(160);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);

        String imageData = getString(
                auction, "imageBase64", ""
        );

        if (!imageData.isBlank()) {
            try {
                byte[] bytes = Base64.getDecoder().decode(imageData);
                imageView.setImage(
                        new Image(new ByteArrayInputStream(bytes))
                );
            } catch (IllegalArgumentException ignored) {
                // Bỏ qua ảnh không hợp lệ.
            }
        }

        imageContainer.getChildren().add(imageView);

        String name = getString(
                auction, "productName",
                getString(auction, "name",
                        getString(auction, "itemId", "Sản phẩm"))
        );

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("product-name");

        String category = getString(auction, "category", "Khác");
        String status = getString(auction, "status", "UNKNOWN");

        Label categoryLabel =
                new Label(category + " • " + status);

        double price = getNumber(
                auction, "currentPrice",
                getNumber(auction, "startPrice", 0)
        );

        Label priceLabel = new Label(
                String.format(Locale.US, "Giá: %,.0f", price)
        );

        Label timerLabel = new Label();
        updateTimerLabel(auction, timerLabel);

        boolean ended = isEnded(status) || hasExpired(auction);

        if (!ended) {
            Timeline timer = new Timeline(
                    new KeyFrame(
                            Duration.seconds(1),
                            event -> updateTimerLabel(
                                    auction, timerLabel
                            )
                    )
            );

            timer.setCycleCount(Timeline.INDEFINITE);
            timer.play();
            timers.add(timer);
        }

        Button detailButton = new Button(
                ended ? "ĐÃ KẾT THÚC" : "BID NOW"
        );

        detailButton.setMaxWidth(Double.MAX_VALUE);
        boolean canBid = "BIDDER".equalsIgnoreCase(
                SessionContext.getInstance().getCurrentRole());
        detailButton.setDisable(ended || !canBid);

        detailButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog(
                    String.format(Locale.US, "%.0f", price + 1));
            dialog.setTitle("Đặt giá");
            dialog.setHeaderText(name);
            dialog.setContentText("Giá mới (tối thiểu "
                    + String.format(Locale.US, "%,.0f", price + 1) + "): ");
            dialog.showAndWait().ifPresent(value -> submitBid(auction, value));
        });

        card.getChildren().addAll(
                imageContainer,
                nameLabel,
                categoryLabel,
                priceLabel,
                timerLabel,
                detailButton
        );

        return card;
    }

    private void updateTimerLabel(
            JsonObject auction,
            Label timerLabel) {

        String status = getString(auction, "status", "");

        if (isEnded(status)) {
            timerLabel.setText("ĐÃ KẾT THÚC");
            return;
        }

        String endTimeText = getString(auction, "endTime", "");

        if (endTimeText.isBlank()) {
            timerLabel.setText("");
            return;
        }

        try {
            LocalDateTime endTime = parseEndTime(auction, endTimeText);

            java.time.Duration remaining =
                    java.time.Duration.between(
                            LocalDateTime.now(), endTime
                    );

            if (remaining.isNegative()
                    || remaining.isZero()) {
                timerLabel.setText("ĐÃ KẾT THÚC");
                return;
            }

            timerLabel.setText(String.format(
                    "Còn %02d giờ %02d phút %02d giây",
                    remaining.toHours(),
                    remaining.toMinutesPart(),
                    remaining.toSecondsPart()
            ));

        } catch (DateTimeParseException | ArithmeticException e) {
            timerLabel.setText("");
        }
    }

    private LocalDateTime parseEndTime(JsonObject auction, String endTimeText) {
        JsonElement endTimeElement = auction.get("endTime");
        if (endTimeElement != null && endTimeElement.isJsonPrimitive()
                && endTimeElement.getAsJsonPrimitive().isNumber()) {
            return Instant.ofEpochMilli(endTimeElement.getAsLong())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return LocalDateTime.parse(endTimeText);
    }

    private boolean hasExpired(JsonObject auction) {
        String endTimeText = getString(auction, "endTime", "");
        if (endTimeText.isBlank()) {
            return false;
        }
        try {
            return !parseEndTime(auction, endTimeText).isAfter(LocalDateTime.now());
        } catch (DateTimeParseException | ArithmeticException exception) {
            return false;
        }
    }

    private void submitBid(JsonObject auction, String bidText) {
        double amount;
        try {
            amount = Double.parseDouble(bidText.trim());
        } catch (NumberFormatException exception) {
            showMessage(Alert.AlertType.WARNING, "Giá không hợp lệ",
                    "Hãy nhập giá dưới dạng số, ví dụ 1500.");
            return;
        }
        if (!Double.isFinite(amount)) {
            showMessage(Alert.AlertType.WARNING, "Giá không hợp lệ",
                    "Giá phải là một số hữu hạn.");
            return;
        }

        String userId = SessionContext.getInstance().getCurrentUserId();
        String auctionId = getString(auction, "auctionId", "");
        if (userId == null || auctionId.isBlank()
                || !SocketClientService.getInstance().placeBid(auctionId, amount)) {
            showMessage(Alert.AlertType.ERROR, "Lỗi kết nối",
                    "Không gửi được giá. Hãy kiểm tra kết nối server.");
        }
    }

    private boolean isEnded(String status) {
        return "FINISHED".equalsIgnoreCase(status)
                || "PAID".equalsIgnoreCase(status)
                || "ENDED".equalsIgnoreCase(status)
                || "CANCELED".equalsIgnoreCase(status);
    }

    private String getString(
            JsonObject object,
            String key,
            String defaultValue) {

        if (object == null
                || !object.has(key)
                || object.get(key).isJsonNull()) {
            return defaultValue;
        }

        try {
            return object.get(key).getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double getNumber(
            JsonObject object,
            String key,
            double defaultValue) {

        if (object == null
                || !object.has(key)
                || object.get(key).isJsonNull()) {
            return defaultValue;
        }

        try {
            return object.get(key).getAsDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @FXML
    private void handleFilterCategory(javafx.event.ActionEvent event) {

        Button clicked = (Button) event.getSource();

        currentCategory = clicked == filterAll
                ? "ALL"
                : clicked.getText().trim();

        updateFilterStyles(clicked);
        renderAuctionList();
    }

    @FXML
    private void handleFilterStatus(javafx.event.ActionEvent event) {

        Button clicked = (Button) event.getSource();

        if (clicked == filterStatusActive) {
            currentStatus = "ACTIVE";
        } else if (clicked == filterStatusEnded) {
            currentStatus = "ENDED";
        } else {
            currentStatus = "ALL";
        }

        updateStatusFilterStyles(clicked);
        renderAuctionList();
    }

    private void updateFilterStyles(Button selected) {
        Button[] buttons = {
                filterWatches, filterBags, filterJewelry,
                filterFineart, filterElectronics, filterVehicles,
                filterCollectibles, filterOther, filterAll
        };

        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("filter-btn-active");
            }
        }

        if (selected != null
                && !selected.getStyleClass()
                .contains("filter-btn-active")) {
            selected.getStyleClass().add("filter-btn-active");
        }
    }

    private void updateStatusFilterStyles(Button selected) {
        Button[] buttons = {
                filterStatusAll, filterStatusActive, filterStatusEnded
        };

        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("filter-btn-active");
            }
        }

        if (selected != null
                && !selected.getStyleClass()
                .contains("filter-btn-active")) {
            selected.getStyleClass().add("filter-btn-active");
        }
    }

    private void stopTimers() {
        for (Timeline timer : timers) {
            timer.stop();
        }

        timers.clear();
    }


    @FXML
    private void handleOpenProfile() {
        SessionContext session = SessionContext.getInstance();
        showMessage(Alert.AlertType.INFORMATION, "Hồ sơ người dùng", "Thông tin tài khoản",
                "Tên đăng nhập: " + valueOrFallback(session.getCurrentUsername())
                        + "\nVai trò: " + valueOrFallback(session.getCurrentRole()));
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Đăng xuất");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        alert.setContentText("Phiên đăng nhập hiện tại sẽ kết thúc.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SocketClientService.getInstance().logout();
                SessionContext.getInstance().logout();
                SocketClientService.getInstance().setMessageListener(null);
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource("/com/auction/client/login-view.fxml"));
                    javafx.scene.Parent root = loader.load();
                    javafx.stage.Stage stage = (javafx.stage.Stage)
                            productGrid.getScene().getWindow();
                    stage.setScene(new javafx.scene.Scene(root));
                    stage.setTitle("Auctions - Đăng nhập");
                } catch (java.io.IOException exception) {
                    showMessage(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình đăng nhập.");
                }
            }
        });
    }

    @FXML
    private void handleMyBidHistory() {
        if (!SocketClientService.getInstance().requestMyBidHistory()) {
            showMessage(Alert.AlertType.ERROR, "Lịch sử đấu giá",
                    "Không thể tải lịch sử. Hãy kiểm tra kết nối server.");
        }
    }

    private void showBidHistory(JsonElement payload) {
        if (payload == null || !payload.isJsonArray() || payload.getAsJsonArray().isEmpty()) {
            showMessage(Alert.AlertType.INFORMATION, "Lịch sử đấu giá", "My Bids",
                    "Bạn chưa có lượt đặt giá nào.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        List<String> entries = new ArrayList<>();
        for (JsonElement element : payload.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject transaction = element.getAsJsonObject();
            String auctionId = getString(transaction, "auctionId", "?");
            double amount = getNumber(transaction, "bidAmount", 0);
            long timestamp = (long) getNumber(transaction, "timestamp", 0);
            entries.add(auctionId + " — " + String.format(Locale.US, "%,.2f", amount)
                    + " — " + formatter.format(Instant.ofEpochMilli(timestamp)));
        }
        showMessage(Alert.AlertType.INFORMATION, "Lịch sử đấu giá", "My Bids",
                String.join("\n", entries));
    }

    private String valueOrFallback(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private void showMessage(Alert.AlertType type, String title, String content) {
        showMessage(type, title, null, content);
    }

    private void showMessage(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
