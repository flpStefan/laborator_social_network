package com.example.lab7;

import com.example.lab7.gui.AdminController;
import com.example.lab7.repository.RepoDB.FriendRequestRepoDB;
import com.example.lab7.repository.RepoDB.PrietenieRepoDB;
import com.example.lab7.repository.RepoDB.UserRepoDB;
import com.example.lab7.service.FriendshipRequestService;
import com.example.lab7.service.PrietenieService;
import com.example.lab7.service.UtilizatorService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminGUI extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        UserRepoDB userRepoDB = new UserRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","");
        PrietenieRepoDB prietenieRepoDB = new PrietenieRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","");
        FriendRequestRepoDB friendRequestRepoDB = new FriendRequestRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","");
        UtilizatorService utilizatorService = new UtilizatorService(userRepoDB);
        PrietenieService prietenieService = new PrietenieService(prietenieRepoDB, userRepoDB);
        FriendshipRequestService friendshipRequestService = new FriendshipRequestService(friendRequestRepoDB, userRepoDB, prietenieRepoDB);

        FXMLLoader fxmlLoader = new FXMLLoader(AdminGUI.class.getResource("admin_gui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Social Network");
        stage.setScene(scene);
        AdminController controller = fxmlLoader.getController();
        controller.setService(prietenieService, utilizatorService, friendshipRequestService, 10);
        stage.show();
    }

}