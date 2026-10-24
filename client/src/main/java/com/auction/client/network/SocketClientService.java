
package com.auction.client.network;

import com.auction.shared.network.MessageDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SocketClientService {

    private static final SocketClientService INSTANCE =
            new SocketClientService();

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private final Gson gson = new Gson();

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenerThread;

    private Consumer<JsonObject> messageListener;

    private SocketClientService() {
    }

    public static SocketClientService getInstance() {
        return INSTANCE;
    }

    public synchronized void setMessageListener(
            Consumer<JsonObject> listener) {
        this.messageListener = listener;
    }

    public synchronized boolean ensureConnected() {
        try {
            if (socket != null
                    && socket.isConnected()
                    && !socket.isClosed()) {
                return true;
            }

            socket = new Socket(HOST, PORT);

            reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );

            writer = new PrintWriter(
                    socket.getOutputStream(),
                    true,
                    StandardCharsets.UTF_8
            );

            startListenerThread();
            return true;

        } catch (IOException e) {
            System.err.println(
                    "Không thể kết nối server: " + e.getMessage()
            );
            return false;
        }
    }

    private void startListenerThread() {
        listenerThread = new Thread(() -> {
            try {
                String json;

                while ((json = reader.readLine()) != null) {
                    handleIncomingMessage(json);
                }

            } catch (IOException e) {
                System.err.println(
                        "Mất kết nối server: " + e.getMessage()
                );

            } finally {
                cleanup();
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized void sendMessage(
            String action,
            String sender,
            Object payload) {

        if (!ensureConnected()) {
            return;
        }

        MessageDTO message =
                new MessageDTO(action, sender, payload);

        writer.println(gson.toJson(message));

        if (writer.checkError()) {
            System.err.println("Không gửi được dữ liệu đến server.");
        }
    }

    private void handleIncomingMessage(String json) {
        try {
            JsonObject message =
                    JsonParser.parseString(json).getAsJsonObject();

            JsonElement actionElement = message.get("action");

            String action = actionElement == null
                    || actionElement.isJsonNull()
                    ? ""
                    : actionElement.getAsString();

            System.out.println("Server gửi: " + action);

            Consumer<JsonObject> listener;

            synchronized (this) {
                listener = messageListener;
            }

            if (listener != null) {
                listener.accept(message);
            }

        } catch (Exception e) {
            System.err.println(
                    "Không đọc được JSON từ server: " + e.getMessage()
            );
        }
    }

    public void login(
            String username,
            String password,
            String role) {

        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("password", password);
        payload.addProperty("role", role);

        sendMessage("LOGIN", username, payload);
    }

    public void registerUser(
            String username,
            String password,
            String email) {

        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("password", password);
        payload.addProperty("email", email);

        sendMessage("REGISTER", username, payload);
    }

    public void requestAuctionList() {
        sendMessage("GET_AUCTIONS", "CLIENT", null);
    }

    public synchronized void cleanup() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        if (writer != null) {
            writer.close();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        reader = null;
        writer = null;
        socket = null;
    }
}
