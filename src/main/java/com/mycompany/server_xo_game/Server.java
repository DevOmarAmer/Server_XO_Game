package com.mycompany.server_xo_game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static final int PORT = 8888;
    // Store online players and their ClientHandlers
    public static ConcurrentHashMap<String, ClientHandler> onlinePlayers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Attempting to start server on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            
            System.out.println("=================================================================================");
            System.out.println("Server started on port " + PORT);
            System.out.println("=================================================================================");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread t = new Thread(handler);
                t.start();
            }

        } catch (IOException e) {
            System.err.println("Server failed to start. Port " + PORT + " is likely already in use.");
            e.printStackTrace();
        }
    }
}
