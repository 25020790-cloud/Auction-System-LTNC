package com.auction.client;

import com.auction.client.network.SocketClientService;
import com.auction.shared.constants.Actions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private Button registerButton;

    private Stage currentStage;

    @FXML
    public void initialize() {
        roleChoiceBox.getItems().setAll("BIDDER", "SELLER");
        roleChoiceBox.setValue("BIDDER");
        SocketClientService.getInstance().setMessageListener(this::handleServerMessage);
    }

    @FXML
    private void handleRegister(javafx.event.ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || email.isEmpty() || password.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng nhập tên đăng nhập, email và mật khẩu.");
            return;
        }

        currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        registerButton.setDisable(true);
        boolean sent = SocketClientService.getInstance().registerUser(
                username, password, email, roleChoiceBox.getValue());
        if (!sent) {
            registerButton.setDisable(false);
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối",
                    "Không gửi được yêu cầu đăng ký. Hãy kiểm tra server đang chạy.");
        }
    }

    private void handleServerMessage(JsonObject message) {
        String action = stringValue(message.get("action"));
        if (Actions.REGISTER_SUCCESS.equals(action)) {
            Platform.runLater(() -> {
                registerButton.setDisable(false);
                showAlert(Alert.AlertType.INFORMATION, "Đăng ký thành công",
                        payloadMessage(message, "Tạo tài khoản thành công. Hãy đăng nhập."));
                showLoginView();
            });
        } else if (Actions.REGISTER_FAILED.equals(action) || Actions.ERROR.equals(action)) {
            Platform.runLater(() -> {
                registerButton.setDisable(false);
                showAlert(Alert.AlertType.ERROR, "Đăng ký thất bại",
                        payloadMessage(message, "Không thể tạo tài khoản."));
            });
        }
    }

    @FXML
    private void handleBackToLogin(javafx.event.ActionEvent event) {
        currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        showLoginView();
    }

    private void showLoginView() {
        if (currentStage == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/client/login-view.fxml"));
            Parent root = loader.load();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Auctions - Đăng nhập");
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình đăng nhập.");
        }
    }

    private String payloadMessage(JsonObject message, String fallback) {
        JsonElement payload = message.get("payload");
        return payload != null && payload.isJsonPrimitive() ? payload.getAsString() : fallback;
    }

    private String stringValue(JsonElement element) {
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
