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
    
    
    
  
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
   
    }

    
    @FXML
    private void onClickstartButton(ActionEvent event) {
   
    }
    

}