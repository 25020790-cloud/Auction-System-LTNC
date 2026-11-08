package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/login/login-view.fxml"));

        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Auction - Đăng nhập");
        stage.setScene(scene);

        stage.setWidth(1000);
        stage.setHeight(650);

        stage.setMinWidth(800);
        stage.setMinHeight(550);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
