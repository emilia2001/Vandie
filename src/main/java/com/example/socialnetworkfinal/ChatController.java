package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.*;
import com.example.socialnetworkfinal.domain.exceptions.MessageException;
import com.example.socialnetworkfinal.repository.paging.PageableRepoImplementation;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class ChatController extends PageController {
    @FXML
    protected ListView<Message> lvChatWindow ;
    @FXML
    private TextArea messageBox;
    @FXML
    protected TableView<Conversation> conversationList;
    @FXML
    protected TableColumn<Conversation,Conversation> namesColumn;
    @FXML
    TableColumn<Conversation, Conversation> pictureColumn;
    @FXML
    Label convName;
    @FXML
    Circle convPic;

    User activeUser;
    private Message selected = null;


    ObservableList<Message> chatMessages = FXCollections.observableArrayList();//create observablelist for listview
    ObservableList<Message> currentMessages = FXCollections.observableArrayList();//create observablelist for listview
    ObservableList<Conversation> chats = FXCollections.observableArrayList();
    ObservableList<Conversation> sorted_chats = FXCollections.observableArrayList();
    @Override
    public void initialize() {
        pictureColumn.setCellFactory(param -> {
            Circle circle = new Circle();
            circle.setRadius(20);
            TableCell<Conversation, Conversation> cell = new TableCell<Conversation, Conversation>() {
                public void updateItem(Conversation conv, boolean empty) {
                    if (conv != null) {
                        circle.setVisible(true);
                        final Integer[] nrParticipants = {0};
                        conv.getUsers().forEach(u->{
                            if(!u.getId().equals(userId)) {
                                nrParticipants[0] ++;
                            }

                        });
                        if(nrParticipants[0] > 1) {
                            try {
                                circle.setFill(new ImagePattern(new Image(new FileInputStream("src/main/resources/com/example/socialnetworkfinal/images/group2.png"))));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            conv.getUsers().forEach(u -> {
                                if (!u.getId().equals(userId)) {
                                    try {
                                        circle.setFill(new ImagePattern(new Image(new FileInputStream(u.getProfilePictureUrl()))));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } else {
                        circle.setVisible(false);
                    }
                }
            };
            cell.setGraphic(circle);
            return cell;
        });
        pictureColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        namesColumn.setCellFactory(param -> {
            VBox box = new VBox();
            box.setStyle("-fx-background-color: transparent;");
            TableCell<Conversation, Conversation> cell = new TableCell<Conversation, Conversation>() {
                public void updateItem(Conversation conv, boolean empty) {
                    if (conv != null) {
                        Label name = new Label(conv.getName() + "\n");
                        name.setStyle("-fx-background-color: transparent;");
                        name.setAlignment(Pos.CENTER_LEFT);
                        name.setTextFill(Color.WHITE);
                        name.setFont(Font.font(
                                "Arial",
                                FontPosture.REGULAR,
                                Font.getDefault().getSize()
                        ));
                        Label mess = new Label(conv.getLastMessage());
                        mess.setStyle("-fx-background-color: transparent;");
                        mess.setAlignment(Pos.CENTER_LEFT);
                        mess.setFont(Font.font(
                                "Arial",
                                FontPosture.ITALIC,
                                Font.getDefault().getSize() - 1
                        ));
                        mess.setTextFill(Color.web("#dadce0"));
                        box.getChildren().clear();
                        box.getChildren().addAll(name, mess);
                    }
                    else
                        box.getChildren().clear();
                }
            };
            cell.setGraphic(box);
            return cell;
        });
        namesColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        conversationList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> prepareConversations()) );
        lvChatWindow.setOnMouseClicked(x -> handleSelectedMessage());
        convName.setAlignment(Pos.CENTER);
        Label holder2 = new Label("You have no conversations");
        holder2.setTextFill(Color.WHITE);
        conversationList.setPlaceholder(holder2);
        conversationList.setRowFactory(tableView -> {
            final TableRow<Conversation> row = new TableRow<>();

            row.hoverProperty().addListener((observable) -> {
                final Conversation conv = row.getItem();

                if (row.isHover() && conv != null) {
                    row.setTooltip(new Tooltip(conv.getName()));
                }
            });

            return row;
        });
        messageBox.textProperty().addListener((observable, oldValue, newValue) -> {
            Message message = lvChatWindow.getSelectionModel().getSelectedItem();
            lvChatWindow.getSelectionModel().select(message);
        });
        text.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchBar(newValue);});
    }

    @Override
    public void setService(User user, Long id, Page page, UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        System.out.println("in chat set service" + user);
        logoutButton.getScene().getStylesheets().add("com/example/socialnetworkfinal/Style.css");
        super.setService(user, id, page, userService, friendshipService, messageService, eventService);
        chatMessages.setAll(page.getMessages());
        initializeChatsVariants();
        //logoutButton.getScene().getStygit checkoutlesheets().add(getClass().getResource("Style.css").toExternalForm());
    }


    private void handleSelectedMessage() {
        Message msg = lvChatWindow.getSelectionModel().getSelectedItem();
        System.out.println(msg);
        if(this.selected == null || !msg.equals(this.selected))
            this.selected = msg;
        else{
            this.selected = null;
            lvChatWindow.getSelectionModel().clearSelection();
            System.out.println(lvChatWindow.getSelectionModel().getSelectedItems());
        }
    }

    public void initializeChatsVariants(){
        //aici gasesc toate chaturile osbile existenta ale userului curent
        for (Message message : chatMessages){
            List<User> participants = new ArrayList<>();
            participants.addAll(message.getTo());
            participants.add(message.getFrom());
            Collections.sort(participants, new UserIdComparator());
            if(!chats.stream().anyMatch(x -> x.getUsers().equals(participants))){
                String name = "";
                for (User user : participants) {
                    if (user.getId() != userId) {
                        if(name.equals(""))
                            name = name + user.getFirstName() ;
                        else
                            name += ", " + user.getFirstName() ;
                    }
                }
//
                //List<Long> idParticipants = participants.stream().map( u -> u.getId()).collect(Collectors.toList());
                Conversation conversation = new Conversation(name, participants);
                conversation.setLastMessageDate(message.getDate());
                conversation.setLastMessage(message.getMessage());
                chats.add(conversation);

            }
            else{
                Conversation conversation = chats.stream().filter(x -> x.getUsers().equals(participants)).collect(Collectors.toList()).get(0);

                if (message.getDate().isAfter(conversation.getLastMessageDate())) {
                    conversation.setLastMessageDate(message.getDate());
                    conversation.setLastMessage(message.getMessage());
                }

            }
        }

        sorted_chats.setAll(chats.stream().sorted(new Comparator<Conversation>() {
                            @Override
                            public int compare(Conversation o1, Conversation o2) {
                                return
                                        o2.getLastMessageDate().compareTo(o1.getLastMessageDate());
                            }
                        })
                        .toList());

        conversationList.setItems(sorted_chats);
    }

    public List<Message> getGroupConversation(List<Long> ids){
        //functia fin service dar pt lista noastra
        List<Message> result = chatMessages.stream()
                .filter(m -> (ids.contains(m.getFrom().getId())))
                .filter(m -> m.getTo().size()==ids.size() - 1)
                .filter(m -> (m.getTo().stream().allMatch(x -> ids.contains(x.getId()))))
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toList());
        return result;
    }

    public static String insertString(
            String originalString,
            String stringToBeInserted,
            int index)
    {

        String newString = originalString.substring(0, index + 1)
                + stringToBeInserted
                + originalString.substring(index + 1);

        return newString;
    }
    public static String insertEndLine(String string, int index){
        String newString = string;
        if(string.length() > index)
            for(int i = index; i< string.length(); i=i + index)
                newString = insertString(newString,"\n",i);
        return  newString;
    }

    public void prepareConversations() {
        //aici initializez ca la clickul pe tabela chat urilor sa se deschida conversatiile respective
        Conversation conv = conversationList.getSelectionModel().getSelectedItems().get(0);
                List<Message> messages = getGroupConversation(conv.getUsers().stream().map(u -> u.getId()).collect(Collectors.toList()));
                currentMessages.remove(0,currentMessages.size());
                messages.forEach(x -> currentMessages.add(x));
                lvChatWindow.setItems(currentMessages);
                lvChatWindow.setCellFactory(param -> {
                    ListCell<Message> cell = new ListCell<Message>(){
                        Label lblUserLeft = new Label();
                        Label lblTextLeft = new Label();
                        VBox hBoxLeft = new VBox(lblUserLeft, lblTextLeft);
                        Label lblUserRight = new Label();
                        Label lblTextRight = new Label();
                        VBox hBoxRight = new VBox(lblUserRight,lblTextRight);
                        Label lblUserReplyLeft = new Label();
                        Label lblTextReplyLeft = new Label();
                        VBox hBoxReplyLeft = new VBox(lblUserReplyLeft, lblTextReplyLeft);
                        VBox replyBoxLeft = new VBox(hBoxReplyLeft, hBoxLeft);
                        Label lblUserReplyRight = new Label();
                        Label lblTextReplyRight = new Label();
                        VBox hBoxReplyRight = new VBox(lblUserReplyRight, lblTextReplyRight);
                        VBox replyBoxRight = new VBox(hBoxReplyRight, hBoxRight);

                        {

                            lvChatWindow.getStyleClass().add("chat-hbox");
                            hBoxLeft.getStyleClass().add("chat-hbox");


                            hBoxLeft.setAlignment(Pos.CENTER_LEFT);
                            hBoxLeft.setSpacing(5);
                            hBoxRight.setAlignment(Pos.CENTER_RIGHT);


                            hBoxRight.setSpacing(5);
                            hBoxRight.getStyleClass().add("chat-hbox");
                            hBoxReplyRight.setAlignment(Pos.CENTER_RIGHT);
                            hBoxReplyRight.setSpacing(5);
                            hBoxReplyRight.getStyleClass().add("chat-hbox");
                            hBoxReplyLeft.setAlignment(Pos.CENTER_LEFT);
                            hBoxReplyLeft.setSpacing(5);
                            hBoxReplyLeft.getStyleClass().add("chat-hbox");
                            hBoxReplyLeft.getStyleClass().add("chat-hbox");
                            replyBoxLeft.setAlignment(Pos.CENTER_LEFT);
                            replyBoxLeft.setSpacing(5);

                            replyBoxRight.setAlignment(Pos.CENTER_RIGHT);
                            replyBoxRight.setSpacing(5);

                        }
                        @Override
                        protected void updateItem(Message item, boolean empty) {
                            super.updateItem(item, empty);

                            if(empty)
                            {
                                setText(null);
                                setGraphic(null);
                            }
                            else{
                                if(item.getFrom().getId()!= userId)
                                {
                                    if(item.getReply() == null) {
                                        lblTextLeft.getStyleClass().add("chat-bubble-left");
                                        lblUserLeft.setText(item.getFrom().getFirstName() + ": ");
                                        lblTextLeft.setText(item.getMessage());
                                        setGraphic(hBoxLeft);
                                    }
                                    else {
                                        lblUserReplyLeft.setText("↪" + item.getReply().getFrom().getFirstName() + ":");
                                        lblTextReplyLeft.setText(item.getReply().getMessage());
                                        lblTextReplyLeft.getStyleClass().add("chat-bubble-reply");
                                        setGraphic(hBoxReplyLeft);
                                        lblUserLeft.setText(item.getFrom().getFirstName() + ":");
                                        lblTextLeft.setText(item.getMessage());
                                        lblTextLeft.getStyleClass().add("chat-bubble-left");
                                        setGraphic(hBoxLeft);
                                        replyBoxLeft = new VBox(hBoxReplyLeft, hBoxLeft);
                                        setGraphic(replyBoxLeft);
                                    }
                                }
                                else{
                                    if(item.getReply() == null){
                                        lblTextRight.getStyleClass().add("chat-bubble-right");
                                        lblUserRight.setText(item.getFrom().getFirstName() + ":");
                                        lblTextRight.setText(item.getMessage());

                                        setGraphic(hBoxRight);
                                    }
                                    else {

                                        lblUserReplyRight.setText("↪" +item.getReply().getFrom().getFirstName() + ":");
                                        lblTextReplyRight.setText(item.getReply().getMessage());
                                        lblTextReplyRight.getStyleClass().add("chat-bubble-reply");
                                        setGraphic(hBoxReplyRight);
                                        lblUserRight.setText(item.getFrom().getFirstName() + ":");
                                        lblTextRight.setText(item.getMessage());
                                        lblTextRight.getStyleClass().add("chat-bubble-right");
                                        setGraphic(hBoxRight);
                                        replyBoxRight = new VBox(hBoxReplyRight, hBoxRight);
                                        setGraphic(replyBoxRight);
                                    }
                                }
                            }
                        }

                    };

                    return cell;
                });
        final String[] name = {""};
        final Integer[] nrParticipants = {0};
        conv.getUsers().forEach(u->{
            if(!u.getId().equals(userId)) {
                name[0] += u.getFirstName() + " " + u.getLastName() + "\n";
                nrParticipants[0] ++;
            }

        });
        String desc = "";
        if(nrParticipants[0] > 1) {
            try {
                convPic.setFill(new ImagePattern(new Image(new FileInputStream("src/main/resources/com/example/socialnetworkfinal/images/group2.png"))));
                desc = "   Group members\n";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            conv.getUsers().forEach(u -> {
                if (!u.getId().equals(userId)) {
                    try {
                        convPic.setFill(new ImagePattern(new Image(new FileInputStream(u.getProfilePictureUrl()))));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            desc = "   Private conversation\n";
        }
        desc += name[0];
        convName.setText(desc);
        Platform.runLater(() -> lvChatWindow.scrollTo(currentMessages.size() - 1));

    }

    @Override
    public FXMLLoader openWindow(String location, Stage stage, String windowName) {
        stage.show();
        return super.openWindow(location, stage, windowName);
    }

    public void handleCompose() {
        FXMLLoader loader = openWindow("ComposeMessageView.fxml", new Stage(), "Compose");
        ComposeMessageController controller = loader.getController();
        if(activeUser == null)
            activeUser = userService.getUserByName(page.getFirstName(), page.getLastName());
        System.out.println(activeUser);
        controller.setService(userId, page, userService, friendshipService, messageService, eventService, activeUser, chats, currentMessages, conversationList, lvChatWindow);
    }

    public void handleSendMessage() {
        String text = messageBox.getText();
        if(!text.isEmpty()) {
            messageBox.clear();
            if( activeUser == null)
                activeUser = userService.getUser(userId);
            Conversation conv = (Conversation) conversationList.getSelectionModel().getSelectedItem();
            List<User> to = conv.getUsers().stream()
                            .filter(u -> !u.getId().equals(userId))
                            .collect(Collectors.toList());
            if(this.selected == null)
                messageService.sendMessage(activeUser, to, text);
            else {
                try {
                    messageService.replyToMessage(activeUser, text, selected.getId());
                    this.selected = null;
                    lvChatWindow.getSelectionModel().clearSelection(0);
                } catch (MessageException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
            Message message = messageService.getLastMessageSent(userId);
            page.getMessages().add(message);
            conv.setLastMessageDate(message.getDate());
            conv.setLastMessage(message.getMessage());
            currentMessages.add(message);
            chatMessages.add(message);
            Platform.runLater(() -> lvChatWindow.scrollTo(currentMessages.size() - 1));
            sorted_chats.setAll(chats.stream().sorted(new Comparator<Conversation>() {
                        @Override
                        public int compare(Conversation o1, Conversation o2) {
                            return
                                    o2.getLastMessageDate().compareTo(o1.getLastMessageDate());
                        }
                    })
                    .toList());
            conversationList.setItems(sorted_chats);
        }
    }

}

