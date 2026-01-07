package com.mycompany.server_xo_game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static ServerSocket serverSocket;
    public static final int PORT = 8888;
    public static volatile boolean isRunning = false;
    // Store online players and their ClientHandlers
    public static ConcurrentHashMap<String, ClientHandler> onlinePlayers = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        System.out.println("Attempting to start server on port " + PORT + "...");
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("=================================================================================");
            System.out.println("Server started on port " + PORT);
            System.out.println("=================================================================================");
            
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    ClientHandler handler = new ClientHandler(clientSocket);
                     new Thread(handler).start();
                  
                 
                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        System.out.println("Server socket closed");
                        break;
                    }
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Server failed to start. Port " + PORT + " is likely already in use.");
            e.printStackTrace();
        }     }
}