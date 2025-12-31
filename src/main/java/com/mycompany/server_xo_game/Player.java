/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server_xo_game;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author MahmoudTarek
 */
public class Player{
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private int points;
    private int status;
    
    public Player(String username, String email, String plainPassword, int points, int status) {
    this.username = username;
    this.email = email;
    this.passwordHash = hashPassword(plainPassword);
    this.points = points;
    this.status = status;
    }

    public int getId() {
        return id;
    }
    public String getIdStr() {
        return id+"";
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
        this.passwordHash = hashPassword(passwordHash);
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
    
    //Hashing Method
    public static String hashPassword(String password) {
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
