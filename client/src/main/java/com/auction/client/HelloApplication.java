package com.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlUrl = HelloApplication.class.getResource("/com/auction/client/login-view.fxml");

        if (fxmlUrl == null) {
            throw new IOException("Không tìm thấy file login-view.fxml");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(fxmlLoader.load(), 900, 560);

        stage.setTitle("Auctions - Đăng nhập");
        stage.setScene(scene);
        stage.show();
    }
}