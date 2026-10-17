package com.auction.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Auction System");
        stage.setScene(new Scene(new StackPane(new Label("Auction System")), 480, 320));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
