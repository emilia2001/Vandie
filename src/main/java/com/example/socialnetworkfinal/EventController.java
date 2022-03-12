package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.Event;
import com.example.socialnetworkfinal.domain.Page;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

public class EventController extends  PageController{

    @FXML
    Label noEventsLabel;
    @FXML
    Label eventNameLabel;
    @FXML
    Label eventDateLabel;
    @FXML
    Label eventLocationLabel;
    @FXML
    Label eventCategoryLabel;
    @FXML
    Label eventDescriptionLabel;
    @FXML
    Button nextEventButton;
    @FXML
    Button previousEventButton;
    @FXML
    Label eventsDetailsLabel;
    @FXML
    Button participateEventButton;
    @FXML
    Button onOffNotifications;
    @FXML
    Button notificationsButton;
    @FXML
    Circle eventImageCircle;

    Integer currentIdEvent;
    @FXML
    public void initialize() {
         previousEventButton.setVisible(false);
        text.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchBar(newValue);});
    }

    //eu incarc aici toate eventurile ca am nev de ele darrr as putea sa le bag alea la care
    //particip in page sa nu mai fac cautare tot timpu hmmm ci sa dau numa in list
    //tb sa fac descrierea sa aiba un anumit numar de caractere ca dupa se vede cu ... ew
    //poza event
    //pagina notificari
    public void setService(User user, Long id, Page page, UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        super.setService(user, id, page, userService, friendshipService, messageService, eventService);
        events.setAll(eventService.getEventsOnPage(0));
        if(eventService.getNextEvents().isEmpty()) {
            nextEventButton.setVisible(false);
        } else
            eventService.getPrevEvents();
        currentIdEvent = 0;
        initializeEvent(events);
    }
    public void  handleCreateEvent(){
        FXMLLoader loader = openWindow("CreateEvent.fxml", (Stage) logoutButton.getScene().getWindow(),"Profile" );
        CreateEventController controller = loader.getController();
        controller.setService(user, userId, page, userService, friendshipService, messageService, eventService);
    }

    public void  handleNotifications(){
        FXMLLoader loader = openWindow("NotificationsView.fxml", (Stage) logoutButton.getScene().getWindow(),"Profile" );
        NotificationsController controller = loader.getController();
        controller.setService(user, userId, page, userService, friendshipService, messageService, eventService);
    }

    public void initializeEvent(ObservableList<Event> events){
        if (events.size() == 0)
            showEvent(null);
        else {
            Event event = events.get(0);
            showEvent(event);
            if(event.getNotifications().containsKey(userId)) {
                participateEventButton.setText("Don't Participate");
                onOffNotifications.setVisible(true);
                if(event.getNotifications().get(userId).equals(1))
                    onOffNotifications.setText("Turn Off Notifications");
                else onOffNotifications.setText("Turn On Notifications");
            }
            else {
                participateEventButton.setText("Participate");
                onOffNotifications.setVisible(false);
            }
        }

    }
    public void handleNext(){
        previousEventButton.setVisible(true);
        Set<Event> nextEvent = eventService.getNextEvents();
        events.setAll(nextEvent);
        if(eventService.getNextEvents().isEmpty()) {
            nextEventButton.setVisible(false);
        } else
            eventService.getPrevEvents();
        initializeEvent(events);
    }

    public void handlePrevious(){
        nextEventButton.setVisible(true);
        Set<Event> prevEvent = eventService.getPrevEvents();
        events.setAll(prevEvent);
        if(eventService.getPrevEvents().isEmpty()) {
            previousEventButton.setVisible(false);
        } else
            eventService.getNextEvents();
        initializeEvent(events);
    }

    public void handleParticipate(){
        if(participateEventButton.getText().equals("Don't Participate")){
            eventService.deleteAttendance(userId,events.get(currentIdEvent).getId());
            events.get(currentIdEvent).getNotifications().remove(userId);
            onOffNotifications.setVisible(false);
            participateEventButton.setText("Participate");
        }
            else{
            eventService.addAttendance(userId,events.get(currentIdEvent).getId());
            events.get(currentIdEvent).getNotifications().put(userId,1);
            onOffNotifications.setVisible(true);
            onOffNotifications.setText("Turn Off Notifications");
            participateEventButton.setText("Don't Participate");
        }

    }

    public void  handleOnOffNotification(){
        if (onOffNotifications.getText().equals("Turn Off Notifications")){
            eventService.updateAttendance(userId, events.get(currentIdEvent).getId(), -1);
            onOffNotifications.setText("Turn On Notifications");
        }
        else{
            eventService.updateAttendance(userId, events.get(currentIdEvent).getId(), 1);
            onOffNotifications.setText("Turn Off Notifications");
        }
    }

    public void showEvent(Event event){
        if(event == null) {
            noEventsLabel.setText("No upcoming events");
            eventCategoryLabel.setText("");
            eventDateLabel.setText("");
            eventDescriptionLabel.setText("");
            eventLocationLabel.setText("");
            eventNameLabel.setText("");
            eventsDetailsLabel.setText("");
        }
        else{
            try {
                Image image = new Image(new FileInputStream(event.getEventPictureUrl()));
                eventImageCircle.setFill(new ImagePattern(image));
                eventImageCircle.setSmooth(true);
                eventImageCircle.setCache(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            noEventsLabel.setText("");
            eventCategoryLabel.setText(event.getCategory());
            Integer day1 = event.getStartDate().getDayOfMonth();
            Integer day2 = event.getEndDate().getDayOfMonth();
            Integer month1 = event.getStartDate().getMonthValue();
            Integer month2 = event.getEndDate().getMonthValue();
            Integer year1 = event.getStartDate().getYear();
            Integer year2 = event.getEndDate().getYear();
            String start_day = day1.toString();
            String end_day = day2.toString();
            String date = start_day + "." + month1.toString() + "." + year1 +  " - "  + end_day + "." + month2.toString() + "." + year2 ;
            eventDateLabel.setText(date);
            eventDescriptionLabel.setText(event.getDescription());
            eventLocationLabel.setText(event.getLocation());
            eventNameLabel.setText(event.getName());
        }
    }
}
