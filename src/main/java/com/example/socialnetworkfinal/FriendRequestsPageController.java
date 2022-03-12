package com.example.socialnetworkfinal;


import com.example.socialnetworkfinal.domain.DTOFriendship;
import com.example.socialnetworkfinal.domain.Friendship;
import com.example.socialnetworkfinal.domain.Page;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.exceptions.FriendshipException;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FriendRequestsPageController extends PageController implements Observer<FriendshipEvent> {
    ObservableList<DTOFriendship> requests = FXCollections.observableArrayList();

    @FXML
    TableColumn<DTOFriendship, DTOFriendship> pictureColumn;
    @FXML
    TableColumn<DTOFriendship, String> lastNameColumn;
    @FXML
    TableColumn<DTOFriendship, String> firstNameColumn;
    @FXML
    TableColumn<DTOFriendship, Date> dateColumn;
    @FXML
    TableColumn<DTOFriendship, String> stateColumn;
    @FXML
    TableColumn<DTOFriendship, DTOFriendship> acceptColumn;
    @FXML
    TableColumn<DTOFriendship, DTOFriendship> declineColumn;
    @FXML
    TableColumn<DTOFriendship, DTOFriendship> cancelColumn;
    @FXML
    Pane centralPane;
    @FXML
    Button nextButton;
    @FXML
    Button prevButton;
    @FXML
    Label pageNrLabel;

    @FXML
    TableView<DTOFriendship> requestsReceivedTable;
    @FXML
    TableView<DTOFriendship> requestsSentTable;
    @FXML
    ComboBox<String> comboBoxRequestType;

    @FXML
    public void initialize() {
        pictureColumn = new TableColumn<>();
        pictureColumn.setCellFactory(param -> {
            Circle circle = new Circle();
            circle.setRadius(20);
            TableCell<DTOFriendship, DTOFriendship> cell = new TableCell<DTOFriendship, DTOFriendship>() {
                public void updateItem(DTOFriendship item, boolean empty) {
                    if (item != null) {
                        try {
                            circle.setVisible(true);
                            circle.setFill( new ImagePattern(new Image(new FileInputStream(item.getPicture_url()))));
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
        pictureColumn.setPrefWidth(50);
        pictureColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<DTOFriendship, String>("lastName"));
        firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<DTOFriendship, String>("firstName"));
        dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<DTOFriendship, Date>("date"));
        stateColumn = new TableColumn<>("State");
        stateColumn.setCellValueFactory(new PropertyValueFactory<DTOFriendship, String>("state"));
        acceptColumn = new TableColumn<>();
        acceptColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
//        acceptColumn.setMaxWidth(50);
        acceptColumn.setCellFactory(param -> new TableCell<DTOFriendship, DTOFriendship>(){
            private final Button acceptButton = new Button();

            @Override
            protected void updateItem(DTOFriendship item, boolean empty) {
                acceptButton.setId("acceptButton");
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(acceptButton);
                acceptButton.setOnAction(event -> handleAccept(item));
                if(item.getState().equals("rejected"))
                    acceptButton.setVisible(false);
                else
                    acceptButton.setVisible(true);
            }
        });
        acceptColumn.setPrefWidth(40);
        declineColumn = new TableColumn<>();
        declineColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        declineColumn.setPrefWidth(40);
        declineColumn.setCellFactory(param -> new TableCell<DTOFriendship, DTOFriendship>(){
            private final Button declineButton = new Button();

            @Override
            protected void updateItem(DTOFriendship item, boolean empty) {
                declineButton.setId("declineButton");
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(declineButton);
                declineButton.setOnAction(event -> handleDecline(item));
                if(item.getState().equals("rejected"))
                    declineButton.setVisible(false);
                else
                    declineButton.setVisible(true);
            }
        });
        cancelColumn = new TableColumn<>();
        cancelColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        cancelColumn.setCellFactory(param -> new TableCell<DTOFriendship, DTOFriendship>(){
            private final Button cancelButton = new Button();

            @Override
            protected void updateItem(DTOFriendship item, boolean empty) {
                cancelButton.setId("cancelButton");
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(cancelButton);
                cancelButton.setOnAction(event -> handleCancelRequest(item));
                if(item.getState().equals("rejected"))
                    cancelButton.setVisible(false);
                else
                    cancelButton.setVisible(true);
            }
        });
        cancelColumn.setPrefWidth(40);
        firstNameColumn.setPrefWidth(80);
        lastNameColumn.setPrefWidth(80);
        stateColumn.setPrefWidth(60);
        dateColumn.setPrefWidth(80);

        requestsReceivedTable = new TableView<>();
        requestsReceivedTable.setId("receivedTable");
        Label holder = new Label("You have no received requests");
        holder.setTextFill(Color.WHITE);
        requestsReceivedTable.setPlaceholder(holder);
        requestsReceivedTable.setLayoutX(20);
        requestsReceivedTable.setLayoutY(100);
        requestsReceivedTable.setPrefSize(450, 220);
        requestsReceivedTable.getColumns().addAll(pictureColumn, firstNameColumn, lastNameColumn, stateColumn, dateColumn, acceptColumn, declineColumn);
        centralPane.getChildren().add(requestsReceivedTable);
        requestsReceivedTable.setItems(requests);

        requestsSentTable = new TableView<>();
        Label holder2 = new Label("You have no sent requests");
        holder2.setTextFill(Color.WHITE);
        requestsSentTable.setPlaceholder(holder2);
        requestsSentTable.setLayoutX(20);
        requestsSentTable.setLayoutY(100);
        requestsSentTable.setPrefSize(400, 220);
        requestsSentTable.getColumns().addAll(pictureColumn, firstNameColumn, lastNameColumn, stateColumn, dateColumn, cancelColumn);
        centralPane.getChildren().add(requestsSentTable);
        requestsSentTable.setItems(requests);

        comboBoxRequestType.getSelectionModel().selectedItemProperty().addListener((x, y, z) -> handleFilter());
        text.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchBar(newValue);
        });
        prevButton.setVisible(false);
        pageNrLabel.setText("1");
    }

    public void setService(User user, Long id, Page page, UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        super.setService(user, id, page, userService, friendshipService, messageService, eventService);
        friendshipService.setPageable(new PageableRepoImplementation(0, 4));
        friendshipService.setPageSize(4);
        page.getFriendshipsRequests().clear();
        page.getFriendshipsRequests().addAll(friendshipService.getReceivedOnPage(0, userId));
        List<DTOFriendship> requests = getRequests();
        if( !requests.isEmpty() )
            this.requests.setAll(requests);
        comboBoxRequestType.getItems().setAll(new ArrayList<>(List.of("Received", "Sent")));
        comboBoxRequestType.getSelectionModel().selectFirst();
        if(friendshipService.getNextReceived(userId).isEmpty()) {
            nextButton.setVisible(false);
        } else {
            friendshipService.getPrevReceived(userId);
            nextButton.setVisible(true);
        }
    }

    public List<DTOFriendship> getRequests() {
        return page.getFriendshipsRequests()
                .stream()
                .map(f -> new DTOFriendship(f.getFriend1().getId().equals(userId) ? f.getFriend2().getLastName() : f.getFriend1().getLastName(),
                        f.getFriend1().getId().equals(userId) ? f.getFriend2().getFirstName() : f.getFriend1().getFirstName(),
                        f.getDate(),
                        f.getState(),
                        f.getFriend1().getId().equals(userId) ? f.getFriend2().getProfilePictureUrl() : f.getFriend1().getProfilePictureUrl()))
                .collect(Collectors.toList());
    }

    private void handleFilter() {
        prevButton.setVisible(false);
        String requestType = comboBoxRequestType.getSelectionModel().getSelectedItem();
        switch (requestType) {
            case "Received" -> handleReceived();
            case "Sent" -> handleSent();
        }
    }

    public void handleReceived() {
        if(friendshipService.getNextReceived(userId).isEmpty()) {
            nextButton.setVisible(false);
        } else{
            friendshipService.getPrevReceived(userId);
            nextButton.setVisible(true);
        }
        requestsSentTable.setVisible(false);
        requestsReceivedTable.setVisible(true);
        page.getFriendshipsRequests().clear();
        page.getFriendshipsRequests().addAll(friendshipService.getReceivedOnPage(0, userId));
        requests.setAll(getReceived());
    }

    public void handleSent() {
        friendshipService.setPageable(new PageableRepoImplementation(0, 4));
        if(friendshipService.getNextSent(userId).isEmpty()) {
            nextButton.setVisible(false);
        } else {
            friendshipService.getPrevSent(userId);
            nextButton.setVisible(true);
        }
        requestsReceivedTable.setVisible(false);
        requestsSentTable.setVisible(true);
        page.getFriendshipsRequests().clear();
        page.getFriendshipsRequests().addAll(friendshipService.getSentOnPage(0, userId));
        requests.setAll(getSent());
    }

    public List<DTOFriendship> getReceived() {
        List<DTOFriendship> requests = page.getFriendshipsRequests()
                .stream()
                //.filter(x -> x.getId().getRight().equals(userId))
                .map(f -> new DTOFriendship(f.getFriend1().getLastName(),
                        f.getFriend1().getFirstName(),
                        f.getDate(),
                        f.getState(),
                        f.getFriend1().getId().equals(userId) ? f.getFriend2().getProfilePictureUrl() : f.getFriend1().getProfilePictureUrl()))
                .collect(Collectors.toList());
        return requests;
    }

    public List<DTOFriendship> getSent() {
        List<DTOFriendship> requests = page.getFriendshipsRequests()
                .stream()
                //.filter(x -> x.getId().getLeft().equals(userId))
                .map(f -> new DTOFriendship(f.getFriend2().getLastName(),
                        f.getFriend2().getFirstName(),
                        f.getDate(),
                        f.getState(),
                        f.getFriend1().getId().equals(userId) ? f.getFriend2().getProfilePictureUrl() : f.getFriend1().getProfilePictureUrl()))
                .collect(Collectors.toList());
        return requests;

    }

    public void handleAccept(DTOFriendship selected){
        if (selected != null) {
            try {
                User userToAccept = userService.getUserByName(selected.getFirstName(), selected.getLastName());
                friendshipService.acceptFriendship(new Friendship(userToAccept.getId(),userId,  "approved"));
                //sterg utilizatorul pe care l-am adaugat din lista de useri potentiali sa-i adaug
                usersNotFriends.remove(userToAccept.getFirstName() + " " + userToAccept.getLastName());
                //adaug prietenul in lista de prieteni(care e deja salvata in Page)
                page.getFriends().add(userToAccept);
                //sterg cerera din cereri(deja salvate in Page)
                page.getFriendshipsRequests().clear();
                page.getFriendshipsRequests().addAll(friendshipService.getReceivedOnPage(0, userId));
                pageNrLabel.setText("1");
                prevButton.setVisible(false);
                if(friendshipService.getNextReceived(userId).isEmpty()) {
                    nextButton.setVisible(false);
                } else{
                    friendshipService.getPrevReceived(userId);
                    nextButton.setVisible(true);
                }
                requests.setAll(getReceived());
            }catch(FriendshipException fe){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setContentText(fe.getMessage());
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText("No friend selected!");
            alert.showAndWait();
        }

    }

    public void handleDecline(DTOFriendship selected){
        if (selected != null) {
            try {
                User userToDecline = userService.getUserByName(selected.getFirstName(), selected.getLastName());
                friendshipService.declineFriendship(new Friendship( userToDecline.getId(),userId, "rejected"));
                usersNotFriends.remove(userToDecline.getFirstName() + " " + userToDecline.getLastName());
                comboBoxRequestType.getSelectionModel().selectNext();
                comboBoxRequestType.getSelectionModel().selectFirst();
                page.getFriendshipsRequests().clear();
                page.getFriendshipsRequests().addAll(friendshipService.getReceivedOnPage(0, userId));
                pageNrLabel.setText("1");
                prevButton.setVisible(false);
                if(friendshipService.getNextReceived(userId).isEmpty()) {
                    nextButton.setVisible(false);
                } else{
                    friendshipService.getPrevReceived(userId);
                    nextButton.setVisible(true);
                }
                requests.setAll(getReceived());
            }catch(FriendshipException fe){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setContentText(fe.getMessage());
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText("No friend selected!");
            alert.showAndWait();
        }
    }

    @Override
    public void handleSendRequest() {
        super.handleSendRequest();
        if (comboBoxRequestType.getSelectionModel().getSelectedItem().equals("Sent")) {
            page.getFriendshipsRequests().clear();
            page.getFriendshipsRequests().addAll(friendshipService.getSentOnPage(0, userId));
            pageNrLabel.setText("1");
            prevButton.setVisible(false);
            if (friendshipService.getNextSent(userId).isEmpty()) {
                nextButton.setVisible(false);
            } else {
                friendshipService.getPrevSent(userId);
                nextButton.setVisible(true);
            }
            requests.setAll(getSent());
        }
    }

    public void handleCancelRequest(DTOFriendship selected) {
        try {
            User userToCancel = userService.getUserByName(selected.getFirstName(), selected.getLastName());
            friendshipService.cancelFriendsip(userId, userToCancel.getId());
            usersNotFriends.add(userToCancel.getFirstName() + " " + userToCancel.getLastName());
            page.getFriendshipsRequests().clear();
            page.getFriendshipsRequests().addAll(friendshipService.getSentOnPage(0, userId));
            pageNrLabel.setText("1");
            prevButton.setVisible(false);
            if(friendshipService.getNextSent(userId).isEmpty()) {
                nextButton.setVisible(false);
            } else{
                friendshipService.getPrevSent(userId);
                nextButton.setVisible(true);
            }
            requests.setAll(getSent());
        }catch(FriendshipException fe){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText(fe.getMessage());
            alert.showAndWait();
        }
    }

    @Override
    public void update(FriendshipEvent friendshipEvent) {
        Long id1 = friendshipEvent.getData().getId().getLeft();
        Long id2 = friendshipEvent.getData().getId().getRight();
        if(id1.equals(userId) || id2.equals(userId)) {
            super.update(friendshipEvent);
            if(comboBoxRequestType.getSelectionModel().getSelectedItem().equals("Sent"))
                requests.setAll(getSent());
            else
                requests.setAll(getReceived());
        }
    }

    public void getNextRequests() {
        prevButton.setVisible(true);
        if(comboBoxRequestType.getSelectionModel().getSelectedItem().equals("Received")) {
            page.getFriendshipsRequests().clear();
            page.getFriendshipsRequests().addAll(friendshipService.getNextReceived(userId));
            pageNrLabel.setText(String.valueOf(Long.valueOf(pageNrLabel.getText()) + 1));
            requests.setAll(getReceived());
            visibilityNextReceived();
        } else {
            page.getFriendshipsRequests().clear();
            page.getFriendshipsRequests().addAll(friendshipService.getNextSent(userId));
            pageNrLabel.setText(String.valueOf(Long.valueOf(pageNrLabel.getText()) + 1));
            requests.setAll(getSent());
            visibilityNextSent();
        }
    }

    public void getPrevRequests() {
        nextButton.setVisible(true);
        if(comboBoxRequestType.getSelectionModel().getSelectedItem().equals("Received")) {
            page.getFriendshipsRequests().clear();
            page.getFriendshipsRequests().addAll(friendshipService.getPrevReceived(userId));
            pageNrLabel.setText(String.valueOf(Long.valueOf(pageNrLabel.getText()) - 1));
            requests.setAll(getReceived());
            visibilityPrevReceived();
        } else {
            page.getFriendshipsRequests().clear();
            page.getFriendshipsRequests().addAll(friendshipService.getPrevSent(userId));
            pageNrLabel.setText(String.valueOf(Long.valueOf(pageNrLabel.getText()) - 1));
            requests.setAll(getSent());
            visibilityPrevSent();
        }
    }

    public void visibilityNextReceived() {
        if(friendshipService.getNextReceived(userId).isEmpty()) {
            nextButton.setVisible(false);
        } else
            friendshipService.getPrevReceived(userId);
    }
    public void visibilityNextSent() {
        if(friendshipService.getNextSent(userId).isEmpty()) {
            nextButton.setVisible(false);
        } else
            friendshipService.getPrevSent(userId);
    }

    public void visibilityPrevReceived() {
        if(friendshipService.getPrevReceived(userId).isEmpty()) {
            prevButton.setVisible(false);
        } else
            friendshipService.getNextReceived(userId);
    }
    public void visibilityPrevSent() {
        if(friendshipService.getPrevSent(userId).isEmpty()) {
            prevButton.setVisible(false);
        } else
            friendshipService.getNextSent(userId);
    }
}


