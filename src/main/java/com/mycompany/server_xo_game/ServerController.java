/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server_xo_game;

import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author dell
 */
public class ServerController {

    public static void login(ClientHandler client, JSONObject request) {
        String username = request.getString("username");
        String password = request.getString("password");

        boolean success = DAO.login(username, password); // call DAO
        JSONObject response = new JSONObject();
        response.put("type", "login_response");
        if (success) {
            client.setUsername(username);
            Server.onlinePlayers.put(username, client);
            response.put("status", "success");
        } else {
            response.put("status", "failed");
            response.put("reason", "Invalid credentials");
        }
        client.sendMessage(response);
    }

    public static void register(ClientHandler client, JSONObject request) {
        String username = request.getString("username");
        String password = request.getString("password");
        String email = request.getString("email");

        JSONObject response = new JSONObject();
        response.put("type", "register_response");

        DAO dao = null;
        try {
            dao = new DAO();
            boolean success = dao.register(new PlayerModel(username, email, password, 0, 0));
            response.put("status", success ? "success" : "failed");
        } catch (SQLException ex) {
            System.err.println("Registration Failed: " + ex.getMessage());
            ex.printStackTrace();
            System.getLogger(ServerController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            response.put("status", "failed");
            response.put("reason", ex.getMessage());
        } finally {
            if (dao != null) {
                try {
                    dao.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        client.sendMessage(response);
    }

    public static void sendAvailablePlayers(ClientHandler client) {
        JSONArray players = new JSONArray();
        // Iterate over values to access ClientHandler objects (which hold the status)
        for (ClientHandler handler : Server.onlinePlayers.values()) {
            if (!handler.getUsername().equals(client.getUsername())) {
                JSONObject playerInfo = new JSONObject();
                playerInfo.put("username", handler.getUsername());
                playerInfo.put("status", handler.getStatus().toString()); // Add status
                players.put(playerInfo);
            }
        }
        JSONObject response = new JSONObject();
        response.put("type", "available_players");
        response.put("players", players);
        client.sendMessage(response);
    }

    // Send invite to another player
    public static void sendInvite(ClientHandler sender, JSONObject request) {
        String toUser = request.getString("to");
        ClientHandler receiver = Server.onlinePlayers.get(toUser);

        if (receiver != null) {
            JSONObject invite = new JSONObject();
            invite.put("type", "invitation");
            invite.put("from", sender.getUsername());
            receiver.sendMessage(invite);
        }
    }

    // Handle invite response
    public static void handleInviteResponse(ClientHandler responder, JSONObject request) {
        String fromUser = request.getString("from");
        boolean accepted = request.getBoolean("accepted");

        ClientHandler sender = Server.onlinePlayers.get(fromUser);
        if (sender != null) {
            JSONObject response = new JSONObject();
            response.put("type", "invite_response");
            response.put("accepted", accepted);

            sender.sendMessage(response);

            if (accepted) {
                // Set both players to IN_GAME
                sender.setStatus(PlayerStatus.IN_GAME);
                responder.setStatus(PlayerStatus.IN_GAME);

                // Start a new game session
                GameSession session = new GameSession(sender, responder);
                new Thread(session).start();
            }
        }
    }

    // Handle move made by a player
    public static void handleMove(ClientHandler client, JSONObject request) {
        int row = request.getInt("row");
        int col = request.getInt("col");
        // Find the GameSession for this player
        GameSession session = GameSessionManager.getSession(client);
        if (session != null) {
            session.makeMove(client, row, col);
        }
    }

}
