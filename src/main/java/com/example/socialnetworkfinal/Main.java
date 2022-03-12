package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.Event;
import com.example.socialnetworkfinal.domain.validators.EventValidator;
import com.example.socialnetworkfinal.domain.validators.FriendshipValidator;
import com.example.socialnetworkfinal.domain.validators.MessageValidator;
import com.example.socialnetworkfinal.domain.validators.UserValidator;
import com.example.socialnetworkfinal.repository.database.EventDBRepository;
import com.example.socialnetworkfinal.repository.database.FriendshipDBRepository;
import com.example.socialnetworkfinal.repository.database.MessageDBRepository;
import com.example.socialnetworkfinal.repository.database.UserDBRepository;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.example.socialnetworkfinal.service.UserService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class Main extends Application {

    UserService userService;
    FriendshipService friendshipService;
    MessageService messageService;
    EventService eventService;

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        UserDBRepository userRepository = new UserDBRepository("jdbc:postgresql://localhost:5432/SocialNetwork", "postgres", "Emilia1608", new UserValidator());
        userService = new UserService(userRepository);
        FriendshipDBRepository friendshipRepository = new FriendshipDBRepository("jdbc:postgresql://localhost:5432/SocialNetwork", "postgres", "Emilia1608", new FriendshipValidator());
        friendshipService = new FriendshipService(friendshipRepository);
        MessageDBRepository messageRepository = new MessageDBRepository("jdbc:postgresql://localhost:5432/SocialNetwork", "postgres", "Emilia1608", new MessageValidator());
        messageService = new MessageService(messageRepository);
        EventDBRepository eventDBRepository = new EventDBRepository("jdbc:postgresql://localhost:5432/SocialNetwork", "postgres", "Emilia1608", new EventValidator());
        eventService = new EventService(eventDBRepository);
        //System.out.println(friendshipRepository.getUserReceivedRequests(25l));
        initView(primaryStage);
        //initView(secondStage);
        primaryStage.show();
        //secondStage.show();
    }

    private void initView(Stage primaryStage) throws IOException {

        FXMLLoader loginLoader = new FXMLLoader();
        loginLoader.setLocation(getClass().getResource("login-view.fxml"));
        BorderPane loginLayout = loginLoader.load();
        primaryStage.setScene(new Scene(loginLayout));

        LoginController loginController = loginLoader.getController();
        loginController.setService(userService, friendshipService, messageService, eventService);

    }

}
