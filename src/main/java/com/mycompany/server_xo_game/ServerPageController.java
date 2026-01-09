package com.mycompany.server_xo_game;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.animation.AnimationTimer;

public class ServerPageController implements Initializable {
    @FXML
    private ImageView bgImg;
    @FXML
    private VBox wholeVbox;
    @FXML
    private Label gameTitle;
    @FXML
    private Button startButton;
    @FXML
    private Circle statusIndicator;
    @FXML
    private Label statusLabel;
    @FXML
    private PieChart pieChart;
    @FXML
    private VBox onlineVbox;
    @FXML
    private Label onlineNum;
    @FXML
    private VBox inGameVbox;
    @FXML
    private Label inGameNum;
    @FXML
    private VBox offlineVbox;
    @FXML
    private Label offlineNum;
    @FXML
    private Label serverIpLabel;
    @FXML
    private Label serverIpValue;
    
    private boolean serverRunning = false;
    private Thread serverThread;
    private AnimationTimer statsUpdater;
    
    // PieChart data
    private PieChart.Data onlineData;
    private PieChart.Data inGameData;
    private PieChart.Data offlineData;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupPieChart();
        setupStatsUpdater();
        updateStats();
    }
    
    private void setupPieChart() {
        // Initialize pie chart data
        onlineData = new PieChart.Data("Online", 0);
        inGameData = new PieChart.Data("In Game", 0);
        offlineData = new PieChart.Data("Offline", 0);
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            onlineData,
            inGameData,
            offlineData
        );
        
        pieChart.setData(pieChartData);
        pieChart.setLegendVisible(false);
        pieChart.setLabelsVisible(true);
        pieChart.setStartAngle(90);
        pieChart.setAnimated(true);
        
        // Style the chart segments
        Platform.runLater(() -> {
            onlineData.getNode().setStyle("-fx-pie-color: #00d2ff;");
            inGameData.getNode().setStyle("-fx-pie-color: #ff007f;");
            offlineData.getNode().setStyle("-fx-pie-color: #8a2be2;");
        });
    }
    
    private void setupStatsUpdater() {
        // Update stats every second when server is running
        statsUpdater = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000) { // 1 second
                    updateStats();
                    lastUpdate = now;
                }
            }
        };
    }
    
    private void updateStats() {
        Platform.runLater(() -> {
            if (serverRunning) {
                // Count players by status
                int onlineCount = 0;
                int inGameCount = 0;
                int totalPlayers = 0;
                
                // Count online and in-game players
                if (Server.onlinePlayers != null) {
                    for (ClientHandler client : Server.onlinePlayers.values()) {
                        if (client.getStatus() == PlayerStatus.ONLINE) {
                            onlineCount++;
                        } else if (client.getStatus() == PlayerStatus.IN_GAME) {
                            inGameCount++;
                        }
                    }
                    totalPlayers = Server.onlinePlayers.size();
                }
                
                // Get total registered players from database
                int totalRegistered = getTotalPlayersFromDB();
                int offlineCount = totalRegistered - totalPlayers;
                
                // Update labels
                onlineNum.setText(String.valueOf(onlineCount));
                inGameNum.setText(String.valueOf(inGameCount));
                offlineNum.setText(String.valueOf(offlineCount));
                
                // Update pie chart
                onlineData.setPieValue(onlineCount > 0 ? onlineCount : 0.1);
                inGameData.setPieValue(inGameCount > 0 ? inGameCount : 0.1);
                offlineData.setPieValue(offlineCount > 0 ? offlineCount : 0.1);
                
                // Update status indicator
                statusIndicator.setFill(javafx.scene.paint.Color.web("#00ff88"));
                statusIndicator.setStyle("-fx-effect: dropshadow(gaussian, rgba(0, 255, 136, 0.9), 15, 0.7, 0, 0);");
                statusLabel.setText("Server Online");
                statusLabel.setStyle("-fx-text-fill: #00ff88;");
            } else {
                // Server stopped - show zeros
                onlineNum.setText("0");
                inGameNum.setText("0");
                
                // Show total registered players as offline
                int totalRegistered = getTotalPlayersFromDB();
                offlineNum.setText(String.valueOf(totalRegistered));
                
                onlineData.setPieValue(0.1);
                inGameData.setPieValue(0.1);
                offlineData.setPieValue(totalRegistered > 0 ? totalRegistered : 0.1);
                
                // Update status indicator
                statusIndicator.setFill(javafx.scene.paint.Color.web("#F44336"));
                statusIndicator.setStyle("-fx-effect: dropshadow(gaussian, rgba(244, 67, 54, 0.9), 15, 0.7, 0, 0);");
                statusLabel.setText("Server Offline");
                statusLabel.setStyle("-fx-text-fill: #F44336;");
            }
        });
    }
    
    private int getTotalPlayersFromDB() {
        try {
            return DAO.getTotalPlayerCount();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    @FXML
    private void onClickstartButton(ActionEvent event) {
        if (!serverRunning) {
            startServer();
        } else {
            stopServer();
        }
    }
    
    private void startServer() {
        serverThread = new Thread(() -> {
            try {
                Server.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    startButton.setText("Start Server");
                    startButton.getStyleClass().remove("server-running");
                    serverRunning = false;
                });
            }
        });
        
        serverThread.setDaemon(true);
        serverThread.start();
        
        serverRunning = true;
        startButton.setText("Stop Server");
        startButton.getStyleClass().add("server-running");
        
        // Start updating stats
        statsUpdater.start();
        
        System.out.println("Server started successfully");
        
        // Display Server IP
        String ipAddress = Server.getServerIP();
        Platform.runLater(() -> {
            serverIpValue.setText(ipAddress);
        });
    }
    
    private void stopServer() {
        try {
            // Close all client connections
            if (Server.onlinePlayers != null) {
                for (ClientHandler client : Server.onlinePlayers.values()) {
                    try {
                        client.sendMessage(new org.json.JSONObject()
                            .put("type", "server_shutdown")
                            .put("message", "Server is shutting down"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Server.onlinePlayers.clear();
            }
            
            // Stop the server socket
            if (Server.serverSocket != null && !Server.serverSocket.isClosed()) {
                Server.serverSocket.close();
            }
            
            // Stop stats updater
            statsUpdater.stop();
            
            serverRunning = false;
            startButton.setText("Start Server");
            startButton.getStyleClass().remove("server-running");
            
            // Clear Server IP
            Platform.runLater(() -> {
                serverIpValue.setText("");
            });
            
            // Update stats one last time to show all offline
            updateStats();
            
            System.out.println("Server stopped successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void cleanup() {
        if (statsUpdater != null) {
            statsUpdater.stop();
        }
        if (serverRunning) {
            stopServer();
        }
    }
}