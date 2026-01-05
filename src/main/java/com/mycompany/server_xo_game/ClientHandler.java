package com.mycompany.server_xo_game;

import java.io.*;
import java.net.Socket;
import org.json.JSONObject;
import org.json.JSONException;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username; // logged-in player's username

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(JSONObject json) {
        out.println(json.toString());
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                try {
                    JSONObject request = new JSONObject(message);
                    handleRequest(request);
                } catch (JSONException e) {
                    System.err.println("Received invalid JSON from client: " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + username);
        } finally {
            if (username != null) {
                Server.onlinePlayers.remove(username);
            }
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handleRequest(JSONObject request) {
        String type = request.getString("type");
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
            case "invite":
                ServerController.sendInvite(this, request);
                break;
            case "invite_response":
                ServerController.handleInviteResponse(this, request);
                break;
            case "move":
                ServerController.handleMove(this, request);
                break;
            default:
                System.out.println("Unknown request type: " + type);
        }
    }

    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }
}
