package com.auction.server.network;

import com.auction.server.core.AuctionOrchestrator;
import com.auction.shared.constants.Actions;
import com.auction.shared.network.MessageDTO;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private final int port;
    private final String bindAddress;
    private ServerSocket serverSocket;
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private volatile boolean isRunning = false;
    private volatile AuctionOrchestrator orchestrator;
    private final Gson gson = new Gson();

    public SocketServer(int port) {
        this(port, System.getProperty("auction.server.bind",
                System.getenv().getOrDefault("AUCTION_SERVER_BIND", "127.0.0.1")));
    }

    public SocketServer(int port, String bindAddress) {
        this.port = port;
        this.bindAddress = bindAddress;
    }

    public void setOrchestrator(AuctionOrchestrator orchestrator) {
        if (orchestrator == null) {
            throw new IllegalArgumentException("Orchestrator is required");
        }
        this.orchestrator = orchestrator;
    }

    public String handleClientMessage(String rawJson) {
        return handleClientMessage(rawJson, new ClientSession());
    }

    public String handleClientMessage(String rawJson, ClientSession session) {
        AuctionOrchestrator currentOrchestrator = orchestrator;
        if (currentOrchestrator == null) {
            return gson.toJson(new MessageDTO(Actions.ERROR, "server", "Server is not configured"));
        }
        return currentOrchestrator.handleClientMessage(rawJson, session);
    }

    public void start() {
        if (orchestrator == null) {
            throw new IllegalStateException("Set an AuctionOrchestrator before starting the socket server");
        }
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(bindAddress));
            System.out.println("Socket Server đang lắng nghe tại " + bindAddress + ":" + port);

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
        com.auction.server.ServerApp.main(args);
    }
}
