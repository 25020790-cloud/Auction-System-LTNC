package com.auction.server.network;

import com.auction.server.core.AuctionOrchestrator;
import com.auction.shared.constants.Actions;
import com.auction.shared.network.MessageDTO;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private final int port;
    private ServerSocket serverSocket;
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private volatile boolean isRunning = false;
    private volatile AuctionOrchestrator orchestrator;
    private final Gson gson = new Gson();

    public SocketServer(int port) {
        this.port = port;
    }

    public void setOrchestrator(AuctionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public String handleMessage(String rawJson) {
        AuctionOrchestrator currentOrchestrator = orchestrator;
        if (currentOrchestrator != null) {
            return currentOrchestrator.handleClientMessage(rawJson);
        }
        return gson.toJson(new MessageDTO(Actions.AUCTION_UPDATE, "server", "Connected successfully"));
    }

    public void start() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Socket Server đang lắng nghe tại port: " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client mới kết nối: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Lỗi ServerSocket: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    public void broadcast(String jsonMessage) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(jsonMessage);
            }
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int defaultPort = 8888;
        SocketServer server = new SocketServer(defaultPort);
        server.start();
    }
}
