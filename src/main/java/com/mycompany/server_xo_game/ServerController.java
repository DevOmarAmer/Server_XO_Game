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
            // Note: 0 points, 0 status (assuming 0 is offline/default)
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
    // -------------------------------------------------------------------------
    //  NEW METHODS FIXED BELOW
    // -------------------------------------------------------------------------

    public static void getProfile(ClientHandler client) {
        DAO dao = null;
        try {
            dao = new DAO();
            PlayerModel player = dao.getPlayer(client.getUsername());

            JSONObject response = new JSONObject();
            response.put("type", "profile_response");

            if (player != null) {
                response.put("status", "success");
                response.put("username", player.getUsername());
                response.put("email", player.getEmail());
                response.put("score", player.getPoints());
                // If you don't have these columns in DB yet, send 0 or calculate them
                response.put("wins", player.getWins());
                response.put("losses", player.getLosses());
                response.put("draws", player.getDraws());
            } else {
                response.put("status", "failed");
            }
            client.sendMessage(response);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            if (dao != null) try {
                dao.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void updateProfile(ClientHandler client, JSONObject request) {
        String newUsername = request.getString("username");
        String newEmail = request.getString("email");
        String newPassword = request.optString("password", ""); // Empty if not changing

        DAO dao = null;
        try {
            dao = new DAO();
            boolean success = dao.updatePlayerInfo(client.getUsername(), newUsername, newEmail, newPassword);

            JSONObject response = new JSONObject();
            response.put("type", "update_profile_response");

            if (success) {
                response.put("status", "success");

                // IMPORTANT: If username changed, update the Server's map
                if (!client.getUsername().equals(newUsername)) {
                    Server.onlinePlayers.remove(client.getUsername()); // Remove old key
                    client.setUsername(newUsername);                   // Update object
                    Server.onlinePlayers.put(newUsername, client);     // Add new key
                }
            } else {
                response.put("status", "failed");
                response.put("reason", "Update failed (Username might be taken)");
            }
            client.sendMessage(response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Send failure message so client loader stops
            JSONObject response = new JSONObject();
            response.put("type", "update_profile_response");
            response.put("status", "failed");
            response.put("reason", "Database Error");
            client.sendMessage(response);
        } finally {
            if (dao != null) try {
                dao.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void deleteAccount(ClientHandler client) {
        DAO dao = null;
        try {
            dao = new DAO();
            dao.deletePlayer(client.getUsername());

            JSONObject response = new JSONObject();
            response.put("type", "delete_account_response");
            response.put("status", "success");
            client.sendMessage(response);

            // Cleanup
            Server.onlinePlayers.remove(client.getUsername());
        } catch (SQLException ex) {
            ex.printStackTrace();
            JSONObject response = new JSONObject();
            response.put("type", "delete_account_response");
            response.put("status", "failed");
            client.sendMessage(response);
        } finally {
            if (dao != null) try {
                dao.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void logout(ClientHandler client) {
        if (client.getUsername() != null) {
            Server.onlinePlayers.remove(client.getUsername());
            System.out.println(client.getUsername() + " logged out.");
        }
    }

    public static void getLeaderboard(ClientHandler client) {
        DAO dao = null;
        try {
            dao = new DAO();
            java.util.List<PlayerModel> players = dao.getTopPlayers();

            JSONArray list = new JSONArray();
            for (PlayerModel p : players) {
                JSONObject playerJson = new JSONObject();
                playerJson.put("username", p.getUsername());
                playerJson.put("score", p.getPoints());
                list.put(playerJson);
            }

            JSONObject response = new JSONObject();
            response.put("type", "leaderboard_response");
            response.put("status", "success");
            response.put("leaderboard", list);

            client.sendMessage(response);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (dao != null) try {
                dao.close();
            } catch (SQLException e) {
            }
        }
    }
    
        public static void handleEndSession(ClientHandler client) {
    
        System.out.println("[Server] Handling end_session for: " + client.getUsername());
    
        
    
        GameSession session = GameSessionManager.getSession(client);
    
        if (session != null) {
    
            // Just cleanup, no penalties since game already ended
    
            session.cleanupSession();
    
        }
    
        
    
        // Set player back to ONLINE
    
        client.setStatus(PlayerStatus.ONLINE);
    
    }
    
    
    
        public static void handleStatusUpdate(ClientHandler client, JSONObject request) {
    
            String statusString = request.getString("status");
    
            try {
    
                            PlayerStatus newStatus = PlayerStatus.valueOf(statusString.toUpperCase());
    
                            client.setStatus(newStatus);
    
                            System.out.println("Player " + client.getUsername() + " status updated to: " + newStatus.name());
    
                
    
                            // Update status in the database
    
                            DAO dao = null;
    
                            try {
    
                                dao = new DAO();
    
                                dao.updatePlayerStatus(client.getUsername(), newStatus.ordinal());
    
                            } catch (SQLException e) {
    
                                System.err.println("Error updating player status in DB for " + client.getUsername() + ": " + e.getMessage());
    
                            } finally {
    
                                if (dao != null) {
    
                                    try {
    
                                        dao.close();
    
                                    } catch (SQLException e) {
    
                                        e.printStackTrace();
    
                                    }
    
                                }
    
                            }
    
                
    
                            // Notify all online players about the status change
    
                Server.onlinePlayers.values().forEach(handler -> {
    
                    sendAvailablePlayers(handler); // Resend the updated list of available players
    
                });
    
    
    
            } catch (IllegalArgumentException e) {
    
                System.err.println("Invalid PlayerStatus received: " + statusString);
    
            }
    
        }
    
    }
