package com.mycompany.server_xo_game;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static ServerSocket serverSocket;
    public static final int PORT = 8888;
    public static volatile boolean isRunning = false;
    // Store online players and their ClientHandlers
    public static ConcurrentHashMap<String, ClientHandler> onlinePlayers = new ConcurrentHashMap<>();
    
    public static String getServerIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            String wifiIP = null;
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                String displayName = iface.getDisplayName().toLowerCase();
                boolean isPreferred = displayName.contains("wi-fi") || displayName.contains("wlan") || displayName.contains("eth");

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.isLinkLocalAddress() || addr.isLoopbackAddress() || !(addr instanceof java.net.Inet4Address))
                        continue;
                    
                    if (isPreferred) {
                        return addr.getHostAddress();
                    }
                    if (wifiIP == null) {
                        wifiIP = addr.getHostAddress();
                    }
                }
            }
            if (wifiIP != null) {
                return wifiIP;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Unavailable";
    }
    
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