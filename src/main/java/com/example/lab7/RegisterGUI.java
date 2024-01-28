package com.example.lab7;

import com.example.lab7.gui.RegisterController;
import com.example.lab7.repository.RepoDB.UserRepoDB;
import com.example.lab7.service.UtilizatorService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterGUI extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        UserRepoDB userRepoDB = new UserRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","");
        UtilizatorService service = new UtilizatorService(userRepoDB);


        FXMLLoader fxmlLoader = new FXMLLoader(RegisterGUI.class.getResource("register.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450, 300);
        RegisterController controller = fxmlLoader.getController();
        controller.setService(service);
        stage.setTitle("Register");
        stage.setScene(scene);
        stage.show();
    }

}