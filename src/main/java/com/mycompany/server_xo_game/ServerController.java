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
         DAO dao;
        try {
            dao = new DAO();
       
        boolean success = dao.register(new PlayerModel(username, email, password, 0, 0));
        JSONObject response = new JSONObject();
        response.put("status", success ? "success" : "failed");
        client.sendMessage(response);
         } catch (SQLException ex) {
            System.getLogger(ServerController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
        public static void sendAvailablePlayers(ClientHandler client) {
        JSONArray players = new JSONArray();
        for (String name : Server.onlinePlayers.keySet()) {
            if (!name.equals(client.getUsername())) {
                players.put(name);
            }
        }
        JSONObject response = new JSONObject();
        response.put("type", "available_players");
        response.put("players", players);
        client.sendMessage(response);
    }

    
}
