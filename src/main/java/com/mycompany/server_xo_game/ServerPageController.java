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
    
    private PieChart.Data onlineData;
    private PieChart.Data inGameData;
    private PieChart.Data offlineData;
    private AnimationTimer statsUpdater;
      private void setupPieChart() {
       
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
        
        Platform.runLater(() -> {
            onlineData.getNode().setStyle("-fx-pie-color: #00d2ff;");
            inGameData.getNode().setStyle("-fx-pie-color: #ff007f;");
            offlineData.getNode().setStyle("-fx-pie-color: #8a2be2;");
        });
    }
      
          private void setupStatsUpdater() {
       
        statsUpdater = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000) { 

                    lastUpdate = now;
                }
            }
        };
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
   
    }

    
    @FXML
    private void onClickstartButton(ActionEvent event) {
   
    }
    

}