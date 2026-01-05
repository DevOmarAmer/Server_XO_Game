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

    public static void addSession(ClientHandler p1, ClientHandler p2, GameSession session) {
        sessions.put(p1.getUsername(), session);
        sessions.put(p2.getUsername(), session);
    }

    public static GameSession getSession(ClientHandler player) {
        return sessions.get(player.getUsername());
    }

    public static void removeSession(GameSession session) {
        sessions.values().removeIf(s -> s == session);
    }
}
