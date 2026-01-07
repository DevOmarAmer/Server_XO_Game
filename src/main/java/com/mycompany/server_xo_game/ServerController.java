package com.mycompany.server_xo_game;

import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;

public class ServerController {

    public static void login(ClientHandler client, JSONObject request) {
        String username = request.getString("username");
        String password = request.getString("password");

        boolean success = DAO.login(username, password);
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
        for (ClientHandler handler : Server.onlinePlayers.values()) {
            if (!handler.getUsername().equals(client.getUsername())) {
                JSONObject playerInfo = new JSONObject();
                playerInfo.put("username", handler.getUsername());
                playerInfo.put("status", handler.getStatus().toString());
                players.put(playerInfo);
            }
        }
        JSONObject response = new JSONObject();
        response.put("type", "available_players");
        response.put("players", players);
        client.sendMessage(response);
    }

    public static void sendInvite(ClientHandler sender, JSONObject request) {
        String toUser = request.getString("to");
        ClientHandler receiver = Server.onlinePlayers.get(toUser);

        if (receiver != null && receiver.getStatus() == PlayerStatus.ONLINE) {
            JSONObject invite = new JSONObject();
            invite.put("type", "invitation");
            invite.put("from", sender.getUsername());
            receiver.sendMessage(invite);
            
            // Send confirmation to sender
            JSONObject confirmation = new JSONObject();
            confirmation.put("type", "sent");
            confirmation.put("status", "success");
            sender.sendMessage(confirmation);
        } else {
            JSONObject error = new JSONObject();
            error.put("type", "invite_sent");
            error.put("status", "failed");
            error.put("reason", receiver == null ? "Player not found" : "Player is busy");
            sender.sendMessage(error);
        }
    }

    public static void handleInviteResponse(ClientHandler responder, JSONObject request) {
        String fromUser = request.getString("from");
        boolean accepted = request.getBoolean("accepted");

        ClientHandler sender = Server.onlinePlayers.get(fromUser);
        if (sender != null) {
            JSONObject response = new JSONObject();
            response.put("type", "invite_response");
            response.put("accepted", accepted);
            response.put("from", responder.getUsername());

            sender.sendMessage(response);

            if (accepted) {
                sender.setStatus(PlayerStatus.IN_GAME);
                responder.setStatus(PlayerStatus.IN_GAME);

                GameSession session = new GameSession(sender, responder);
                new Thread(session).start();
            }
        }
    }

    public static void handleMove(ClientHandler client, JSONObject request) {
        int row = request.getInt("row");
        int col = request.getInt("col");
        GameSession session = GameSessionManager.getSession(client);
        if (session != null) {
            session.makeMove(client, row, col);
        }
    }
    
    public static void handlePlayAgain(ClientHandler client, JSONObject request) {
        GameSession session = GameSessionManager.getSession(client);
        if (session != null) {
            session.handlePlayAgainRequest(client);
        }
    }
    
    public static void handleQuitGame(ClientHandler client) {
        GameSession session = GameSessionManager.getSession(client);
        if (session != null) {
            session.handlePlayerQuit(client);
        }
    }
}