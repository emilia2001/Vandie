package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.*;
import com.example.socialnetworkfinal.domain.exceptions.MessageException;
import com.example.socialnetworkfinal.domain.exceptions.ValidationException;
import com.example.socialnetworkfinal.repository.paging.PageableRepoImplementation;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ComposeMessageController extends ChatController{
    private List<User> to = new ArrayList<>();
    private List<User> possibleRecipients = new ArrayList<>();
    ObservableList<User> friends = FXCollections.observableArrayList();
    @FXML
    protected ListView<Message> lvChatWindow = new ListView<>();
    @FXML
    TextArea textArea;
    @FXML
    TextField toField;
    @FXML
    TableView<User> friendsList;
    @FXML
    TableColumn<User, String> nameColumn;
    @FXML
    TableColumn<User, User> pictureColumn;
    @FXML
    TextField searchFriend;
    @FXML
    Label error;

    @Override
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("fullName"));
        pictureColumn.setCellFactory(param -> {
            Circle circle = new Circle();
            circle.setRadius(25);
            TableCell<User, User> cell = new TableCell<User, User>() {
                public void updateItem(User item, boolean empty) {
                    System.out.println(item);
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
        friendsList.setItems(friends);
        Label holder = new Label("You have no friends");
        holder.setTextFill(Color.WHITE);
        friendsList.setPlaceholder(holder);
        searchFriend.textProperty().addListener(x -> handleSearchFriend());
        friendsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> handleAddTo()) );
    }

    public void setService(Long id, Page page, UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService, User activeUser, ObservableList<Conversation> chats, ObservableList<Message> currentMessages, TableView<Conversation> conversationList, ListView<Message> lvChatWindow) {

        this.userId = id;
        this.page = page;
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.eventService = eventService;
        page.getFriends().clear();
        page.getFriends().addAll(userService.getAllFriends(userId));
        page.getFriends().forEach(possibleRecipients::add);
        friends.setAll(possibleRecipients);
        this.activeUser = activeUser;
        this.chats = chats;
        this.currentMessages = currentMessages;
        this.conversationList = conversationList;
        this.lvChatWindow = lvChatWindow;
        System.out.println((Stage)lvChatWindow.getScene().getWindow());
      }



    public void handleCreateConversation() {
        String text = textArea.getText();
        List<User> users = new ArrayList<>();
        users.addAll(to);
        users.add(activeUser);
        final String[] name = {""};
        to.forEach(u -> name[0] += u.getFirstName() + " " + u.getLastName() + " ");
        if(!text.isEmpty()) {
            Conversation conv = new Conversation(name[0], users);
            activeUser.setFriends(page.getFriends());
            try {
                messageService.sendMessage(activeUser, to, text);
                Message message = messageService.getLastMessageSent(userId);
                page.getMessages().add(message);
                Stage stage = (Stage) textArea.getScene().getWindow();
                stage.close();
                FXMLLoader loginLoader = openWindow("ChatView.fxml", (Stage)lvChatWindow.getScene().getWindow(),"Login" );
                ChatController chatController = loginLoader.getController();
                System.out.println("compose" + user);
                chatController.setService(activeUser, userId, page,userService, friendshipService, messageService, eventService);
                conversationList.getSelectionModel().clearAndSelect(chats.size() - 1);
            } catch (MessageException | ValidationException e) {
                error.setText(e.getMessage());
            }
            if(!chats.contains(conv))
                chats.add(conv);


        }
    }

    public void handleClear(ActionEvent actionEvent) {
        toField.clear();
        possibleRecipients.addAll(to);
        to.clear();
        friends.setAll(possibleRecipients);
    }

    public void handleSearchFriend() {
        Predicate<String> nameFilter = n -> n.toLowerCase().contains(searchFriend.getText().toLowerCase());
        Function<User, String> userDTO = u -> u.getFirstName() + " " + u.getLastName();
        friends.setAll(possibleRecipients.stream()
                .filter(f -> nameFilter.test(userDTO.apply(f)))
                .collect(Collectors.toList()));
    }

    public void handleAddTo() {
        User selected = friendsList.getSelectionModel().getSelectedItem();
        friendsList.getSelectionModel().clearSelection();
        if(selected != null) {
            to.add(selected);
            if(!toField.getText().isEmpty())
                toField.appendText(", ");
            toField.appendText(selected.toString());
            possibleRecipients.remove(selected);
            friends.setAll(possibleRecipients);
        }
    }

}
