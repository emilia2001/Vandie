package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.Page;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.repository.paging.PageableRepoImplementation;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import com.example.socialnetworkfinal.utils.observer.FriendshipEvent;
import com.example.socialnetworkfinal.utils.observer.Observer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FriendsPageController extends PageController implements Observer<FriendshipEvent>{
    ObservableList<User> friends = FXCollections.observableArrayList();

    @FXML
    TableColumn<User, User> pictureColumn;
    @FXML
    TableColumn<User, String> emailColumn;
    @FXML
    TableColumn<User, String> lastNameColumn;
    @FXML
    TableColumn<User, String> firstNameColumn;
    @FXML
    TableColumn<User, User> deleteColumn;
    @FXML
    TableView<User> friendsTable;
    @FXML
    Button nextButton;
    @FXML
    Button prevButton;
    @FXML
    Label pageNrLabel;

    @FXML
    public void initialize() {
        pictureColumn.setCellFactory(param -> {
            Circle circle = new Circle();
            circle.setRadius(25);
            TableCell<User, User> cell = new TableCell<User, User>() {
                public void updateItem(User item, boolean empty) {
                    if (item != null) {
                        try {
                            circle.setVisible(true);
                            circle.setFill( new ImagePattern(new Image(new FileInputStream(item.getProfilePictureUrl()))));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else
                        circle.setVisible(false);
                }
            };
            cell.setGraphic(circle);
            return cell;
        });
        pictureColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        emailColumn.setCellValueFactory(new PropertyValueFactory<User, String>("email"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        deleteColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        deleteColumn.setCellFactory(param -> new TableCell<User, User>(){
            private final Button deleteButton = new Button();

            @Override
            protected void updateItem(User user, boolean empty) {
                deleteButton.setId("deleteButton");
                super.updateItem(user, empty);

                if (user == null) {
                    setGraphic(null);
                    return;
                }

                setGraphic(deleteButton);
                deleteButton.setOnAction(event -> handleDeleteFriend(user));
            }
        });
        friendsTable.setItems(friends);
        Label holder = new Label("You have no friends");
        holder.setTextFill(Color.WHITE);
        friendsTable.setPlaceholder(holder);
        text.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchBar(newValue);
        });
        prevButton.setVisible(false);
        pageNrLabel.setText("1");
    }

    public void setService(User user, Long id, Page page, UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        super.setService(user, id, page, userService, friendshipService, messageService, eventService);
        userService.setPageable(new PageableRepoImplementation(0, 4));
        List<User> friends = page.getFriends();
        if( !friends.isEmpty() )
            this.friends.setAll(friends);
        if(userService.getNextFriends(userId).isEmpty()) {
            nextButton.setVisible(false);
        } else
            userService.getPrevFriends(userId);
    }

    public void handleDeleteFriend(User selected) {
//        User selected = (User) friendsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "\tAre you sure you want do remove " + selected.getLastName() + " " + selected.getFirstName() + " \nfrom your friend list?",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle("Remove friend");
            alert.showAndWait();
            if(alert.getResult().equals(ButtonType.YES)) {
                friendshipService.removeFriendship(userId, selected.getId());
                //sterg prietenul si din lista de prieteni din page, si din lista observata de tabel
                page.getFriends().clear();
                page.getFriends().addAll(userService.getFriendsOnPage(0, userId));
                pageNrLabel.setText("1");
                prevButton.setVisible(false);
                friends.setAll(page.getFriends());
                if(userService.getNextFriends(userId).isEmpty()) {
                    nextButton.setVisible(false);
                } else{
                    userService.getPrevFriends(userId);
                    nextButton.setVisible(true);
                }

                //il adaug in lista de necunoscuti
                usersNotFriends.add(selected.getFirstName() + " " + selected.getLastName());
        }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText("No friend selected!");
            alert.showAndWait();
        }
    }

    public void handleFriendRequests() {
        this.friendshipService.removeObserver(this);
        FXMLLoader loader = openWindow("FriendRequestsPageView.fxml", (Stage) logoutButton.getScene().getWindow(),"FriendRequests" );
        FriendRequestsPageController controller = loader.getController();
        controller.setService(user, userId, page, userService, friendshipService, messageService, eventService);
    }

    @Override
    public void update(FriendshipEvent friendshipEvent) {
        Long id1 = friendshipEvent.getData().getId().getLeft();
        Long id2 = friendshipEvent.getData().getId().getRight();
        if(id1.equals(userId) || id2.equals(userId)){
            super.update(friendshipEvent);
            friends.setAll(page.getFriends());
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendsPageController)) return false;
        if (!super.equals(o)) return false;
        FriendsPageController that = (FriendsPageController) o;
        return Objects.equals(userId, that.userId) && Objects.equals(userService, that.userService) && Objects.equals(friendshipService, that.friendshipService) && Objects.equals(messageService, that.messageService);
    }


    public void getNextFriends() {
        prevButton.setVisible(true);
        pageNrLabel.setText(String.valueOf(Long.valueOf(pageNrLabel.getText()) + 1));
        Set<User> nextFriends = userService.getNextFriends(userId);
        page.getFriends().clear();
        page.getFriends().addAll(nextFriends);
        System.out.println(page.getFriends());
        friends.setAll(page.getFriends());
        if(userService.getNextFriends(userId).isEmpty()) {
            nextButton.setVisible(false);
        } else
            userService.getPrevFriends(userId);
    }

    public void getPrevFriends() {
        nextButton.setVisible(true);
        pageNrLabel.setText(String.valueOf(Long.valueOf(pageNrLabel.getText()) - 1));
        Set<User> nextFriends = userService.getPrevFriends(userId);
        page.getFriends().clear();
        page.getFriends().addAll(nextFriends);
        System.out.println(page.getFriends());
        friends.setAll(page.getFriends());
        if(userService.getPrevFriends(userId).isEmpty()) {
            prevButton.setVisible(false);
        } else
            userService.getNextFriends(userId);
    }
}
