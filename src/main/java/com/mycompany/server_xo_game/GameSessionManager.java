/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server_xo_game;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Omar
 */
public class GameSessionManager {

    private static ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    // Add session for both players
    public static void addSession(ClientHandler p1, ClientHandler p2, GameSession session) {
        sessions.put(p1.getUsername(), session);
        sessions.put(p2.getUsername(), session);
    }

    // Retrieve session by player
    public static GameSession getSession(ClientHandler player) {
        return sessions.get(player.getUsername());
    }

    // Remove session by specific player (This is the method your GameSession code needs)
    public static void removeSession(ClientHandler player) {
        if (player != null && player.getUsername() != null) {
            sessions.remove(player.getUsername());
        }
    }

    // Remove session by value (Keep this if you use it elsewhere, otherwise it's optional)
    public static void removeSession(GameSession session) {
        sessions.values().removeIf(s -> s == session);
    }
}
