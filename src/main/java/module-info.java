module com.example.elektroniczny_dziennik {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;


    opens com.example.elektroniczny_dziennik to javafx.fxml;
    exports com.example.elektroniczny_dziennik;
}