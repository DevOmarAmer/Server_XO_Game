package com.mycompany.server_xo_game;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;

public class PrimaryController implements Initializable {

    @FXML
    private ToggleButton toggleBtn_id;
    
    @FXML
    private Button show_chart_id;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // اللون الافتراضي عند التشغيل
        toggleBtn_id.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    }    

    @FXML
    private void handleToggle() {
        if (toggleBtn_id.isSelected()) {
            toggleBtn_id.setText("Server Off");
            toggleBtn_id.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            System.out.println("Server is OFF");
        } else {
            toggleBtn_id.setText("Server On");
            toggleBtn_id.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            System.out.println("Server is ON");
        }
    }

    @FXML
    private void showChart() {
        System.out.println("Show Chart Clicked");
    }
}
