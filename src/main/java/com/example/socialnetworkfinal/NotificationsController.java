package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.Event;
import com.example.socialnetworkfinal.domain.Page;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;


public class NotificationsController extends PageController{
    @FXML
    TableColumn<Event, String> notificationsColumn;
    @FXML
    TableColumn<Event, Event> pictureColumn;
    @FXML
    TableView<Event> notificationsTable;

    public void setService(User user, Long id, Page page, UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        super.setService(user, id, page, userService, friendshipService, messageService, eventService);
        notifiableEvents.setAll(eventService.getAllUsersNotifiableEvents(userId));
        notifiableEvents.forEach(x -> x.setMessage());
        initialize();
    }

    @FXML
    public void initialize() {
        text.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchBar(newValue);});
        pictureColumn.setCellFactory(param -> {
            Circle circle = new Circle();
            circle.setRadius(20);
            TableCell<Event, Event> cell = new TableCell<Event, Event>() {
                public void updateItem(Event item, boolean empty) {
                    if (item != null) {
                        try {
                            circle.setVisible(true);
                            circle.setFill( new ImagePattern(new Image(new FileInputStream(item.getEventPictureUrl()))));
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
        notificationsColumn.setCellValueFactory(new PropertyValueFactory<Event, String>("message"));
        notificationsTable.setItems(notifiableEvents);
        Label holder = new Label("You have no notifications");
        holder.setTextFill(Color.WHITE);
        notificationsTable.setPlaceholder(holder);
    }
}
