package com.mycompany.server_xo_game;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PlayerModel {

    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private int points;
    private int status;
    private int wins;
    private int draws;
    private int losses;

    // Constructor for creating a new player (hashes the password)
    public PlayerModel(String username, String email, String plainPassword, int points, int status) {
        this.username = username;
        this.email = email;
        this.passwordHash = hashPassword(plainPassword);
        this.points = points;
        this.status = status;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
    }

    // Constructor for retrieving a player from DB (assumes password is already hashed)
    public PlayerModel(String username, String email, String hashedPassword, int points, int status, int wins, int draws, int losses) {
        this.username = username;
        this.email = email;
        this.passwordHash = hashedPassword;
        this.points = points;
        this.status = status;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
    }


    public int getId() {
        return id;
    }

    public String getIdStr() {
        return id + "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    //Hashing Method
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
