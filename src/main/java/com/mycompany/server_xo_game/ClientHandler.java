package com.mycompany.server_xo_game;

import java.io.*;
import java.net.Socket;
import org.json.JSONObject;
import org.json.JSONException;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private PlayerStatus status;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.status = PlayerStatus.ONLINE;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendMessage(JSONObject json) {
        System.out.println("[Server] Sending to " + username + ": " + json.toString());
        out.println(json.toString());
    }
    
    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                try {
                    System.out.println("[Server] Received from " + username + ": " + message);
                    JSONObject request = new JSONObject(message);
                    handleRequest(request);
                } catch (JSONException e) {
                    System.err.println("Received invalid JSON from client: " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + username);
        } finally {
            cleanup();
        }
    }
    
    private void handleRequest(JSONObject request) {
        String type = request.getString("type");
        System.out.println("[Server] Handling request type: " + type + " from " + username);
        
        switch (type) {
            case "login":
                ServerController.login(this, request);
                break;
            case "register":
                ServerController.register(this, request);
                break;
            case "get_available_players":
                ServerController.sendAvailablePlayers(this);
                break;
            case "send_invite":  
                ServerController.sendInvite(this, request);
                break;
            case "invite_response":
                ServerController.handleInviteResponse(this, request);
                break;
            case "move":
                ServerController.handleMove(this, request);
                break;
            case "play_again":
                ServerController.handlePlayAgain(this, request);
                break;
            case "quit_game":
                ServerController.handleQuitGame(this);
                break;
            default:
                System.out.println("Unknown request type: " + type);
        }
    }
    
    private void cleanup() {
        if (username != null) {
            Server.onlinePlayers.remove(username);
            GameSession session = GameSessionManager.getSession(this);
            if (session != null) {
                session.handlePlayerQuit(this);
            }
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setStatus(PlayerStatus status) {
        this.status = status;
    }
    
    public PlayerStatus getStatus() {
        return status;
    }
}