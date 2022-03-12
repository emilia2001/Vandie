package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.Event;
import com.example.socialnetworkfinal.domain.Page;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.exceptions.UserException;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class LoginController {

    UserService userService;
    FriendshipService friendshipService;
    MessageService messageService;
    EventService eventService;

    @FXML
    TextField emailTextField;
    @FXML
    PasswordField passwordTextField;
    @FXML
    Label errorLabel;

    public void setService(UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.eventService = eventService;
    }

    private String encodePassword(String password) {
        String encryptedpassword = null;
        try
        {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(password.getBytes());
            byte[] bytes = m.digest();
            StringBuilder s = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                s.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            encryptedpassword = s.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return encryptedpassword;
    }

    public void popUpNotifications(Long userId){
        if(!eventService.getAllUsersNotifiableEvents(userId).isEmpty()){
            for(Event event :eventService.getAllUsersNotifiableEvents(userId)){
                event.setMessage();
                Notifications notificationsBuilder =  Notifications.create()
                        .title("You have an upcoming event!")
                        .text(event.getMessage())
                        .position(Pos.BOTTOM_RIGHT);
                notificationsBuilder.darkStyle();
                notificationsBuilder.show();
            }
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

    public void signin() {
        try {
            System.out.println("In login controller dupa ce dau signin");
            Long start = System.currentTimeMillis();
            userService.setPageSize(4);
            User user = userService.getUserByEmail(emailTextField.getText(), encodePassword(passwordTextField.getText()));
            Long start2 = System.currentTimeMillis();
            System.out.println(start2 - start);
            friendshipService.setPageSize(4);
            Page page = new Page(user.getEmail(), user.getLastName(), user.getFirstName(), user.getFriends(), messageService.getUserMessages(user.getId()), new ArrayList<>(friendshipService.getReceivedOnPage(0, user.getId())) );;
            Long start3 = System.currentTimeMillis();
            System.out.println(start3 - start2);
            popUpNotifications(user.getId());
            FXMLLoader loader = openWindow("PageView.fxml", (Stage) emailTextField.getScene().getWindow(), "Personal page");
            Long start4 = System.currentTimeMillis();
            System.out.println(start4 - start3);
            ProfileController controller = loader.getController();
            eventService.setPageSize(1);
            controller.setService(user, user.getId(), page, userService, friendshipService, messageService, eventService);
            Long start5 = System.currentTimeMillis();
            System.out.println(start5 - start4);
        } catch (UserException ex) {
            errorLabel.setText(ex.getMessage());
        }

    }

    public void register() {
        FXMLLoader loader = openWindow("register-view.fxml", (Stage) emailTextField.getScene().getWindow(), "Registration");
        RegisterController controller = loader.getController();
        controller.setService(userService, friendshipService, messageService, eventService);

    }
}
