package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.Event;
import com.example.socialnetworkfinal.domain.Page;
import com.example.socialnetworkfinal.domain.exceptions.EventException;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;

public class  CreateEventController extends PageController{
    String pictureUrl;
    File selectedFile;

    @FXML
    TextField eventName;
    @FXML
    DatePicker startEvent;
    @FXML
    DatePicker endEvent;
    @FXML
    TextField locationEvent;
    @FXML
    TextField categoryEvent;
    @FXML
    TextField descriptionEvent;
    @FXML
    Circle eventPictureCircle;

    @FXML
    Label errorEvent;

    public void initialize() {
        pictureUrl = "src\\main\\resources\\com\\example\\socialnetworkfinal\\images\\celebration.jpg";
        try {
            eventPictureCircle.setFill(new ImagePattern(new Image(new FileInputStream(pictureUrl))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setService(Long id, Page page, UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        super.setService(user, id, page, userService, friendshipService, messageService, eventService);

    }

    public void handleAddPicture() {
        Stage secondStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(secondStage);
        if(selectedFile != null) {
            pictureUrl = selectedFile.toURI().toString();
            Image image = new Image(pictureUrl);
            eventPictureCircle.setFill(new ImagePattern(image));
        }
        secondStage.close();
    }
    public  void handleAddEvent(){
        String name = eventName.getText();
        String category = categoryEvent.getText();
        String location = locationEvent.getText();
        String description = descriptionEvent.getText();
        LocalDate startDate = startEvent.getValue();
        LocalDate endDate = endEvent.getValue();
        try {
            String picUrl;
            if( !pictureUrl.equals("src\\main\\resources\\com\\example\\socialnetworkfinal\\images\\celebration.jpg")) {
                picUrl = "src\\main\\resources\\com\\example\\socialnetworkfinal\\images";
                picUrl += selectedFile.getName();
                try {
                    Files.copy(selectedFile.toPath(), Path.of(picUrl), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else
                picUrl = pictureUrl;
            Event event = new Event(name, startDate, endDate, location, category, description);
            event.setEventPictureUrl(picUrl);
            HashMap<Long,Integer> notif = new HashMap<>();
            notif.put(userId,1);
            event.setNotifications(notif);
            eventService.addEvent(event);
            Event event1 = eventService.getLastUsersEvent(userId);
            System.out.println(event1);
            eventService.addAttendance(userId,event1.getId());
            events.add(event);
            notifiableEvents.setAll(eventService.getAllUsersNotifiableEvents(userId));
            FXMLLoader loader = openWindow("EventsView.fxml", (Stage) logoutButton.getScene().getWindow(),"Profile" );
            EventController controller = loader.getController();
            controller.setService(user, userId, page, userService, friendshipService, messageService, eventService);
        }catch (EventException ex){
            errorEvent.setText(ex.getMessage());
        }

    }

    public void handleBack() {
        System.out.println("apasat");
    }
}
