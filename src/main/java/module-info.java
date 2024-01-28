module com.example.lab7 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;

    opens com.example.lab7 to javafx.fxml;
    exports com.example.lab7;
    /*exports com.example.lab7.gui;
    exports com.example.lab7.domain;*/
    opens com.example.lab7.gui to javafx.fxml;
    exports com.example.lab7.gui;
    exports com.example.lab7.domain;
    opens com.example.lab7.domain to javafx.fxml;
}