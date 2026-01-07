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

    public static final String INSERT
            = "INSERT INTO Player (username, email, password_hash, points,Status) VALUES (?, ?, ?, ?,?)";

    public static final String UPDATE
            = "UPDATE Player SET username=?, email=?, password_hash=?, points=? ,Status=? WHERE player_id=?";

    public static final String DELETE
            = "DELETE FROM Player WHERE player_id=?";

    public static final String DISPLAY_ALL
            = "SELECT * FROM Player";

    public DAO() throws SQLException {
        con = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/TEAM1",
                "Team1",
                "team1"
        );
        System.out.println("Successfully Connected...");
    }

    public boolean register(PlayerModel p) throws SQLException {
        PreparedStatement ps = con.prepareStatement(INSERT);
        ps.setString(1, p.getUsername());
        ps.setString(2, p.getEmail());
        ps.setString(3, p.getPasswordHash());
        ps.setInt(4, p.getPoints());
        ps.setInt(5, p.getStatus());
        int rows = ps.executeUpdate();
        return rows > 0;
    }

    public void update(PlayerModel p, int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(UPDATE);
        ps.setString(1, p.getUsername());
        ps.setString(2, p.getEmail());
        ps.setString(3, p.getPasswordHash());
        ps.setInt(4, p.getPoints());
        ps.setInt(5, p.getStatus());
        ps.setInt(6, id);
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

    // Static helper methods for server use
    public static boolean login(String username, String plainPassword) {
        String sql = "SELECT password_hash FROM Player WHERE username = ?";
        try (Connection c = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/TEAM1",
                "Team1",
                "team1"); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("password_hash");
                    return stored != null && stored.equals(PlayerModel.hashPassword(plainPassword));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // public static boolean register(String username, String email, String plainPassword) {
    //     String sql = "INSERT INTO Player (username, email, password_hash, points,Status) VALUES (?, ?, ?, 0, 0)";
    //     try (Connection c = DriverManager.getConnection(
    //             "jdbc:derby://localhost:1527/TEAM1",
    //             "Team1",
    //             "team1");
    //          PreparedStatement ps = c.prepareStatement(sql)) {
    //         ps.setString(1, username);
    //         ps.setString(2, email);
    //         ps.setString(3, PlayerModel.hashPassword(plainPassword));
    //         return ps.executeUpdate() == 1;
    //     } catch (SQLException ex) {
    //         ex.printStackTrace();
    //         return false;
    //     }
    // }
    public static void updateScore(String username, int delta) {
        String sql = "UPDATE Player SET points = points + ? WHERE username = ?";
        try (Connection c = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/TEAM1",
                "Team1",
                "team1"); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * You need to create the GAME_RECORDS table in your database for this to
     * work. SQL to create table: CREATE TABLE GAME_RECORDS ( record_id INT
     * PRIMARY KEY GENERATED ALWAYS AS IDENTITY, game_data CLOB, played_at
     * TIMESTAMP DEFAULT CURRENT_TIMESTAMP );
     */
    public static void saveGame(String gameData) {
        String sql = "INSERT INTO GAME_RECORDS (game_data) VALUES (?)";
        try (Connection c = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/TEAM1",
                "Team1",
                "team1"); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, gameData);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void close() throws SQLException {
        if (con != null && !con.isClosed()) {
            con.close();
        }
    }

    public PlayerModel getPlayer(String username) {
        String sql = "SELECT * FROM Player WHERE username = ?";
        try (Connection c = DriverManager.getConnection("jdbc:derby://localhost:1527/TEAM1", "Team1", "team1"); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Assuming status is stored as int; adjust if needed based on your DB schema
                    return new PlayerModel(
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getInt("points"),
                            rs.getInt("Status")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

// 2. Update Player Data (Email/Password)
    public boolean updatePlayerInfo(String currentUsername, String newUsername, String newEmail, String newPassword) {
        // 1. Define SQL: We update username, email, and optionally password
        String sql;
        boolean isPasswordChanged = (newPassword != null && !newPassword.isEmpty());

        if (isPasswordChanged) {
            sql = "UPDATE Player SET username = ?, email = ?, password_hash = ? WHERE username = ?";
        } else {
            sql = "UPDATE Player SET username = ?, email = ? WHERE username = ?";
        }

        try (Connection c = DriverManager.getConnection("jdbc:derby://localhost:1527/TEAM1", "Team1", "team1"); PreparedStatement ps = c.prepareStatement(sql)) {

            // 2. Set Parameters
            ps.setString(1, newUsername);
            ps.setString(2, newEmail);

            if (isPasswordChanged) {
                ps.setString(3, PlayerModel.hashPassword(newPassword)); // Hash the new password
                ps.setString(4, currentUsername); // WHERE username = current
            } else {
                ps.setString(3, currentUsername); // WHERE username = current
            }

            // 3. Execute
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

// 3. Delete Player
    public void deletePlayer(String username) {
        String sql = "DELETE FROM Player WHERE username = ?";
        try (Connection c = DriverManager.getConnection("jdbc:derby://localhost:1527/TEAM1", "Team1", "team1"); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
        public static int getTotalPlayerCount() {
        String sql = "SELECT COUNT(*) as total FROM Player";
        try (Connection c = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/TEAM1",
                "Team1",
                "team1"); 
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
    public static int getPlayerCountByStatus(int status) {
        String sql = "SELECT COUNT(*) as total FROM Player WHERE Status = ?";
        try (Connection c = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/TEAM1",
                "Team1",
                "team1"); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setInt(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}
