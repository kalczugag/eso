package com.example.elektroniczny_dziennik;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {
    @FXML private Label loggedLabel;
    @FXML private Pane sidebarContainer;
    @FXML private Pane mainContainer;

    @FXML private AnchorPane sidebarRoot;

    @FXML private Button dashboardButton;
    @FXML private Button gradesButton;
    @FXML private Button attendanceButton;
    @FXML private Button gradeEntryButton;
    @FXML private Button userManagementButton;

    private Parent root;
    private Scene scene;
    private Stage stage;

    public User user;
    private Map<String, Button> navigationButtons;
    private Button currentActiveButton = null;

    public void displayUser(User user) throws IOException {
        this.user = user;
        loggedLabel.setText("Zalogowano jako: " + this.user.getFirstName() + " " + this.user.getLastName() + " (" + this.user.getRole() + ")");

        loadSidebar();
        loadInitialView();
    }

    private void loadSidebar() throws IOException {
        String sidebarFxml;
        if (this.user.getRole().equals("admin")) {
            sidebarFxml = "adminSidebar.fxml";
        } else if (this.user.getRole().equals("nauczyciel")) {
            sidebarFxml = "teacherSidebar.fxml";
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
            navigationButtons.put("teacherDashboard.fxml", dashboardButton);

            try {
                dashboardButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/dashboard.png"))));
            } catch (Exception e) { System.out.println("Brak ikony dashboard.png"); }
        }
        if (gradesButton != null) {
            navigationButtons.put("grades.fxml", gradesButton);

            gradesButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/grades.png"))));
        }
        if (attendanceButton != null) {
            navigationButtons.put("attendance.fxml", attendanceButton);
        }
        if (gradeEntryButton != null) {
            navigationButtons.put("gradeEntry.fxml", gradeEntryButton);

            gradeEntryButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/grades.png"))));
        }
        if (userManagementButton != null) {
            navigationButtons.put("userManagement.fxml", userManagementButton);

            userManagementButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/management.png"))));
        }
    }

    private void loadInitialView() throws IOException {
        var role = user.getRole();

        if (role.equals("admin")) {
            loadView("adminDashboard.fxml");
        } else if(role.equals("student")) {
            loadView("studentDashboard.fxml");
        } else {
            loadView("teacherDashboard.fxml");
        }
    }

    public void loadView(String fxml) throws IOException {
        //Parent loadedView = FXMLLoader.load(getClass().getResource(fxml));
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent loadedView = loader.load();
        mainContainer.getChildren().clear();
        mainContainer.getChildren().add(loadedView);

        if (fxml.equals("studentDashboard.fxml")) {
            StudentDashboardController controller = loader.getController();
            controller.setUser(this.user);
        }

        if (fxml.equals("teacherDashboard.fxml")) {
            TeacherDashboardController controller = loader.getController();
            controller.setUser(this.user);
            controller.setMainController(this);
        }

        if(fxml.equals("gradeEntry.fxml")){
            GradeEntryController controller = loader.getController();
            controller.setUser(user);
        }

        if (loadedView instanceof Pane) {
            Pane viewPane = (Pane) loadedView;
            viewPane.prefWidthProperty().bind(mainContainer.widthProperty());
            viewPane.prefHeightProperty().bind(mainContainer.heightProperty());
        }
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

    public void showGradeEntry() throws IOException {
        loadView("gradeEntry.fxml");
    }

    public void showUserManagement() throws IOException {
        loadView("userManagement.fxml");
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