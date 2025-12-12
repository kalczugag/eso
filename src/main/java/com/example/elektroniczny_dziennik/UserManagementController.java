package com.example.elektroniczny_dziennik;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class UserManagementController {

    @FXML TextField searchField;
    @FXML Button searchBtn;

    @FXML private AnchorPane anchorPane;

    @FXML private TableView<ObservableList<String>> userTable;
    @FXML private TableColumn<ObservableList<String>, String> idColumn;
    @FXML private TableColumn<ObservableList<String>, String> firstNameColumn;
    @FXML private TableColumn<ObservableList<String>, String> lastNameColumn;
    @FXML private TableColumn<ObservableList<String>, String> loginColumn;
    @FXML private TableColumn<ObservableList<String>, String> roleColumn;

    private ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

    @FXML public void initialize() {
        anchorPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if(newScene != null){
                newScene.setOnKeyPressed(event -> {
                    if(event.getCode().equals(KeyCode.ENTER)) searchUser();
                });
            }
        });

        try{
            initializeTable();
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    void initializeTable() throws SQLException {
        data.clear();

        try(var conn = Database.getConnection()){
            var statement = conn.prepareStatement("SELECT id, first_name, last_name, login, role FROM user" +
                    " WHERE role <> 'admin'");
            var result = statement.executeQuery();

            while(result.next()){
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(result.getString("id"));
                row.add(result.getString("first_name"));
                row.add(result.getString("last_name"));
                row.add(result.getString("login"));
                row.add(result.getString("role"));

                data.add(row);
            }

            idColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getFirst()));
            firstNameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(1)));
            lastNameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(2)));
            loginColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(3)));
            roleColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(4)));

            idColumn.setText("ID");
            firstNameColumn.setText("Imię");
            lastNameColumn.setText("Nazwisko");
            loginColumn.setText("Login");
            roleColumn.setText("Rola");

            userTable.setItems(data);
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void searchUser(){
        String text = searchField.getText().toLowerCase();

        if(text.isEmpty()){
            userTable.setItems(data);
        }
        else {
            ObservableList<ObservableList<String>> filtered = FXCollections.observableArrayList();

            for(ObservableList<String> row : data){
                String firstName = row.get(1);
                String lastName = row.get(2);

                if(firstName.toLowerCase().contains(text) || lastName.toLowerCase().contains(text)) filtered.add(row);
            }

            userTable.setItems(filtered);
        }
    }

    public void deleteUser(){
        ObservableList<String> selectedRow = userTable.getSelectionModel().getSelectedItem();

        if(selectedRow == null){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Niepoprawna akcja");
            alert.setHeaderText("Zaznacz użytkownika do usunięcia");
            alert.showAndWait();
            return;
        }

        String userRole = selectedRow.get(4);
        int userId = Integer.parseInt(selectedRow.getFirst());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie");
        alert.setHeaderText("Czy na pewno chcesz usunąć użytkownika:\n" + selectedRow.get(1) + " " + selectedRow.get(2) + " (" + selectedRow.get(4) + ")?");
        alert.setContentText("Nie będzie można tego cofnąć");
        alert.showAndWait().filter(ButtonType.OK::equals).ifPresent(b -> {
            try(var conn = Database.getConnection()){
                switch(userRole){
                    case "student" -> {
                        var getStudentIdQuery = conn.prepareStatement("SELECT student.id FROM student" +
                                " JOIN user ON user.id = student.user_id" +
                                " WHERE user.id = ?");
                        getStudentIdQuery.setInt(1, userId);
                        var result = getStudentIdQuery.executeQuery();

                        if(result.next()){
                            int studentId = result.getInt(1);

                            var removeStudentQuery = conn.prepareStatement("DELETE FROM student WHERE id = ?");
                            removeStudentQuery.setInt(1, studentId);

                            var removeGradesQuery = conn.prepareStatement("DELETE FROM grades WHERE student_id = ?");
                            removeGradesQuery.setInt(1, studentId);

                            var removeFromUserQuery = conn.prepareStatement("DELETE FROM user WHERE id = ?");
                            removeFromUserQuery.setInt(1, userId);

                            removeGradesQuery.executeUpdate();
                            removeStudentQuery.executeUpdate();
                            removeFromUserQuery.executeUpdate();
                        }
                    }

                    case "nauczyciel" -> {
                        var getTeacherIdQuery = conn.prepareStatement("SELECT t.id FROM teacher AS t" +
                                " JOIN user AS u ON u.id = t.user_id" +
                                " WHERE u.id = ?");
                        getTeacherIdQuery.setInt(1, userId);

                        var result = getTeacherIdQuery.executeQuery();

                        if(result.next()){
                            int teacherId = result.getInt("id");

                            var removeTeacherSubjectQuery = conn.prepareStatement("DELETE FROM teacher_subject WHERE teacher_id = ?");
                            removeTeacherSubjectQuery.setInt(1, teacherId);

                            var removeTeacherQuery = conn.prepareStatement("DELETE FROM teacher WHERE id = ?");
                            removeTeacherQuery.setInt(1, teacherId);

                            var removeFromUserQuery = conn.prepareStatement("DELETE FROM user WHERE id = ?");
                            removeFromUserQuery.setInt(1, userId);

                            removeTeacherSubjectQuery.executeUpdate();
                            removeTeacherQuery.executeUpdate();
                            removeFromUserQuery.executeUpdate();
                        }
                    }
                }

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Sukces");
                info.setHeaderText("Poprawnie usunięto użytkownika");
                info.showAndWait();

                initializeTable();
            }
            catch (SQLException e){
                System.out.println(e.getMessage());
            }
        });
    }

    public void addStudent(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addStudent.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setTitle("Dodaj studenta");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            initializeTable();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void addTeacher(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTeacher.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setTitle("Dodaj nauczyciela");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            initializeTable();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}