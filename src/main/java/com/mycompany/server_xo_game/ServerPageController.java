/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.server_xo_game;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
/**
 * FXML Controller class
 *
 * @author dell
 */
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
    private HBox statsHbox;
    @FXML
    private VBox onlineVbox;
    @FXML
    private Label onlineTitle;
    @FXML
    private Label onlineNum;
    @FXML
    private PieChart pieChart;
    @FXML
    private VBox offlineVbox;
    @FXML
    private Label offlineTitle;
    @FXML
    private Label offlineNum;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    @FXML
    private void onClickstartButton(ActionEvent event) {
    }

}
