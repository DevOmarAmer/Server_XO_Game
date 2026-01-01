/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server_xo_game;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author dell
 */
public class DAO {

    private Connection con;

    public static final String INSERT =
            "INSERT INTO Player (username, email, password_hash, points,Status) VALUES (?, ?, ?, ?,?)";

    public static final String UPDATE =
            "UPDATE Player SET username=?, email=?, password_hash=?, points=? ,Status=? WHERE player_id=?";

    public static final String DELETE =
            "DELETE FROM Player WHERE player_id=?";

    public static final String DISPLAY_ALL =
            "SELECT * FROM Player";

    public DAO() throws SQLException {
        con = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/TEAM1",
                "Team1",
                "team1"
        );
        System.out.println("Successfully Connected...");
    }

    public void insert(PlayerModel p) throws SQLException {
        PreparedStatement ps = con.prepareStatement(INSERT);
        ps.setString(1, p.getUsername());
        ps.setString(2, p.getEmail());
        ps.setString(3, p.getPasswordHash());
        ps.setInt(4, p.getPoints());
        ps.setInt(5, p.getStatus());
        ps.executeUpdate();
    }

    public void update(PlayerModel p,int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(UPDATE);
        ps.setString(1, p.getUsername());
        ps.setString(2, p.getEmail());
        ps.setString(3, p.getPasswordHash());
        ps.setInt(4, p.getPoints());
        ps.setInt(5, p.getStatus());
        ps.setInt(6,id);
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DELETE);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public ResultSet displayAll() throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                DISPLAY_ALL,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
        );
        return ps.executeQuery();
    }
    
}
