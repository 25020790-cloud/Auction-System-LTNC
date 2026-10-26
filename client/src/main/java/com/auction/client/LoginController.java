
package com.auction.client;

import com.auction.client.network.SocketClientService;
import com.auction.client.session.SessionContext;
import com.auction.shared.constants.Actions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink signupLink;
    @FXML private ToggleButton roleSellerBtn;
    @FXML private ToggleButton roleBidderBtn;

    private Stage currentStage;

    @FXML
    public void initialize() {
        if (roleBidderBtn != null) {
            roleBidderBtn.setSelected(true);
        }

        SocketClientService.getInstance()
                .setMessageListener(this::handleServerMessage);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Cảnh báo",
                    "Vui lòng nhập đầy đủ Username và Password!"
            );
            return;
        }

        currentStage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();

        if (loginButton != null) {
            loginButton.setDisable(true);
        }

        String selectedRole = getSelectedRole();

        if (!SocketClientService.getInstance().login(username, password, selectedRole)) {
            loginButton.setDisable(false);
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối",
                    "Không gửi được yêu cầu đăng nhập. Hãy kiểm tra server đang chạy.");
        }
    }

    private void handleServerMessage(JsonObject message) {
        String action = getString(message, "action", "");
        JsonObject payload = getObject(message, "payload");

        if (Actions.LOGIN_SUCCESS.equals(action)) {
            Platform.runLater(() -> handleLoginSuccess(payload));

        } else if (Actions.LOGIN_FAILED.equals(action)
                || Actions.ERROR.equals(action)) {
            JsonElement payloadElement = message.get("payload");
            String errorMessage = payloadElement != null && payloadElement.isJsonPrimitive()
                    ? payloadElement.getAsString()
                    : getString(payload, "message", "Đăng nhập thất bại. Vui lòng thử lại.");

            Platform.runLater(() -> {
                if (loginButton != null) {
                    loginButton.setDisable(false);
                }

                showAlert(
                        Alert.AlertType.ERROR,
                        "Lỗi đăng nhập",
                        errorMessage
                );
            });
        }
    }

    private void handleLoginSuccess(JsonObject data) {
        if (loginButton != null) {
            loginButton.setDisable(false);
        }

        if (data == null) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Lỗi đăng nhập",
                    "Server không trả về thông tin tài khoản."
            );
            return;
        }

        String role = getString(data, "role", "");

        if (role.isBlank()) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Lỗi tài khoản",
                    "Tài khoản thiếu vai trò (role)."
            );
            return;
        }

        String windowTitle;
        windowTitle = "Luxury Auctions — Danh sách sản phẩm (" + role + ")";

        try {
            String username = getString(data, "username", "");
            String userId = getString(data, "userId", username);
            SessionContext.getInstance().login(userId, username, role);

            java.net.URL auctionListUrl = getClass().getResource(
                    "/com/auction/client/auction-list-view.fxml");
            if (auctionListUrl == null) {
                throw new IOException("Không tìm thấy giao diện danh sách đấu giá.");
            }
            FXMLLoader loader = new FXMLLoader(auctionListUrl);

            Parent root = loader.load();

            if (currentStage != null) {
                currentStage.setScene(new Scene(root));
                currentStage.setTitle(windowTitle);
                currentStage.setMinWidth(1024);
                currentStage.setMinHeight(700);
                currentStage.setMaximized(true);
                currentStage.show();
            }

        } catch (IOException e) {
            SessionContext.getInstance().logout();
            showAlert(
                    Alert.AlertType.ERROR,
                    "Lỗi",
                    "Không thể mở giao diện: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        showAlert(
                Alert.AlertType.INFORMATION,
                "Thông báo",
                "Chức năng khôi phục mật khẩu đang được xây dựng."
        );
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("register-view.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Aureus — Tạo tài khoản mới");
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.show();

        } catch (IOException e) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Lỗi",
                    "Không thể mở trang đăng ký: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleRoleToggle(ActionEvent event) {
        if (roleSellerBtn == null || roleBidderBtn == null) {
            return;
        }

        ToggleButton selected = (ToggleButton) event.getSource();

        if (selected == roleSellerBtn && roleSellerBtn.isSelected()) {
            roleBidderBtn.setSelected(false);
        } else if (selected == roleBidderBtn
                && roleBidderBtn.isSelected()) {
            roleSellerBtn.setSelected(false);
        } else if (!roleSellerBtn.isSelected()
                && !roleBidderBtn.isSelected()) {
            selected.setSelected(true);
        }
    }

    private String getSelectedRole() {
        return roleSellerBtn != null && roleSellerBtn.isSelected()
                ? "SELLER"
                : "BIDDER";
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

    private JsonObject getObject(JsonObject object, String key) {
        if (object == null
                || !object.has(key)
                || !object.get(key).isJsonObject()) {
            return null;
        }

        return object.getAsJsonObject(key);
    }

    private double getDouble(
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

    private void showAlert(
            Alert.AlertType type,
            String title,
            String content) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
