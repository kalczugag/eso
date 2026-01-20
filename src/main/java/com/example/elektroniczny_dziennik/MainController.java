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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Główny kontroler aplikacji zarządzający nawigacją i układem okna.
 * Odpowiada za ładowanie odpowiedniego paska bocznego (sidebar) w zależności od roli użytkownika
 * oraz dynamiczne podmienianie zawartości głównego kontenera (dashboard, oceny, etc.).
 */
public class MainController {

    /** Etykieta wyświetlająca informację o zalogowanym użytkowniku. */
    @FXML private Label loggedLabel;

    /** Kontener na pasek boczny (menu). */
    @FXML private Pane sidebarContainer;

    /** Główny kontener, w którym wyświetlane są poszczególne widoki (np. tabela ocen). */
    @FXML private Pane mainContainer;

    /** Główny węzeł paska bocznego. */
    @FXML private AnchorPane sidebarRoot;

    // --- Przyciski nawigacyjne ---

    /** Przycisk nawigacji do pulpitu głównego. */
    @FXML private Button dashboardButton;

    /** Przycisk nawigacji do widoku ocen (dla studenta). */
    @FXML private Button gradesButton;

    /** Przycisk nawigacji do wprowadzania ocen (dla nauczyciela). */
    @FXML private Button gradeEntryButton;

    /** Przycisk nawigacji do zarządzania użytkownikami (dla admina). */
    @FXML private Button userManagementButton;

    /** Przycisk nawigacji do zarządzania przedmiotami (dla admina). */
    @FXML private Button subjectManagementButton;

    // --- Zmienne sceny ---
    private Parent root;
    private Scene scene;
    private Stage stage;

    /** Obiekt aktualnie zalogowanego użytkownika. */
    public User user;

    /** Mapa mapująca nazwy plików FXML na przyciski nawigacyjne, co pozwala na obsługę stanu "aktywny". */
    private Map<String, Button> navigationButtons;

    /** Przycisk, który jest aktualnie aktywny (podświetlony w menu). */
    private Button currentActiveButton = null;

    /**
     * Ustawia zalogowanego użytkownika i inicjalizuje interfejs.
     * Wyświetla dane użytkownika na pasku, ładuje odpowiednie menu boczne
     * i otwiera domyślny widok startowy (dashboard).
     *
     * @param user Obiekt zalogowanego użytkownika.
     * @throws IOException W przypadku błędu ładowania plików FXML.
     */
    public void displayUser(User user) throws IOException {
        this.user = user;
        loggedLabel.setText("Zalogowano jako: " + this.user.getFirstName() + " " + this.user.getLastName() + " (" + this.user.getRole() + ")");

        loadSidebar();
        loadInitialView();
    }

    /**
     * Ładuje pasek boczny (Sidebar) odpowiedni dla roli użytkownika.
     * Nauczyciel, uczeń i administrator mają osobne pliki FXML menu.
     *
     * @throws IOException Gdy nie uda się załadować pliku FXML paska bocznego.
     */
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
        loader.setController(this); // Ten kontroler obsługuje zdarzenia z paska bocznego
        Parent sidebarView = loader.load();

        sidebarContainer.getChildren().clear();
        sidebarContainer.getChildren().add(sidebarView);

        initializeButtonMap();
    }

    /**
     * Inicjalizuje mapę przycisków i ustawia ikony.
     * Powiązuje nazwy widoków (np. "grades.fxml") z obiektami przycisków,
     * co umożliwia sterowanie ich podświetleniem.
     */
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
        if (gradeEntryButton != null) {
            navigationButtons.put("gradeEntry.fxml", gradeEntryButton);
            gradeEntryButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/grades.png"))));
        }
        if (userManagementButton != null) {
            navigationButtons.put("userManagement.fxml", userManagementButton);
            userManagementButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/management.png"))));
        }
        if(userManagementButton != null){
            navigationButtons.put("subjectManagement.fxml", subjectManagementButton);
        }
    }

    /**
     * Ładuje widok startowy (Dashboard) w zależności od roli użytkownika.
     * @throws IOException Gdy wystąpi błąd ładowania pliku FXML.
     */
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

    /**
     * Główna metoda ładująca widok do centralnego kontenera aplikacji.
     * Przekazuje również obiekt użytkownika do kontrolerów załadowanych widoków.
     *
     * @param fxml Nazwa pliku FXML do załadowania.
     * @throws IOException W przypadku braku pliku lub błędu parsowania FXML.
     */
    public void loadView(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent loadedView = loader.load();
        mainContainer.getChildren().clear();
        mainContainer.getChildren().add(loadedView);

        // Przekazywanie danych do specyficznych kontrolerów
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

        // Skalowanie widoku do rozmiarów kontenera
        if (loadedView instanceof Pane) {
            Pane viewPane = (Pane) loadedView;
            viewPane.prefWidthProperty().bind(mainContainer.widthProperty());
            viewPane.prefHeightProperty().bind(mainContainer.heightProperty());
        }
        setActiveButton(fxml);
    }

    /**
     * Ustawia styl CSS "active" dla przycisku odpowiadającego aktualnemu widokowi.
     * Usuwa styl z poprzedniego przycisku.
     *
     * @param viewName Nazwa widoku, na podstawie której identyfikowany jest przycisk.
     */
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

    // --- Metody obsługi zdarzeń (Event Handlers) ---

    /** Wyświetla pulpit główny. */
    public void showDashboard() throws IOException {
        loadInitialView();
    }

    /** Wyświetla widok ocen ucznia. */
    public void showGrades() throws IOException {
        loadView("grades.fxml");
    }

    /** Wyświetla widok wprowadzania ocen (dla nauczyciela). */
    public void showGradeEntry() throws IOException {
        loadView("gradeEntry.fxml");
    }

    /** Wyświetla panel zarządzania użytkownikami. */
    public void showUserManagement() throws IOException {
        loadView("userManagement.fxml");
    }

    /** Wyświetla panel zarządzania przedmiotami. */
    public void showSubjectManagement() throws IOException {
        loadView("subjectManagement.fxml");
    }

    /**
     * Wylogowuje użytkownika.
     * Wyświetla okno potwierdzenia, a po zatwierdzeniu przenosi do ekranu logowania.
     *
     * @param e Zdarzenie kliknięcia przycisku wylogowania.
     * @throws IOException Gdy nie uda się załadować widoku logowania.
     */
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