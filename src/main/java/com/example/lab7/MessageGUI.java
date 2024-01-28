package com.example.lab7;

import com.example.lab7.gui.MessageController;
import com.example.lab7.gui.RegisterController;
import com.example.lab7.repository.RepoDB.MessageRepoDB;
import com.example.lab7.repository.RepoDB.PrietenieRepoDB;
import com.example.lab7.repository.RepoDB.UserRepoDB;
import com.example.lab7.service.MessageService;
import com.example.lab7.service.PrietenieService;
import com.example.lab7.service.UtilizatorService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MessageGUI extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        PrietenieRepoDB prietenieRepoDB = new PrietenieRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","");
        UserRepoDB userRepoDB = new UserRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","");
        MessageRepoDB messageRepoDB = new MessageRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","", userRepoDB);
        UtilizatorService utilizatorService = new UtilizatorService(userRepoDB);
        PrietenieService prietenieService = new PrietenieService(prietenieRepoDB, userRepoDB);
        MessageService messageService = new MessageService(messageRepoDB,userRepoDB);


        FXMLLoader fxmlLoader = new FXMLLoader(MessageGUI.class.getResource("messages.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        MessageController controller = fxmlLoader.getController();
        controller.setService(utilizatorService, prietenieService, messageService, 1l, 2);
        stage.setTitle("Messages");
        stage.setScene(scene);
        stage.show();
    }

}
