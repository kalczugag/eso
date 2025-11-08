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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private Label loggedLabel;
    @FXML private Pane sidebarContainer;
    @FXML private Pane mainContainer;

    @FXML private Button dashboardButton;
    @FXML private Button gradesButton;
    @FXML private Button attendanceButton;

    private Parent root;
    private Scene scene;
    private Stage stage;

    public User user;
    private Map<String, Button> navigationButtons;
    private Button currentActiveButton = null;

    public void displayUser(User user) throws IOException {
        this.user = user;
        loggedLabel.setText("Zalogowano jako: " + this.user.getUsername() + " (" + this.user.getRole() + ")");

        loadSidebar();
        loadInitialView();
    }

    private void loadSidebar() throws IOException {
        String sidebarFxml;
        if (this.user.getRole().equals("admin")) {
            sidebarFxml = "adminSidebar.fxml";
        } else {
            sidebarFxml = "studentSidebar.fxml";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(sidebarFxml));
        loader.setController(this);
        Parent sidebarView = loader.load();

        sidebarContainer.getChildren().clear();
        sidebarContainer.getChildren().add(sidebarView);

        initializeButtonMap();
    }

    private void initializeButtonMap() {
        navigationButtons = new HashMap<>();

        if (dashboardButton != null) {
            navigationButtons.put("adminDashboard.fxml", dashboardButton);
            navigationButtons.put("studentDashboard.fxml", dashboardButton);
        }
        if (gradesButton != null) {
            navigationButtons.put("grades.fxml", gradesButton);
        }
        if (attendanceButton != null) {
            navigationButtons.put("attendance.fxml", attendanceButton);
        }
    }

    private void loadInitialView() throws IOException {
        if (this.user.getRole().equals("admin")) {
            loadView("adminDashboard.fxml");
        } else {
            loadView("studentDashboard.fxml");
        }
    }

    public void loadView(String fxml) throws IOException {
        StackPane loadedView = FXMLLoader.load(getClass().getResource(fxml));
        mainContainer.getChildren().clear();
        mainContainer.getChildren().add(loadedView);

        loadedView.prefWidthProperty().bind(mainContainer.widthProperty());
        loadedView.prefHeightProperty().bind(mainContainer.heightProperty());

        setActiveButton(fxml);
    }

    private void setActiveButton(String viewName) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active");
        }

        Button activeButton = navigationButtons.get(viewName);
        if (activeButton != null) {
            activeButton.getStyleClass().add("active");
            currentActiveButton = activeButton;
        } else {
            currentActiveButton = null;
        }
    }

    public void showDashboard() throws IOException {
        loadInitialView();
    }

    public void showGrades() throws IOException {
        loadView("grades.fxml");
    }

    public void showAttendance() throws IOException {
        loadView("attendance.fxml");
    }

    public void logout(ActionEvent e) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Wylogowywanie");
        alert.setContentText("Czy na pewno chcesz sie wylogowac?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            root = FXMLLoader.load(getClass().getResource("loginView.fxml"));
            scene = new Scene(root);

            stage = (Stage)((Node) e.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        }
    }
}