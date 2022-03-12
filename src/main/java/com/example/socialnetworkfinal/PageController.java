package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.Event;
import com.example.socialnetworkfinal.domain.Friendship;
import com.example.socialnetworkfinal.domain.Page;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.exceptions.FriendshipException;
import com.example.socialnetworkfinal.domain.exceptions.UserException;
import com.example.socialnetworkfinal.domain.exceptions.ValidationException;
import com.example.socialnetworkfinal.repository.paging.PageableRepoImplementation;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import com.example.socialnetworkfinal.utils.observer.FriendshipEvent;
import com.example.socialnetworkfinal.utils.observer.Observer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PageController implements Observer<FriendshipEvent> {
    ObservableList<String> usersNotFriends = FXCollections.observableArrayList();
    ObservableList<Event> events = FXCollections.observableArrayList();
    ObservableList<Event> notifiableEvents = FXCollections.observableArrayList();
    Long userId;
    Page page;
    UserService userService;
    FriendshipService friendshipService;
    MessageService messageService;
    EventService eventService;
    User user;

    @FXML
    Button logoutButton;
    @FXML
    Label userName;
    @FXML
    Pane upPane;
    @FXML
    GridPane container;
    @FXML
    BorderPane borderPaneSearch;
    @FXML
    TextField text;

    @FXML
    Button close;

    @FXML
    Button sendRequest;
    @FXML
    HBox searchBox;
    @FXML
    Circle smallProfilePic;
    @FXML
    Pane centralPane;

    @FXML
    public void initialize() {
        text.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchBar(newValue);
        });
    }

    public void setService(User user, Long id, Page page, UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        this.user = user;
        this.userId = id;
        this.page = page;
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.eventService = eventService;
        userName.setAlignment(Pos.CENTER);
        userName.setText(page.getLastName() + "\n" + page.getFirstName());
        try {
            Image image = new Image(new FileInputStream(user.getProfilePictureUrl()));
            smallProfilePic.setFill(new ImagePattern(image));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }



    public FXMLLoader openWindow(String location, Stage stage, String windowName) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(location));
        stage.setTitle(windowName);
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.setScene(scene);
        return loader;
    }

    public void handleLogout() {
        this.friendshipService.removeAAllObservers();
        FXMLLoader loginLoader = openWindow("login-view.fxml", (Stage) logoutButton.getScene().getWindow(),"Login" );
        LoginController loginController = loginLoader.getController();
        loginController.setService(userService, friendshipService, messageService, eventService);

    }

    public void handleChats() {
        this.friendshipService.removeObserver(this);
        FXMLLoader loginLoader = openWindow("ChatView.fxml", (Stage) logoutButton.getScene().getWindow(),"Login" );
        ChatController chatController = loginLoader.getController();
        chatController.setService(user, userId, page,userService, friendshipService, messageService, eventService);
    }

    protected List<String> getUnknownUsers(){
        List<User> friends = userService.getAllFriends(userId);
        List<Friendship> requests = friendshipService.getAllRequests(userId);
        Predicate<User> notInFriends = n -> !friends.contains(n);
        Predicate<User> notMe = n -> !n.getId().equals(userId);
        return  userService.getAllUsers().stream()
                .filter(notInFriends.and(notMe))
                .map(n -> n.getFirstName()+" "+n.getLastName())
                .collect(Collectors.toList());
    }
    public void handleSearchBarBorder(String newValue) {
        //setez utilizatorii necunoscuti doar prima data cand folosesc search bar, dupa o modific pe parcurs
        //de aia merge mai greu prima data search bar-ul, ca se incarca userii necunoscuti
        //searchBox.toFront();
        if (usersNotFriends.isEmpty())
            usersNotFriends = FXCollections.observableArrayList(getUnknownUsers());
        if(borderPaneSearch.getChildren().size()>1){
            borderPaneSearch.getChildren().remove(1);
        }

        //borderPaneSearch.add(populateDropDownMenu(newValue),0,1);
    }

    public void handleSearchBar(String newValue) {
        //setez utilizatorii necunoscuti doar prima data cand folosesc search bar, dupa o modific pe parcurs
        //de aia merge mai greu prima data search bar-ul, ca se incarca userii necunoscuti
        //searchBox.toFront();
        if (usersNotFriends.isEmpty())
            usersNotFriends = FXCollections.observableArrayList(getUnknownUsers());
        if(container.getChildren().size()>1){
            container.getChildren().remove(1);
        }
        container.add(populateDropDownMenu(newValue),0,1);
    }

    public void handleSendRequest(){
        String[] tokens = text.getText().split(" ");
        User user;
        if (tokens.length < 2 ){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText("Insufficient details");
            alert.showAndWait();
        }
        else if (tokens.length > 3 ){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText("Too many details");
            alert.showAndWait();
        }
        else {
            try {
                if (tokens.length == 3 ){
                    tokens[0] = tokens[0] + " " + tokens[1];
                    tokens[1] = tokens[2];
                }
                user = userService.getUserByName(tokens[0], tokens[1]);
                friendshipService.requestFriendship(userId, user.getId());
                //sterg userul la care am trimis cerere din lista de useri necunoscuti
                usersNotFriends.remove(user.getFirstName() + " " + user.getLastName());
                //caut cerera pe care am trimis-o ca sa ii aflu id-ul si o adaug in cererile deja salvate in page(nu mai dau din setAll, doar o adaug pe asta)
                Friendship request = friendshipService.getFriendship(userId, user.getId());
                page.getFriendshipsRequests().add(request);
                text.clear();
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                alert1.setTitle("Success");
                alert1.setContentText("Friendship request sent!");
                alert1.showAndWait();
            } catch (UserException | FriendshipException | ValidationException ue) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setContentText(ue.getMessage());
                alert.showAndWait();
            }
        }
        text.clear();
        if(container.getChildren().size() > 1)  {
            container.getChildren().remove(1);
        }
    }

    public void handleX() {
        text.clear();
        if(container.getChildren().size() > 1)  {
            container.getChildren().remove(1);
        }
    }


    public void handleFriends() {
        this.friendshipService.removeObserver(this);
        userService.setPageable(new PageableRepoImplementation(0, 4));
        page.getFriends().clear();
        page.getFriends().addAll(userService.getFriendsOnPage(0, userId));
        FXMLLoader loader = openWindow("FriendsPageView.fxml", (Stage) logoutButton.getScene().getWindow(),"Friends" );
        FriendsPageController controller = loader.getController();
        controller.setService(user, userId, page, userService, friendshipService, messageService, eventService);
    }

    public  VBox populateDropDownMenu(String text1) {
        //aici nu mai primesc de fiecare data lista de optiune, ci lucrez pe aia salvata (userNotFriends)
        VBox dropDownMenu = new VBox();
        dropDownMenu.setBackground(new Background(new BackgroundFill(Color.WHITE, null,null)));
        dropDownMenu.setAlignment(Pos.CENTER);
        int count = 0;
        for(String option : usersNotFriends){
            if(!text1.replace(" ", "").isEmpty() && option.toUpperCase().contains(text1.toUpperCase()) && count < 4){
                Label label = new Label(option);
                count ++;
                label.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        label.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, null,null )));
                        text.setText(label.getText());
                    }
                });
                dropDownMenu.getChildren().add(label);
                //label.toFront();
            }
        }

        return dropDownMenu;
    }



    @Override
    public void update(FriendshipEvent friendshipEvent) {
        System.out.println(friendshipEvent.getData().toString() + friendshipEvent.getType());
        Long id1 = friendshipEvent.getData().getId().getLeft();
        Long id2 = friendshipEvent.getData().getId().getRight();
        User user1 = userService.getUser(id1);
        User user2 = userService.getUser(id2);
        friendshipEvent.getData().setFriend1(user1);
        friendshipEvent.getData().setFriend2(user2);
        switch (friendshipEvent.getType()) {
            case ADD -> {
                if (id2.equals(userId)) {
                    page.getFriendshipsRequests().add(friendshipEvent.getData());
                    if(!usersNotFriends.isEmpty())
                        usersNotFriends.remove(user1);
                }
            }
            case CANCEL -> {
                if (id2.equals(userId)) {
                    page.getFriendshipsRequests().remove(friendshipEvent.getData());
                    if(!usersNotFriends.isEmpty())
                        usersNotFriends.add(user1.getFirstName() + " " + user1.getLastName());
                }
            }
            case ACCEPT -> {
                if (id1.equals(userId)) {
                    page.getFriendshipsRequests().remove(friendshipEvent.getData());
                    page.getFriends().add(user2);
                    if(!usersNotFriends.isEmpty())
                        usersNotFriends.remove(user2.getFirstName() + " " + user2.getLastName());
                }
            }
            case DECLINE -> {
                if (id1.equals(userId)) {
                    page.getFriendshipsRequests().remove(friendshipEvent.getData());
                    Friendship declined = friendshipService.getFriendship(id1, id2);
                    page.getFriendshipsRequests().add(declined);
                    if(!usersNotFriends.isEmpty())
                        usersNotFriends.remove(user2.getFirstName() + " " + user2.getLastName());
                }
            }
            case REMOVE -> {
                User removed = null;
                if (id2.equals(userId))
                    removed = user1;
                else if (id1.equals(userId))
                    removed = user2;
                if (removed != null) {
                    page.getFriends().remove(removed);
                    if(!usersNotFriends.isEmpty())
                        usersNotFriends.add(removed.getFirstName() + " " + removed.getLastName());
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageController)) return false;
        PageController that = (PageController) o;
        return Objects.equals(userId, that.userId) && Objects.equals(userService, that.userService) && Objects.equals(friendshipService, that.friendshipService) && Objects.equals(messageService, that.messageService);
    }

    public void handleProfile() {
        this.friendshipService.removeObserver(this);
        FXMLLoader loader = openWindow("PageView.fxml", (Stage) logoutButton.getScene().getWindow(),"Profile" );
        ProfileController controller = loader.getController();
        controller.setService(user, userId, page, userService, friendshipService, messageService, eventService);
    }

    public void handleEvents() {
        eventService.setPageable(new PageableRepoImplementation(0, 1));
        this.friendshipService.removeObserver(this);
        FXMLLoader loader = openWindow("EventsView.fxml", (Stage) logoutButton.getScene().getWindow(),"Events" );
        EventController controller = loader.getController();
        controller.setService(user, userId, page, userService, friendshipService, messageService, eventService);
    }
}
