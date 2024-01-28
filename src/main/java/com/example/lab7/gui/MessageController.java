package com.example.lab7.gui;

import com.example.lab7.domain.Message;
import com.example.lab7.domain.Utilizator;
import com.example.lab7.gui.alert.FriendRequestActionsAlert;
import com.example.lab7.gui.alert.MessageActionAlert;
import com.example.lab7.gui.alert.UserActionsAlert;
import com.example.lab7.repository.Page;
import com.example.lab7.repository.Pageable;
import com.example.lab7.service.MessageService;
import com.example.lab7.service.PrietenieService;
import com.example.lab7.service.UtilizatorService;
import com.example.lab7.utils.events.ChangeEventType;
import com.example.lab7.utils.events.FriendRequestTaskChangeEvent;
import com.example.lab7.utils.events.MessageTaskChangeEvent;
import com.example.lab7.utils.events.TaskChangeEvent;
import com.example.lab7.utils.observer.Observer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageController implements Observer<TaskChangeEvent> {
    private Long loggedUserID;
    private UtilizatorService utilizatorService;
    private PrietenieService prietenieService;
    private MessageService messageService;
    private ObservableList<String> modelFriends = FXCollections.observableArrayList();
    private ObservableList<Message> modelMessage = FXCollections.observableArrayList();
    @FXML
    private ListView<String> listFriends;

    @FXML ListView<Message> listMessages;

    @FXML
    private Button exitButton;

    @FXML
    private TextField messageText;

    //-----------------------------------------paginare-----------------------------------------\\

    private int pageSize = 2;
    private int currentFriendPage = 0;
    private int totalNumberOfFriends = 0;
    private int currentMessagePage = 0;
    private int totalNumberOfMessages = 0;
    private Long from_id = 0l;
    private Long to_id = 0l;

    @FXML
    private Button nextButtonFriend;
    @FXML
    private Button previousButtonFriend;
    @FXML
    public Button nextButtonMessage;
    @FXML
    public Button previousButtonMessage;

    @FXML
    public void initialize() {
        listFriends.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listFriends.setItems(modelFriends);
    }


    public void setService(UtilizatorService utilizatorService, PrietenieService prietenieService,
                           MessageService messageService, Long loggedUserID, Integer pageSize){
        this.messageService = messageService;
        this.utilizatorService = utilizatorService;
        this.prietenieService = prietenieService;
        this.loggedUserID = loggedUserID;
        this.pageSize = pageSize;

        prietenieService.addObserver(this);
        utilizatorService.addObserver(this);
        messageService.addObserver(this);
        initFriendList();
    }

    private void initFriendList() {

        Page<Long> page = prietenieService.getFriendsIds(loggedUserID, new Pageable(currentFriendPage, pageSize));

        List<String> allFriendsUser = new ArrayList<>();
        page.getElements().forEach(x -> allFriendsUser.add(utilizatorService.getEntityById(x).get().getEmail()));



        int maxPage = (int) Math.ceil((double) page.getTotalElements() / pageSize ) - 1;
        if(currentFriendPage > maxPage) {
            currentFriendPage = maxPage;
            page = prietenieService.getFriendsIds(loggedUserID, new Pageable(currentFriendPage, pageSize));
        }

        modelFriends.setAll(allFriendsUser);
        totalNumberOfFriends = page.getTotalElements();

        previousButtonFriend.setDisable(currentFriendPage == 0);
        nextButtonFriend.setDisable((currentFriendPage + 1) * pageSize >= totalNumberOfFriends);
    }


    private void loadListOfMessages(Long userIdFrom, Long userIdTo) {
        /*listMessages.getItems().clear();
        modelMessage.clear();
        for (Message msg : messageService.getMessagesBetweenTwoUsers(userIdFrom, userIdTo)) {

            if(msg.getReply() != null) {
                String message = msg.getMessage();

                String prefix = "Replied to message: '" + messageService.getEntityById(msg.getReply().getId()).get().getMessage()
                        + "' \nReply: ";
                String finalMessage = prefix + message;
                msg.setMessage(finalMessage);
            }

            modelMessage.add(msg);
        }

        listMessages.setItems(modelMessage);*/

        listMessages.getItems().clear();
        modelMessage.clear();

        Page<Message> page = messageService.getAllBetweenUsers(new Pageable(currentMessagePage, pageSize), userIdFrom, userIdTo);

        int maxPage = (int) Math.ceil((double) page.getTotalElements() / pageSize ) - 1;
        if(maxPage == -1) maxPage = 0;
        if(currentMessagePage > maxPage) {
            currentMessagePage = maxPage;
            page = messageService.getAllBetweenUsers(new Pageable(currentMessagePage, pageSize), userIdFrom, userIdTo);
        }

        page.getElements().forEach(x -> {
            if(x.getReply() != null) {
                String message = x.getMessage();

                String prefix = "Replied to message: '" + messageService.getEntityById(x.getReply().getId()).get().getMessage()
                        + "' \nReply: ";
                String finalMessage = prefix + message;
                x.setMessage(finalMessage);
            }

            modelMessage.add(x);});
        listMessages.setItems(modelMessage);
        totalNumberOfMessages = page.getTotalElements();

        previousButtonMessage.setDisable(currentMessagePage == 0);
        nextButtonMessage.setDisable((currentMessagePage + 1) * pageSize >= totalNumberOfMessages);
    }

    public void handleExit(){
        Node src = exitButton;
        Stage stage = (Stage) src.getScene().getWindow();

        stage.close();
    }

    public void handleSend(){

        List<String> email = listFriends.getSelectionModel().getSelectedItems();
        if(email.isEmpty()){
            MessageActionAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Trebuie selectat un email!");
            return;
        }
        String text = messageText.getText();
        if(text.isEmpty()){
            MessageActionAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Mesajul nu poate sa fie gol!");
            return;
        }

        List<Utilizator> utilizatori = email.stream()
                .map(x -> {return utilizatorService.findUserByEmail(x).get();})
                .toList();



        if(utilizatori.size() == 1) {
            Message msg = listMessages.getSelectionModel().getSelectedItem();

            Message message = new Message(utilizatorService.getEntityById(loggedUserID).get(),
                    utilizatori.get(0), LocalDateTime.now(), text);

            if(msg != null)
                message.setReply(msg);

            messageService.add(message);
            loadListOfMessages(loggedUserID, utilizatori.get(0).getId());
        }
        else
            utilizatori.forEach(x ->
            { messageService.addMessage(loggedUserID, x.getId(), text); });

        messageService.notifyObservers(new MessageTaskChangeEvent(ChangeEventType.ADD, null));
        update(new FriendRequestTaskChangeEvent(ChangeEventType.ADD, null));
        messageText.clear();
    }

    public void handleSelect(){
        List<String> emails = listFriends.getSelectionModel().getSelectedItems();
        if(emails.size() == 1) {
            Utilizator utilizator = utilizatorService.findUserByEmail(emails.get(0)).get();
            from_id = loggedUserID;
            to_id = utilizator.getId();
            loadListOfMessages(loggedUserID, utilizator.getId());
        }
        else {
            listMessages.getItems().clear();
            modelMessage.clear();
        }
    }

    @Override
    public void update(TaskChangeEvent taskChangeEvent) {
        handleSelect();
        initFriendList();
    }

    public void handleNextButtonFriend(){
        currentFriendPage++;
        initFriendList();
    }

    public void handlePreviousButtonFriend(){
        currentFriendPage--;
        initFriendList();
    }

    public void handleNextButtonMessage(){
        if(from_id != 0 && to_id != 0) {
            currentMessagePage++;
            loadListOfMessages(from_id, to_id);
        }
    }

    public void handlePreviousButtonMessage(){
        if(from_id != 0 && to_id != 0) {
            currentMessagePage--;
            loadListOfMessages(from_id, to_id);
        }
    }
}
