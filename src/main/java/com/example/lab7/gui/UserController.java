package com.example.lab7.gui;

import com.example.lab7.MessageGUI;
import com.example.lab7.domain.*;
import com.example.lab7.gui.alert.FriendRequestActionsAlert;
import com.example.lab7.repository.Page;
import com.example.lab7.repository.Pageable;
import com.example.lab7.service.FriendshipRequestService;
import com.example.lab7.service.MessageService;
import com.example.lab7.service.PrietenieService;
import com.example.lab7.service.UtilizatorService;
import com.example.lab7.utils.events.*;
import com.example.lab7.utils.observer.Observer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserController implements Observer<TaskChangeEvent> {

    private Long loggedUserId;

    private PrietenieService prietenieService;
    private UtilizatorService utilizatorService;
    private MessageService messageService;
    private FriendshipRequestService friendshipRequestService;

    ObservableList<Utilizator> modelFriendList = FXCollections.observableArrayList();
    ObservableList<Utilizator> modelFriendRequestList = FXCollections.observableArrayList();

    //-----------------------------------------friendRequests-----------------------------------------\\

    //-----------------------------------------friendListTable-----------------------------------------\\
    @FXML
    private TableView<Utilizator> friendListTable;

    @FXML
    private TableColumn<Utilizator, String> friendListLastNameColumn;

    @FXML
    private TableColumn<Utilizator, String> friendListFirstNameColumn;

    @FXML
    private TableColumn<Utilizator, String> friendListEmailColumn;

    //-----------------------------------------friendRequestsTable-----------------------------------------\\
    @FXML
    private TableView<Utilizator> friendRequestTable;

    @FXML
    private TableColumn<Utilizator, String> friendRequestLastNameColumn;

    @FXML
    private TableColumn<Utilizator, String> friendRequestFirstNameColumn;

    @FXML
    private TableColumn<Utilizator, String> friendRequestEmailColumn;

    @FXML
    private Button exitButton;

    @FXML
    private TextField emailText;

    @FXML
    private Tab messageTab;
    //-----------------------------------------paginare-----------------------------------------\\

    private int pageSize = 2;
    private int currentFriendPage = 0;
    private int totalNumberOfFriends = 0;

    private int currentFriendRequestsPage = 0;
    private int totalNumberOfFriendRequests = 0;

    @FXML
    private Button nextButtonFriend;
    @FXML
    private Button previousButtonFriend;
    @FXML
    private Button nextButtonFriendRequest;
    @FXML
    private Button previousButtonFriendRequest;


    public void setService(Long loggedUserId, PrietenieService prietenieService, UtilizatorService utilizatorService,
                                                    FriendshipRequestService friendshipRequestService, MessageService messageService, Integer pageSize){
        this.loggedUserId = loggedUserId;
        this.prietenieService = prietenieService;
        this.utilizatorService = utilizatorService;
        this.friendshipRequestService = friendshipRequestService;
        this.messageService = messageService;
        this.pageSize = pageSize;

        friendshipRequestService.addObserver(this);
        utilizatorService.addObserver(this);
        prietenieService.addObserver(this);
        initializeFriendListTable();
        initializeFriendRequestListTable();
    }

    @FXML
    public void initialize() {
        friendListFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        friendListLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        friendListEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        friendListTable.setItems(modelFriendList);

        friendRequestFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        friendRequestLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        friendRequestEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        friendRequestTable.setItems(modelFriendRequestList);
    }

    private void initializeFriendListTable(){

        Page<Long> page = prietenieService.getFriendsIds(loggedUserId, new Pageable(currentFriendPage, pageSize));

        List<Utilizator> allFriendsUser = new ArrayList<>();
        page.getElements().forEach(x -> allFriendsUser.add(utilizatorService.getEntityById(x).get()));



        int maxPage = (int) Math.ceil((double) page.getTotalElements() / pageSize ) - 1;
        if(maxPage == -1) maxPage = 0;
        if(currentFriendPage > maxPage) {
            currentFriendPage = maxPage;
            page = prietenieService.getFriendsIds(loggedUserId, new Pageable(currentFriendPage, pageSize));
        }

        modelFriendList.setAll(allFriendsUser);
        totalNumberOfFriends = page.getTotalElements();

        previousButtonFriend.setDisable(currentFriendPage == 0);
        nextButtonFriend.setDisable((currentFriendPage + 1) * pageSize >= totalNumberOfFriends);
    }

    private void initializeFriendRequestListTable(){
        /*List<Long> friendRequests = friendshipRequestService.getFriendRequestIds(loggedUserId);
        Iterable<Utilizator> allFriendRequestsUser = friendRequests.stream()
                .map(x -> {return utilizatorService.getEntityById(x).get();})
                .collect(Collectors.toList());

        List<Utilizator> allFriendRequests = StreamSupport.stream(allFriendRequestsUser.spliterator(), false).toList();
        modelFriendRequestList.setAll(allFriendRequests);*/

        Page<Long> page = friendshipRequestService.getFriendRequestIds(loggedUserId, new Pageable(currentFriendRequestsPage, pageSize));

        List<Utilizator> allRequests = new ArrayList<>();
        page.getElements().forEach(x -> allRequests.add(utilizatorService.getEntityById(x).get()));

        int maxPage = (int) Math.ceil((double) page.getTotalElements() / pageSize ) - 1;
        if(maxPage == -1) maxPage = 0;
        if(currentFriendRequestsPage > maxPage) {
            currentFriendRequestsPage = maxPage;
            page = friendshipRequestService.getFriendRequestIds(loggedUserId, new Pageable(currentFriendRequestsPage, pageSize));
        }

        modelFriendRequestList.setAll(allRequests);
        totalNumberOfFriendRequests = page.getTotalElements();

        previousButtonFriendRequest.setDisable(currentFriendRequestsPage == 0);
        nextButtonFriendRequest.setDisable((currentFriendRequestsPage+1)*pageSize >= totalNumberOfFriendRequests);
    }

    public void handleExit(){
        Node src = exitButton;
        Stage stage = (Stage) src.getScene().getWindow();

        stage.close();
    }

    public void handleAccept(){
        Utilizator utilizator = friendRequestTable.getSelectionModel().getSelectedItem();
        Tuple<Long, Long> ids = new Tuple<>(utilizator.getId(), loggedUserId);

        FriendRequest friendRequest = friendshipRequestService.getEntityById(ids).get();
        friendRequest.setStatus(FriendRequestStatus.APPROVED);
        friendshipRequestService.update(friendRequest);

        Prietenie prietenie = new Prietenie(LocalDateTime.now());
        prietenie.setId(ids);
        prietenieService.add(prietenie);

        friendshipRequestService.notifyObservers(new FriendRequestTaskChangeEvent(ChangeEventType.ADD, friendRequest));
        update(new PrietenieTaskChangeEvent(ChangeEventType.ADD, prietenie));
    }

    public void handleReject(){
        Utilizator utilizator = friendRequestTable.getSelectionModel().getSelectedItem();
        Tuple<Long, Long> ids = new Tuple<>(utilizator.getId(), loggedUserId);

        FriendRequest friendRequest = friendshipRequestService.getEntityById(ids).get();
        friendRequest.setStatus(FriendRequestStatus.REJECTED);
        friendshipRequestService.update(friendRequest);

        friendshipRequestService.notifyObservers(new FriendRequestTaskChangeEvent(ChangeEventType.ADD, friendRequest));
        update(new FriendRequestTaskChangeEvent(ChangeEventType.DELETE, friendRequest));
    }

    public void handleSend(){
        String email = emailText.getText();

        Optional<Utilizator> utilizator = utilizatorService.findUserByEmail(email);
        if(utilizator.isEmpty()) {
            FriendRequestActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! E-mailul introdus nu exista!");
            emailText.getText();
            return;
        }

        FriendRequestStatus friendRequestStatus = FriendRequestStatus.PENDING;
        FriendRequest friendRequest = new FriendRequest(friendRequestStatus);
        friendRequest.setId(new Tuple<>(loggedUserId, utilizator.get().getId()));

        try {
            Optional<FriendRequest> sentRequest = friendshipRequestService.add(friendRequest);
            if(sentRequest.isPresent())
                FriendRequestActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Exista deja o cerere catre utilizatorul dat!");
            else {
                friendshipRequestService.notifyObservers(new FriendRequestTaskChangeEvent(ChangeEventType.ADD, friendRequest));
                update(new FriendRequestTaskChangeEvent(ChangeEventType.ADD, friendRequest));
                FriendRequestActionsAlert.showMessage(null, Alert.AlertType.INFORMATION, "Succes!", "Cererea a fost trimisa cu succes!");
            }
        }
        catch (Exception e){
            FriendRequestActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());}

        emailText.clear();
    }

    @Override
    public void update(TaskChangeEvent taskChangeEvent) {
        initializeFriendListTable();
        initializeFriendRequestListTable();
    }

    public void setMessageController() throws IOException {
        FXMLLoader loader = new FXMLLoader(MessageGUI.class.getResource("messages.fxml"));
        Parent root = loader.load();

        MessageController messageController = loader.getController();
        messageController.setService(utilizatorService, prietenieService, messageService, loggedUserId, pageSize);
        messageTab.setContent(root);
    }

    public void handleNextButtonFriend(){
        currentFriendPage++;
        initializeFriendListTable();
    }

    public void handlePreviousButtonFriend(){
        currentFriendPage--;
        initializeFriendListTable();
    }

    public void handleNextButtonFriendRequest(){
        currentFriendRequestsPage++;
        initializeFriendRequestListTable();
    }

    public void handlePreviousButtonFriendRequest(){
        currentFriendRequestsPage--;
        initializeFriendRequestListTable();
    }

}
