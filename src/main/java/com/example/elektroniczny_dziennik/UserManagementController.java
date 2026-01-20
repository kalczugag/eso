package com.example.elektroniczny_dziennik;

import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Kontroler panelu zarządzania użytkownikami (tylko dla Administratora).
 * Umożliwia przeglądanie, wyszukiwanie, dodawanie, edycję i usuwanie użytkowników.
 */
public class UserManagementController {

    /** Pole tekstowe do wyszukiwania użytkowników po imieniu lub nazwisku. */
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Button addUserBtn;

    /** Tabela wyświetlająca listę użytkowników. */
    @FXML private TableView<ObservableList<String>> userTable;

    // Kolumny tabeli (dane przechowywane jako lista Stringów dla uproszczenia)
    @FXML private TableColumn<ObservableList<String>, String> idColumn;
    @FXML private TableColumn<ObservableList<String>, String> firstNameColumn;
    @FXML private TableColumn<ObservableList<String>, String> lastNameColumn;
    @FXML private TableColumn<ObservableList<String>, String> loginColumn;
    @FXML private TableColumn<ObservableList<String>, String> roleColumn;

    /** Kolumna z przyciskami akcji (Edytuj / Usuń). */
    @FXML private TableColumn<ObservableList<String>, Void> actionsColumn;

    /** Lista przechowująca dane użytkowników załadowane z bazy. */
    private ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

    /**
     * Inicjalizuje kontroler.
     * Konfiguruje kolumny tabeli, dodaje przyciski akcji do wierszy oraz ładuje dane z bazy.
     */
    @FXML
    public void initialize() {
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) searchUser();
        });

        // Konfiguracja kolumn dla danych typu List<String>
        idColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(0)));
        firstNameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(1)));
        lastNameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(2)));
        loginColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(3)));
        roleColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(4)));

        addActionsToTable();

        try {
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Dodaje dynamiczne przyciski "Edytuj" i "Usuń" do każdej komórki w kolumnie akcji.
     * Wykorzystuje mechanizm CellFactory JavaFX.
     */
    private void addActionsToTable() {
        Callback<TableColumn<ObservableList<String>, Void>, TableCell<ObservableList<String>, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<ObservableList<String>, Void> call(final TableColumn<ObservableList<String>, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edytuj");
                    private final Button deleteBtn = new Button("Usuń");
                    private final Separator separator = new Separator(Orientation.VERTICAL);
                    private final HBox pane = new HBox(5, editBtn,separator, deleteBtn);

                    {
                        // Stylowanie przycisków w kodzie (inline CSS)
                        editBtn.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-cursor: hand;");
                        deleteBtn.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: ef4444; -fx-font-size: 11px; -fx-cursor: hand;");
                        separator.setStyle("-fx-max-height: 12; -fx-valignment: center;");
                        pane.setStyle("-fx-alignment: CENTER;");

                        editBtn.setOnAction(event -> handleEditUser(getTableView().getItems().get(getIndex())));
                        deleteBtn.setOnAction(event -> handleDeleteUser(getTableView().getItems().get(getIndex())));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        actionsColumn.setCellFactory(cellFactory);
    }

    /**
     * Pobiera wszystkich użytkowników z bazy danych i wypełnia tabelę.
     * @throws SQLException W przypadku błędu połączenia z bazą.
     */
    private void loadUsers() throws SQLException {
        data.clear();
        try (var conn = Database.getConnection()) {
            var statement = conn.prepareStatement("SELECT id, first_name, last_name, login, role FROM user");
            var result = statement.executeQuery();
            while (result.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(result.getString("id"));
                row.add(result.getString("first_name"));
                row.add(result.getString("last_name"));
                row.add(result.getString("login"));
                row.add(result.getString("role"));
                data.add(row);
            }
            userTable.setItems(data);
        }
    }

    /** Otwiera formularz dodawania nowego użytkownika. */
    @FXML
    public void addUser() {
        openUserForm(null, "Dodaj Użytkownika");
    }

    /** * Obsługuje edycję wybranego użytkownika.
     * @param rowData Dane wiersza z tabeli.
     */
    private void handleEditUser(ObservableList<String> rowData) {
        openUserForm(rowData, "Edycja Użytkownika");
    }

    /**
     * Otwiera okno modalne z formularzem użytkownika (UserFormController).
     * * @param userData Dane użytkownika do edycji (null dla nowego użytkownika).
     * @param title Tytuł okna.
     */
    private void openUserForm(ObservableList<String> userData, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("userForm.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            if (userData != null) {
                controller.setEditData(userData);
            } else {
                controller.setCreateMode();
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            loadUsers(); // Odświeżenie listy po zamknięciu okna
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Usuwa użytkownika z bazy danych.
     * Usuwa również powiązane dane kaskadowo (np. oceny studenta, przypisane przedmioty nauczyciela).
     * * @param rowData Dane użytkownika do usunięcia.
     */
    private void handleDeleteUser(ObservableList<String> rowData) {
        String fullName = rowData.get(1) + " " + rowData.get(2);
        String role = rowData.get(4);
        int userId = Integer.parseInt(rowData.get(0));

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć użytkownika " + fullName + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try (var conn = Database.getConnection()) {
                    // Logika usuwania kaskadowego dla poszczególnych ról
                    if ("student".equals(role)) {
                        var rs = conn.createStatement().executeQuery("SELECT id FROM student WHERE user_id=" + userId);
                        if (rs.next()) {
                            int sid = rs.getInt(1);
                            conn.createStatement().executeUpdate("DELETE FROM grades WHERE student_id=" + sid);
                            conn.createStatement().executeUpdate("DELETE FROM student WHERE id=" + sid);
                        }
                    } else if ("nauczyciel".equals(role)) {
                        var rs = conn.createStatement().executeQuery("SELECT id FROM teacher WHERE user_id=" + userId);
                        if (rs.next()) {
                            int tid = rs.getInt(1);
                            conn.createStatement().executeUpdate("DELETE FROM teacher_subject WHERE teacher_id=" + tid);
                            conn.createStatement().executeUpdate("DELETE FROM teacher WHERE id=" + tid);
                        }
                    }
                    conn.createStatement().executeUpdate("DELETE FROM user WHERE id=" + userId);
                    loadUsers();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    // Metody obsługi zdarzeń FXML
    @FXML public void searchUser() { searchLogic(); }
    @FXML public void addStudent() { openDialog("addStudent.fxml", "Dodaj Studenta"); }
    @FXML public void addTeacher() { openDialog("addTeacher.fxml", "Dodaj Nauczyciela"); }

    /** Pomocnicza metoda otwierająca proste okno dialogowe z pliku FXML. */
    private void openDialog(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
            loadUsers();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Filtruje listę użytkowników w tabeli na podstawie wpisanego tekstu.
     * Wyszukuje po imieniu lub nazwisku.
     */
    private void searchLogic() {
        String text = searchField.getText().toLowerCase();
        if(text.isEmpty()) userTable.setItems(data);
        else {
            ObservableList<ObservableList<String>> filtered = FXCollections.observableArrayList();
            for(ObservableList<String> row : data){
                if(row.get(1).toLowerCase().contains(text) || row.get(2).toLowerCase().contains(text)) {
                    filtered.add(row);
                }
            }
            userTable.setItems(filtered);
        }
    }
}