
package com.auction.client;

import com.auction.client.network.SocketClientService;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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

        if (!"AUCTION_UPDATE".equals(action)
                && !"AUCTION_LIST".equals(action)
                && !"GET_AUCTIONS_SUCCESS".equals(action)) {
            return;
        }

        JsonElement payload = message.get("payload");

        if (payload == null || payload.isJsonNull()) {
            return;
        }

        JsonArray auctions = null;

        if (payload.isJsonArray()) {
            auctions = payload.getAsJsonArray();

        } else if (payload.isJsonObject()) {
            JsonObject data = payload.getAsJsonObject();

            if (data.has("auctions")
                    && data.get("auctions").isJsonArray()) {
                auctions = data.getAsJsonArray("auctions");

            } else if (data.has("items")
                    && data.get("items").isJsonArray()) {
                auctions = data.getAsJsonArray("items");
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

        Platform.runLater(() -> {
            auctionList.clear();
            auctionList.addAll(received);
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

        boolean ended = isEnded(status);

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
                getString(auction, "name", "Sản phẩm")
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

        boolean ended = isEnded(status);

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
        detailButton.setDisable(ended);

        detailButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông tin đấu giá");
            alert.setHeaderText(name);
            alert.setContentText(
                    "Giá hiện tại: "
                            + String.format(Locale.US, "%,.0f", price)
                            + "\nChức năng chi tiết chưa được kết nối."
            );
            alert.showAndWait();
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
            LocalDateTime endTime =
                    LocalDateTime.parse(endTimeText);

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

        } catch (DateTimeParseException e) {
            timerLabel.setText("");
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hồ sơ người dùng");
        alert.setHeaderText("Thông tin tài khoản");
        alert.setContentText(
                "Chức năng xem hồ sơ chưa được kết nối."
        );
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Đăng xuất");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        alert.setContentText("Phiên đăng nhập hiện tại sẽ kết thúc.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Chưa chuyển màn hình vì cần biết
                // đường dẫn FXML đăng nhập và cách đóng socket.
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Đăng xuất");
                info.setHeaderText(null);
                info.setContentText(
                        "Bạn đã xác nhận đăng xuất. "
                                + "Cần kết nối thêm logic đăng xuất của ứng dụng."
                );
                info.showAndWait();
            }
        });
    }

    @FXML
    private void handleMyBidHistory() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Lịch sử đấu giá");
        alert.setHeaderText("My Bids");
        alert.setContentText(
                "Chức năng xem lịch sử đấu giá chưa được kết nối."
        );
        alert.showAndWait();
    }
}
