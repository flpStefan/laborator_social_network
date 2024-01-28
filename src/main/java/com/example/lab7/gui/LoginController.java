package com.example.lab7.gui;

import com.example.lab7.AdminGUI;
import com.example.lab7.MessageGUI;
import com.example.lab7.RegisterGUI;
import com.example.lab7.UserGUI;
import com.example.lab7.domain.PasswordEncoder;
import com.example.lab7.domain.Utilizator;
import com.example.lab7.gui.alert.LoginActionAlert;
import com.example.lab7.service.FriendshipRequestService;
import com.example.lab7.service.MessageService;
import com.example.lab7.service.PrietenieService;
import com.example.lab7.service.UtilizatorService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    private UtilizatorService utilizatorService;
    private PrietenieService prietenieService;
    private MessageService messageService;

    private FriendshipRequestService friendshipRequestService;

    @FXML
    private TextField emailText;

    @FXML
    private TextField passwordText;

    @FXML
    private TextField pagesizeText;

    @FXML
    private Button exitButton;

    public void setService(UtilizatorService utilizatorService, PrietenieService prietenieService,
                           FriendshipRequestService friendshipRequestService, MessageService messageService){
        this.prietenieService = prietenieService;
        this.utilizatorService = utilizatorService;
        this.friendshipRequestService = friendshipRequestService;
        this.messageService = messageService;
    }

    public void handleCreate(){
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);

        FXMLLoader loader = new FXMLLoader(RegisterGUI.class.getResource("register.fxml"));
        try {
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setService(utilizatorService);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setTitle("Create account");
            dialogStage.showAndWait();
        }
        catch (IOException e){
            LoginActionAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void handleExit(){
        Node src = exitButton;
        Stage stage = (Stage) src.getScene().getWindow();

        stage.close();
    }

    public void handleLogin() {
        String email = emailText.getText();
        String password = passwordText.getText();
        String pageSizeString = pagesizeText.getText();
        if(email.isEmpty() || password.isEmpty() || pageSizeString.isEmpty()){
            LoginActionAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! Datele nu pot sa fie nule!");
            emailText.clear();
            passwordText.clear();
            return;
        }

        try{
            Integer pageSizeInt = Integer.parseInt(pageSizeString);
        }
        catch (Exception E){
            LoginActionAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! Page size-ul trebuie sa fie un numar intreg!");
            return;
        }
        Integer pageSizeInt = Integer.parseInt(pageSizeString);

        if(email.equals("admin@admin.com") && password.equals("admin123")) {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(AdminGUI.class.getResource("admin_gui.fxml"));
            try {
                Scene scene = new Scene(fxmlLoader.load(), 600, 400);
                stage.setScene(scene);

                AdminController adminController = fxmlLoader.getController();
                adminController.setService(prietenieService, utilizatorService, friendshipRequestService, pageSizeInt);
                stage.show();
            }
            catch(IOException e){
                LoginActionAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
            }
            emailText.clear();
            passwordText.clear();
            return;
        }

        Optional<Utilizator> utilizator = utilizatorService.findUserByEmail(email);
        if(utilizator.isEmpty()) {
            LoginActionAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! E-mailul introdus nu exista!");
            emailText.clear();
            passwordText.clear();
            return;
        }

        if(PasswordEncoder.checkPassword(password, utilizator.get().getPassword())) {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(UserGUI.class.getResource("user_gui.fxml"));
            try {
                Parent root = loader.load();
                UserController userController = loader.getController();
                userController.setService(utilizator.get().getId(), prietenieService, utilizatorService, friendshipRequestService, messageService, pageSizeInt);
                userController.setMessageController();
                stage.setTitle("Welcome, " + utilizator.get().getFirstName() + "!");
                stage.setScene(new Scene(root,800,600));
                stage.show();
            }
            catch (IOException e) {
                LoginActionAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        else
            LoginActionAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! Datele introduse sunt incorecte!");

        emailText.clear();
        passwordText.clear();
    }
}
