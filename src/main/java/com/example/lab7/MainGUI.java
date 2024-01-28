package com.example.lab7;

import com.example.lab7.gui.LoginController;
import com.example.lab7.repository.RepoDB.FriendRequestRepoDB;
import com.example.lab7.repository.RepoDB.MessageRepoDB;
import com.example.lab7.repository.RepoDB.PrietenieRepoDB;
import com.example.lab7.repository.RepoDB.UserRepoDB;
import com.example.lab7.service.FriendshipRequestService;
import com.example.lab7.service.MessageService;
import com.example.lab7.service.PrietenieService;
import com.example.lab7.service.UtilizatorService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainGUI extends Application {


    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        UserRepoDB userRepoDB = new UserRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","");
        PrietenieRepoDB prietenieRepoDB = new PrietenieRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","");
        FriendRequestRepoDB friendRequestRepoDB = new FriendRequestRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","");
        MessageRepoDB messageRepoDB = new MessageRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","", userRepoDB);
        UtilizatorService utilizatorService = new UtilizatorService(userRepoDB);
        PrietenieService prietenieService = new PrietenieService(prietenieRepoDB, userRepoDB);
        FriendshipRequestService friendshipRequestService = new FriendshipRequestService(friendRequestRepoDB, userRepoDB, prietenieRepoDB);
        MessageService messageService = new MessageService(messageRepoDB, userRepoDB);

        FXMLLoader fxmlLoader = new FXMLLoader(MainGUI.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450, 300);
        LoginController controller = fxmlLoader.getController();
        controller.setService(utilizatorService, prietenieService, friendshipRequestService, messageService);

        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

}