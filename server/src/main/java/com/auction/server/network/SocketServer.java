package com.auction.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketServer {
    private static final int MAX_CLIENTS = 64;
    private static final int QUEUED_CLIENTS = 128;
    private static final int CLIENT_READ_TIMEOUT_MS = 120_000;

    private final int port;
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            MAX_CLIENTS,
            MAX_CLIENTS,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(QUEUED_CLIENTS),
            new ThreadPoolExecutor.AbortPolicy());
    private volatile ServerSocket serverSocket;
    private volatile boolean isRunning;

    public SocketServer(int port) {
        this.port = port;
    }

    public void start() {
        if (isRunning || threadPool.isShutdown()) {
            throw new IllegalStateException("Server đang chạy hoặc đã dừng");
        }
        isRunning = true;
        try (ServerSocket listeningSocket = new ServerSocket(port)) {
            serverSocket = listeningSocket;
            System.out.println("Socket Server đang lắng nghe tại port: " + port);

            while (isRunning) {
                Socket clientSocket = listeningSocket.accept();
                if (!isRunning) {
                    clientSocket.close();
                    break;
                }
                try {
                    clientSocket.setSoTimeout(CLIENT_READ_TIMEOUT_MS);
                } catch (IOException e) {
                    clientSocket.close();
                    System.err.println("Không thể cấu hình kết nối client: " + e.getMessage());
                    continue;
                }

                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                try {
                    threadPool.execute(handler);
                    System.out.println("Client mới kết nối: " + clientSocket.getRemoteSocketAddress());
                } catch (RejectedExecutionException e) {
                    handler.cleanup();
                    System.err.println("Từ chối kết nối do server đã đạt giới hạn client");
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Lỗi Socket Server: " + e.getMessage());
            }
        } finally {
            serverSocket = null;
            stop();
        }
    }

    public void broadcast(String jsonMessage) {
        List<ClientHandler> clientSnapshot;
        synchronized (clients) {
            clientSnapshot = new ArrayList<>(clients);
        }
        for (ClientHandler client : clientSnapshot) {
            client.sendMessage(jsonMessage);
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }

    public void stop() {
        isRunning = false;
        ServerSocket listeningSocket = serverSocket;
        if (listeningSocket != null && !listeningSocket.isClosed()) {
            try {
                listeningSocket.close();
            } catch (IOException e) {
                System.err.println("Không thể đóng server socket: " + e.getMessage());
            }
        }

        List<ClientHandler> clientSnapshot;
        synchronized (clients) {
            clientSnapshot = new ArrayList<>(clients);
        }
        for (ClientHandler client : clientSnapshot) {
            client.cleanup();
        }

        threadPool.shutdownNow();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Một số luồng client chưa dừng sau 5 giây");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        int port = args.length == 0 ? 8888 : Integer.parseInt(args[0]);
        SocketServer server = new SocketServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "server-shutdown"));
        server.start();
    }
}
