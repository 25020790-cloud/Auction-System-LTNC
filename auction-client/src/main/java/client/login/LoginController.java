package client.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberCheckBox;

    @FXML
    private HBox loginRoot;

    @FXML
    private VBox loginLeft;

    @FXML
    private StackPane loginRight;

    @FXML
    private ImageView loginImage;

    @FXML
    private Button loginButton;

    @FXML
    public void initialize() {
        loginLeft.prefWidthProperty().bind(loginRoot.widthProperty().multiply(0.48));
        loginImage.fitWidthProperty().bind(loginRight.widthProperty().multiply(0.88));
        loginImage.fitHeightProperty().bind(loginRight.heightProperty().multiply(0.86));
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng nhập đầy đủ thông tin");
            alert.showAndWait();

            return;
        }

        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        System.out.println("Ghi nhớ đăng nhập: " + rememberCheckBox.isSelected());

        Stage stage = (Stage) loginButton.getScene().getWindow();

        try {
            // Tải giao diện đấu giá
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/auction/auction-view.fxml"));

            Scene scene = new Scene(loader.load());

            // Chuyển sang màn hình đấu giá
            stage.setScene(scene);
            stage.setTitle("Auction System");

            stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể mở màn hình đấu giá");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
