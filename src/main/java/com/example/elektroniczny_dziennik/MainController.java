package com.example.elektroniczny_dziennik;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    // header FXML elements
    @FXML private Label loggedLabel;

    // left container FXML elements
    @FXML private Button adminSettingsBtn;

    // Containers
    @FXML Pane mainContainer;


    private Parent view;
    private Stage stage;

    public User user;

    public void displayUser(User user) throws IOException{
        this.user = user;
        loggedLabel.setText("Zalogowano jako: " + this.user.getUsername() + " (" + this.user.getRole() + ")");
        loadView("grades.fxml");

        adminSettingsBtn.setVisible(this.user.getRole().equals("admin"));
    }

    public void showGrades() throws IOException{
        loadView("grades.fxml");
    }

    public void showAttendance() throws IOException{
        loadView("attendance.fxml");
    }

    public void logout(ActionEvent e){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Wylogowywanie");
        alert.setContentText("Czy na pewno chcesz sie wylogowac?");

        if(alert.showAndWait().get() == ButtonType.OK){
            stage = (Stage)((Node)e.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    public void loadView(String fxml) throws IOException {
        view = FXMLLoader.load(getClass().getResource(fxml));
        mainContainer.getChildren().clear();
        mainContainer.getChildren().add(view);
    }
}
