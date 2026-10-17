package com.auction.server.network;

import com.auction.shared.constants.Actions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final SocketServer server;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isRunning = true;

    public ClientHandler(Socket clientSocket, SocketServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8);

            String jsonLine;
            // Đọc liên tục các thông điệp từ client gửi lên
            while (isRunning && (jsonLine = in.readLine()) != null) {
                handleMessage(jsonLine);
            }
        } catch (IOException e) {
            System.err.println("Mất kết nối với client: " + clientSocket.getRemoteSocketAddress());
        } finally {
            cleanup();
        }
    }

    private void handleMessage(String rawJson) {
        System.out.println("Server nhận từ [" + clientSocket.getRemoteSocketAddress() + "]: " + rawJson);
        // Phản hồi mẫu kiểm tra kết nối
        sendMessage("{\"action\":\"" + Actions.AUCTION_UPDATE + "\",\"payload\":\"Connected successfully\"}");
    }

    public void sendMessage(String jsonMessage) {
        if (out != null) {
            out.println(jsonMessage);
        }
    }

    public void cleanup() {
        isRunning = false;
        server.removeClient(this);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}