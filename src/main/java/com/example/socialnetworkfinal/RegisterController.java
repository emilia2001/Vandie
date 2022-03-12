package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.Page;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.exceptions.UserException;
import com.example.socialnetworkfinal.domain.exceptions.ValidationException;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class RegisterController {

    UserService userService;
    FriendshipService friendshipService;
    MessageService messageService;
    EventService eventService;
    String pictureUrl;
    File selectedFile;

    @FXML
    Button registerButton;
    @FXML
    Hyperlink backButton;
    @FXML
    TextField emailField;
    @FXML
    TextField firstNameField;
    @FXML
    TextField lastNameField;
    @FXML
    PasswordField passwordField;
    @FXML
    PasswordField confirmField;
    @FXML
    Label errorLabel;
    @FXML
    Circle profilePicture;


    @FXML
    public void initialize() {
        pictureUrl = "src\\main\\resources\\com\\example\\socialnetworkfinal\\images\\defaultProfilePic.png";
        try {
            profilePicture.setFill(new ImagePattern(new Image(new FileInputStream(pictureUrl))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setService(UserService service, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        this.userService = service;
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

     public void  handleBack(){
         FXMLLoader loginLoader = openWindow("login-view.fxml", (Stage) backButton.getScene().getWindow(), "Login");
         LoginController loginController = loginLoader.getController();
         loginController.setService(userService, friendshipService, messageService, eventService);
     }

     public void handleRegister() {
        String email = emailField.getText();
        String lastName = lastNameField.getText();
        String firstName = firstNameField.getText();
        String password = passwordField.getText();
        String confirm = confirmField.getText();
        if (!password.equals(confirm))
            errorLabel.setText("Confirm password doesn't match password");
        else {
            try {
                String picUrl;
                if( !pictureUrl.equals("src\\main\\resources\\com\\example\\socialnetworkfinal\\images\\defaultProfilePic.png")) {
                    picUrl = "src\\main\\resources\\com\\example\\socialnetworkfinal\\images\\profilePics";
                    picUrl += selectedFile.getName();
                    try {
                        Files.copy(selectedFile.toPath(), Path.of(picUrl), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else
                    picUrl = pictureUrl;
                User user = new User(email, firstName, lastName, encodePassword(password));
                user.setProfilePictureUrl(picUrl);
                user.setCoverPictureUrl("src/main/resources/com/example/socialnetworkfinal/images/defaultCover.png");
                userService.addUser(user);
                FXMLLoader loader = openWindow("PageView.fxml", (Stage) emailField.getScene().getWindow(),"Personal page" );
                ProfileController controller = loader.getController();
                Page page = new Page(email, lastName, firstName, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                user = userService.getUserByEmail(email, encodePassword(password));
                System.out.println(user.getCoverPictureUrl());
                controller.setService(user, user.getId(), page, userService, friendshipService, messageService, eventService);
            } catch (ValidationException ex) {
                errorLabel.setText(ex.getMessage());
            } catch (UserException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("This email is already taken!");
                alert.showAndWait();
            }
        }

    }

    public void handleAddPicture() {
        Stage secondStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(secondStage);
        if(selectedFile != null) {
            pictureUrl = selectedFile.toURI().toString();
            Image image = new Image(pictureUrl);
            profilePicture.setFill(new ImagePattern(image));
        }
        secondStage.close();
    }
}
