package com.example.lab7.gui;

import com.example.lab7.domain.FriendRequest;
import com.example.lab7.domain.Prietenie;
import com.example.lab7.domain.Tuple;
import com.example.lab7.domain.Utilizator;
import com.example.lab7.gui.alert.UserActionsAlert;
import com.example.lab7.repository.Page;
import com.example.lab7.repository.Pageable;
import com.example.lab7.service.FriendshipRequestService;
import com.example.lab7.service.PrietenieService;
import com.example.lab7.service.UtilizatorService;
import com.example.lab7.utils.events.ChangeEventType;
import com.example.lab7.utils.events.PrietenieTaskChangeEvent;
import com.example.lab7.utils.events.TaskChangeEvent;
import com.example.lab7.utils.events.UserTaskChangeEvent;
import com.example.lab7.utils.observer.Observer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AdminController implements Observer<TaskChangeEvent> {
    private PrietenieService prietenieService;
    private UtilizatorService utilizatorService;
    private FriendshipRequestService friendshipRequestService;
    ObservableList<Utilizator> modelUser = FXCollections.observableArrayList();
    ObservableList<Prietenie> modelPrietenie = FXCollections.observableArrayList();
    ObservableList<FriendRequest> modelFriendRequest = FXCollections.observableArrayList();

    private int pageSize = 10;
    private int currentRequestPage = 0;
    private int totalNumberOfRequests = 0;
    private int currentFriendshipPage = 0;
    private int totalNumberOfFriendships = 0;

    private int currentUsersPage = 0;
    private int totalNumberOfUsers = 0;


    //User

    @FXML
    public TableView<Utilizator> usersTable;

    @FXML
    public TableColumn<Utilizator, Long> idColumn;

    @FXML
    public TableColumn<Utilizator, String> firstNameColumn;

    @FXML
    private TableColumn<Utilizator, String> lastNameColumn;

    @FXML
    private TableColumn<Utilizator, String> emailColumn;

    @FXML
    private TableColumn<Utilizator, String> passwordColumn;
    @FXML
    public TextField prenumeText;

    @FXML
    public TextField numeText;

    @FXML
    public TextField idText;
    @FXML
    public TextField emailText;

    @FXML
    public TextField passwordText;

    @FXML
    public Button exitButton1;

    // Friendship

    @FXML
    public TableView<Prietenie> friendshipTable;

    @FXML
    public TableColumn<Prietenie, Long> user1Column;

    @FXML
    public TableColumn<Prietenie, Long> user2Column;

    @FXML
    private TableColumn<Prietenie, LocalDateTime> friendsFromColumn;
    @FXML
    public TextField user1Text;

    @FXML
    public TextField user2Text;

    @FXML
    public TextField friendsFromText;

    public  Button exitButton2;

    //Friendship Request
    @FXML
    private TableView<FriendRequest> requestTable;

    @FXML
    private TableColumn<FriendRequest, Long> id1RequestColumn;

    @FXML
    private TableColumn<FriendRequest, Long> id2RequestColumn;

    @FXML
    private TableColumn<FriendRequest, LocalDateTime> dateRequestColumn;

    @FXML
    private TableColumn<FriendRequest, String> statusRequestColumn;

    @FXML
    private Button nextFriendRequestButton;

    @FXML
    private Button previousFriendRequestButton;

    @FXML
    private Button nextFriendshipButton;

    @FXML
    private Button previousFriendshipButton;

    @FXML
    private Button nextUserButton;

    @FXML
    private Button previousUserButton;

    public void setService(PrietenieService prietenieService, UtilizatorService utilizatorService, FriendshipRequestService friendshipRequestService, Integer pageSizeInt){
        this.prietenieService = prietenieService;
        this.utilizatorService = utilizatorService;
        this.friendshipRequestService = friendshipRequestService;
        this.pageSize = pageSizeInt;
        prietenieService.addObserver(this);
        utilizatorService.addObserver(this);
        friendshipRequestService.addObserver(this);
        initializeTableDataUser();
        initializeTableDataFriendship();
        initializaTableRequest();
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<Utilizator, Long>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("email"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("password"));
        usersTable.setItems(modelUser);

        user1Column.setCellValueFactory(cellData -> {
            Tuple<Long, Long> tuple = cellData.getValue().getId();
            return new SimpleObjectProperty<>(tuple.getLeft());
        });
        user2Column.setCellValueFactory(cellData -> {
            Tuple<Long, Long> tuple = cellData.getValue().getId();
            return new SimpleObjectProperty<>(tuple.getRight());
        });

        friendsFromColumn.setCellValueFactory(cellData -> {
            LocalDateTime tuple = cellData.getValue().getDate();
            return new SimpleObjectProperty<>(tuple);
        });
        friendshipTable.setItems(modelPrietenie);

        id1RequestColumn.setCellValueFactory(cellData -> {
            Tuple<Long, Long> tuple = cellData.getValue().getId();
            return new SimpleObjectProperty<>(tuple.getLeft());
        });
        id2RequestColumn.setCellValueFactory(cellData -> {
            Tuple<Long, Long> tuple = cellData.getValue().getId();
            return new SimpleObjectProperty<>(tuple.getRight());
        });

        dateRequestColumn.setCellValueFactory(cellData -> {
            LocalDateTime tuple = cellData.getValue().getDate();
            return new SimpleObjectProperty<>(tuple);
        });
        statusRequestColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        requestTable.setItems(modelFriendRequest);
    }

    private void initializaTableRequest(){
        /*Iterable<FriendRequest> allRequests = friendshipRequestService.getAll();

        List<FriendRequest> allRequestsList = StreamSupport.stream(allRequests.spliterator(), false).toList();
        modelFriendRequest.setAll(allRequestsList);*/

        Page<FriendRequest> page = friendshipRequestService.findAll(new Pageable(currentRequestPage, pageSize));

        int maxPage = (int) Math.ceil((double) page.getTotalElements() / pageSize ) - 1;
        if(maxPage == -1) maxPage = 0;
        if(currentRequestPage > maxPage) {
            currentRequestPage = maxPage;
            page = friendshipRequestService.findAll(new Pageable(currentRequestPage, pageSize));
        }

        modelFriendRequest.setAll(StreamSupport.stream(page.getElements().spliterator(),
                false).collect(Collectors.toList()));
        totalNumberOfRequests = page.getTotalElements();

        previousFriendRequestButton.setDisable(currentRequestPage == 0);
        nextFriendRequestButton.setDisable((currentRequestPage+1)*pageSize >= totalNumberOfRequests);
    }

    @Override
    public void update(TaskChangeEvent taskChangeEvent) {
        initializeTableDataUser();
        initializeTableDataFriendship();
    }

    // USER

    public void initializeTableDataUser(){
        /*Iterable<Utilizator> allUsers = utilizatorService.getAll();
        List<Utilizator> allUsersList = StreamSupport.stream(allUsers.spliterator(), false).toList();
        modelUser.setAll(allUsersList);*/

        Page<Utilizator> page = utilizatorService.findAll(new Pageable(currentUsersPage, pageSize));

        int maxPage = (int) Math.ceil((double) page.getTotalElements() / pageSize ) - 1;
        if(maxPage == -1) maxPage = 0;
        if(currentUsersPage > maxPage) {
            currentUsersPage = maxPage;
            page = utilizatorService.findAll(new Pageable(currentUsersPage, pageSize));
        }

        modelUser.setAll(StreamSupport.stream(page.getElements().spliterator(),
                false).collect(Collectors.toList()));
        totalNumberOfUsers = page.getTotalElements();

        previousUserButton.setDisable(currentUsersPage == 0);
        nextUserButton.setDisable((currentUsersPage+1)*pageSize >= totalNumberOfUsers);
    }

    public void handleAddUser(){
        String prenume = prenumeText.getText();
        String nume = numeText.getText();
        String email = emailText.getText();
        String password = passwordText.getText();

        try {
            Utilizator user = new Utilizator(prenume, nume,email,password);

            try {
                Optional<Utilizator> addedUser = utilizatorService.add(user);
                if (addedUser.isPresent())
                    UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! Exista deja un utilizator cu ID-ul dat!");
                else{
                    update(new UserTaskChangeEvent(ChangeEventType.ADD, user));
                }
            }
            catch (Exception e){
                UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        catch (Exception e){
            UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "ID invalid! ID-ul trebuie sa fie un numar!");
        }

        prenumeText.clear();
        numeText.clear();
        idText.clear();
        emailText.clear();
        passwordText.clear();
    }

    public void handleDeleteUser(){
        try {
            Long id = Long.parseLong(idText.getText());
            try {
                Optional<Utilizator> deletedUser = utilizatorService.delete(id);
                if (deletedUser.isEmpty())
                    UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! Nu exista utilizator cu ID-ul dat!");
                else update(new UserTaskChangeEvent(ChangeEventType.DELETE, deletedUser.get()));
            }
            catch (Exception e){
                UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        catch (Exception e){
            UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "ID invalid! ID-ul trebuie sa fie un numar!");
        }

        prenumeText.clear();
        numeText.clear();
        idText.clear();
        emailText.clear();
        passwordText.clear();
    }

    public void handleUpdateUser(){
        String prenume = prenumeText.getText();
        String nume = numeText.getText();
        String email = emailText.getText();
        String password = passwordText.getText();

        try {
            Long id = Long.parseLong(idText.getText());
            Utilizator user = new Utilizator(prenume, nume,email,password);
            user.setId(id);
            try {
                Optional<Utilizator> updatedUser = utilizatorService.update(user);
                if (updatedUser.isEmpty())
                    UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! Nu exista utilizatorul dat!");
                else update(new UserTaskChangeEvent(ChangeEventType.UPDATE, user));
            }
            catch (Exception e){
                UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        catch (Exception e){
            UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "ID invalid! ID-ul trebuie sa fie un numar!");
        }

        prenumeText.clear();
        numeText.clear();
        idText.clear();
        emailText.clear();
        passwordText.clear();
    }

    public void handleFindUser(){
        try {
            Long id = Long.parseLong(idText.getText());
                Optional<Utilizator> foundUser = utilizatorService.getEntityById(id);
                if (foundUser.isEmpty()) {
                    UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! Nu exista utilizator cu ID-ul dat!");
                    prenumeText.clear();
                    numeText.clear();
                    emailText.clear();
                    passwordText.clear();
                }
                else{
                    prenumeText.setText(foundUser.get().getFirstName());
                    numeText.setText(foundUser.get().getLastName());
                    emailText.setText(foundUser.get().getEmail());
                    passwordText.setText(foundUser.get().getPassword());
                }
        }
        catch (Exception e){
            UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "ID invalid! ID-ul trebuie sa fie un numar!");
            prenumeText.clear();
            numeText.clear();
            emailText.clear();
            passwordText.clear();
        }
    }

    public void handleExitUser(){
        Node src = exitButton1;
        Stage stage = (Stage) src.getScene().getWindow();

        stage.close();
    }

    public void handleSelectUser(){
        Utilizator utilizator = usersTable.getSelectionModel().getSelectedItem();

        idText.setText(utilizator.getId().toString());
        prenumeText.setText(utilizator.getFirstName());
        numeText.setText(utilizator.getLastName());
        emailText.setText(utilizator.getEmail());
        passwordText.setText(utilizator.getPassword());
    }

    // FRIENDSHIP

    public void initializeTableDataFriendship(){
        /*Iterable<Prietenie> allFriendships = prietenieService.getAll();
        List<Prietenie> allFriendshipsList = StreamSupport.stream(allFriendships.spliterator(), false).toList();
        modelPrietenie.setAll(allFriendshipsList);*/

        Page<Prietenie> page = prietenieService.findAll(new Pageable(currentFriendshipPage, pageSize));

        int maxPage = (int) Math.ceil((double) page.getTotalElements() / pageSize ) - 1;
        if(maxPage == -1) maxPage = 0;
        if(currentFriendshipPage > maxPage) {
            currentFriendshipPage = maxPage;
            page = prietenieService.findAll(new Pageable(currentFriendshipPage, pageSize));
        }

        modelPrietenie.setAll(StreamSupport.stream(page.getElements().spliterator(),
                false).collect(Collectors.toList()));
        totalNumberOfFriendships = page.getTotalElements();

        previousFriendshipButton.setDisable(currentFriendshipPage == 0);
        nextFriendshipButton.setDisable((currentFriendshipPage+1)*pageSize >= totalNumberOfFriendships);
    }

    public void handleAddFriendship(){
        try {
            Long id1 = Long.parseLong(user1Text.getText());
            Long id2 = Long.parseLong(user2Text.getText());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String date = friendsFromText.getText();
            Prietenie prietenie = new Prietenie(LocalDateTime.parse(date, formatter));
            prietenie.setId(new Tuple<>(id1,id2));

            try {
                Optional<Prietenie> addedFriend = prietenieService.add(prietenie);
                if (addedFriend.isPresent())
                    UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! Exista deja prietenia data!");
                else{
                    update(new PrietenieTaskChangeEvent(ChangeEventType.ADD, prietenie));
                }
            }
            catch (Exception e){
                UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        catch (Exception e){
            UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
        }

        user1Text.clear();
        user2Text.clear();
        friendsFromText.clear();
    }

    public void handleDeleteFriendship(){
        try {
            Long id1 = Long.parseLong(user1Text.getText());
            Long id2 = Long.parseLong(user2Text.getText());

            try {
                Optional<Prietenie> deletedFriend = prietenieService.delete(new Tuple<>(id1, id2));
                if (deletedFriend.isEmpty())
                    UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! Nu exista prietenia ceruta!");
                else{
                    update(new PrietenieTaskChangeEvent(ChangeEventType.DELETE, deletedFriend.get()));
                }
            }
            catch (Exception e){
                UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        catch (Exception e){
            UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
        }

        user1Text.clear();
        user2Text.clear();
        friendsFromText.clear();
    }

    public void handleUpdateFriendship(){
        try {
            Long id1 = Long.parseLong(user1Text.getText());
            Long id2 = Long.parseLong(user2Text.getText());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String date = friendsFromText.getText();
            Prietenie prietenie = new Prietenie(LocalDateTime.parse(date, formatter));
            prietenie.setId(new Tuple<>(id1,id2));

            try {
                Optional<Prietenie> editedFriendship = prietenieService.update(prietenie);
                if (editedFriendship.isPresent())
                    UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", "Eroare! Nu exista prietenia data!");
                else{
                    update(new PrietenieTaskChangeEvent(ChangeEventType.UPDATE, prietenie));
                }
            }
            catch (Exception e){
                UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
        catch (Exception e){
            UserActionsAlert.showMessage(null, Alert.AlertType.ERROR, "Error", e.getMessage());
        }

        user1Text.clear();
        user2Text.clear();
        friendsFromText.clear();
    }

    public void handleExitFriendship(){
        Node src = exitButton2;
        Stage stage = (Stage) src.getScene().getWindow();

        stage.close();
    }

    public void handleCommunities(){
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        Integer number = utilizatorService.nrComunitati();
        Label label = new Label("In retea sunt " + number + " comunitati!");
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> dialogStage.close());

        AnchorPane layout = new AnchorPane();
        layout.getChildren().addAll(label, exitButton);

        AnchorPane.setTopAnchor(label,20d);
        AnchorPane.setLeftAnchor(label,20d);

        AnchorPane.setBottomAnchor(exitButton,20d);
        AnchorPane.setRightAnchor(exitButton,10d);

        Scene dialogScene = new Scene(layout, 200, 100);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    public void handleBiggestCommunity(){
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);

        ListView<Utilizator> listView = new ListView<>();
        ObservableList<Utilizator> items = FXCollections.observableArrayList(utilizatorService.comunitateaSociabila());
        listView.setItems(items);

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> dialogStage.close());

        VBox dialogLayout = new VBox(10);
        dialogLayout.getChildren().addAll(listView, exitButton);
        dialogLayout.setPadding(new Insets(10));

        Scene dialogScene = new Scene(dialogLayout, 300, 200);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    public void handleSelectFriendship(){
        Prietenie prietenie = friendshipTable.getSelectionModel().getSelectedItem();

        user1Text.setText(prietenie.getId().getLeft().toString());
        user2Text.setText(prietenie.getId().getRight().toString());
        friendsFromText.setText(prietenie.getDate().toString());
    }

    public void handleNextFriendRequestButton(){
        currentRequestPage++;
        initializaTableRequest();
    }

    public void handlePreviousFriendRequestButton(){
        currentRequestPage--;
        initializaTableRequest();
    }

    public void handleNextFriendshipButton(){
        currentFriendshipPage++;
        initializeTableDataFriendship();
    }

    public void handlePreviousFriendshipButton(){
        currentFriendshipPage--;
        initializeTableDataFriendship();
    }

    public void handleNextUserButton(){
        currentUsersPage++;
        initializeTableDataUser();
    }

    public void handlePreviousUserButton(){
        currentUsersPage--;
        initializeTableDataUser();
    }
}