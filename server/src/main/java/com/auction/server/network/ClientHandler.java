package com.auction.server.network;

import com.auction.shared.constants.Actions;
import com.auction.shared.network.MessageDTO;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler implements Runnable {
    private static final Gson GSON = new Gson();
    private static final int MAX_MESSAGE_CHARS = 64 * 1024;

    private final Socket clientSocket;
    private final SocketServer server;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private volatile PrintWriter out;

    public ClientHandler(Socket clientSocket, SocketServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8))) {
            out = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8);

            String jsonLine;
            while (!closed.get() && (jsonLine = readMessage(in)) != null) {
                handleMessage(jsonLine);
            }
        } catch (IOException e) {
            if (!closed.get()) {
                System.err.println("Kết nối client bị đóng hoặc lỗi: "
                        + clientSocket.getRemoteSocketAddress());
            }
        } finally {
            cleanup();
        }
    }

    private String readMessage(BufferedReader in) throws IOException {
        StringBuilder message = new StringBuilder();
        int character;
        while ((character = in.read()) != -1) {
            if (character == '\n') {
                int length = message.length();
                if (length > 0 && message.charAt(length - 1) == '\r') {
                    message.setLength(length - 1);
                }
                return message.toString();
            }
            if (message.length() >= MAX_MESSAGE_CHARS) {
                throw new IOException("Thông điệp vượt quá giới hạn cho phép");
            }
            message.append((char) character);
        }
        return message.isEmpty() ? null : message.toString();
    }

    private void handleMessage(String rawJson) {
        final MessageDTO request;
        try {
            request = GSON.fromJson(rawJson, MessageDTO.class);
        } catch (JsonParseException e) {
            sendResponse(Actions.ERROR, "JSON không hợp lệ");
            return;
        }

        if (request == null || request.getAction() == null || request.getAction().isBlank()) {
            sendResponse(Actions.ERROR, "Thiếu action trong thông điệp");
            return;
        }

        // Chỉ ghi action và địa chỉ kết nối; payload có thể chứa thông tin đăng nhập.
        System.out.println("Nhận action " + request.getAction() + " từ "
                + clientSocket.getRemoteSocketAddress());

        switch (request.getAction()) {
            case Actions.LOGIN -> sendResponse(Actions.LOGIN_FAILED,
                    "Chức năng đăng nhập chưa được triển khai");
            case Actions.REGISTER -> sendResponse(Actions.REGISTER_FAILED,
                    "Chức năng đăng ký chưa được triển khai");
            case Actions.BID -> sendResponse(Actions.BID_FAILED,
                    "Chức năng đặt giá chưa được triển khai");
            case Actions.DISCONNECT -> cleanup();
            default -> sendResponse(Actions.ERROR, "Action không được hỗ trợ");
        }
    }

    private void sendResponse(String action, String message) {
        sendMessage(GSON.toJson(new MessageDTO(action, "SERVER", message)));
    }

    public void sendMessage(String jsonMessage) {
        PrintWriter writer = out;
        if (writer != null && !closed.get()) {
            writer.println(jsonMessage);
            if (writer.checkError()) {
                cleanup();
            }
        }
    }

    public void cleanup() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        server.removeClient(this);
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Không thể đóng socket client: " + e.getMessage());
        }
        PrintWriter writer = out;
        if (writer != null) {
            writer.close();
        }
    }
}
