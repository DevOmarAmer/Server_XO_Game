package com.mycompany.server_xo_game;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DAO {

    private Connection con;

    // Updated to include new stats columns
    public static final String INSERT = "INSERT INTO Player (username, email, password_hash, points, Status, wins, losses, draws) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "UPDATE Player SET username=?, email=?, password_hash=?, points=?, Status=?, wins=?, losses=?, draws=? WHERE username=?";
    public static final String DELETE = "DELETE FROM Player WHERE username=?";
    public static final String DISPLAY_ALL = "SELECT * FROM Player";

    public DAO() throws SQLException {
        con = DriverManager.getConnection("jdbc:derby://localhost:1527/TEAM1", "Team1", "team1");
        System.out.println("Successfully Connected...");
        // Attempt to update the table schema when the DAO is initialized
        updateTableSchema();
    }

    private void updateTableSchema() {
        try (Statement stmt = con.createStatement()) {
            // Use a separate try-catch for each ALTER statement
            try {
                stmt.execute("ALTER TABLE Player ADD wins INT DEFAULT 0");
                System.out.println("Column 'wins' added to Player table.");
            } catch (SQLException e) {
                if (e.getSQLState().equals("42X01")) { // Column already exists
                    // This is expected, do nothing
                } else {
                    throw e;
                }
            }
            try {
                stmt.execute("ALTER TABLE Player ADD losses INT DEFAULT 0");
                System.out.println("Column 'losses' added to Player table.");
            } catch (SQLException e) {
                if (e.getSQLState().equals("42X01")) { // Column already exists
                    // This is expected, do nothing
                } else {
                    throw e;
                }
            }
            try {
                stmt.execute("ALTER TABLE Player ADD draws INT DEFAULT 0");
                System.out.println("Column 'draws' added to Player table.");
            } catch (SQLException e) {
                if (e.getSQLState().equals("42X01")) { // Column already exists
                    // This is expected, do nothing
                } else {
                    throw e;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating table schema: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public boolean register(PlayerModel p) throws SQLException {
        PreparedStatement ps = con.prepareStatement(INSERT);
        ps.setString(1, p.getUsername());
        ps.setString(2, p.getEmail());
        ps.setString(3, p.getPasswordHash());
        ps.setInt(4, 0); // Initial points
        ps.setInt(5, 0); // Initial status (e.g., offline)
        ps.setInt(6, 0); // Initial wins
        ps.setInt(7, 0); // Initial losses
        ps.setInt(8, 0); // Initial draws
        int rows = ps.executeUpdate();
        return rows > 0;
    }

    public static boolean login(String username, String plainPassword) {
        String sql = "SELECT password_hash FROM Player WHERE username = ?";
        try (Connection c = DriverManager.getConnection("jdbc:derby://localhost:1527/TEAM1", "Team1", "team1");
             PreparedStatement ps = c.prepareStatement(sql)) {
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
        public static void updatePlayerStats(String username, String result) {
        String columnToUpdate;
        int pointsChange;

        switch (result.toUpperCase()) {
            case "WIN":
                columnToUpdate = "wins";
                pointsChange = 10; // Example: +10 points for a win
                break;
            case "LOSS":
                columnToUpdate = "losses";
                pointsChange = -5; // Example: -5 points for a loss
                break;
            case "DRAW":
                columnToUpdate = "draws";
                pointsChange = 2;  // Example: +2 points for a draw
                break;
            default:
                return; // Do nothing for unknown result
        }

        String sql = "UPDATE Player SET " + columnToUpdate + " = " + columnToUpdate + " + 1, points = points + ? WHERE username = ?";
        try (Connection c = DriverManager.getConnection("jdbc:derby://localhost:1527/TEAM1", "Team1", "team1");
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pointsChange);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public PlayerModel getPlayer(String username) {
        String sql = "SELECT * FROM Player WHERE username = ?";
        try (Connection c = DriverManager.getConnection("jdbc:derby://localhost:1527/TEAM1", "Team1", "team1");
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PlayerModel(
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getInt("points"),
                            rs.getInt("Status"),
                            rs.getInt("wins"),
                            rs.getInt("draws"),
                            rs.getInt("losses")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public boolean updatePlayerInfo(String currentUsername, String newUsername, String newEmail, String newPassword) {
        String sql;
        boolean isPasswordChanged = (newPassword != null && !newPassword.isEmpty());

        if (isPasswordChanged) {
            sql = "UPDATE Player SET username = ?, email = ?, password_hash = ? WHERE username = ?";
        } else {
            sql = "UPDATE Player SET username = ?, email = ? WHERE username = ?";
        }

        try (Connection c = DriverManager.getConnection("jdbc:derby://localhost:1527/TEAM1", "Team1", "team1");
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, newUsername);
            ps.setString(2, newEmail);

            if (isPasswordChanged) {
                ps.setString(3, PlayerModel.hashPassword(newPassword));
                ps.setString(4, currentUsername);
            } else {
                ps.setString(3, currentUsername);
            }

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void deletePlayer(String username) {
        String sql = "DELETE FROM Player WHERE username = ?";
        try (Connection c = DriverManager.getConnection("jdbc:derby://localhost:1527/TEAM1", "Team1", "team1");
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
        public java.util.List<PlayerModel> getTopPlayers() {
        java.util.List<PlayerModel> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM Player ORDER BY points DESC";

        try (Connection c = DriverManager.getConnection("jdbc:derby://localhost:1527/TEAM1", "Team1", "team1");
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new PlayerModel(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getInt("points"),
                        rs.getInt("Status"),
                        rs.getInt("wins"),
                        rs.getInt("draws"),
                        rs.getInt("losses")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
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

    public void close() throws SQLException {
        if (con != null && !con.isClosed()) {
            con.close();
        }
    }    
}
